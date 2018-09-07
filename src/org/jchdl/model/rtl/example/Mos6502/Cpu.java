// jchdl: Jianchang Constructed Hardware Description Library
// Copyright (c) 2018 Jianchang Wang <wjcdx@qq.com>
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package org.jchdl.model.rtl.example.Mos6502;

import org.jchdl.model.rtl.core.datatype.Bit;
import org.jchdl.model.rtl.core.datatype.Bits;
import org.jchdl.model.rtl.core.datatype.Reg;
import org.jchdl.model.rtl.core.datatype.annotation.Range;
import org.jchdl.model.rtl.core.event.ChangingEvent;
import org.jchdl.model.rtl.core.event.PosEdgeEvent;
import org.jchdl.model.rtl.core.io.annotation.Input;
import org.jchdl.model.rtl.core.io.annotation.Output;
import org.jchdl.model.rtl.core.meta.Module;
import org.jchdl.model.rtl.core.meta.PropagateManager;

//ref: https://github.com/Arlet/verilog-6502
public class Cpu extends Module {
    @Input
    private Bit clk;          // CPU clock
    @Input
    private Bit reset;        // reset signal
    @Input
    @Range(msb = 7, lsb = 0)
    private Bits DI;// data in, read bus
    @Input
    private Bit IRQ;// interrupt request
    @Input
    private Bit NMI;// non-maskable interrupt request
    @Input
    private Bit RDY;// Ready signal. Pauses CPU when RDY=0

    @Output
    @Range(msb = 15, lsb = 0)
    private Reg AB;// address bus
    @Output
    @Range(msb = 7, lsb = 0)
    private Reg DO;// data out, write bus
    @Output
    private Reg WE;// write enable

    public Cpu(Module parent, Bit clk, Bit reset, Bits DI, Bit IRQ, Bit NMI, Bit RDY,
               Reg AB, Reg DO, Reg WE) {
        super(parent);
        //inputs
        this.clk = clk;
        this.reset = reset;
        this.DI = DI;
        this.IRQ = IRQ;
        this.NMI = NMI;
        this.RDY = RDY;
        //outputs
        this.AB = AB;
        this.DO = DO;
        this.WE = WE;
        //construct logic blocks
        construct();
    }

    @Override
    public void logic() {
        // select regfile
        assign(regfile).from(() -> AXYS[regsel.intVal()]).with(() ->
                regfile.assign(Bits.from(AXYS[regsel.intVal()]))
        );

        /*
         * Program Counter Increment/Load. First calculate the base value in
         * PC_temp.
         */
        when(ChangingEvent.of(state, I, IRQ, NMI_edge, ABH, ABL, PC, DIMUX, ADD, PCL, res)
        ).run(this::updatePcTemp);

        /*
         * Determine wether we need PC_temp, or PC_temp + 1
         */
        when(ChangingEvent.of(state, I, IRQ, NMI_edge, backwards, CO)).run(this::updatePcInc);

        /*
         * Set new PC
         */
        when(PosEdgeEvent.of(clk)).run(this::updatePC);

        /*
         * Address Generator
         */
        when(ChangingEvent.of(state, DIMUX, ADD, ABL, ABH, regfile, PC)).run(this::updateAB);

        /*
         * ABH/ABL pair is used for registering previous address bus state.
         * This can be used to keep the current address, freeing up the original
         * source of the address, such as the ALU or DI.
         */
        when(PosEdgeEvent.of(clk)).run(this::updateABHL);

        /*
         * Data Out MUX
         */
        when(ChangingEvent.of(state, ADD, PCH, PCL, php, P, IRQ, NMI_edge, regfile)).run(this::updateDO);

        /*
         * Write Enable Generator
         */
        when(ChangingEvent.of(state, store)).run(this::updateWE);

        /*
         * register file, contains A, X, Y and S (stack pointer) registers. At each
         * cycle only 1 of those registers needs to be accessed, so they combined
         * in a small memory, saving resources.
         */
        when(ChangingEvent.of(state, load_reg, plp)).run(this::updateWriteRegister);

        /*
         * BCD adjust logic
         */
        when(PosEdgeEvent.of(clk)).run(this::updateAdjBcd);

        // adjustment term to be added to ADD[3:0] based on the following
        // adj_bcd: '1' if doing ADC/SBC with D=1
        // adc_bcd: '1' if doing ADC with D=1
        // HC     : half carry bit from ALU
        when(ChangingEvent.of(adj_bcd, adc_bcd, HC)).run(this::updateADJL);

        // adjustment term to be added to ADD[7:4] based on the following
        // adj_bcd: '1' if doing ADC/SBC with D=1
        // adc_bcd: '1' if doing ADC with D=1
        // CO     : carry out bit from ALU
        when(ChangingEvent.of(adj_bcd, adc_bcd, CO)).run(this::updateADJH);

        /*
         * write to a register. Usually this is the (BCD corrected) output of the
         * ALU, but in case of the JSR0 we use the S register to temporarily store
         * the PCL. This is possible, because the S register itself is stored in
         * the ALU during those cycles.
         */
        when(PosEdgeEvent.of(clk)).run(this::updateAxysRegister);

        /*
         * register select logic. This determines which of the A, X, Y or
         * S registers will be accessed.
         */
        when(ChangingEvent.of(state, index_y, dst_reg, src_reg)).run(this::updateRegSel);

        /*
         * ALU
         */
        new Alu(this, clk,
                alu_shift_right.bit(0),
                Bits.from(alu_op),
                Bits.from(AI),
                Bits.from(BI),
                CI.bit(0),
                adc_bcd.and(state.eq(FETCH)),
                RDY,
                Reg.to(ADD),
                Reg.to(CO),
                AV,
                AZ,
                Reg.to(AN),
                Reg.to(HC)
        );

        /*
         * Select current ALU operation
         */
        when(ChangingEvent.of(state, op, backwards)).run(this::updateAluOp);

        /*
         * Determine shift right signal to ALU
         */
        when(ChangingEvent.of(state, shift_right)).run(this::updateAluShiftRight);

        /*
         * Sign extend branch offset.
         */

        when(PosEdgeEvent.of(clk)).run(this::updateBackwards);

        /*
         * ALU A Input MUX
         */
        when(ChangingEvent.of(state, ADD, regfile, DIMUX, ABH, load_only)).run(this::updateAI);

        /*
         * ALU B Input mux
         */
        when(ChangingEvent.of(state, PCL, DIMUX)).run(this::updateBI);

        /*
         * ALU CI (carry in) mux
         */
        when(ChangingEvent.of(state, CO, rotate, C, shift, inc, compare, load_only)).run(this::updateCI);

        /*
         * Processor Status Register update
         *
         */
        /*
         * Update C flag when doing ADC/SBC, shift/rotate, compare
         */
        when(PosEdgeEvent.of(clk)).run(this::updateC);

        /*
         * Update Z, N flags when writing A, X, Y, Memory, or when doing compare
         */
        when(PosEdgeEvent.of(clk)).run(this::updateZ);
        when(PosEdgeEvent.of(clk)).run(this::updateN);
        /*
         * Update I flag
         */
        when(PosEdgeEvent.of(clk)).run(this::updateI);
        /*
         * Update D flag
         */
        when(PosEdgeEvent.of(clk)).run(this::updateD);
        /*
         * Update V flag
         */
        when(PosEdgeEvent.of(clk)).run(this::updateV);

        /*
         * Instruction decoder
         */

        /*
         * IR register/mux. Hold previous DI value in IRHOLD in PULL0 and PUSH0
         * states. In these states, the IR has been prefetched, and there is no
         * time to read the IR again before the next decode.
         */
        when(PosEdgeEvent.of(clk)).run(this::updateIRHOLD);
        assign(IR).from(IRQ, I, NMI_edge, IRHOLD_valid, IRHOLD, DIMUX).with(() -> {
            IR.assign(NMI_edge.or(I.not().and(IRQ)).boolVal() ? Bits.inst(8, 0x00) :
                    (IRHOLD_valid.boolVal() ? Bits.from(IRHOLD) : DIMUX)
            );
        });

        when(PosEdgeEvent.of(clk)).run(this::updateDIHOLD);
        assign(DIMUX).from(RDY, DIHOLD, DI).with(() -> DIMUX.assign(RDY.not().boolVal() ? Bits.from(DIHOLD) : DI));

        /*
         * Microcode state machine
         */
        when(PosEdgeEvent.of(clk), PosEdgeEvent.of(reset)).run(this::updateState);
        /*
         * Additional control signals
         */
        when(PosEdgeEvent.of(clk)).run(this::updateRes);
        when(PosEdgeEvent.of(clk)).run(this::updateLoadReg);
        when(PosEdgeEvent.of(clk)).run(this::updateDstReg);
        when(PosEdgeEvent.of(clk)).run(this::updateSrcReg);
        when(PosEdgeEvent.of(clk)).run(this::updateIndexY);
        when(PosEdgeEvent.of(clk)).run(this::updateStore);
        when(PosEdgeEvent.of(clk)).run(this::updateWriteBack);
        when(PosEdgeEvent.of(clk)).run(this::updateLoadOnly);
        when(PosEdgeEvent.of(clk)).run(this::updateInc);
        when(PosEdgeEvent.of(clk)).run(this::updateAdcSbc);
        when(PosEdgeEvent.of(clk)).run(this::updateAdcBcd);
        when(PosEdgeEvent.of(clk)).run(this::updateShift);
        when(PosEdgeEvent.of(clk)).run(this::updateCompare);
        when(PosEdgeEvent.of(clk)).run(this::updateShiftRight);
        when(PosEdgeEvent.of(clk)).run(this::updateRotate);
        when(PosEdgeEvent.of(clk)).run(this::updateOp);
        when(PosEdgeEvent.of(clk)).run(this::updateBitIns);
        when(PosEdgeEvent.of(clk)).run(this::updateSpecialInstructions);
        when(PosEdgeEvent.of(clk)).run(this::updateCondCode);

        when(ChangingEvent.of(cond_code, N, V, C, Z)).run(this::updateCondTrue);

        when(PosEdgeEvent.of(clk)).run(() -> NMI_1.set(NMI));
        when(PosEdgeEvent.of(clk)).run(this::updateNmiEdge);
    }

    private void updateNmiEdge() {
        if (NMI_edge.and(state.eq(BRK3)).boolVal()) {
            NMI_edge.set(0);
        } else if (NMI.and(NMI_1.not().bit(0)).boolVal()) {
            NMI_edge.set(1);
        }
    }

    private void updateCondTrue() {
        switch (cond_code.intVal()) {
            case 0b000:
                cond_true.set(N.not());
                break;
            case 0b001:
                cond_true.set(N);
                break;
            case 0b010:
                cond_true.set(V.not());
                break;
            case 0b011:
                cond_true.set(V);
                break;
            case 0b100:
                cond_true.set(C.not());
                break;
            case 0b101:
                cond_true.set(C);
                break;
            case 0b110:
                cond_true.set(Z.not());
                break;
            case 0b111:
                cond_true.set(Z);
                break;
        }
    }

    private void updateCondCode() {
        if (RDY.boolVal()) {
            cond_code.set(IR.intVal(7, 5));
        }
    }

    private void updateSpecialInstructions() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            php.set(IR.eq(0x08));
            clc.set(IR.eq(0x18));
            plp.set(IR.eq(0x28));
            sec.set(IR.eq(0x38));
            cli.set(IR.eq(0x58));
            sei.set(IR.eq(0x78));
            clv.set(IR.eq(0xb8));
            cld.set(IR.eq(0xd8));
            sed.set(IR.eq(0xf8));
            brk.set(IR.eq(0x00));
        }
    }

    private void updateBitIns() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("0b0010_x100")) { // BIT zp/abs
                bit_ins.set(1);
            } else {
                bit_ins.set(0);
            }
        }
    }

    private void updateOp() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("0b00xx_xx10")) op.set(OP_ROL);       // ROL: ASL
            else if (IR.match("0b0010_x100")) op.set(OP_AND); // BIT zp/abs
            else if (IR.match("0b01xx_xx10")) op.set(OP_A);   // ROR: LSR
            else if (IR.match("",
                    "0b1000_1000",// DEY
                    "0b1100_1010",// DEX
                    "0b110x_x110",// DEC
                    "0b11xx_xx01",// CMP, SBC
                    "0b11x0_0x00",// CPX, CPY (imm, zpg)
                    "0b11x0_1100"
            )) {
                op.set(OP_SUB);
            } else if (IR.match("",
                    "0b010x_xx01",// EOR
                    "0b00xx_xx01" // ORA, AND
            )) {
                op.set(Bits.inst(2, 0b11).concat(IR.part(6, 5)));
            } else {
                op.set(OP_ADD);
            }
        }
    }

    private void updateRotate() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("",
                    "0b0x1x_1010",   // ROL A: ROR A
                    "0b0x1x_x110"    // ROR: ROL
            )) {
                rotate.set(1);
            } else {
                rotate.set(0);
            }
        }
    }

    private void updateShiftRight() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("0b01xx_xx10")) {   // ROR: LSR
                shift_right.set(1);
            } else {
                shift_right.set(0);
            }
        }
    }

    private void updateCompare() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("",
                    "0b11x0_0x00",   // CPX: CPY (imm/zp)
                    "0b11x0_1100",   // CPX: CPY (abs)
                    "0b110x_xx01"    // CMP
            )) {
                compare.set(1);
            } else {
                compare.set(0);
            }
        }
    }

    private void updateShift() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("",
                    "0b0xxx_x110",   // ASL: ROL: LSR: ROR (abs: absx: zpg: zpgx)
                    "0b0xxx_1010"    // ASL: ROL: LSR: ROR (acc)
            )) {
                shift.set(1);
            } else {
                shift.set(0);
            }
        }
    }

    private void updateAdcBcd() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("0b011x_xx01")) { // ADC
                adc_bcd.set(D);
            } else {
                adc_bcd.set(0);
            }
        }
    }

    private void updateAdcSbc() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("0bx11x_xx01")) { // SBC: ADC
                adc_sbc.set(1);
            } else {
                adc_sbc.set(0);
            }
        }
    }

    private void updateInc() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("",
                    "0b111x_x110",   // INC
                    "0b11x0_1000"    // INX: INY
            )) {
                inc.set(1);
            } else {
                inc.set(0);
            }
        }
    }

    private void updateLoadOnly() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("0b101x_xxxx")) { // LDA: LDX: LDY
                load_only.set(1);
            } else {
                load_only.set(0);
            }
        }
    }

    private void updateWriteBack() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("",
                    "0b0xxx_x110",   // ASL: ROL: LSR: ROR
                    "0b11xx_x110"    // DEC/INC
            )) {
                write_back.set(1);
            } else {
                write_back.set(0);
            }
        }
    }

    private void updateStore() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("",
                    "0b100x_x1x0",   // STX: STY
                    "0b100x_xx01"    // STA
            )) {
                store.set(1);
            } else {
                store.set(0);
            }
        }
    }

    private void updateIndexY() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("",
                    "0bxxx1_0001",   // INDY
                    "0b10x1_x110",   // LDX/STX zpg/abs, Y
                    "0bxxxx_1001"    // abs, Y
            )) {
                index_y.set(1);
            } else {
                index_y.set(0);
            }
        }
    }

    private void updateSrcReg() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("",
                    "0b1011_1010"   // TSX
            )) {
                src_reg.set(Constants.SEL_S.intVal());
            } else if (IR.match("",
                    "0b100x_x110",   // STX
                    "0b100x_1x10",   // TXA: TXS
                    "0b1110_xx00",   // INX: CPX
                    "0b1100_1010"    // DEX
            )) {
                src_reg.set(Constants.SEL_X.intVal());
            } else if (IR.match("",
                    "0b100x_x100",   // STY
                    "0b1001_1000",   // TYA
                    "0b1100_xx00",   // CPY
                    "0b1x00_1000"    // DEY: INY
            )) {
                src_reg.set(Constants.SEL_Y.intVal());
            } else {
                src_reg.set(Constants.SEL_A.intVal());
            }
        }
    }

    private void updateDstReg() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("",
                    "0b1110_1000",   // INX
                    "0b1100_1010",   // DEX
                    "0b101x_xx10"    // LDX: TAX: TSX
            )) {
                dst_reg.set(Constants.SEL_X.intVal());
            } else if (IR.match("",
                    "0b0x00_1000",   // PHP: PHA
                    "0b1001_1010"    // TXS
            )) {
                dst_reg.set(Constants.SEL_S.intVal());
            } else if (IR.match("",
                    "0b1x00_1000",   // DEY: DEX
                    "0b101x_x100",   // LDY
                    "0b1010_0000",   // LDY #imm: TAY
                    "0b1010_1000"    // LDY #imm: TAY
            )) {
                dst_reg.set(Constants.SEL_Y.intVal());
            } else {
                dst_reg.set(Constants.SEL_A.intVal());
            }
        }
    }

    private void updateLoadReg() {
        if (state.eq(DECODE).and(RDY).boolVal()) {
            if (IR.match("",
                    "0b0xx0_1010",    // ASLA: ROLA: LSRA: RORA
                    "0b0xxx_xx01",    // ORA: AND: EOR: ADC
                    "0b100x_10x0",    // DEY: TYA: TXA: TXS
                    "0b1010_xxx0",    // LDA/LDX/LDY
                    "0b1011_1010",    // TSX
                    "0b1011_x1x0",    // LDX/LDY
                    "0b1100_1010",    // DEX
                    "0b1x1x_xx01",    // LDA: SBC
                    "0bxxx0_1000"     // DEY: TAY: INY: INX
            )) {
                load_reg.set(1);
            } else {
                load_reg.set(0);
            }
        }
    }

    private void updateRes() {
        if (reset.boolVal()) {
            res.set(1);
        } else if (state.eq(DECODE).boolVal()) {
            res.set(0);
        }
    }

    private void updateState() {
        if (reset.boolVal()) {
            state.set(BRK0);
        } else if (RDY.boolVal()) {
            switch (state.intVal()) {
                case DECODE:
                    if (IR.match("0b0000_0000")) state.set(BRK0);
                    else if (IR.match("0b0010_0000"))
                        state.set(JSR0);
                    else if (IR.match("0b0010_1100")) state.set(ABS0);  // BIT abs
                    else if (IR.match("0b0100_0000")) state.set(RTI0);
                    else if (IR.match("0b0100_1100")) state.set(JMP0);
                    else if (IR.match("0b0110_0000")) state.set(RTS0);
                    else if (IR.match("0b0110_1100")) state.set(JMPI0);
                    else if (IR.match("0b0x00_1000")) state.set(PUSH0);
                    else if (IR.match("0b0x10_1000")) state.set(PULL0);
                    else if (IR.match("0b0xx1_1000")) state.set(REG);   // CLC, SEC, CLI, SEI
                    else if (IR.match("0b1xx0_00x0")) state.set(FETCH); // IMM
                    else if (IR.match("0b1xx0_1100")) state.set(ABS0);  // X/Y abs
                    else if (IR.match("0b1xxx_1000")) state.set(REG);   // DEY, TYA, ...
                    else if (IR.match("0bxxx0_0001")) state.set(INDX0);
                    else if (IR.match("0bxxx0_01xx")) state.set(ZP0);
                    else if (IR.match("0bxxx0_1001")) state.set(FETCH); // IMM
                    else if (IR.match("0bxxx0_1101")) state.set(ABS0);  // even E column
                    else if (IR.match("0bxxx0_1110")) state.set(ABS0);  // even E column
                    else if (IR.match("0bxxx1_0000")) state.set(BRA0);  // odd 0 column
                    else if (IR.match("0bxxx1_0001")) state.set(INDY0); // odd 1 column
                    else if (IR.match("0bxxx1_01xx")) state.set(ZPX0);  // odd 4,5,6,7 columns
                    else if (IR.match("0bxxx1_1001")) state.set(ABSX0); // odd 9 column
                    else if (IR.match("0bxxx1_11xx")) state.set(ABSX0); // odd C, D, E, F columns
                    else if (IR.match("0bxxxx_1010")) state.set(REG);   // <shift> A, TXA, ...  NOP
                    break;
                case ZP0:
                    state.set(write_back.boolVal() ? READ : FETCH);
                    break;
                case ZPX0:
                    state.set(ZPX1);
                    break;
                case ZPX1:
                    state.set(write_back.boolVal() ? READ : FETCH);
                    break;
                case ABS0:
                    state.set(ABS1);
                    break;
                case ABS1:
                    state.set(write_back.boolVal() ? READ : FETCH);
                    break;
                case ABSX0:
                    state.set(ABSX1);
                    break;
                case ABSX1:
                    state.set(CO.or(store.or(write_back).bit(0)).boolVal() ? ABSX2 : FETCH);
                    break;
                case ABSX2:
                    state.set(write_back.boolVal() ? READ : FETCH);
                    break;
                case INDX0:
                    state.set(INDX1);
                    break;
                case INDX1:
                    state.set(INDX2);
                    break;
                case INDX2:
                    state.set(INDX3);
                    break;
                case INDX3:
                    state.set(FETCH);
                    break;
                case INDY0:
                    state.set(INDY1);
                    break;
                case INDY1:
                    state.set(INDY2);
                    break;
                case INDY2:
                    state.set(CO.or(store.bit(0)).boolVal() ? INDY3 : FETCH);
                    break;
                case INDY3:
                    state.set(FETCH);
                    break;
                case READ:
                    state.set(WRITE);
                    break;
                case WRITE:
                    state.set(FETCH);
                    break;
                case FETCH:
                    state.set(DECODE);
                    break;
                case REG:
                    state.set(DECODE);
                    break;
                case PUSH0:
                    state.set(PUSH1);
                    break;
                case PUSH1:
                    state.set(DECODE);
                    break;
                case PULL0:
                    state.set(PULL1);
                    break;
                case PULL1:
                    state.set(PULL2);
                    break;
                case PULL2:
                    state.set(DECODE);
                    break;
                case JSR0:
                    state.set(JSR1);
                    break;
                case JSR1:
                    state.set(JSR2);
                    break;
                case JSR2:
                    state.set(JSR3);
                    break;
                case JSR3:
                    state.set(FETCH);
                    break;
                case RTI0:
                    state.set(RTI1);
                    break;
                case RTI1:
                    state.set(RTI2);
                    break;
                case RTI2:
                    state.set(RTI3);
                    break;
                case RTI3:
                    state.set(RTI4);
                    break;
                case RTI4:
                    state.set(DECODE);
                    break;
                case RTS0:
                    state.set(RTS1);
                    break;
                case RTS1:
                    state.set(RTS2);
                    break;
                case RTS2:
                    state.set(RTS3);
                    break;
                case RTS3:
                    state.set(FETCH);
                    break;
                case BRA0:
                    state.set(cond_true.boolVal() ? BRA1 : DECODE);
                    break;
                case BRA1:
                    state.set(CO.xor(backwards.bit(0)).boolVal() ? BRA2 : DECODE);
                    break;
                case BRA2:
                    state.set(DECODE);
                    break;
                case JMP0:
                    state.set(JMP1);
                    break;
                case JMP1:
                    state.set(DECODE);
                    break;
                case JMPI0:
                    state.set(JMPI1);
                    break;
                case JMPI1:
                    state.set(JMP0);
                    break;
                case BRK0:
                    state.set(BRK1);
                    break;
                case BRK1:
                    state.set(BRK2);
                    break;
                case BRK2:
                    state.set(BRK3);
                    break;
                case BRK3:
                    state.set(JMP0);
                    break;
            }
        }
    }

    private void updateDIHOLD() {
        if (RDY.boolVal()) {
            DIHOLD.set(DI);
        }
    }

    private void updateIRHOLD() {
        if (reset.boolVal()) {
            IRHOLD_valid.set(0);
        } else if (RDY.boolVal()) {
            if (state.eq(PULL0).or(state.eq(PUSH0)).boolVal()) {
                IRHOLD.set(DIMUX);
                IRHOLD_valid.set(1);
            } else if (state.eq(DECODE).boolVal()) {
                IRHOLD_valid.set(0);
            }
        }
    }

    private void updateV() {
        if (state.eq(RTI2).boolVal()) {
            V.set(DIMUX.bit(6));
        } else if (state.eq(DECODE).boolVal()) {
            if (adc_sbc.boolVal()) V.set(AV);
            if (clv.boolVal()) V.set(0);
            if (plp.boolVal()) V.set(ADD.bit(6));
        } else if (state.eq(FETCH).and(bit_ins.bit(0)).boolVal()) {
            V.set(DIMUX.bit(6));
        }
    }

    private void updateD() {
        if (state.eq(RTI2).boolVal()) {
            D.set(DIMUX.bit(3));
        } else if (state.eq(DECODE).boolVal()) {
            if (sed.boolVal()) D.set(1);
            if (cld.boolVal()) D.set(0);
            if (plp.boolVal()) D.set(ADD.bit(3));
        }
    }

    private void updateI() {
        if (state.eq(BRK3).boolVal()) {
            I.set(1);
        } else if (state.eq(RTI2).boolVal()) {
            I.set(DIMUX.bit(2));
        } else if (state.eq(REG).boolVal()) {
            if (sei.boolVal()) I.set(1);
            if (cli.boolVal()) I.set(0);
        } else if (state.eq(DECODE).boolVal()) {
            if (plp.boolVal()) I.set(ADD.bit(2));
        }
    }

    private void updateN() {
        if (state.eq(WRITE).boolVal()) {
            N.set(AN);
        } else if (state.eq(RTI2).boolVal()) {
            N.set(DIMUX.bit(7));
        } else if (state.eq(DECODE).boolVal()) {
            if (plp.boolVal()) {
                N.set(ADD.bit(7));
            } else if (load_reg
                    .and(regsel.ne(Constants.SEL_S.intVal()))
                    .or(compare.bit(0)).boolVal()) {
                N.set(AN);
            }
        } else {
            N.set(DIMUX.bit(7));
        }
    }

    private void updateZ() {
        if (state.eq(WRITE).boolVal()) {
            Z.set(AZ);
        } else if (state.eq(RTI2).boolVal()) {
            Z.set(DIMUX.bit(1));
        } else if (state.eq(DECODE).boolVal()) {
            if (plp.boolVal()) {
                Z.set(ADD.bit(1));
            } else if (load_reg
                    .and(regsel.ne(Constants.SEL_S.intVal()))
                    .or(compare.bit(0))
                    .or(bit_ins.bit(0)).boolVal()) {
                Z.set(AZ);
            }
        }
    }

    private void updateC() {
        if (shift.and(state.eq(WRITE)).boolVal()) {
            C.set(CO);
        } else if (state.eq(RTI2).boolVal()) {
            C.set(DIMUX.bit(0));
        } else if (write_back.not().and(state.eq(DECODE)).boolVal()) {
            if (adc_sbc.or(shift).or(compare).boolVal()) {
                C.set(CO);
            } else if (plp.boolVal()) {
                C.set(ADD.bit(0));
            } else {
                if (sec.boolVal()) C.set(1);
                if (clc.boolVal()) C.set(0);
            }
        }
    }

    private void updateCI() {
        switch (state.intVal()) {
            case INDY2:
            case BRA1:
            case ABSX1:
                CI.set(CO);
                break;

            case DECODE:
            case ABS1:
//                CI.set(Bit.BIT_X);
                CI.set(0);
                break;

            case READ:
            case REG:
                CI.set(rotate.boolVal() ? C.intVal() :
                        (shift.boolVal() ? 0b0 : inc.intVal())
                );
                break;

            case FETCH:
                CI.set(rotate.boolVal() ? C.intVal() :
                        (compare.boolVal() ? 0b1 :
                                (shift.or(load_only).boolVal() ? 0b0 : C.intVal())
                        )
                );
                break;

            case PULL0:
            case RTI0:
            case RTI1:
            case RTI2:
            case RTS0:
            case RTS1:
            case INDY0:
            case INDX1:
                CI.set(1);
                break;

            default:
                CI.set(0);
                break;
        }
    }

    private void updateBI() {
        switch (state.intVal()) {
            case BRA1:
            case RTS1:
            case RTI0:
            case RTI1:
            case RTI2:
            case INDX1:
            case READ:
            case REG:
            case JSR0:
            case JSR1:
            case JSR2:
            case BRK0:
            case BRK1:
            case BRK2:
            case PUSH0:
            case PUSH1:
            case PULL0:
            case RTS0:
                BI.set(0);
                break;

            case BRA0:
                BI.set(PCL);
                break;

            case DECODE:
            case ABS1:
//                    BI = 8 'hxx;
                BI.set(0x00);
//                BI.bit(0).set(new Bit(Bit.BIT_X));
                break;


            default:
                BI.set(DIMUX);
                break;
        }
    }

    private void updateAI() {
        switch (state.intVal()) {
            case JSR1:
            case RTS1:
            case RTI1:
            case RTI2:
            case BRK1:
            case BRK2:
            case INDX1:
                AI.set(ADD);
                break;

            case REG:
            case ZPX0:
            case INDX0:
            case ABSX0:
            case RTI0:
            case RTS0:
            case JSR0:
            case JSR2:
            case BRK0:
            case PULL0:
            case INDY1:
            case PUSH0:
            case PUSH1:
                AI.set(regfile);
                break;

            case BRA0:
            case READ:
                AI.set(DIMUX);
                break;

            case BRA1:
                AI.set(ABH);
                break;       // don't use PCH in case we're

            case FETCH:
                AI.set(load_only.boolVal() ? 0 : regfile.intVal());
                break;

            case DECODE:
            case ABS1:
//                    AI = 8 'hxx; break;     // don' t care
                AI.set(0x00);
//                AI.bit(0).set(new Bit(Bit.BIT_X));
                break;

            default:
                AI.set(0);
                break;
        }
    }

    private void updateBackwards() {
        if (RDY.boolVal()) {
            backwards.set(DIMUX.bit(7));
        }
    }

    private void updateAluShiftRight() {
        if (state.eq(FETCH)
                .or(state.eq(REG))
                .or(state.eq(READ)).boolVal()) {
            alu_shift_right.set(shift_right);
        } else {
            alu_shift_right.set(0);
        }
    }

    private void updateAluOp() {
        switch (state.intVal()) {
            case READ:
                alu_op.set(op);
                break;
            case BRA1:
                alu_op.set(backwards.boolVal() ? OP_SUB : OP_ADD);
                break;
            case FETCH:
            case REG:
                alu_op.set(op);
                break;
            case DECODE:
            case ABS1:
//                    alu_op = 1 'bx;
                alu_op.set(0);
                break;
            case PUSH1:
            case BRK0:
            case BRK1:
            case BRK2:
            case JSR0:
            case JSR1:
                alu_op.set(OP_SUB);
                break;
            default:
                alu_op.set(OP_ADD);
                break;
        }
    }

    private void updateRegSel() {
        switch (state.intVal()) {
            case INDY1:
            case INDX0:
            case ZPX0:
            case ABSX0:
                regsel.set(index_y.boolVal() ? Constants.SEL_Y.intVal() : Constants.SEL_X.intVal());
                break;
            case DECODE:
                regsel.set(dst_reg);
                break;
            case BRK0:
            case BRK3:
            case JSR0:
            case JSR2:
            case PULL0:
            case PULL1:
            case PUSH1:
            case RTI0:
            case RTI3:
            case RTS0:
            case RTS2:
                regsel.set(Constants.SEL_S.intVal());
                break;
            default:
                regsel.set(src_reg);
                break;
        }
    }

    private void updateAxysRegister() {
        if (write_register.and(RDY).boolVal()) {
            AXYS[regsel.intVal()].set(state.eq(JSR0).boolVal() ? DIMUX :
                    ADD.part(7, 4).add(Bits.from(ADJH)).concat(ADD.part(3, 0).add(Bits.from(ADJL)))
            );
        }
    }

    private void updateADJH() {
        switch (adj_bcd.concat(adc_bcd, CO).intVal()) {
            case 0b100:
                ADJH.set(10);
                break;  // SBC, and digital borrow
            case 0b101:
                ADJH.set(0);
                break;   // SBC, but no borrow
            case 0b110:
                ADJH.set(0);
                break;   // ADC, but no carry
            case 0b111:
                ADJH.set(6);
                break;   // ADC, and decimal/digital carry
            default:
                ADJH.set(0);
                break;   // no BCD instruction
        }
    }

    private void updateADJL() {
        switch (adj_bcd.concat(adc_bcd, HC).intVal()) {
            case 0b100:
                ADJL.set(10);
                break;  // SBC, and digital borrow
            case 0b101:
                ADJL.set(0);
                break;   // SBC, but no borrow
            case 0b110:
                ADJL.set(0);
                break;   // ADC, but no carry
            case 0b111:
                ADJL.set(6);
                break;   // ADC, and decimal/digital carry
            default: //0b0xx
                ADJL.set(0);
                break;   // no BCD instruction
        }
    }

    private void updateAdjBcd() {
        adj_bcd.set(adc_sbc.and(D));
    }

    private void updateWriteRegister() {
        switch (state.intVal()) {
            case DECODE:
                write_register.set(load_reg.and(plp.not()));
                break;
            case PULL1:
            case RTS2:
            case RTI3:
            case BRK3:
            case JSR0:
            case JSR2:
                write_register.set(1);
                break;
            default:
                write_register.set(0);
                break;
        }
    }

    private void updateWE() {
        switch (state.intVal()) {
            case BRK0:   // writing to stack or memory
            case BRK1:
            case BRK2:
            case JSR0:
            case JSR1:
            case PUSH1:
            case WRITE:
                WE.set(1);
                break;

            case INDX3:  // only if doing a STA: STX or STY
            case INDY3:
            case ABSX2:
            case ABS1:
            case ZPX1:
            case ZP0:
                WE.set(store);
                break;

            default:
                WE.set(0);
                break;
        }
    }

    private void updateDO() {
        switch (state.intVal()) {
            case WRITE:
                DO.set(ADD);
                break;

            case JSR0:
            case BRK0:
                DO.set(PCH);
                break;

            case JSR1:
            case BRK1:
                DO.set(PCL);
                break;

            case PUSH1:
                DO.set((php.boolVal() ? P : ADD));
                break;

            case BRK2:
                DO.set(NMI_edge.or(IRQ).boolVal() ?
                        P.and(Bits.inst(8, 0b1110_1111)) : P);
                break;

            default:
                DO.set(regfile);
                break;
        }
    }

    private void updateABHL() {
        if (RDY.and(state.ne(PUSH0))
                .and(state.ne(PUSH1))
                .and(state.ne(PULL0))
                .and(state.ne(PULL1))
                .and(state.ne(PULL2)).boolVal()) {
            ABL.set(AB.bits(7, 0));
            ABH.set(AB.bits(15, 8));
        }
    }

    private void updateAB() {
        switch (state.intVal()) {
            case ABSX1:
            case INDX3:
            case INDY2:
            case JMP1:
            case JMPI1:
            case RTI4:
            case ABS1:
                AB.set(DIMUX.concat(ADD));
                break;

            case BRA2:
            case INDY3:
            case ABSX2:
                AB.set(ADD.concat(ABL));
                break;

            case BRA1:
                AB.set(ABH.concat(ADD));
                break;

            case JSR0:
            case PUSH1:
            case RTS0:
            case RTI0:
            case BRK0:
                AB.set(Bits.inst(8, STACKPAGE).concat(regfile));
                break;

            case BRK1:
            case JSR1:
            case PULL1:
            case RTS1:
            case RTS2:
            case RTI1:
            case RTI2:
            case RTI3:
            case BRK2:
                AB.set(Bits.inst(8, STACKPAGE).concat(ADD));
                break;

            case INDY1:
            case INDX1:
            case ZPX1:
            case INDX2:
                AB.set(Bits.inst(8, ZEROPAGE).concat(ADD));
                break;

            case ZP0:
            case INDY0:
                AB.set(Bits.inst(8, ZEROPAGE).concat(DIMUX));
                break;

            case REG:
            case READ:
            case WRITE:
                AB.set(ABH.concat(ABL));
                break;

            default:
                AB.set(PC);
                break;
        }
    }

    private void updatePC() {
        if (RDY.boolVal()) {
            PC.set(PC_temp.add(PC_inc));
        }
    }

    private void updatePcInc() {
        switch (state.intVal()) {
            case DECODE:
                if (I.not().and(IRQ).or(NMI_edge.bit(0)).boolVal()) {
                    PC_inc.set(0);
                } else {
                    PC_inc.set(1);
                }
                break;

            case ABS0:
            case ABSX0:
            case FETCH:
            case BRA0:
            case BRA2:
            case BRK3:
            case JMPI1:
            case JMP1:
            case RTI4:
            case RTS3:
                PC_inc.set(1);
                break;
            case BRA1:
                PC_inc.set(backwards.not().xor(CO));
                break;

            default:
                PC_inc.set(0);
                break;
        }
    }

    private void updatePcTemp() {
        switch (state.intVal()) {
            case DECODE:
                if (I.not().and(IRQ).or(NMI_edge.bit(0)).boolVal()) {
                    PC_temp.set(ABH.concat(ABL));
                } else {
                    PC_temp.set(PC);
                }
                break;
            case JMP1:
            case JMPI1:
            case JSR3:
            case RTS3:
            case RTI4:
                PC_temp.set(DIMUX.concat(ADD));
                break;
            case BRA1:
                PC_temp.set(ABH.concat(ADD));
                break;
            case BRA2:
                PC_temp.set(ADD.concat(PCL));
                break;
            case BRK2:
                PC_temp.set(res.boolVal() ? 0xfffc : (
                        NMI_edge.boolVal() ? 0xfffa : 0xfffe
                ));
                break;
            default:
                PC_temp.set(PC);
                break;
        }
    }

    /*
     * internal signals
     */
    private Reg PC = new Reg(16);// Program Counter
    private Reg ABL = new Reg(8);// Address Bus Register LSB
    private Reg ABH = new Reg(8);// Address Bus Register MSB
    private Bits ADD = new Bits(8);// Adder Hold Register (registered in ALU)

    private Reg DIHOLD = new Reg(8);// Hold for Data In
    private Reg DIHOLD_valid = new Reg(1);
    private Bits DIMUX = new Bits(8);

    private Reg IRHOLD = new Reg(8);// Hold for Instruction register
    private Reg IRHOLD_valid = new Reg(1);// Valid instruction in IRHOLD

    private Reg A = new Reg(8);// Accumulator
    private Reg X = new Reg(8);// X register
    private Reg Y = new Reg(8);// Y register
    private Reg S = new Reg(8);// Stack pointer
    private Reg[] AXYS = {A, S, X, Y};// A, X, Y and S register file

    private Reg C = new Reg(1);// carry flag (init at zero to avoid X's in ALU sim)
    private Reg Z = new Reg(1);// zero flag
    private Reg I = new Reg(1);// interrupt flag
    private Reg D = new Reg(1);// decimal flag
    private Reg V = new Reg(1);// overflow flag
    private Reg N = new Reg(1);// negative flag
    private Bit AZ = new Bit();// ALU Zero flag
    private Bit AV = new Bit();// ALU overflow flag
    private Bit AN = new Bit();// ALU negative flag
    private Bit HC = new Bit();// ALU half carry

    private Reg AI = new Reg(8);// ALU Input A
    private Reg BI = new Reg(8);// ALU Input B
    private Bits IR = new Bits(8);// Instruction register
    private Reg CI = new Reg(1);// Carry In
    private Bit CO = new Bit();// Carry Out
    private Bits PCH = new Bits(PC.bits(15, 8));
    private Bits PCL = new Bits(PC.bits(7, 0));

    private Reg NMI_edge = new Reg(1);// captured NMI edge

    private Reg regsel = new Reg(2);// Select A, X, Y or S register
    private Bits regfile = new Bits(8);// Selected register output

    private Bits P = Bits.from(N.concat(V, Reg.inst(2, 0b11), D, I, Z, C));
    /*
     * instruction decoder/sequencer
     */
    private Reg state = new Reg(6);
    /*
     * control signals
     */
    private Reg PC_inc = new Reg(1);// Increment PC
    private Reg PC_temp = new Reg(16);// intermediate value of PC
    private Reg src_reg = new Reg(2);// source register index
    private Reg dst_reg = new Reg(2);// destination register index

    private Reg index_y = new Reg(1);// if set, then Y is index reg rather than X
    private Reg load_reg = new Reg(1);// loading a register (A, X, Y, S) in this instruction
    private Reg inc = new Reg(1);// increment
    private Reg write_back = new Reg(1);// set if memory is read/modified/written
    private Reg load_only = new Reg(1);// LDA/LDX/LDY instruction
    private Reg store = new Reg(1);// doing store (STA/STX/STY)
    private Reg adc_sbc = new Reg(1);// doing ADC/SBC
    private Reg compare = new Reg(1);// doing CMP/CPY/CPX
    private Reg shift = new Reg(1);// doing shift/rotate instruction
    private Reg rotate = new Reg(1);// doing rotate (no shift)
    private Reg backwards = new Reg(1);// backwards branch
    private Reg cond_true = new Reg(1);// branch condition is true
    private Reg cond_code = new Reg(3);// condition code bits from instruction
    private Reg shift_right = new Reg(1);// Instruction ALU shift/rotate right
    private Reg alu_shift_right = new Reg(1);// Current cycle shift right enable
    private Reg op = new Reg(4);// Main ALU operation for instruction
    private Reg alu_op = new Reg(4);// Current cycle ALU operation
    private Reg adc_bcd = new Reg(1);// ALU should do BCD style carry
    private Reg adj_bcd = new Reg(1);// results should be BCD adjusted

    /*
     * some flip flops to remember we're doing special instructions. These
     * get loaded at the DECODE state, and used later
     */
    private Reg bit_ins = new Reg(1);// doing BIT instruction
    private Reg plp = new Reg(1);// doing PLP instruction
    private Reg php = new Reg(1);// doing PHP instruction
    private Reg clc = new Reg(1);// clear carry
    private Reg sec = new Reg(1);// set carry
    private Reg cld = new Reg(1);// clear decimal
    private Reg sed = new Reg(1);// set decimal
    private Reg cli = new Reg(1);// clear interrupt
    private Reg sei = new Reg(1);// set interrupt
    private Reg clv = new Reg(1);// clear overflow
    private Reg brk = new Reg(1);// doing BRK

    private Reg res = new Reg(1);// in reset

    private Reg write_register = new Reg(1);// set when register file is written

    private Reg ADJL = new Reg(4);
    private Reg ADJH = new Reg(4);

    private Reg NMI_1 = new Reg(1);

    // Constants
//    private static final int SEL_A = 0;
//    private static final int SEL_S = 1;
//    private static final int SEL_X = 2;
//    private static final int SEL_Y = 3;

    /*
     * ALU operations
     */
    private static final int OP_OR = 0b1100;
    private static final int OP_AND = 0b1101;
    private static final int OP_EOR = 0b1110;
    private static final int OP_ADD = 0b0011;
    private static final int OP_SUB = 0b0111;
    private static final int OP_ROL = 0b1011;
    private static final int OP_A = 0b1111;

    /*
     * Microcode state machine. Basically, every addressing mode has its own
     * path through the state machine. Additional information, such as the
     * operation, source and destination registers are decoded in parallel, and
     * kept in separate flops.
     */
    private static final int ABS0 = 0; // ABS     - fetch LSB
    private static final int ABS1 = 1; // ABS     - fetch MSB
    private static final int ABSX0 = 2; // ABS, X  - fetch LSB and send to ALU (+X)
    private static final int ABSX1 = 3; // ABS, X  - fetch MSB and send to ALU (+Carry)
    private static final int ABSX2 = 4; // ABS, X  - Wait for ALU (only if needed)
    private static final int BRA0 = 5; // Branch  - fetch offset and send to ALU (+PC[7:0])
    private static final int BRA1 = 6; // Branch  - fetch opcode, and send PC[15:8] to ALU
    private static final int BRA2 = 7; // Branch  - fetch opcode (if page boundary crossed)
    private static final int BRK0 = 8; // BRK/IRQ - push PCH, send S to ALU (-1)
    private static final int BRK1 = 9; // BRK/IRQ - push PCL, send S to ALU (-1)
    private static final int BRK2 = 10; // BRK/IRQ - push P, send S to ALU (-1)
    private static final int BRK3 = 11; // BRK/IRQ - write S, and fetch @ fffe
    private static final int DECODE = 12; // IR is valid, decode instruction, and write prev reg
    private static final int FETCH = 13; // fetch next opcode, and perform prev ALU op
    private static final int INDX0 = 14; // (ZP,X)  - fetch ZP address, and send to ALU (+X)
    private static final int INDX1 = 15; // (ZP,X)  - fetch LSB at ZP+X, calculate ZP+X+1
    private static final int INDX2 = 16; // (ZP,X)  - fetch MSB at ZP+X+1
    private static final int INDX3 = 17; // (ZP,X)  - fetch data
    private static final int INDY0 = 18; // (ZP),Y  - fetch ZP address, and send ZP to ALU (+1)
    private static final int INDY1 = 19; // (ZP),Y  - fetch at ZP+1, and send LSB to ALU (+Y)
    private static final int INDY2 = 20; // (ZP),Y  - fetch data, and send MSB to ALU (+Carry)
    private static final int INDY3 = 21; // (ZP),Y) - fetch data (if page boundary crossed)
    private static final int JMP0 = 22; // JMP     - fetch PCL and hold
    private static final int JMP1 = 23; // JMP     - fetch PCH
    private static final int JMPI0 = 24; // JMP IND - fetch LSB and send to ALU for delay (+0)
    private static final int JMPI1 = 25; // JMP IND - fetch MSB, proceed with JMP0 state
    private static final int JSR0 = 26; // JSR     - push PCH, save LSB, send S to ALU (-1)
    private static final int JSR1 = 27; // JSR     - push PCL, send S to ALU (-1)
    private static final int JSR2 = 28; // JSR     - write S
    private static final int JSR3 = 29; // JSR     - fetch MSB
    private static final int PULL0 = 30; // PLP/PLA - save next op in IRHOLD, send S to ALU (+1)
    private static final int PULL1 = 31; // PLP/PLA - fetch data from stack, write S
    private static final int PULL2 = 32; // PLP/PLA - prefetch op, but don't increment PC
    private static final int PUSH0 = 33; // PHP/PHA - send A to ALU (+0)
    private static final int PUSH1 = 34; // PHP/PHA - write A/P, send S to ALU (-1)
    private static final int READ = 35; // Read memory for read/modify/write (INC, DEC, shift)
    private static final int REG = 36; // Read register for reg-reg transfers
    private static final int RTI0 = 37; // RTI     - send S to ALU (+1)
    private static final int RTI1 = 38; // RTI     - read P from stack
    private static final int RTI2 = 39; // RTI     - read PCL from stack
    private static final int RTI3 = 40; // RTI     - read PCH from stack
    private static final int RTI4 = 41; // RTI     - read PCH from stack
    private static final int RTS0 = 42; // RTS     - send S to ALU (+1)
    private static final int RTS1 = 43; // RTS     - read PCL from stack
    private static final int RTS2 = 44; // RTS     - write PCL to ALU, read PCH
    private static final int RTS3 = 45; // RTS     - load PC and increment
    private static final int WRITE = 46; // Write memory for read/modify/write
    private static final int ZP0 = 47; // Z-page  - fetch ZP address
    private static final int ZPX0 = 48; // ZP, X   - fetch ZP, and send to ALU (+X)
    private static final int ZPX1 = 49; // ZP, X   - load from memory

    /*
     * Address Generator
     */
    private static final int ZEROPAGE = 0x00;
    private static final int STACKPAGE = 0x01;

    public String getStateName() {
        String statename = "";
        switch (state.intVal()) {
            case DECODE:
                statename = "DECODE";
                break;
            case REG:
                statename = "REG";
                break;
            case ZP0:
                statename = "ZP0";
                break;
            case ZPX0:
                statename = "ZPX0";
                break;
            case ZPX1:
                statename = "ZPX1";
                break;
            case ABS0:
                statename = "ABS0";
                break;
            case ABS1:
                statename = "ABS1";
                break;
            case ABSX0:
                statename = "ABSX0";
                break;
            case ABSX1:
                statename = "ABSX1";
                break;
            case ABSX2:
                statename = "ABSX2";
                break;
            case INDX0:
                statename = "INDX0";
                break;
            case INDX1:
                statename = "INDX1";
                break;
            case INDX2:
                statename = "INDX2";
                break;
            case INDX3:
                statename = "INDX3";
                break;
            case INDY0:
                statename = "INDY0";
                break;
            case INDY1:
                statename = "INDY1";
                break;
            case INDY2:
                statename = "INDY2";
                break;
            case INDY3:
                statename = "INDY3";
                break;
            case READ:
                statename = "READ";
                break;
            case WRITE:
                statename = "WRITE";
                break;
            case FETCH:
                statename = "FETCH";
                break;
            case PUSH0:
                statename = "PUSH0";
                break;
            case PUSH1:
                statename = "PUSH1";
                break;
            case PULL0:
                statename = "PULL0";
                break;
            case PULL1:
                statename = "PULL1";
                break;
            case PULL2:
                statename = "PULL2";
                break;
            case JSR0:
                statename = "JSR0";
                break;
            case JSR1:
                statename = "JSR1";
                break;
            case JSR2:
                statename = "JSR2";
                break;
            case JSR3:
                statename = "JSR3";
                break;
            case RTI0:
                statename = "RTI0";
                break;
            case RTI1:
                statename = "RTI1";
                break;
            case RTI2:
                statename = "RTI2";
                break;
            case RTI3:
                statename = "RTI3";
                break;
            case RTI4:
                statename = "RTI4";
                break;
            case RTS0:
                statename = "RTS0";
                break;
            case RTS1:
                statename = "RTS1";
                break;
            case RTS2:
                statename = "RTS2";
                break;
            case RTS3:
                statename = "RTS3";
                break;
            case BRK0:
                statename = "BRK0";
                break;
            case BRK1:
                statename = "BRK1";
                break;
            case BRK2:
                statename = "BRK2";
                break;
            case BRK3:
                statename = "BRK3";
                break;
            case BRA0:
                statename = "BRA0";
                break;
            case BRA1:
                statename = "BRA1";
                break;
            case BRA2:
                statename = "BRA2";
                break;
            case JMP0:
                statename = "JMP0";
                break;
            case JMP1:
                statename = "JMP1";
                break;
            case JMPI0:
                statename = "JMPI0";
                break;
            case JMPI1:
                statename = "JMPI1";
                break;
        }
        return statename;
    }

    public static void main(String[] args) throws InterruptedException {
        Bit clk = new Bit();
        Bit reset = new Bit();
        Bits DI = new Bits(8, 0x00);
        Bit IRQ = new Bit();
        Bit NMI = new Bit();
        Bit RDY = new Bit();
        Reg AB = new Reg(16, 0x0000);
        Reg DO = new Reg(8, 0x00);
        Reg WE = new Reg(1, 0b0);

        Cpu cpu = new Cpu(null, clk, reset, DI, IRQ, NMI, RDY, AB, DO, WE);
        PropagateManager.propagate(cpu);

        reset.set();

        clk.clr();
        Thread.sleep(500);
        PropagateManager.propagate(cpu);
        clk.set();
        Thread.sleep(500);
        PropagateManager.propagate(cpu);
        System.out.printf("state: %6s, AB: 0x%04X\n", cpu.getStateName(), AB.intVal());

        reset.clr();
        RDY.set();

        while (true) {
            clk.clr();
            Thread.sleep(500);
            PropagateManager.propagate(cpu);
            clk.set();
            Thread.sleep(500);
            PropagateManager.propagate(cpu);
            System.out.printf("state: %6s, AB: 0x%04X\n", cpu.getStateName(), AB.intVal());
        }
    }
}

