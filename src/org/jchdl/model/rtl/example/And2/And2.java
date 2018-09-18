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
package org.jchdl.model.rtl.example.And2;

import org.jchdl.model.rtl.core.datatype.Bit;
import org.jchdl.model.rtl.core.io.annotation.Input;
import org.jchdl.model.rtl.core.io.annotation.Output;
import org.jchdl.model.rtl.core.meta.Module;
import org.jchdl.model.rtl.core.meta.PropagateManager;
import org.jchdl.model.rtl.example.And;

public class And2 extends Module {
    @Input
    private Abc s;
    @Output
    private Bit r;

    public And2(Module parent, Bit r, Abc s) {
        super(parent);
        this.s = s;
        this.r = r;
        construct();
    }

    @Override
    public void logic() {
        Bit ab = new Bit();
        And.inst(this, ab, s.a, s.b);
        And.inst(this, r, ab, s.c);
    }

    public static void main(String[] args) {
        Abc abc = new Abc();
        Bit r = new Bit();
        And2 aa = new And2(null, r, abc);

        abc.a.assign(Bit.BIT_1);
        PropagateManager.propagate(aa);
        System.out.println("out: " + r.toString());

        abc.b.assign(Bit.BIT_1);
        PropagateManager.propagate(aa);
        System.out.println("out: " + r.toString());

        abc.c.assign(Bit.BIT_1);
        PropagateManager.propagate(aa);
        System.out.println("out: " + r.toString());

        abc.a.assign(Bit.BIT_0);
        PropagateManager.propagate(aa);
        System.out.println("out: " + r.toString());

        abc.a.assign(Bit.BIT_1);
        PropagateManager.propagate(aa);
        System.out.println("out: " + r.toString());

        aa.toVerilog();
    }
}
