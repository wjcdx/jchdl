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
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.operator.concat.Concat;
import org.jchdl.model.gsl.operator.conditional.Conditional;
import org.jchdl.model.gsl.operator.relational.Ge;
import org.jchdl.model.gsl.operator.shift.LogicalLeft;

// treat operands as Two's complement codes, which means operands are signed.
// because it uses Sub which can only handle Two's complement coded operands,
// it can not handle plain bits yet.
// TODO: BUG: if every bit of in2 equals 0 or 1, the result is wrong.
public class Div extends Node {
    private int nBits = 0;

    // in1 = out * in2 + remainder
    public Div(WireVec out, WireVec remainder, WireVec in1, WireVec in2) {
        nBits = in1.nBits();
        in(in1.wires());
        in(in2.wires());
        out(out.wires());
        out(remainder.wires());
        construct();
    }

    @Override
    public void logic() {
        int nDoubleBits = 2 * nBits;
        // 0. pad higher bits.
        WireVec in1 = new WireVec(inputs(0, nBits));
        WireVec pad1 = WireVec.pulledDown(nBits);
        WireVec in1d = new WireVec(nDoubleBits);
        Concat.inst(in1d, in1, pad1);

        // 1. pad lower bits.
        WireVec in2 = new WireVec(inputs(nBits, 2 * nBits));
        WireVec pad2 = WireVec.pulledDown(nBits);
        WireVec in2d = new WireVec(nDoubleBits);
        Concat.inst(in2d, pad2, in2);

        for (int i = 0; i < nBits; i++) {
            // shift in1d left
            WireVec in1Shifted = new WireVec(nDoubleBits);
            LogicalLeft.inst(in1Shifted, in1d, 1);

            // check if in1d >= in2d
            Wire ge = new Wire();
            Ge.inst(ge, in1Shifted, in2d);

            // calculate difference
            WireVec diff = new WireVec(nDoubleBits);
            Sub.inst(diff, in1Shifted, in2d);

            // add 1 to record the multiple of in2d
            WireVec zeros = WireVec.pulledDown(nDoubleBits);
            Wire cin = Wire.pulledUp();
            Wire cout = Wire.toGround();
            WireVec sum = new WireVec(nDoubleBits);
            Add.inst(sum, cout, diff, zeros, cin);

            // decide which one to be used as next loop
            WireVec in1New = new WireVec(nDoubleBits);
            Conditional.inst(in1New, sum, in1Shifted, ge);

            in1d = in1New;
        }
        // output out/remainder.
        in1d.connect(outputs());
    }

    public static Div inst(WireVec out, WireVec remainder, WireVec in1, WireVec in2) {
        return new Div(out, remainder, in1, in2);
    }

    public static void main(String args[]) {
        WireVec in1 = new WireVec(8);
        WireVec in2 = new WireVec(8);
        WireVec rem = new WireVec(8);
        WireVec out = new WireVec(8);

        Div.inst(out, rem, in1, in2);

        in1.assign(new Value[]{
                Value.V1, Value.V1, Value.V1, Value.V1,
                Value.V1, Value.V0, Value.V0, Value.V0,
        });
        in2.assign(new Value[]{
                Value.V1, Value.V1, Value.V0, Value.V0,
                Value.V0, Value.V0, Value.V0, Value.V0,
        });

        PropagateManager.add(in1, in2);
        PropagateManager.propagateParallel();

        System.out.print("out: ");
        for (int i = out.nBits() - 1; i >= 0; i--) {
            System.out.print(out.wire(i).getValue().toString());
        }
        System.out.println();

        System.out.print("rem: ");
        for (int i = rem.nBits() - 1; i >= 0; i--) {
            System.out.print(rem.wire(i).getValue().toString());
        }
        System.out.println();
    }
}
