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
import org.jchdl.model.rtl.core.datatype.Bit;
import org.jchdl.model.rtl.core.io.annotation.Input;
import org.jchdl.model.rtl.core.io.annotation.Output;
import org.jchdl.model.rtl.core.meta.Module;
import org.jchdl.model.rtl.core.meta.PropagateManager;

/**
 * @author wjcdx
 */
public class Adder4 extends Module {
    @Input
    private Bits a;
    @Input
    private Bits b;
    @Output
    private Bits s;

    public Adder4(Module parent, Bits s, Bits a, Bits b) {
        super(parent);
        this.a = a;
        this.b = b;
        this.s = s;
        construct();
    }

    private void assignS() {
        s.assign(a.add(b));
    }

    @Override
    public void logic() {
        assign(s).from(a, b).with(this::assignS);
    }

    public static void main(String[] args) {
        Bits a = new Bits(8, Bit.BIT_0);
        Bits b = new Bits(8, Bit.BIT_0);
        Bits s = new Bits(8, Bit.BIT_0);
        Adder4 adder4 = new Adder4(null, s, a, b);

        PropagateManager.propagate(adder4);
        System.out.println("out: " + s);

        a.bit(0).assign(Bit.BIT_1);
        a.bit(1).assign(Bit.BIT_1);
        a.bit(3).assign(Bit.BIT_1);
        b.bit(3).assign(Bit.BIT_1);
        PropagateManager.propagate(adder4);
        System.out.println("out: " + s);

        adder4.toVerilog();
    }
}
