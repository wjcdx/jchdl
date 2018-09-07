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
package org.jchdl.model.gsl.operator.conditional;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.ni.atomic.And;
import org.jchdl.model.gsl.core.gate.ni.atomic.Or;
import org.jchdl.model.gsl.core.gate.no.Not;
import org.jchdl.model.gsl.core.meta.Node;

// out = (sel == 0) ? in1 : in2
public class Mux extends Node {
    private Wire in1;
    private Wire in2;
    private Wire sel;
    private Wire out;
    private Wire out1;
    private Wire out2;
    private Wire selNot;

    public Mux(Wire out, Wire in1, Wire in2, Wire sel) {
        in(in1);
        in(in2);
        in(sel);
        out(out);
        construct();
    }

    @Override
    public void logic() {
        in1 = new Wire(in(0));
        in2 = new Wire(in(1));
        sel = new Wire(in(2));
        out = new Wire(out(0));

        selNot = new Wire();
        Not.inst(selNot, sel);

        out1 = new Wire();
        And.inst(out1, in1, selNot);

        out2 = new Wire();
        And.inst(out2, in2, sel);

        Or.inst(out, out1, out2);
    }

    public static Mux inst(Wire out, Wire in1, Wire in2, Wire sel) {
        return new Mux(out, in1, in2, sel);
    }

    public static Mux[] inst(Wire[] out, Wire[] in1, Wire[] in2, Wire[] sel) {
        Mux[] muxes = new Mux[out.length];
        for (int i = 0; i < out.length; i++) {
            muxes[i] = new Mux(out[i], in1[i], in2[i], sel[i]);
        }
        return muxes;
    }

    public static Mux[] inst(Wire[] out, Wire[] in1, Wire[] in2, Wire sel) {
        Mux[] muxes = new Mux[out.length];
        for (int i = 0; i < out.length; i++) {
            muxes[i] = new Mux(out[i], in1[i], in2[i], sel);
        }
        return muxes;
    }

    public static void main(String[] args) {
        Wire in1 = new Wire();
        Wire in2 = new Wire();
        Wire sel = new Wire();
        Wire out = new Wire();
        Mux mux = Mux.inst(out, in1, in2, sel);
        mux.toVerilog();
    }
}
