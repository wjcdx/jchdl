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
package org.jchdl.model.rtl.example;

import org.jchdl.model.rtl.core.datatype.Reg;
import org.jchdl.model.rtl.core.datatype.Bit;
import org.jchdl.model.rtl.core.event.PosEdgeEvent;
import org.jchdl.model.rtl.core.io.annotation.Input;
import org.jchdl.model.rtl.core.io.annotation.Output;
import org.jchdl.model.rtl.core.meta.Module;
import org.jchdl.model.rtl.core.meta.PropagateManager;

public class Counter4 extends Module {
    @Output
    private Reg out;
    @Input
    private Bit reset;
    @Input
    private Bit clk;

    public Counter4(Module parent, Reg out, Bit reset, Bit clk) {
        super(parent);
        this.out = out;
        this.reset = reset;
        this.clk = clk;
        construct();
    }

    @Override
    public void logic() {
        when(PosEdgeEvent.of(clk)).run(() -> {
            if (reset.boolVal()) {
                out.set(0);
            } else {
                out.set(out.intVal() + 1);
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        Reg out = new Reg(4, Bit.BIT_1);
        Bit reset = new Bit(Bit.BIT_0);
        Bit clk = new Bit(Bit.BIT_0);

        Module m = new Counter4(null, out, reset, clk);
        m.toVerilog();
        PropagateManager.propagate(m);
        System.out.println("out: " + out);

        reset.set();
        PropagateManager.propagate(m);
        System.out.println("out: " + out);

        clk.clr();
        PropagateManager.propagate(m);
        System.out.println("out: " + out);

        clk.set();
        PropagateManager.propagate(m);
        System.out.println("out: " + out);

        clk.clr();
        PropagateManager.propagate(m);
        System.out.println("out: " + out);

        clk.set();
        PropagateManager.propagate(m);
        System.out.println("out: " + out);

        reset.clr();
        PropagateManager.propagate(m);
        System.out.println("out: " + out);

        System.out.println("ticking...");
        while (true) {
            Thread.sleep(500);
            clk.clr();
            PropagateManager.propagate(m);

            Thread.sleep(500);
            clk.set();
            PropagateManager.propagate(m);
            System.out.println("out: " + out);
        }
    }
}
