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
package org.jchdl.model.gsl.operator.arithmetic;

import org.jchdl.model.gsl.core.datatype.helper.WireVec;
import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.value.Value;

// treat operands as plain bits
public class Add extends Node {
    private int nBits = 0;

    private WireVec in1;
    private WireVec in2;
    private Wire cin;
    private WireVec sum;
    private Wire cout;

    public Add(WireVec sum, Wire cout, WireVec in1, WireVec in2, Wire cin) {
        nBits = in1.nBits();
        in(in1.wires());
        in(in2.wires());
        in(cin);
        out(sum.wires());
        out(cout);
        construct();
    }

    @Override
    public void logic() {
        in1 = new WireVec(inputs(0, nBits));
        in2 = new WireVec(inputs(nBits, 2*nBits));
        cin = new Wire(in(-1));

        sum = new WireVec(outputs(0, nBits));

        cout = cin;
        for (int i = 0; i < nBits; i++) {
            Wire coutNext = new Wire();
            FullAdder.inst(sum.wire(i), coutNext, in1.wire(i), in2.wire(i), cout);
            cout = coutNext;
        }
        cout.connect(out(-1));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName() + "_" + nBits;
    }

    public static Add inst(WireVec sum, Wire cout, WireVec in1, WireVec in2, Wire cin) {
        return new Add(sum, cout, in1, in2, cin);
    }

    public static void main(String args[]) {
        WireVec in1 = new WireVec(8);
        WireVec in2 = new WireVec(8);
        WireVec out = new WireVec(8);
        Wire cin = new Wire();
        Wire cout = new Wire();

        Add.inst(out, cout, in1, in2, cin);

        in1.assign(new Value[] { //0b0000_0010
                Value.V0, Value.V1, Value.V0, Value.V0,
                Value.V0, Value.V0, Value.V0, Value.V0,
        });
        in2.assign(new Value[] { // 0b1111_1111
                Value.V1, Value.V1, Value.V1, Value.V1,
                Value.V1, Value.V1, Value.V1, Value.V1,
        });
        cin.assign(Value.V1);

        in1.propagate();
        in2.propagate();
        cin.propagate();

        System.out.println("c_sum: " + cout + "_" + out);

        Add.inst(out, cout, in1, in2, cin).toVerilog();
    }
}

