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
import org.jchdl.model.gsl.core.gate.ni.And;
import org.jchdl.model.gsl.core.gate.ni.Or;
import org.jchdl.model.gsl.core.gate.no.Not;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.value.Value;

import java.util.Arrays;

public class Mux4to1 extends Node {

    public Mux4to1(Wire out, Wire i0, Wire i1, Wire i2, Wire i3, Wire s1, Wire s0) {
        // collect wires
        in(Wire.array(i0, i1, i2, i3, s1, s0));
        out(out);
        // construct logic and connect to external wires
        construct();
    }

    @Override
    public void logic() {
        Wire i0 = new Wire(in(0));
        Wire i1 = new Wire(in(1));
        Wire i2 = new Wire(in(2));
        Wire i3 = new Wire(in(3));
        Wire s1 = new Wire(in(4));
        Wire s0 = new Wire(in(5));
        Wire out = new Wire();
        out.connect(out(0));

        Wire s1n = new Wire();
        Wire s0n = new Wire();
        Not.inst(s1n, s1);
        Not.inst(s0n, s0);

        Wire y0 = new Wire();
        Wire y1 = new Wire();
        Wire y2 = new Wire();
        Wire y3 = new Wire();
        And.inst(y0, i0, s1n, s0n);
        And.inst(y1, i1, s1n, s0);
        And.inst(y2, i2, s1, s0n);
        And.inst(y3, i3, s1, s0);

        Or.inst(out, y0, y1, y2, y3);
    }

    public static Mux4to1 inst(Wire out, Wire i0, Wire i1, Wire i2, Wire i3, Wire s1, Wire s0) {
        return new Mux4to1(out, i0, i1, i2, i3, s1, s0);
    }

    public static void test() {
        Wire out = new Wire();
        Wire i0 = new Wire();
        Wire i1 = new Wire();
        Wire i2 = new Wire();
        Wire i3 = new Wire();
        Wire s1 = new Wire();
        Wire s0 = new Wire();

        Mux4to1.inst(out, i0, i1, i2, i3, s1, s0);

        i0.assign(Value.V1);
        i1.assign(Value.V0);
        i2.assign(Value.V1);
        i3.assign(Value.V0);
        s1.assign(Value.V1);
        s0.assign(Value.V0);

        i0.propagate();
        i1.propagate();
        i2.propagate();
        i3.propagate();
        s1.propagate();
        s0.propagate();

        System.out.println("out: " + out.getValue().toString());
    }

    public static void main(String args[]) {
        Wire out = new Wire();
        WireVec dat = new WireVec(4);
        WireVec sel = new WireVec(2);

        Mux4to1.inst(out,
                dat.wire(0),
                dat.wire(1),
                dat.wire(2),
                dat.wire(3),
                sel.wire(1),
                sel.wire(0)
                );

        dat.assign(new Value[] {Value.V1, Value.V0, Value.V1, Value.V0 });
        sel.assign(new Value[] {Value.V1, Value.V1 });

        dat.propagate();
        sel.propagate();

        System.out.println("out: " + out.getValue().toString());
    }
}
