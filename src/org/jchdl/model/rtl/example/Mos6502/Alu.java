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


/**
 * ALU.
 * <p>
 * AI and BI are 8 bit inputs. Result in OUT.
 * CI is Carry In.
 * CO is Carry Out.
 * <p>
 * op[3:0] is defined as follows:
 * <p>
 * 0011   AI + BI
 * 0111   AI - BI
 * 1011   AI + AI
 * 1100   AI | BI
 * 1101   AI & BI
 * 1110   AI ^ BI
 * 1111   AI
 */
//ref: https://github.com/Arlet/verilog-6502
public class Alu extends Module {
    @Input
    private Bit clk;
    @Input
    private Bit right;
    @Input
    @Range(msb = 3, lsb = 0)
    private Bits op;
    @Input
    @Range(msb = 7, lsb = 0)
    private Bits AI;
    @Input
    @Range(msb = 7, lsb = 0)
    private Bits BI;
    @Input
    private Bit CI; // while op is A-B, CI should be set to 1.
    @Input
    private Bit BCD;
    @Input
    private Bit RDY;
    @Output
    @Range(msb = 7, lsb = 0)
    private Reg OUT;
    @Output
    private Reg CO;
    @Output
    private Bit V;
    @Output
    private Bit Z;
    @Output
    private Reg N;
    @Output
    private Reg HC;

    public Alu(Module parent, Bit clk, Bit right, Bits op,
               Bits AI, Bits BI, Bit CI, Bit BCD, Bit RDY,
               Reg OUT, Reg CO, Bit V, Bit Z,
               Reg N, Reg HC) {
        super(parent);
        this.clk = clk;
        this.right = right;
        this.op = op;
        this.AI = AI;
        this.BI = BI;
        this.CI = CI;
        this.BCD = BCD;
        this.RDY = RDY;

        this.OUT = OUT;
        this.CO = CO;
        this.V = V;
        this.Z = Z;
        this.N = N;
        this.HC = HC;
        construct();
    }

    @Override
    public void logic() {
        assign(tempCI).from(right, op.part(3, 2), CI).with(() ->
                tempCI.assign(right.or(op.part(3, 2).eq(0b11)).boolVal() ? Bit.B0 : CI)
        );

        when(ChangingEvent.of(op.part(1, 0), right, AI, BI, CI)).run(this::updateTempLogic);

        when(ChangingEvent.of(op.part(3, 2), BI, tempLogic)).run(this::updateTempBI);

        assign(temp).from(tempH, tempL.part(3, 0)).with(() ->
                temp.assign(Bits.from(tempH.concat(tempL.part(3, 0))))
        );

        assign(HC9).from(BCD, tempL.part(3, 1)).with(() ->
                HC9.assign(BCD.and(tempL.part(3, 1).ge(5)))
        );
        assign(CO9).from(BCD, tempH.part(3, 1)).with(() ->
                CO9.assign(BCD.and(tempH.part(3, 1).ge(5)))
        );
        assign(tempHC).from(HC9, tempL.bit(4)).with(() ->
                tempHC.assign(HC9.or(tempL.bit(4)))
        );

        when(ChangingEvent.of(tempLogic.part(3, 0), tempBI.part(3, 0), tempCI)).run(() ->
                tempL.set(tempLogic.intVal(3, 0) + tempBI.intVal(3, 0) + tempCI.value)
        );
        when(ChangingEvent.of(tempLogic.part(8, 4), tempBI.part(7, 4), tempHC)).run(() ->
                tempH.set(tempLogic.intVal(8, 4) + tempBI.intVal(7, 4) + tempHC.value)
        );

        when(ChangingEvent.of(AI.bit(7))).run(() -> AI7.set(AI.bit(7)));
        when(ChangingEvent.of(tempBI.bit(7))).run(() -> BI7.set(tempBI.bit(7)));
        when(PosEdgeEvent.of(clk)).run(this::updateFlags);
        assign(V).from(AI7, BI7, CO, N).with(() ->
                V.assign(AI7.xor(BI7).xor(CO).xor(N).bit(0))
        );
        assign(Z).from(OUT).with(() ->
                Z.assign(Bit.inst(OUT.boolVal()))
        );
    }

    // calculate the logic operations. The 'case' can be done in 1 LUT per
    // bit. The 'right' shift is a simple mux that can be implemented by
    // F5MUX.
    private void updateTempLogic() {
        switch (op.intVal(1, 0)) {
            case 0b00:
                tempLogic.set(AI.or(BI));
                break;
            case 0b01:
                tempLogic.set(AI.and(BI));
                break;
            case 0b10:
                tempLogic.set(AI.xor(BI));
                break;
            case 0b11:
                tempLogic.set(AI);
                break;
        }
        if (right.boolVal()) {
            tempLogic.set(AI.part(0).concat(CI, AI.part(7, 1)));
        }
    }

    // Add logic result to BI input. This only makes sense when logic = AI.
    // This stage can be done in 1 LUT per bit, using carry chain logic.
    private void updateTempBI() {
        switch (op.intVal(3, 2)) {
            case 0b00:
                tempBI.set(BI);
                break;
            case 0b01:
                tempBI.set(BI.not());
                break;
            case 0b10:
                tempBI.set(tempLogic);
                break;
            case 0b11:
                tempBI.set(0);
                break;
        }
    }

    private void updateFlags() {
        if (RDY.boolVal()) {
            OUT.set(temp.bits(7, 0));
            CO.set(temp.bit(8).or(CO9));
            N.set(temp.bit(7));
            HC.set(tempHC);
        }
    }

    private Bit tempCI = new Bit();
    private Reg tempLogic = new Reg(9);
    private Reg tempBI = new Reg(8);
    private Reg tempL = new Reg(5);
    private Reg tempH = new Reg(5);
    private Bits temp = new Bits(9);
    private Bit HC9 = new Bit();
    private Bit CO9 = new Bit();
    private Bit tempHC = new Bit();
    private Reg AI7 = new Reg(1);
    private Reg BI7 = new Reg(1);

    public static void main(String[] args) {
        Bits AI = new Bits(8);
        Bits BI = new Bits(8, 0b11);
        Bit CI = new Bit(Bit.BIT_0);
        Bits op = new Bits(4, 0b0111);
        Bit clk = new Bit();
        Bit right = new Bit(Bit.BIT_0);
        Bit BCD = new Bit(Bit.BIT_0);
        Bit RDY = new Bit(Bit.BIT_1);

        Reg OUT = new Reg(8);
        Reg CO = new Reg(1);
        Bit Z = new Bit();
        Bit V = new Bit();
        Reg N = new Reg(1);
        Reg HC = new Reg(1);

        Module alu = new Alu(null, clk, right, op, AI, BI, CI, BCD, RDY,
                OUT, CO, V, Z, N, HC);
        PropagateManager.propagate(alu);
        System.out.println("out: " + CO + "_" + OUT);

        clk.set();
        PropagateManager.propagate(alu);
        System.out.println("out: " + CO + "_" + OUT);

        // SEC: set CI for A-B.
        CI.set();

        clk.clr();
        PropagateManager.propagate(alu);
        System.out.println("out: " + CO + "_" + OUT);

        clk.set();
        PropagateManager.propagate(alu);
        System.out.println("out: " + CO + "_" + OUT);

        // set a bit
        AI.bit(6).set();
        AI.bit(1).set();

        clk.clr();
        PropagateManager.propagate(alu);
        System.out.println("out: " + CO + "_" + OUT);

        clk.set();
        PropagateManager.propagate(alu);
        System.out.println("out: " + CO + "_" + OUT);

        alu.toVerilog();
    }

    private static void dumpXorResults() {
        for (int i = 0; i <= 0xf; i++) {
            int b0 = (i & 0b0001) > 0 ? 1 : 0;
            int b1 = (i & 0b0010) > 0 ? 1 : 0;
            int b2 = (i & 0b0100) > 0 ? 1 : 0;
            int b3 = (i & 0b1000) > 0 ? 1 : 0;

            System.out.println(
                    (b0 ^ b1 ^ b2 ^ b3) + " "
                            + b0 + " "
                            + b1 + " "
                            + b2 + " "
                            + b3 + " "
            );
        }
    }
}
