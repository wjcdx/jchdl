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
package org.jchdl.model.gsl.core.value;

import java.util.Objects;

public class Value {
    public static final int VALUE_0 = 0x00;
    public static final int VALUE_1 = 0x01;
    public static final int VALUE_X = 0x02;
    public static final int VALUE_Z = 0x03;

    public static final Value V0 = new Value(VALUE_0);
    public static final Value V1 = new Value(VALUE_1);
    public static final Value Vx = new Value(VALUE_X);
    public static final Value Vz = new Value(VALUE_Z);

    public int v = VALUE_X;

    public Value() {
        this(VALUE_X);
    }

    public Value(int v) {
        this.v = v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return v == value.v;
    }

    @Override
    public int hashCode() {
        return Objects.hash(v);
    }

    @Override
    public String toString() {
        String value = "Null";
        switch (v) {
            case VALUE_0:
                value = "0";
                break;
            case VALUE_1:
                value = "1";
                break;
            case VALUE_X:
                value = "x";
                break;
            case VALUE_Z:
                value = "z";
                break;
        }
        return value;
    }

    public static Value[] values(int nBits, int value) {
        Value[] values = new Value[nBits];
        for (Value v : values) {
            v.v = value;
        }
        return values;
    }

    public static Value[] values(int nBits) {
        Value[] values = new Value[nBits];
        for (Value v : values) {
            v.v = VALUE_0;
        }
        return values;
    }
}
