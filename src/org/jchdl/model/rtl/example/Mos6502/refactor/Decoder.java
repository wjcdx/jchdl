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
//package org.jchdl.model.rtl.example.Mos6502.refactor;
//
//import org.jchdl.model.rtl.core.datatype.Bit;
//import org.jchdl.model.rtl.core.datatype.Bits;
//import org.jchdl.model.rtl.core.datatype.Reg;
//import org.jchdl.model.rtl.core.datatype.annotation.Width;
//import org.jchdl.model.rtl.core.event.PosEdgeEvent;
//import org.jchdl.model.rtl.core.io.annotation.Input;
//import org.jchdl.model.rtl.core.io.annotation.Output;
//import org.jchdl.model.rtl.core.meta.Module;
//import org.jchdl.model.rtl.util.Splitter;
//
//public class Decoder extends Module {
//    @Input
//    private Bit clk;
//    @Input
//    private Bit EN;
//    @Input
//    private Bit D;
//    @Input
//    @Width(8)
//    private Bits IR;
//
//    @Output
//    @Width(32)
//    Reg InsFlag;
//
//    public Decoder(Module parent, Bit clk, Bit EN, Bit D, Bits IR,
//                   Reg InsFlag) {
//        super(parent);
//        //inputs
//        this.clk = clk;
//        this.EN = EN;
//        this.D = D;
//        this.IR = IR;
//        //outputs
//        this.InsFlag = InsFlag;
//        //construct logic blocks
//        construct();
//    }
//
//    @Override
//    public void logic() {
//        initPartialRegs();
//        when(PosEdgeEvent.of(clk)).run(this::updateLoadReg);
//        when(PosEdgeEvent.of(clk)).run(this::updateDstReg);
//        when(PosEdgeEvent.of(clk)).run(this::updateSrcReg);
//        when(PosEdgeEvent.of(clk)).run(this::updateIndexY);
//        when(PosEdgeEvent.of(clk)).run(this::updateStore);
//        when(PosEdgeEvent.of(clk)).run(this::updateWriteBack);
//        when(PosEdgeEvent.of(clk)).run(this::updateLoadOnly);
//        when(PosEdgeEvent.of(clk)).run(this::updateInc);
//        when(PosEdgeEvent.of(clk)).run(this::updateAdcSbc);
//        when(PosEdgeEvent.of(clk)).run(this::updateAdcBcd);
//        when(PosEdgeEvent.of(clk)).run(this::updateShift);
//        when(PosEdgeEvent.of(clk)).run(this::updateCompare);
//        when(PosEdgeEvent.of(clk)).run(this::updateShiftRight);
//        when(PosEdgeEvent.of(clk)).run(this::updateRotate);
//        when(PosEdgeEvent.of(clk)).run(this::updateOp);
//        when(PosEdgeEvent.of(clk)).run(this::updateBitIns);
//        when(PosEdgeEvent.of(clk)).run(this::updateSpecialInstructions);
//    }
//
//    private void initPartialRegs() {
//        php = InsFlag.part(0);
//        clc = InsFlag.part(1);
//        plp = InsFlag.part(2);
//        sec = InsFlag.part(3);
//        cli = InsFlag.part(4);
//        sei = InsFlag.part(5);
//        clv = InsFlag.part(6);
//        cld = InsFlag.part(7);
//        sed = InsFlag.part(8);
//        brk = InsFlag.part(9);
//        bit_ins = InsFlag.part(10);
//        rotate = InsFlag.part(11);
//        shift_right = InsFlag.part(12);
//        compare = InsFlag.part(13);
//        shift = InsFlag.part(14);
//        adc_bcd = InsFlag.part(15);
//        adc_sbc = InsFlag.part(16);
//        inc = InsFlag.part(17);
//        load_only = InsFlag.part(18);
//        write_back = InsFlag.part(19);
//        store = InsFlag.part(20);
//        index_y = InsFlag.part(21);
//
//        op = InsFlag.part(25, 22);
//        src_reg = InsFlag.part(27, 26);
//        dst_reg = InsFlag.part(29, 28);
//        load_reg = InsFlag.part(30);
//    }
//
//    private void updateSpecialInstructions() {
//        if (EN.boolVal()) {
//            php.set(Bit.inst(IR.eq(0x08)));
//            clc.set(Bit.inst(IR.eq(0x18)));
//            plp.set(Bit.inst(IR.eq(0x28)));
//            sec.set(Bit.inst(IR.eq(0x38)));
//            cli.set(Bit.inst(IR.eq(0x58)));
//            sei.set(Bit.inst(IR.eq(0x78)));
//            clv.set(Bit.inst(IR.eq(0xb8)));
//            cld.set(Bit.inst(IR.eq(0xd8)));
//            sed.set(Bit.inst(IR.eq(0xf8)));
//            brk.set(Bit.inst(IR.eq(0x00)));
//        }
//    }
//
//    private void updateBitIns() {
//        if (EN.boolVal()) {
//            if (IR.match("0b0010_x100")) { // BIT zp/abs
//                bit_ins.set(1);
//            } else {
//                bit_ins.set(0);
//            }
//        }
//    }
//
//    private void updateOp() {
//        if (EN.boolVal()) {
//            if (IR.match("0b00xx_xx10")) op.set(OP_ROL);       // ROL: ASL
//            else if (IR.match("0b0010_x100")) op.set(OP_AND); // BIT zp/abs
//            else if (IR.match("0b01xx_xx10")) op.set(OP_A);   // ROR: LSR
//            else if (IR.match("",
//                    "0b1000_1000",// DEY
//                    "0b1100_1010",// DEX
//                    "0b110x_x110",// DEC
//                    "0b11xx_xx01",// CMP, SBC
//                    "0b11x0_0x00",// CPX, CPY (imm, zpg)
//                    "0b11x0_1100"
//            )) {
//                op.set(OP_SUB);
//            } else if (IR.match("",
//                    "0b010x_xx01",// EOR
//                    "0b00xx_xx01" // ORA, AND
//            )) {
//                op.set(Bits.inst(2, 0b11).concat(IR.part(6, 5)));
//            } else {
//                op.set(OP_ADD);
//            }
//        }
//    }
//
//    private void updateRotate() {
//        if (EN.boolVal()) {
//            if (IR.match("",
//                    "0b0x1x_1010",   // ROL A: ROR A
//                    "0b0x1x_x110"    // ROR: ROL
//            )) {
//                rotate.set(1);
//            } else {
//                rotate.set(0);
//            }
//        }
//    }
//
//    private void updateShiftRight() {
//        if (EN.boolVal()) {
//            if (IR.match("0b01xx_xx10")) {   // ROR: LSR
//                shift_right.set(1);
//            } else {
//                shift_right.set(0);
//            }
//        }
//    }
//
//    private void updateCompare() {
//        if (EN.boolVal()) {
//            if (IR.match("",
//                    "0b11x0_0x00",   // CPX: CPY (imm/zp)
//                    "0b11x0_1100",   // CPX: CPY (abs)
//                    "0b110x_xx01"    // CMP
//            )) {
//                compare.set(1);
//            } else {
//                compare.set(0);
//            }
//        }
//    }
//
//    private void updateShift() {
//        if (EN.boolVal()) {
//            if (IR.match("",
//                    "0b0xxx_x110",   // ASL: ROL: LSR: ROR (abs: absx: zpg: zpgx)
//                    "0b0xxx_1010"    // ASL: ROL: LSR: ROR (acc)
//            )) {
//                shift.set(1);
//            } else {
//                shift.set(0);
//            }
//        }
//    }
//
//    private void updateAdcBcd() {
//        if (EN.boolVal()) {
//            if (IR.match("0b011x_xx01")) { // ADC
//                adc_bcd.set(D);
//            } else {
//                adc_bcd.set(0);
//            }
//        }
//    }
//
//    private void updateAdcSbc() {
//        if (EN.boolVal()) {
//            if (IR.match("0bx11x_xx01")) { // SBC: ADC
//                adc_sbc.set(1);
//            } else {
//                adc_sbc.set(0);
//            }
//        }
//    }
//
//    private void updateInc() {
//        if (EN.boolVal()) {
//            if (IR.match("",
//                    "0b111x_x110",   // INC
//                    "0b11x0_1000"    // INX: INY
//            )) {
//                inc.set(1);
//            } else {
//                inc.set(0);
//            }
//        }
//    }
//
//    private void updateLoadOnly() {
//        if (EN.boolVal()) {
//            if (IR.match("0b101x_xxxx")) { // LDA: LDX: LDY
//                load_only.set(1);
//            } else {
//                load_only.set(0);
//            }
//        }
//    }
//
//    private void updateWriteBack() {
//        if (EN.boolVal()) {
//            if (IR.match("",
//                    "0b0xxx_x110",   // ASL: ROL: LSR: ROR
//                    "0b11xx_x110"    // DEC/INC
//            )) {
//                write_back.set(1);
//            } else {
//                write_back.set(0);
//            }
//        }
//    }
//
//    private void updateStore() {
//        if (EN.boolVal()) {
//            if (IR.match("",
//                    "0b100x_x1x0",   // STX: STY
//                    "0b100x_xx01"    // STA
//            )) {
//                store.set(1);
//            } else {
//                store.set(0);
//            }
//        }
//    }
//
//    private void updateIndexY() {
//        if (EN.boolVal()) {
//            if (IR.match("",
//                    "0bxxx1_0001",   // INDY
//                    "0b10x1_x110",   // LDX/STX zpg/abs, Y
//                    "0bxxxx_1001"    // abs, Y
//            )) {
//                index_y.set(1);
//            } else {
//                index_y.set(0);
//            }
//        }
//    }
//
//    private void updateSrcReg() {
//        if (EN.boolVal()) {
//            if (IR.match("",
//                    "0b1011_1010"   // TSX
//            )) {
//                src_reg.set(SEL_S);
//            } else if (IR.match("",
//                    "0b100x_x110",   // STX
//                    "0b100x_1x10",   // TXA: TXS
//                    "0b1110_xx00",   // INX: CPX
//                    "0b1100_1010"    // DEX
//            )) {
//                src_reg.set(SEL_X);
//            } else if (IR.match("",
//                    "0b100x_x100",   // STY
//                    "0b1001_1000",   // TYA
//                    "0b1100_xx00",   // CPY
//                    "0b1x00_1000"    // DEY: INY
//            )) {
//                src_reg.set(SEL_Y);
//            } else {
//                src_reg.set(SEL_A);
//            }
//        }
//    }
//
//    private void updateDstReg() {
//        if (EN.boolVal()) {
//            if (IR.match("",
//                    "0b1110_1000",   // INX
//                    "0b1100_1010",   // DEX
//                    "0b101x_xx10"    // LDX: TAX: TSX
//            )) {
//                dst_reg.set(SEL_X);
//            } else if (IR.match("",
//                    "0b0x00_1000",   // PHP: PHA
//                    "0b1001_1010"    // TXS
//            )) {
//                dst_reg.set(SEL_S);
//            } else if (IR.match("",
//                    "0b1x00_1000",   // DEY: DEX
//                    "0b101x_x100",   // LDY
//                    "0b1010_0000",   // LDY #imm: TAY
//                    "0b1010_1000"    // LDY #imm: TAY
//            )) {
//                dst_reg.set(SEL_Y);
//            } else {
//                dst_reg.set(SEL_A);
//            }
//        }
//    }
//
//    private void updateLoadReg() {
//        if (EN.boolVal()) {
//            if (IR.match("",
//                    "0b0xx0_1010",    // ASLA: ROLA: LSRA: RORA
//                    "0b0xxx_xx01",    // ORA: AND: EOR: ADC
//                    "0b100x_10x0",    // DEY: TYA: TXA: TXS
//                    "0b1010_xxx0",    // LDA/LDX/LDY
//                    "0b1011_1010",    // TSX
//                    "0b1011_x1x0",    // LDX/LDY
//                    "0b1100_1010",    // DEX
//                    "0b1x1x_xx01",    // LDA: SBC
//                    "0bxxx0_1000"     // DEY: TAY: INY: INX
//            )) {
//                load_reg.set(1);
//            } else {
//                load_reg.set(0);
//            }
//        }
//    }
//
//    private Reg php;
//    private Reg clc;
//    private Reg plp;
//    private Reg sec;
//    private Reg cli;
//    private Reg sei;
//    private Reg clv;
//    private Reg cld;
//    private Reg sed;
//    private Reg brk;
//    private Reg bit_ins;
//    private Reg rotate;
//    private Reg shift_right;
//    private Reg compare;
//    private Reg shift;
//    private Reg adc_bcd;
//    private Reg adc_sbc;
//    private Reg inc;
//    private Reg load_only;
//    private Reg write_back;
//    private Reg store;
//    private Reg index_y;
//
//    @Width(4)
//    private Reg op;
//    @Width(2)
//    private Reg src_reg;
//    @Width(2)
//    private Reg dst_reg;
//    private Reg load_reg;
//
//    // Constants
//    private static final int SEL_A = 0;
//    private static final int SEL_S = 1;
//    private static final int SEL_X = 2;
//    private static final int SEL_Y = 3;
//
//    /*
//     * ALU operations
//     */
//    private static final int OP_OR = 0b1100;
//    private static final int OP_AND = 0b1101;
//    private static final int OP_EOR = 0b1110;
//    private static final int OP_ADD = 0b0011;
//    private static final int OP_SUB = 0b0111;
//    private static final int OP_ROL = 0b1011;
//    private static final int OP_A = 0b1111;
//
//    private static String flagRegBitNames[] = {
//            "php",
//            "clc",
//            "plp",
//            "sec",
//            "cli",
//            "sei",
//            "clv",
//            "cld",
//            "sed",
//            "brk",
//            "bit_ins",
//            "rotate",
//            "shift_right",
//            "compare",
//            "shift",
//            "adc_bcd",
//            "adc_sbc",
//            "inc",
//            "load_only",
//            "write_back",
//            "store",
//            "index_y",
//            "op",
//            "src_reg",
//            "dst_reg",
//            "load_reg",
//    };
//
//    public static void main(String[] args) {
//        Splitter.splitReg("InsFlag", flagRegBitNames);
//        Splitter.toReg("InsFlag", flagRegBitNames);
//    }
//}
