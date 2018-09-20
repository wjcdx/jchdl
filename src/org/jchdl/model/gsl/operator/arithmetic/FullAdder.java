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

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.ni.atomic.Or;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.value.Value;

public class FullAdder extends Node {
    private Wire a;
    private Wire b;
    private Wire ci;
    private Wire s;
    private Wire co;

    public FullAdder(Wire s, Wire co, Wire a, Wire b, Wire ci) {
        in(Wire.arrayOf(a, b, ci));
        out(Wire.arrayOf(s, co));
        construct();
    }

    // connections within this Node
    @Override
    public void logic() {
        a = new Wire(in(0));
        b = new Wire(in(1));
        ci = new Wire(in(2));

        s = new Wire(out(0));
        co = new Wire(out(1));

        Wire s1 = new Wire();
        Wire c1 = new Wire();
        HalfAdder.inst(s1, c1, a, b);

        Wire c2 = new Wire();
        HalfAdder.inst(s, c2, s1, ci);

        Or.inst(co, c1, c2);
    }

    public static FullAdder inst(Wire sum, Wire cout, Wire a, Wire b, Wire cin) {
        return new FullAdder(sum, cout, a, b, cin);
    }

    public static void main(String args[]) {
        Wire a = new Wire();
        Wire b = new Wire();
        Wire cin = new Wire();
        Wire sum = new Wire();
        Wire cout = new Wire();

        FullAdder.inst(sum, cout, a, b, cin);

        a.assign(Value.V0);
        b.assign(Value.V0);
        cin.assign(Value.V1);

        a.propagate();
        b.propagate();
        cin.propagate();

        System.out.println("Out: " + cout.getValue().toString() + sum.getValue().toString());
    }

}
