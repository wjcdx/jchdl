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
package org.jchdl.model.gsl.operator.concat;

import org.jchdl.model.gsl.assign.Assign;
import org.jchdl.model.gsl.core.datatype.helper.WireVec;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.value.Value;

public class Concat extends Node {
    private WireVec out;
    private WireVec in;

    public Concat(WireVec out, WireVec in1, WireVec... ins) {
        in(in1.wires());
        for (WireVec vec : ins) {
            in(vec.wires());
        }
        out(out.wires());
        construct();
    }

    @Override
    public void logic() {
        out = new WireVec(outputs());
        in  = new WireVec(inputs());
        for (int i = 0; i < nIn(); i++) {
            Assign.inst(out.wire(i), in.wire(i));
        }
    }

    public static Concat inst(WireVec out, WireVec in1, WireVec... ins) {
        return new Concat(out, in1, ins);
    }

    public static void main(String args[]) {
        WireVec in1 = new WireVec(8);
        WireVec in2 = new WireVec(8);
        WireVec in3 = new WireVec(2);
        WireVec out = new WireVec(18);

        Concat.inst(out, in1, in2, in3);

        in1.assign(new Value[]{
                Value.V1, Value.V1, Value.V1, Value.V1,
                Value.Vz, Value.Vx, Value.V0, Value.V1,
        });
        in2.assign(new Value[]{
                Value.V0, Value.Vx, Value.V0, Value.V0,
                Value.V1, Value.V0, Value.V1, Value.V0,
        });
        in1.propagate();
        in2.propagate();
        in3.propagate();

        System.out.print("out: " + out);
        System.out.println();

        Concat.inst(out, in1, in2, in3).toVerilog();
    }
}
