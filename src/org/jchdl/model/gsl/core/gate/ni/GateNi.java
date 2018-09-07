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
package org.jchdl.model.gsl.core.gate.ni;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.ni.atomic.Gate2i;
import org.jchdl.model.gsl.core.meta.Node;

public abstract class GateNi extends Node {

    public GateNi(Wire out, Wire in1, Wire... inWires) {
        // collect wires
        out(out);
        in(in1);
        in(inWires);
        // construct logic and connect to external wires
        construct();
    }

    public abstract Gate2i gate2i(Wire out, Wire in1, Wire in2);

    @Override
    public void logic() {
        int size = inputs().length;
        if (size == 1) {
            Wire out = new Wire(in(0));
            out.connect(out(0));
        } else if (size == 2) {
            Wire in1 = new Wire(in(0));
            Wire in2 = new Wire(in(1));
            Wire out = new Wire(out(0));
            gate2i(out, in1, in2);
        } else {
            Wire out = new Wire(in(0));
            for (int i = 1; i < size; i++) {
                Wire in2 = new Wire(in(i));
                Wire ret = new Wire();
                gate2i(ret, out, in2);
                out = ret;
            }
            out.connect(out(0));
        }
    }
}
