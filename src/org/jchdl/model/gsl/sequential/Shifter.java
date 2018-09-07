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
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.sequential.ff.DFlipFlop;

public class Shifter extends Node {
    private int nBits = 0;

    public Shifter(Wire dso, WireVec dpo, Wire clk, Wire dsi) {
        nBits = dpo.nBits();
        in(clk);
        in(dsi);
        out(dpo.wires());
        out(dso);
        construct();
    }

    @Override
    public void logic() {
        Wire clk = new Wire(in(0));
        Wire qPrev = new Wire(in(1));
        for (int i = 0; i < nBits; i++) {
            Wire q = new Wire();
            Wire nq = Wire.toGround();
            DFlipFlop.inst(q, nq, clk, qPrev);
            q.connect(out(i));
            qPrev = q;
        }
        qPrev.connect(out(-1));
    }

    public static Shifter inst(Wire dso, WireVec dpo, Wire clk, Wire dsi) {
        return new Shifter(dso, dpo, clk, dsi);
    }

    public static void main(String args[]) throws InterruptedException {
        Wire clk = new Wire();
        Wire dsi = new Wire();
        Wire dso = new Wire();
        WireVec dpo = new WireVec(8);

        Shifter.inst(dso, dpo, clk, dsi);

        Value[] values = new Value[]{
                Value.V1, Value.V0, Value.V1, Value.V1,
                Value.V1, Value.V0, Value.V1, Value.V0,
        };

        for (Value value : values) {
            dsi.assign(value);
            PropagateManager.propagateParallel(dsi);
            Clock.tick(clk, 1);

            System.out.println("dso: " + dso.getValue().toString());
            System.out.print("dpo: ");
            for (Wire wire : dpo.wires()) {
                System.out.print(wire.getValue().toString());
            }
            System.out.println();
        }

        System.out.println("\n# free run: ");
        for (Value value : values) {
            Clock.tick(clk, 1);

            System.out.println("dso: " + dso.getValue().toString());
            System.out.print("dpo: ");
            for (Wire wire : dpo.wires()) {
                System.out.print(wire.getValue().toString());
            }
            System.out.println();
        }
    }
}
