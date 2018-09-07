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
package org.jchdl.model.rtl.example;

import org.jchdl.model.rtl.core.datatype.Bits;
import org.jchdl.model.rtl.core.datatype.Reg;
import org.jchdl.model.rtl.core.datatype.Bit;
import org.jchdl.model.rtl.core.event.ChangingEvent;
import org.jchdl.model.rtl.core.io.annotation.Input;
import org.jchdl.model.rtl.core.io.annotation.Output;
import org.jchdl.model.rtl.core.meta.Module;
import org.jchdl.model.rtl.core.meta.PropagateManager;


public class AndReg extends Module {
    @Input
    private Bits a;
    @Input
    private Bits b;
    @Output
    private Reg r;

    public AndReg(Module parent, Reg r, Bits a, Bits b) {
        super(parent);
        this.a = a;
        this.b = b;
        this.r = r;
        construct();
    }

    @Override
    public void logic() {
        when(ChangingEvent.of(a), ChangingEvent.of(b)).run(
                () -> r.set(a.and(b))
        );
    }

    public static void main(String[] args) {
        Bits a = new Bits(8, Bit.BIT_0);
        Bits b = new Bits(8, Bit.BIT_0);
        Reg r = new Reg(8);
        Module module = new AndReg(null, r, a, b);
        module.toVerilog();

        PropagateManager.propagate(module);
        System.out.println("out: " + r);

        a.bit(0).assign(Bit.BIT_1);
        PropagateManager.propagate(module);
        System.out.println("out: " + r);

        b.bit(0).assign(Bit.BIT_1);
        PropagateManager.propagate(module);
        System.out.println("out: " + r);
    }
}
