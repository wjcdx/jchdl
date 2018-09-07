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
package org.jchdl.model.gsl.sequential.ff;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.ni.And;
import org.jchdl.model.gsl.core.gate.ni.Xor;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.sequential.Clock;

public class ToggleFlipFlop extends Node {
    public ToggleFlipFlop(Wire q, Wire nq, Wire clk, Wire t, Wire clr) {
        in(Wire.array(clk, t, clr));
        out(q);
        out(nq);
        construct();
    }

    @Override
    public void logic() {
        Wire clk = new Wire(in(0));
        Wire t = new Wire(in(1));
        Wire clr = new Wire(in(2));
        Wire q = new Wire(out(0));
        Wire nq = new Wire(out(1));

        Wire wXor = new Wire();
        Xor.inst(wXor, t, q);

        Wire wAnd = new Wire();
        And.inst(wAnd, wXor, clr);

        DFlipFlop.inst(q, nq, clk, wAnd);
    }

    public static ToggleFlipFlop inst(Wire q, Wire nq, Wire clk, Wire t, Wire clr) {
        return new ToggleFlipFlop(q, nq, clk, t, clr);
    }

    public static void main(String[] args) throws InterruptedException {
        Wire clk = new Wire();
        Wire t = new Wire();
        Wire clr = new Wire();
        Wire q = new Wire();
        Wire nq = new Wire();

        ToggleFlipFlop.inst(q, nq, clk, t, clr);

        System.out.println("\n# clr = 0");
        clr.assign(Value.V0);
        t.assign(Value.V0);
        PropagateManager.propagateParallel(clr, t);
        System.out.println("q => " + q.getValue().toString() + " nq => " + nq.getValue().toString());
        Clock.tick(clk, q, nq, 3);

        System.out.println("\n# clr = 1");
        clr.assign(Value.V1);
        PropagateManager.propagateParallel(clr);
        System.out.println("q => " + q.getValue().toString() + " nq => " + nq.getValue().toString());
        Clock.tick(clk, q, nq, 3);

        System.out.println("\n# t = 0");
        t.assign(Value.V0);
        PropagateManager.propagateParallel(t);
        System.out.println("t = 0, q => " + q.getValue().toString() + " nq => " + nq.getValue().toString());
        Clock.tick(clk, q, nq, 3);

        System.out.println("\n# t = 1");
        t.assign(Value.V1);
        PropagateManager.propagateParallel(t);
        System.out.println("t = 1, q => " + q.getValue().toString() + " nq => " + nq.getValue().toString());
        Clock.tick(clk, q, nq, 3);
    }


}
