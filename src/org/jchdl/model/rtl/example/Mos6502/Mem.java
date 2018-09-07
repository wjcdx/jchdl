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
import org.jchdl.model.rtl.core.event.PosEdgeEvent;
import org.jchdl.model.rtl.core.event.expression.ExprEvent;
import org.jchdl.model.rtl.core.io.annotation.Input;
import org.jchdl.model.rtl.core.io.annotation.Output;
import org.jchdl.model.rtl.core.meta.Module;
import org.jchdl.model.rtl.core.meta.PropagateManager;

public class Mem extends Module {
    @Input
    Bit clk;
    @Input
    Bit WE;
    @Input
    @Range(msb = 15, lsb = 0)
    Bits AB;
    @Input
    @Range(msb = 8, lsb = 0)
    Bits DI;

    @Output
    @Range(msb = 8, lsb = 0)
    Reg DO;

    public Mem(Module parent, Bit clk, Bit WE, Bits AB, Bits DI, Reg DO) {
        super(parent);
        //inputs
        this.clk = clk;
        this.WE = WE;
        this.AB = AB;
        this.DI = DI;
        //outputs
        this.DO = DO;
        //construct logic blocks
        construct();
//        logic2();
    }

    public void logic2() {
        Reg address = new Reg(16, 0x0000);
        when(PosEdgeEvent.of(clk)).run(() -> {
            address.set(AB);
            if (WE.boolVal()) {
                mem[address.intVal()].set(DI);
            }
        });
        when(ExprEvent.of(() -> mem[address.intVal()])).run(() -> {
            DO.set(mem[address.intVal()]);
        });
    }

    @Override
    public void logic() {
        when(PosEdgeEvent.of(clk)).run(() -> {
            if (WE.boolVal()) {
                mem[AB.intVal()].set(DI);
            }
            DO.set(mem[AB.intVal()]);
        });
    }

    private static int MEM_SIZE = 64 * 1024;
    private static Reg[] mem = new Reg[MEM_SIZE];
    static  {
        int[] code = {
                0xa9, 0x00,              // LDA #$00
                0x20, 0x10, 0x00,        // JSR $0010
                0x4c, 0x02, 0x00,        // JMP $0002

                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x80,

                0xe8,                    // INX
                0x88,                    // DEY
                0xe6, 0x0f,              // INC $0F
                0x38,                    // SEC
                0x69, 0x02,              // ADC #$02
                0x60                     // RTS
        };
        for (int i = 0; i < MEM_SIZE; i++) {
            int v = i < code.length ? code[i] : 0x00;
            mem[i] = new Reg(8, v);
        }
    }

    public static void dump(int high, int low) {
        System.out.printf("mem[%04X:%04X]: ", high, low);
        for (int i = high; i >= low; i--) {
            System.out.printf("0x%02X ", mem[i].intVal());
        }
        System.out.println();
    }

    public static void main(String[] args) throws InterruptedException {
        Bit clk = new Bit();
        Bits DI = new Bits(8, 0x00);
        Bits AB = new Bits(16, 0x0000);
        Bit WE = new Bit();
        Reg DO = new Reg(8, 0x00);
        Mem mem = new Mem(null, clk, WE, AB, DI, DO);

        tick(clk, AB, WE, DI, DO, mem);

        AB.set(0xFFFC);
        tick(clk, AB, WE, DI, DO, mem);

        AB.set(0xFFFD);
        tick(clk, AB, WE, DI, DO, mem);

        DI.assign(Bits.inst(8, 0x88));
        tick(clk, AB, WE, DI, DO, mem);

        WE.assign(Bit.BIT_1);
        tick(clk, AB, WE, DI, DO, mem);
    }

    private static void tick(Bit clk, Bits AB, Bit WE, Bits DI, Reg DO, Mem mem) throws InterruptedException {
        clk.clr();
        Thread.sleep(500);
        PropagateManager.propagate(mem);
        clk.set();
        Thread.sleep(500);
        PropagateManager.propagate(mem);
        System.out.printf("AB: 0x%04X, WE: %d, DI: 0x%02X, DO: 0x%02X\n",
                AB.intVal(), WE.value, DI.intVal(), DO.intVal());
    }
}
