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
import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.value.Value;

// nBits(out) = nBits(in) * count;
public class Replicate extends Node {
    private int nBits = 0;
    private int nCount = 0;

    public Replicate(WireVec out, WireVec in, int count) {
        nBits = in.nBits();
        nCount = count;
        in(in.wires());
        out(out.wires());
        construct();
    }

    public Replicate(WireVec out, Wire in, int count) {
        nBits = 1;
        nCount = count;
        in(in);
        out(out.wires());
        construct();
    }

    @Override
    public void logic() {
        WireVec in = new WireVec(inputs());
        WireVec out = new WireVec(outputs());
        for (int i = 0; i < nCount; i++) {
            for (int j = 0; j < nBits; j++) {
                Assign.inst(out.wire(i * nBits + j), in.wire(j));
            }
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName() + "_" + nBits + "x" + nCount;
    }

    public static Replicate inst(WireVec out, WireVec in, int count) {
        return new Replicate(out, in, count);
    }

    public static Replicate inst(WireVec out, Wire in, int count) {
        return new Replicate(out, in, count);
    }

    public static void main(String args[]) {
        WireVec in1 = new WireVec(8);
        WireVec out = new WireVec(16);
        int count = 2;

        Replicate.inst(out, in1, count);

        in1.assign(new Value[]{
                Value.V0, Value.Vz, Value.V1, Value.V1,
                Value.Vz, Value.Vx, Value.V0, Value.V1,
        });

        in1.propagate();

        System.out.print("out: ");
        for (Wire wire : out.wires()) {
            System.out.print(wire.getValue().toString());
        }
        System.out.println();

        Replicate.inst(out, in1, count).toVerilog();
    }
}
