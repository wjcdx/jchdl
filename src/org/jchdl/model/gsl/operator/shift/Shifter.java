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

import org.jchdl.model.gsl.core.datatype.helper.WireVec;
import org.jchdl.model.gsl.core.meta.Node;

public abstract class Shifter extends Node {
    protected int nBits;
    protected int nShiftBits;

    private WireVec out;
    private WireVec in;

    public Shifter(WireVec out, WireVec in, int nShiftBits) {
        this.nBits = in.nBits();
        this.nShiftBits = nShiftBits > nBits ? nBits : nShiftBits;
        in(in.wires());
        out(out.wires());
        construct();
    }

    public abstract void shift(WireVec out, WireVec in);

    @Override
    public void logic() {
        in = new WireVec(inputs());
        out = new WireVec(outputs());
        shift(out, in);

//        if (nShiftBits == 0) {
//            bridge(0, nBits - nShiftBits);
//        } else if (nBits <= nShiftBits) {
//            // all bits output 0 with a and 0 gate.
//            pad(nBits, 0);
//        } else {
//            pad(nShiftBits, nBits - nShiftBits);
//            bridge(nShiftBits, nBits - nShiftBits);
//        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName() + "_" + nShiftBits;
    }
}
