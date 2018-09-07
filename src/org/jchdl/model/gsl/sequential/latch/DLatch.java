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
package org.jchdl.model.gsl.sequential.latch;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.ni.And;
import org.jchdl.model.gsl.core.gate.ni.Nor;
import org.jchdl.model.gsl.core.gate.no.Not;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;

public class DLatch extends Node {
    private Wire e;
    private Wire d;
    private Wire q;
    private Wire nq;

    public DLatch(Wire q, Wire nq, Wire e, Wire d) {
        in(e);
        in(d);
        out(q);
        out(nq);
        construct();
    }

    @Override
    public void logic() {
        e = new Wire(in(0));
        d = new Wire(in(1));
        q = new Wire(out(0));
        nq = new Wire(out(1));

        Wire nd = new Wire();
        Not.inst(nd, d);

        Wire r = new Wire();
        And.inst(r, nd, e);

        Wire s = new Wire();
        And.inst(s, e, d);

        Nor.inst(q, r, nq);
        Nor.inst(nq, s, q);
    }

    public static DLatch inst(Wire q, Wire nq, Wire e, Wire d) {
        return new DLatch(q, nq, e, d);
    }

    public static void main(String[] args) {
        Wire e = new Wire();
        Wire d = new Wire();
        Wire q = new Wire();
        Wire nq = new Wire();

        Node node = DLatch.inst(q, nq, e, d);

        // trial 1:
        e.assign(Value.V1);
        d.assign(Value.V0);
        PropagateManager.propagateParallel(e, d);
        System.out.println(" q: " + q.getValue().toString());
        System.out.println("nq: " + nq.getValue().toString());

        // trial 2:
        e.assign(Value.V0);
        d.assign(Value.V1);
        PropagateManager.propagateParallel(e, d);
        System.out.println(" q: " + q.getValue().toString());
        System.out.println("nq: " + nq.getValue().toString());

        // trial 3:
        e.assign(Value.V1);
        d.assign(Value.V1);
        PropagateManager.propagateParallel(e, d);
        System.out.println(" q: " + q.getValue().toString());
        System.out.println("nq: " + nq.getValue().toString());

        node.toVerilog();
    }
}
