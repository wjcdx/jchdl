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
import org.jchdl.model.gsl.core.gate.ni.And;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.operator.concat.Concat;
import org.jchdl.model.gsl.operator.shift.LogicalLeft;


// treat operands as plain bits
// width(out) = width(in1) x 2
public class Mul extends Node {
    private int nBits = 0;
    private int nResultBits = 0;

    private Wire cout;
    private WireVec out;
    private WireVec in1;
    private WireVec in2;

    public Mul(WireVec out, Wire cout, WireVec in1, WireVec in2) {
        nBits = in1.nBits();
        nResultBits = out.nBits();
        in(in1.wires());
        in(in2.wires());
        out(out.wires());
        out(cout);
        construct();
    }

    @Override
    public void logic() {
        in1 = new WireVec(inputs(0, nBits));
        WireVec pad1 = WireVec.pulledDown(nBits);
        WireVec in1Padded = new WireVec(nResultBits);
        Concat.inst(in1Padded, in1, pad1);

        in2 = new WireVec(inputs(nBits, 2 * nBits));
        WireVec pad2 = WireVec.pulledDown(nBits);
        WireVec in2Padded = new WireVec(nResultBits);
        Concat.inst(in2Padded, in2, pad2);

        Wire cin = Wire.pulledDown();
        WireVec sum = WireVec.pulledDown(nResultBits);
        for (int i = 0; i < nBits; i++) {
            WireVec outAnd = new WireVec(nResultBits);
            for (int j = 0; j < nResultBits; j++) {
                And.inst(outAnd.wire(j), in1Padded.wire(j), in2Padded.wire(i));
            }

            WireVec outShifted = new WireVec(nResultBits);
            LogicalLeft.inst(outShifted, outAnd, i);

            WireVec sumNext = new WireVec(nResultBits);
            Wire coutNext = new Wire();
            Add.inst(sumNext, coutNext, sum, outShifted, cin);
            cin = coutNext;
            sum = sumNext;
        }
        cin.connect(out(-1));
        sum.connect(outputs(0, nResultBits));

        cout = cin;
        out = sum;
    }

    public static Mul inst(WireVec out, Wire cout, WireVec in1, WireVec in2) {
        return new Mul(out, cout, in1, in2);
    }

    public static void main(String args[]) {
        WireVec in1 = new WireVec(4);
        WireVec in2 = new WireVec(4);
        WireVec out = new WireVec(8);
        Wire cout = new Wire();

        Mul.inst(out, cout, in1, in2);

        in1.assign(new Value[]{
                Value.V1, Value.V1, Value.V1, Value.V1,
        });
        in2.assign(new Value[]{
                Value.V1, Value.V1, Value.V1, Value.V1,
        });

        PropagateManager.add(in1, in2);
        PropagateManager.propagateParallel();

        System.out.print("out: ");
        System.out.print(cout.getValue().toString() + "_");
        for (int i = out.nBits() - 1; i >= 0; i--) {
            System.out.print(out.wire(i).getValue().toString());
        }
        System.out.println();

        Mul.inst(out, cout, in1, in2).toVerilog();
    }
}
