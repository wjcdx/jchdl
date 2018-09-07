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
package org.jchdl.model.rtl.core.datatype;

import org.jchdl.model.annotation.ModelRef;
import org.jchdl.model.rtl.core.datatype.helper.BitVec;
import org.jchdl.model.rtl.core.meta.Bitable;

public class Bits extends BitVec {

    public Bits(int nBits) {
        super(nBits);
    }

    public Bits(int nBits, int value) {
        super(nBits, value);
    }

    public Bits(Bit[] bits) {
        super(bits);
    }

    public Bits part(int bit) {
        return new Bits(bits(bit, bit));
    }

    public Bits part(int msb, int lsb) {
        return new Bits(bits(msb, lsb));
    }

    public void assign(Bits bits) {
        set(bits.bits());
    }

    // bitables: bits in a vec are ordered from lsb to msb,
    //          arrays are ordered from msb to lsb.
    public Bits concat(Bitable... bitables) {
        return new Bits(vecConcat(bitables));
    }

    public Bits and(Bits bits) {
        return new Bits(vecAND(bits.bits()));
    }

    public Bits or(Bits bits) {
        return new Bits(vecOR(bits.bits()));
    }

    public Bits not() {
        return new Bits(vecNOT());
    }

    public Bits xor(Bits bits) {
        return new Bits(vecXOR(bits.bits()));
    }

    public Bits add(Bits bits) {
        return new Bits(vecAdd(bits.bits()));
    }

    @ModelRef
    public static Bits inst(int nBits, int value) {
        return new Bits(nBits, value);
    }

    public static Bits of(Bit bit) {
        return new Bits(new Bit[]{bit});
    }

    public static Bits of(Bit[] bits) {
        return new Bits(bits);
    }

    @ModelRef
    public static Bits from(Reg reg) {
        return new Bits(reg.bits());
    }
}
