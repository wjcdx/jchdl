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
package org.jchdl.model.gsl.sequential;

import org.jchdl.model.gsl.core.datatype.helper.WireVec;
import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.ni.And;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.sequential.ff.DFlipFlop;

public class Register extends Node {
    private int nBits = 0;

    private WireVec out;
    private WireVec in;
    private Wire clk;
    private Wire oe;

    public Register(WireVec out, Wire clk, Wire oe, WireVec in) {
        nBits = in.nBits();
        in(clk);
        in(oe);
        in(in.wires());
        out(out.wires());
        construct();
    }

    @Override
    public void logic() {
        clk = new Wire(in(0));
        oe = new Wire(in(1));
        in = new WireVec(inputs(2));
        out = new WireVec(outputs());

        for (int i = 0; i < nBits; i++) {
            Wire q = new Wire();
            Wire nq = Wire.toGround();
            DFlipFlop.inst(q, nq, clk, in.wire(i));

            And.inst(out.wire(i), q, oe);
        }
    }

    public static Register inst(WireVec out, Wire clk, Wire oe, WireVec in) {
        return new Register(out, clk, oe, in);
    }

    public static void main(String args[]) throws InterruptedException {
        Wire clk = new Wire();
        Wire oe = new Wire();
        WireVec in1 = new WireVec(8);
        WireVec out = new WireVec(8);

        Register.inst(out, clk, oe, in1);

        oe.assign(Value.V1);
        in1.assign(new Value[] {
                Value.V1, Value.V1, Value.V1, Value.V1,
                Value.V1, Value.V0, Value.V1, Value.V1,
        });
        PropagateManager.propagateParallel(oe, in1);
        Clock.tick(clk, 1);
        System.out.print("out: ");
        for (Wire wire : out.wires()) {
            System.out.print(wire.getValue().toString());
        }
        System.out.println();

        Clock.tick(clk, 1);
        System.out.print("out: ");
        for (Wire wire : out.wires()) {
            System.out.print(wire.getValue().toString());
        }
        System.out.println();

        in1.assign(new Value[] {
                Value.V0, Value.V0, Value.V1, Value.V1,
                Value.V1, Value.V0, Value.V1, Value.V1,
        });
        PropagateManager.propagateParallel(in1);

        Clock.tick(clk, 1);
        System.out.print("out: ");
        for (Wire wire : out.wires()) {
            System.out.print(wire.getValue().toString());
        }
        System.out.println();

        oe.assign(Value.V0);
        PropagateManager.propagateParallel(oe);

        System.out.print("out: ");
        for (Wire wire : out.wires()) {
            System.out.print(wire.getValue().toString());
        }
        System.out.println();

        Register.inst(out, clk, oe, in1).toVerilog();
    }
}
