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
package org.jchdl.model.rtl.example.Mos6502.simplify;

import org.jchdl.model.rtl.core.datatype.Bits;
import org.jchdl.model.rtl.core.datatype.Reg;
import org.jchdl.model.rtl.core.datatype.annotation.Range;
import org.jchdl.model.rtl.core.datatype.Bit;
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
public class Alu0 extends Module {
    @Input
    private Bit clk;
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
    @Output
    @Range(msb = 7, lsb = 0)
    private Reg OUT;
    @Output
    private Reg CO;
    @Output
    private Bit Z;

    public Alu0(Module parent, @Input Bit clk, @Input Bits op,
                @Input Bits AI, @Input Bits BI, @Input Bit CI,
                @Output Reg OUT, @Output Reg CO, @Output Bit Z) {
        super(parent);
        //inputs
        this.clk = clk;
        this.op = op;
        this.AI = AI;
        this.BI = BI;
        this.CI = CI;
        //outputs
        this.OUT = OUT;
        this.CO = CO;
        this.Z = Z;
        //construct logic blocks
        construct();
    }

    @Override
    public void logic() {
        Reg tempLogic = new Reg(9);
        when(ChangingEvent.of(op.part(1, 0)), ChangingEvent.of(AI), ChangingEvent.of(BI)).run(() ->
                updateTempLogic(tempLogic)
        );

        Reg tempBI = new Reg(8);
        when(ChangingEvent.of(op.part(3, 2)), ChangingEvent.of(BI), ChangingEvent.of(tempLogic)).run(() ->
                updateTempBI(tempBI, tempLogic)
        );

        Bits temp = new Bits(9);
        when(ChangingEvent.of(tempLogic), ChangingEvent.of(tempBI), ChangingEvent.of(CI)).run(() ->
                temp.set(tempLogic.intVal() + tempBI.intVal() + CI.value)
        );

        when(PosEdgeEvent.of(clk)).run(() -> {
            OUT.set(temp.bits(7, 0));
            CO.set(temp.bit(8));
        });

        assign(Z).from(OUT).with(() ->
                Z.assign(Bit.inst(OUT.intVal() == 0))
        );
    }

    // calculate the logic operations. The 'case' can be done in 1 LUT per
    // bit. The 'right' shift is a simple mux that can be implemented by
    // F5MUX.
    private void updateTempLogic(Reg tempLogic) {
        switch (op.intVal(1, 0)) {
            case 0b00:
                tempLogic.set(AI.or(BI).bits());
                break;
            case 0b01:
                tempLogic.set(AI.and(BI).bits());
                break;
            case 0b10:
                tempLogic.set(AI.xor(BI).bits());
                break;
            case 0b11:
                tempLogic.set(AI.bits());
                break;
        }
    }

    // Add logic result to BI input. This only makes sense when logic = AI.
    // This stage can be done in 1 LUT per bit, using carry chain logic.
    private void updateTempBI(Reg tempBI, Reg tempLogic) {
        switch (op.intVal(3, 2)) {
            case 0b00:
                tempBI.set(BI.bits());
                break;
            case 0b01:
                tempBI.set(BI.not().bits());
                break;
            case 0b10:
                tempBI.set(tempLogic);
                break;
            case 0b11:
                tempBI.set(0);
                break;
        }
    }

    public static void main(String[] args) {
        Bits AI = new Bits(8);
        Bits BI = new Bits(8, 0b11);
        Bit CI = new Bit();
        Bits op = new Bits(4, 0b1011);
        Bit clk = new Bit();

        Reg OUT = new Reg(8);
        Reg CO = new Reg(1);
        Bit Z = new Bit();

        Module alu = new Alu0(null, clk, op, AI, BI, CI, OUT, CO, Z);
        PropagateManager.propagate(alu);
        System.out.println("out: " + OUT);

        clk.set();
        PropagateManager.propagate(alu);
        System.out.println("out: " + OUT);

        clk.clr();
        PropagateManager.propagate(alu);
        System.out.println("out: " + OUT);

        clk.set();
        PropagateManager.propagate(alu);
        System.out.println("out: " + OUT);

        // set a bit
        AI.bit(6).set();
        AI.bit(1).set();

        clk.clr();
        PropagateManager.propagate(alu);
        System.out.println("out: " + OUT);

        clk.set();
        PropagateManager.propagate(alu);
        System.out.println("out: " + OUT);

        AI.bit(0).set();

        clk.clr();
        PropagateManager.propagate(alu);
        System.out.println("out: " + OUT);

        clk.set();
        PropagateManager.propagate(alu);
        System.out.println("out: " + OUT);
    }
}
