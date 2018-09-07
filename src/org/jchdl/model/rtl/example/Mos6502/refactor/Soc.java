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
//import org.jchdl.model.rtl.core.io.annotation.Input;
//import org.jchdl.model.rtl.core.meta.Module;
//import org.jchdl.model.rtl.core.meta.PropagateManager;
//import org.jchdl.model.rtl.example.Mos6502.Mem;
//
//public class Soc extends Module {
//    @Input
//    private Bit clk;
//    @Input
//    private Bit reset;
//    @Input
//    private Bit IRQ;
//    @Input
//    private Bit NMI;
//    @Input
//    private Bit RDY;
//
//    public Soc(Module parent, Bit clk, Bit reset, Bit IRQ, Bit NMI, Bit RDY) {
//        super(parent);
//        //inputs
//        this.clk = clk;
//        this.reset = reset;
//        this.IRQ = IRQ;
//        this.NMI = NMI;
//        this.RDY = RDY;
//        //construct logic blocks
//        construct();
//    }
//
//    @Override
//    public void logic() {
//        DI = new Bits(8, 0x00);
//        AB = new Reg(16, 0x0000);
//        DO = new Reg(8, 0x00);
//        WE = new Reg(1, 0b0);
//        cpu = new Cpu(this, clk, reset, DI, IRQ, NMI, RDY, AB, DO, WE);
//        mem = new Mem(this, clk, WE.bit(0), Bits.from(AB), Bits.from(DO), Reg.to(DI));
//    }
//
//    public void show() {
//        System.out.printf("state: %6s, AB: 0x%04X, WE: %d, DI: 0x%02X, DO: 0x%02X  ",
//                cpu.getStateName(), AB.intVal(), WE.intVal(), DI.intVal(), DO.intVal());
////        Mem.dump(0x01FF, 0x01F0);
//    }
//
//    private Cpu cpu;
//    private Mem mem;
//    private Reg AB;
//    private Reg DO;
//    private Bits DI;
//    private Reg WE;
//
//    public static void main(String[] args) throws InterruptedException {
//        Bit clk = new Bit();
//        Bit reset = new Bit();
//        Bit IRQ = new Bit();
//        Bit NMI = new Bit();
//        Bit RDY = new Bit();
//        Soc soc = new Soc(null, clk, reset, IRQ, NMI, RDY);
//
//        reset.set();
//        tick(clk, soc);
//
//        reset.clr();
//        tick(clk, soc);
//
//        RDY.set();
//        while (true) {
//            tick(clk, soc);
//        }
//    }
//
//    private static void tick(Bit clk, Soc soc) throws InterruptedException {
//        clk.clr();
//        Thread.sleep(500);
//        PropagateManager.propagate(soc);
//        clk.set();
//        Thread.sleep(500);
//        PropagateManager.propagate(soc);
//        soc.show();
//    }
//}
