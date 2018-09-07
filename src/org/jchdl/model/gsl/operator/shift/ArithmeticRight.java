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
package org.jchdl.model.gsl.operator.shift;

import org.jchdl.model.gsl.assign.Assign;
import org.jchdl.model.gsl.core.datatype.helper.WireVec;
import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.ni.And;
import org.jchdl.model.gsl.core.gate.ni.Or;
import org.jchdl.model.gsl.core.meta.Ground;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;

public class ArithmeticRight extends Shifter {

    public ArithmeticRight(WireVec out, WireVec in, int nShiftBits) {
        super(out, in, nShiftBits);
    }

    @Override
    public void shift(WireVec out, WireVec in) {
        int nLeftBits = nBits - nShiftBits;
        // pad shift: in[0, nShiftBits) discarded
        WireVec in2 = WireVec.pulledDown(nShiftBits);
        WireVec out1 = new WireVec(nShiftBits);
        And.inst(out1.wires(), in.wires(0, nShiftBits), in2.wires());

        Wire signBit = in.wire(-1);
        for (int i = 0; i < nShiftBits; i++) {
            Or.inst(out.wire(nLeftBits + i), out1.wire(i), signBit);
        }
        // bridge
        for (int i = 0; i < nLeftBits; i++) {
            Assign.inst(out.wire(i), in.wire(nShiftBits + i));
        }
    }

    public static ArithmeticRight inst(WireVec out, WireVec in, int nShiftBits) {
        return new ArithmeticRight(out, in, nShiftBits);
    }

    public static void main(String args[]) {
        WireVec out = new WireVec(8);
        WireVec in1 = new WireVec(8);
        int nShiftBits = 4;

        ArithmeticRight.inst(out, in1, nShiftBits);

        in1.assign(new Value[] {
                Value.V1, Value.V1, Value.V1, Value.V1,
                Value.Vz, Value.Vx, Value.V0, Value.V1,
        });

        PropagateManager.propagateParallel(in1);
        System.out.println("out: " + out);

        ArithmeticRight.inst(out, in1, nShiftBits).toVerilog();
    }
}