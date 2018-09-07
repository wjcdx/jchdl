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
import org.jchdl.model.gsl.core.gate.ni.Xor;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.operator.concat.Concat;
import org.jchdl.model.gsl.operator.concat.Replicate;
import org.jchdl.model.gsl.operator.conditional.Mux;
import org.jchdl.model.gsl.operator.shift.LogicalLeft;
import org.jchdl.model.gsl.operator.unary.Negative;

// using two's complement code, treat MSB as the sign bit.
//
// Firstly, we change two multiplicand into positive numbers, and add the sign back at last.
// Same as MulC2, because signed numbers are encoded in Two's complement code,
// but implemented in the perspective of a human.
public class MulSigned extends Node {
    private int nBits = 0;
    private int nResultBits = 0;

    public MulSigned(WireVec out, Wire cout, WireVec in1, WireVec in2) {
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
        // 0. pad in1 with its sign bit
        WireVec in1 = new WireVec(inputs(0, nBits));
        Wire sign1 = new Wire(in(nBits - 1));

        WireVec pad1 = new WireVec(nBits);
        Replicate.inst(pad1, sign1, nBits);

        WireVec in1Padded = new WireVec(nResultBits);
        Concat.inst(in1Padded, in1, pad1);

        // 0.1 calculate -in1Padded
        WireVec in1Neg = new WireVec(nResultBits);
        Negative.inst(in1Neg, in1Padded);

        // 0.2 if in1 is negative, we use -in1 as the multiplicand.
        // choose multiplicand from in1Padded and -in1Padded, according to the sign.
        WireVec in1m = new WireVec(nResultBits);
        Mux.inst(in1m.wires(), in1Padded.wires(), in1Neg.wires(), sign1);

        // 1. pad in2 with its sign bit
        WireVec in2 = new WireVec(inputs(nBits, 2 * nBits));
        Wire sign2 = new Wire(in(2 * nBits - 1));

        WireVec pad2 = new WireVec(nBits);
        Replicate.inst(pad2, sign2, nBits);

        WireVec in2Padded = new WireVec(nResultBits);
        Concat.inst(in2Padded, in2, pad2);

        // 1.1 calculate -in2Padded
        WireVec in2Neg = new WireVec(nResultBits);
        Negative.inst(in2Neg, in2Padded);

        // 1.2 if in2 is negative, we use -in2 as the multiplicand.
        // choose multiplicand from in2Padded and -in2Padded, according to the sign.
        WireVec in2m = new WireVec(nResultBits);
        Mux.inst(in2m.wires(), in2Padded.wires(), in2Neg.wires(), sign2);

        Wire cin = Wire.pulledDown();
        WireVec sum = WireVec.pulledDown(nResultBits);
        for (int i = 0; i < nBits; i++) {
            WireVec outAnd = new WireVec(nResultBits);
            for (int j = 0; j < nResultBits; j++) {
                And.inst(outAnd.wire(j), in1m.wire(j), in2m.wire(i));
            }

            WireVec outShifted = new WireVec(nResultBits);
            LogicalLeft.inst(outShifted, outAnd, i);

            WireVec sumNext = new WireVec(nResultBits);
            Wire coutNext = new Wire();
            Add.inst(sumNext, coutNext, sum, outShifted, cin);
            cin = coutNext;
            sum = sumNext;
        }

        // 2. calculate final sign
        Wire signFinal = new Wire();
        Xor.inst(signFinal, sign1, sign2);

        // 2.1 calculate negative of sum
        WireVec sumNeg = new WireVec(nResultBits);
        Negative.inst(sumNeg, sum);

        // 2.2 check if we should add a negative sign back.
        // choose result from sum and sumNeg, according to the final sign
        WireVec sumFinal = new WireVec(nResultBits);
        Mux.inst(sumFinal.wires(), sum.wires(), sumNeg.wires(), signFinal);

        // 3. output the result
        sumFinal.connect(outputs(0, nResultBits));
        cin.connect(out(-1));
    }

    public static MulSigned inst(WireVec out, Wire cout, WireVec in1, WireVec in2) {
        return new MulSigned(out, cout, in1, in2);
    }

    public static void main(String args[]) {
        WireVec in1 = new WireVec(4);
        WireVec in2 = new WireVec(4);
        WireVec out = new WireVec(8);
        Wire cout = new Wire();

        MulSigned.inst(out, cout, in1, in2);

        in1.assign(new Value[]{
                Value.V1, Value.V1, Value.V1, Value.V1,
        });
        in2.assign(new Value[]{
                Value.V1, Value.V0, Value.V1, Value.V1,
        });

        PropagateManager.add(in1, in2);
        PropagateManager.propagateParallel();

        System.out.print("out: ");
        System.out.print(cout.getValue().toString() + "_");
        for (int i = out.nBits() - 1; i >= 0; i--) {
            System.out.print(out.wire(i).getValue().toString());
        }
        System.out.println();
    }
}
