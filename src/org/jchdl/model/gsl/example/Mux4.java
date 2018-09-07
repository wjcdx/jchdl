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
package org.jchdl.model.gsl.example;

import org.jchdl.model.gsl.core.datatype.helper.WireVec;
import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.operator.conditional.Mux;

public class Mux4 extends Node {
    private Wire i0;
    private Wire i1;
    private Wire i2;
    private Wire i3;
    private Wire s1;
    private Wire s0;
    private Wire out;

    public Mux4(Wire out, Wire i0, Wire i1, Wire i2, Wire i3, Wire s1, Wire s0) {
        // collect wires
        in(Wire.array(i0, i1, i2, i3, s1, s0));
        out(out);
        // construct logic and connect to external wires
        construct();
    }

    @Override
    public void logic() {
        i0 = new Wire(in(0));
        i1 = new Wire(in(1));
        i2 = new Wire(in(2));
        i3 = new Wire(in(3));
        s1 = new Wire(in(4));
        s0 = new Wire(in(5));
        out = new Wire(out(0));

        Wire out1 = new Wire();
        Mux.inst(out1, i0, i1, s0);

        Wire out2 = new Wire();
        Mux.inst(out2, i2, i3, s0);

        Mux.inst(out, out1, out2, s1);
    }

    public static Mux4 inst(Wire out, Wire i0, Wire i1, Wire i2, Wire i3, Wire s1, Wire s0) {
        return new Mux4(out, i0, i1, i2, i3, s1, s0);
    }

    public static void main(String args[]) {
        Wire out = new Wire();
        WireVec dat = new WireVec(4);
        WireVec sel = new WireVec(2);

        Mux4 mux4 = Mux4.inst(out,
                dat.wire(0),
                dat.wire(1),
                dat.wire(2),
                dat.wire(3),
                sel.wire(1),
                sel.wire(0)
        );

        dat.assign(new Value[] {Value.V1, Value.V0, Value.V1, Value.V0 });
        //0bs1_s0: 0/1/2/3 => dat 0 1 2 3
        sel.assign(new Value[] {Value.V1, Value.V1 });

        dat.propagate();
        sel.propagate();

        System.out.println("out: " + out.getValue().toString());

        mux4.toVerilog();
    }
}
