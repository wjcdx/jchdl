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
import org.jchdl.model.gsl.core.gate.no.Not;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.sequential.latch.DLatch;

public class DFlipFlop extends Node {
    private Wire q;
    private Wire nq;
    private Wire clk;
    private Wire d;

    public DFlipFlop(Wire q, Wire nq, Wire clk, Wire d) {
        in(clk);
        in(d);
        out(q);
        out(nq);
        construct();
    }

    @Override
    public void logic() {
        clk = new Wire(in(0));
        d = new Wire(in(1));
        q = new Wire(out(0));
        nq = new Wire(out(1));

        Wire nclk = new Wire();
        Not.inst(nclk, clk);

        Wire qm = new Wire();
        Wire nqm = Wire.toGround();
        DLatch.inst(qm, nqm, nclk, d);
        DLatch.inst(q, nq, clk, qm);
    }

    public static DFlipFlop inst(Wire q, Wire nq, Wire clk, Wire d) {
        return new DFlipFlop(q, nq, clk, d);
    }

    public static void main(String[] args) throws InterruptedException {
        Wire clk = new Wire();
        Wire d = new Wire();
        Wire q = new Wire();
        Wire nq = new Wire();

        DFlipFlop.inst(q, nq, clk, d);

        long millis = 1000;
        while (true) {
            Thread.sleep(millis);

            d.assign(Value.V0);
            PropagateManager.propagateParallel(d);
            System.out.println("d => 0, q => " + q.getValue().toString());

            clk.assign(Value.V0);
            PropagateManager.propagateParallel(clk);
            System.out.println("c => 0, q => " + q.getValue().toString());

            Thread.sleep(millis);

            clk.assign(Value.V1);
            PropagateManager.propagateParallel(clk);
            System.out.println("c => 1, q => " + q.getValue().toString());

            Thread.sleep(millis);

            d.assign(Value.V1);
            PropagateManager.propagateParallel(d);
            System.out.println("d => 1, q => " + q.getValue().toString());

            clk.assign(Value.V0);
            PropagateManager.propagateParallel(clk);
            System.out.println("c => 0, q => " + q.getValue().toString());

            Thread.sleep(millis);

            clk.assign(Value.V1);
            PropagateManager.propagateParallel(clk);
            System.out.println("c => 1, q => " + q.getValue().toString());
        }
    }
}
