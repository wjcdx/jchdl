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

import org.jchdl.model.rtl.core.meta.Bitable;

import java.util.Objects;

public class Bit implements Bitable {
    public static final int BIT_0 = 0x0;
    public static final int BIT_1 = 0x1;
    public static final Bit B0 = new Bit(BIT_0);
    public static final Bit B1 = new Bit(BIT_1);

    public int value = BIT_0;
    private String name;

    public Bit() {
    }

    public Bit(int value) {
        this.value = value;
    }

    public Bit(boolean set) {
        this.value = set ? BIT_1 : BIT_0;
    }

    @Override
    public boolean boolVal() {
        return this.equals(BIT_1);
    }

    @Override
    public int intVal() {
        return value;
    }

    @Override
    public long longVal() {
        return (long)value;
    }

    public void assign(int value) {
        this.value = value;
    }

    public void assign(Bit bit) {
        this.value = bit.value;
    }

    public void set(Bit bit) {
        this.value = bit.value;
    }

    public void set(boolean set) {
        this.value = set ? BIT_1 : BIT_0;
    }

    public void set() {
        this.value = BIT_1;
    }

    public void clr() {
        this.value = BIT_0;
    }

    // there should be a new Bit out from the AND logic.
    public Bit and(Bit bit) {
        return new Bit(value & bit.value);
    }

    public Bit or(Bit bit) {
        return new Bit(value | bit.value);
    }

    public Bit not() {
        return new Bit((~value) & 0x01);
    }

    public Bit xor(Bit bit) {
        return new Bit(value ^ bit.value);
    }

    public boolean equals(int value) {
        return (this.value == value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bit bit = (Bit) o;
        return value == bit.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "" + value;
    }

    @Override
    public Bit[] bits() {
        return new Bit[]{this};
    }

    @Override
    public int nBits() {
        return 1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Bit inst(boolean set) {
        return new Bit(set);
    }
}
