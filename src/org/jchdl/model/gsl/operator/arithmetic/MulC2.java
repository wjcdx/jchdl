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
import org.jchdl.model.gsl.core.gate.ni.atomic.And;
import org.jchdl.model.gsl.core.gate.ni.atomic.Xor;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.operator.arithmetic.code.ComplementTwo;
import org.jchdl.model.gsl.operator.concat.Concat;
import org.jchdl.model.gsl.operator.concat.Replicate;
import org.jchdl.model.gsl.operator.conditional.Mux;
import org.jchdl.model.gsl.operator.shift.LogicalLeft;

// using Two's complement code, treat MSB as the sign bit.
//
// Mul for signed numbers, because Two's complement code is used to encode signed numbers,
// but implemented in the perspective of a machine.
public class MulC2 extends Node {
    private int nBits = 0;
    private int nResultBits = 0;

    private WireVec in1;
    private WireVec in2;
    private Wire cout;
    private WireVec out;

    public MulC2(WireVec out, Wire cout, WireVec in1, WireVec in2) {
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
        in1 = new WireVec(inputs(0, nBits));
        Wire sign1 = in1.wire(-1);

        WireVec pad1 = new WireVec(nBits);
        Replicate.inst(pad1, sign1, nBits);

        WireVec in1Padded = new WireVec(nResultBits);
        Concat.inst(in1Padded, in1, pad1);

        // 0.1 calculate two's complement of in1Padded
        WireVec in1c2 = new WireVec(nResultBits);
        ComplementTwo.inst(in1c2, in1Padded);

        // 0.2 choose multiplicand from in1Padded and in1c2, according to the sign
        WireVec in1m = new WireVec(nResultBits);
        Mux.inst(in1m.wires(), in1Padded.wires(), in1c2.wires(), sign1);

        // 1. pad in2 with its sign bit
        in2 = new WireVec(inputs(nBits, 2 * nBits));
        Wire sign2 = in2.wire(-1);

        WireVec pad2 = new WireVec(nBits);
        Replicate.inst(pad2, sign2, nBits);

        WireVec in2Padded = new WireVec(nResultBits);
        Concat.inst(in2Padded, in2, pad2);

        // 1.1 calculate two's complement of in2Padded
        WireVec in2c2 = new WireVec(nResultBits);
        ComplementTwo.inst(in2c2, in2Padded);

        // 1.2 choose multiplicand from in2Padded and in2c2, according to the sign
        WireVec in2m = new WireVec(nResultBits);
        Mux.inst(in2m.wires(), in2Padded.wires(), in2c2.wires(), sign2);

        cout = Wire.pulledDown();
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
            Add.inst(sumNext, coutNext, sum, outShifted, cout);
            cout = coutNext;
            sum = sumNext;
        }

        // 2. calc final sign
        Wire signFinal = new Wire();
        Xor.inst(signFinal, sign1, sign2);

        // 2.1 calc two's complement of sum
        WireVec sumc2 = new WireVec(nResultBits);
        ComplementTwo.inst(sumc2, sum);

        // 2.2 choose result from sum and sumc2, according to the final sign
        out = new WireVec(nResultBits);
        Mux.inst(out.wires(), sum.wires(), sumc2.wires(), signFinal);

        // 3. output the result
        out.connect(outputs(0, nResultBits));
        cout.connect(out(-1));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName() + "_" + nBits;
    }

    public static MulC2 inst(WireVec out, Wire cout, WireVec in1, WireVec in2) {
        return new MulC2(out, cout, in1, in2);
    }

    public static void main(String args[]) {
        WireVec in1 = new WireVec(4);
        WireVec in2 = new WireVec(4);
        WireVec out = new WireVec(8);
        Wire cout = new Wire();

        MulC2.inst(out, cout, in1, in2);

        in1.assign(new Value[]{
                Value.V0, Value.V1, Value.V1, Value.V1,
        });
        in2.assign(new Value[]{
                Value.V1, Value.V0, Value.V1, Value.V1,
        });

        PropagateManager.add(in1, in2);
        PropagateManager.propagateParallel();

        System.out.println("out: " + cout.toString() + "_" + out.toString());

        MulC2.inst(out, cout, in1, in2).toVerilog();
    }
}
