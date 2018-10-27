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
package org.jchdl.model.gsl.sequential;

import org.jchdl.model.gsl.core.datatype.helper.WireVec;
import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.ni.atomic.And;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.operator.conditional.Mux;
import org.jchdl.model.gsl.sequential.ff.DFlipFlop;

public class Counter extends Node {
    private int nBits = 0;

    Wire clk;
    Wire clr;
    WireVec out;

    public Counter(WireVec out, Wire clk, Wire clr) {
        nBits = out.nBits();
        in(clk);
        in(clr);
        out(out.wires());
        construct();
    }

    @Override
    public void logic() {
        clk = new Wire(in(0));
        clr = new Wire(in(1));
        out = new WireVec(outputs());

        Wire nqPrev = new Wire(in(0));
        for (int i = 0; i < nBits; i++) {
            Wire q = out.wire(i);
            Wire nq = new Wire();
            Wire wAnd = new Wire();
            And.inst(wAnd, nq, clr);

            Wire clkFinal = new Wire();
            Mux.inst(clkFinal, clk, nqPrev, clr);

            DFlipFlop.inst(q, nq, clkFinal, wAnd);
            nqPrev = nq;
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName() + "_" + nBits;
    }

    public static Counter inst(WireVec out, Wire clk, Wire clr) {
        return new Counter(out, clk, clr);
    }

    public static void main(String[] args) throws InterruptedException {
        WireVec out = new WireVec(8);
        Wire clk = new Wire();
        Wire clr = new Wire();

        Counter.inst(out, clk, clr);

        System.out.println("\n# clr = 0");
        clr.assign(Value.V0);
        PropagateManager.propagateParallel(clr);
        Clock.tick(clk, 1);
        System.out.println("out: " + out);

        System.out.println("\n# clr = 1");
        clr.assign(Value.V1);
        PropagateManager.propagateParallel(clr);
        System.out.println("out: " + out);

        System.out.println("\n# counting...");
        for (;;) {
            Thread.sleep(1000);
            Clock.tick(clk, 1);
            System.out.println("out: " + out);
        }
    }
}
