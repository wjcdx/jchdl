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
import org.jchdl.model.gsl.core.gate.ni.atomic.Nor;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;

public class SRLatch extends Node {
    private Wire set;
    private Wire reset;
    private Wire q;
    private Wire nq;

    public SRLatch(Wire q, Wire nq, Wire set, Wire reset) {
        in(set);
        in(reset);
        out(q);
        out(nq);
        construct();
    }

    @Override
    public void logic() {
        set = new Wire(in(0));
        reset = new Wire(in(1));
        q = new Wire(out(0));
        nq = new Wire(out(1));

        Nor.inst(q, reset, nq);
        Nor.inst(nq, set, q);
    }

    public static SRLatch inst(Wire q, Wire nq, Wire set, Wire reset) {
        return new SRLatch(q, nq, set, reset);
    }

    public static void main(String[] args) {
        Wire set = new Wire();
        Wire reset = new Wire();
        Wire q = new Wire();
        Wire nq = new Wire();

        SRLatch latch = SRLatch.inst(q, nq, set, reset);

        set.assign(Value.V1);
        reset.assign(Value.V0);

        PropagateManager.propagateParallel(set, reset);

        System.out.println(" q: " + q);
        System.out.println("nq: " + nq);

        latch.toVerilog();
    }
}
