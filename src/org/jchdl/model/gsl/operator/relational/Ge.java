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
package org.jchdl.model.gsl.operator.relational;

import org.jchdl.model.gsl.core.datatype.helper.WireVec;
import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.no.Not;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.operator.arithmetic.Sub;

public class Ge extends Node {
    private int nBits = 0;

    private WireVec in1;
    private WireVec in2;
    private Wire out;

    public Ge(Wire out, WireVec in1, WireVec in2) {
        nBits = in1.nBits();
        in(in1.wires());
        in(in2.wires());
        out(out);
        construct();
    }

    @Override
    public void logic() {
        in1 = new WireVec(inputs(0, nBits));
        in2 = new WireVec(inputs(nBits, 2 * nBits));
        out = new Wire(out(0));

        WireVec out1 = WireVec.toGround(nBits);
        Sub.inst(out1, in1, in2);

        Not.inst(out, out1.wire(-1));
    }

    public static Ge inst(Wire out, WireVec in1, WireVec in2) {
        return new Ge(out, in1, in2);
    }

    public static void main(String args[]) {
        WireVec in1 = new WireVec(4);
        WireVec in2 = new WireVec(4);
        Wire out = new Wire();

        Ge.inst(out, in1, in2);

        in1.assign(new Value[]{
                Value.V1, Value.V1, Value.V0, Value.V1,
        });
        in2.assign(new Value[]{
                Value.V1, Value.V1, Value.V1, Value.V0,
        });

        PropagateManager.add(in1, in2);
        PropagateManager.propagateParallel();

        System.out.println("out: " + out.getValue().toString());

        Ge.inst(out, in1, in2).toVerilog();
    }
}
