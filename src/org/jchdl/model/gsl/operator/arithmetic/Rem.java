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
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;

public class Rem extends Node {
    private int nBits = 0;

    // out = in1 % in2
    public Rem(WireVec out, WireVec in1, WireVec in2) {
        nBits = in1.nBits();
        in(in1.wires());
        in(in2.wires());
        out(out.wires());
        construct();
    }

    @Override
    public void logic() {
        WireVec in1 = new WireVec(inputs(0, nBits));
        WireVec in2 = new WireVec(inputs(nBits, 2 * nBits));

        WireVec div = WireVec.toGround(nBits);
        WireVec rem = new WireVec(nBits);
        rem.connect(outputs());

        Div.inst(div, rem, in1, in2);
    }

    public static Rem inst(WireVec out, WireVec in1, WireVec in2) {
        return new Rem(out, in1, in2);
    }

    public static void main(String args[]) {
        WireVec in1 = new WireVec(2);
        WireVec in2 = new WireVec(2);
        WireVec rem = new WireVec(2);

        Rem.inst(rem, in1, in2);

        in1.assign(new Value[]{
                Value.V1, Value.V0,
        });
        in2.assign(new Value[]{
                Value.V0, Value.V1,
        });

        PropagateManager.add(in1, in2);
        PropagateManager.propagateParallel();

        System.out.print("rem: ");
        for (int i = rem.nBits() - 1; i >= 0; i--) {
            System.out.print(rem.wire(i).getValue().toString());
        }
        System.out.println();
    }
}

