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
package org.jchdl.model.gsl.operator.conditional;

import org.jchdl.model.gsl.core.datatype.helper.WireVec;
import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.no.Not;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.value.Value;

// out = (condition == 1) ? in1 : in2
public class Conditional extends Node {
    private int nBits = 0;

    private WireVec in1;
    private WireVec in2;
    private Wire condition;
    private WireVec out;

    // local named wire
    private Wire sel;

    public Conditional(WireVec out, WireVec in1, WireVec in2, Wire condition) {
        nBits = in1.nBits();
        in(in1.wires());
        in(in2.wires());
        in(condition);
        out(out.wires());
        construct();
    }

    @Override
    public void logic() {
        in1 = new WireVec(inputs(0, nBits));
        in2 = new WireVec(inputs(nBits, 2*nBits));
        condition = new Wire(in(-1));
        out = new WireVec(outputs());

        sel = new Wire();
        Not.inst(sel, condition);

        Mux.inst(out.wires(), in1.wires(), in2.wires(), sel);
    }

    public static Conditional inst(WireVec out, WireVec in1, WireVec in2, Wire condition) {
        return new Conditional(out, in1, in2, condition);
    }

    public static void main(String args[]) {
        WireVec in1 = new WireVec(8);
        WireVec in2 = new WireVec(8);
        WireVec out = new WireVec(8);
        Wire condition = new Wire();

        Conditional.inst(out, in1, in2, condition);

        in1.assign(new Value[] {
                Value.V1, Value.V1, Value.V1, Value.V1,
                Value.Vz, Value.Vx, Value.V0, Value.V1,
        });
        in2.assign(new Value[] {
                Value.V0, Value.Vz, Value.V0, Value.V0,
                Value.V1, Value.V1, Value.V1, Value.V1,
        });
        condition.assign(Value.V0);

        in1.propagate();
        in2.propagate();
        condition.propagate();

        System.out.print("out: ");
        for (Wire wire : out.wires()) {
            System.out.print(wire.getValue().toString());
        }
        System.out.println();

        Conditional.inst(out, in1, in2, condition).toVerilog();
    }
}
