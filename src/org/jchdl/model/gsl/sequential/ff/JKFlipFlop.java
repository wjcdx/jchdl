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
import org.jchdl.model.gsl.core.gate.ni.Or;
import org.jchdl.model.gsl.core.gate.no.Not;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.sequential.Clock;

public class JKFlipFlop extends Node {
    public JKFlipFlop(Wire q, Wire nq, Wire clk, Wire j, Wire k) {
        in(clk);
        in(j);
        in(k);
        out(q);
        out(nq);
        construct();
    }

    @Override
    public void logic() {
        Wire clk = new Wire(in(0));
        Wire j = new Wire(in(1));
        Wire k = new Wire(in(2));
        Wire q = new Wire(out(0));
        Wire nq = new Wire(out(1));

        Wire nk = new Wire();
        Not.inst(nk, k);

        Wire jAnd = new Wire();
        And.inst(jAnd, j, nq);

        Wire nkAnd = new Wire();
        And.inst(nkAnd, nk, q);

        Wire d = new Wire();
        Or.inst(d, jAnd, nkAnd);

        DFlipFlop.inst(q, nq, clk, d);
    }

    public static JKFlipFlop inst(Wire q, Wire nq, Wire clk, Wire j, Wire k) {
        return new JKFlipFlop(q, nq, clk, j, k);
    }

    public static void main(String[] args) throws InterruptedException {
        Wire clk = new Wire();
        Wire j = new Wire();
        Wire k = new Wire();
        Wire q = new Wire();
        Wire nq = new Wire();

        JKFlipFlop.inst(q, nq, clk, j, k);

        j.assign(Value.V0);
        k.assign(Value.V1);
        PropagateManager.propagateParallel(j, k);
        System.out.println("q => " + q.getValue().toString() + " nq => " + nq.getValue().toString());

        Clock.tick(clk, q, nq, 1);

        System.out.println("\n# j = 0, k = 1");
        Clock.tick(clk, q, nq, 3);

        System.out.println("\n# j = 1, k = 1");
        j.assign(Value.V1);
        PropagateManager.propagateParallel(j);
        System.out.println("j = 1, q => " + q.getValue().toString() + " nq => " + nq.getValue().toString());
        Clock.tick(clk, q, nq, 3);

        System.out.println("\n# j = 1, k = 0");
        k.assign(Value.V0);
        PropagateManager.propagateParallel(k);
        System.out.println("k = 0, q => " + q.getValue().toString() + " nq => " + nq.getValue().toString());
        Clock.tick(clk, q, nq, 3);

        System.out.println("\n# j = 0, k = 0");
        j.assign(Value.V0);
        PropagateManager.propagateParallel(j);
        System.out.println("j = 0, q => " + q.getValue().toString() + " nq => " + nq.getValue().toString());
        Clock.tick(clk, q, nq, 3);
    }
}
