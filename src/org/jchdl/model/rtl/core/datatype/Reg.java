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

public class Reg extends BitVec {

    public Reg(int nBits) {
        super(nBits);
    }

    public Reg(int nBits, int value) {
        super(nBits, value);
    }

    public Reg(Bit[] bits) {
        super(bits);
    }

    public Reg part(int bit) {
        return new Reg(bits(bit, bit));
    }

    public Reg part(int msb, int lsb) {
        return new Reg(bits(msb, lsb));
    }

    public void set(Bits bits) {
        set(bits.bits());
    }

    public void set(Reg reg) {
        set(reg.bits());
    }

    // bitables: bits in a vec are ordered from lsb to msb,
    //          arrays are ordered from msb to lsb.
    public Reg concat(Bitable... bitables) {
        return new Reg(vecConcat(bitables));
    }

    public Reg and(Reg reg) {
        return new Reg(vecAND(reg.bits()));
    }

    public Reg or(Reg reg) {
        return new Reg(vecOR(reg.bits()));
    }

    public Reg not() {
        return new Reg(vecNOT());
    }

    public Reg xor(Reg reg) {
        return new Reg(vecXOR(reg.bits()));
    }

    public Reg add(Reg reg) {
        return new Reg(vecAdd(reg.bits()));
    }

    // for 1-bit Reg
    public void set(Bit bit) {
        assert nBits == 1;
        bit(0).set(bit);
    }

    // for 1-bit Reg
    public Bit and(Bit bit) {
        assert nBits == 1;
        return bit(0).and(bit);
    }

    // for 1-bit Reg
    public Bit or(Bit bit) {
        assert nBits == 1;
        return bit(0).or(bit);
    }

    // for 1-bit Reg
    public Bit xor(Bit bit) {
        assert nBits == 1;
        return bit(0).xor(bit);
    }

    public static Reg inst(int nBits, int value) {
        return new Reg(nBits, value);
    }

    public static Reg cst(int nBits, int value) {
        return new Reg(nBits, value);
    }

    public static Reg to(Bit bit) {
        return new Reg(new Bit[]{bit});
    }

    @ModelRef
    public static Reg to(Bits bits) {
        return new Reg(bits.bits());
    }
}
