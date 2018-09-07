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
import org.jchdl.model.gsl.core.gate.ni.And;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;

public class LogicalLeft extends Shifter {
    public LogicalLeft(WireVec out, WireVec in, int nShiftBits) {
        super(out, in, nShiftBits);
    }

    @Override
    public void shift(WireVec out, WireVec in) {
        int nLeftBits = nBits - nShiftBits;
        // pad shift
        WireVec pad = WireVec.pulledDown(nShiftBits);
        And.inst(out.wires(0, nShiftBits), in.wires(nLeftBits), pad.wires());
        // bridge
        for (int i = 0; i < nLeftBits; i++) {
            Assign.inst(out.wire(nShiftBits + i), in.wire(i));
        }
    }

    public static LogicalLeft inst(WireVec out, WireVec in, int nShiftBits) {
        return new LogicalLeft(out, in, nShiftBits);
    }

    public static void main(String args[]) {
        WireVec out = new WireVec(8);
        WireVec in1 = new WireVec(8);
        int nShiftBits = 2;

        LogicalLeft.inst(out, in1, nShiftBits);

        in1.assign(new Value[] {
                Value.Vx, Value.V1, Value.V1, Value.V1,
                Value.Vz, Value.Vx, Value.V0, Value.V1,
        });

        PropagateManager.add(in1);
        PropagateManager.propagateParallel();

        System.out.println("out: " + out);

        LogicalLeft.inst(out, in1, nShiftBits).toVerilog();
    }
}
