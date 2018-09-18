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
package org.jchdl.vc.rtl.m2;

import org.jchdl.model.rtl.core.datatype.Reg;
import org.jchdl.model.rtl.core.meta.Bitable;

import java.lang.reflect.Field;

public class RtlBitable {
    public static final String RTL_BITABLE_TYPE_WIRE = "wire";
    public static final String RTL_BITABLE_TYPE_REG = "reg";

    private String type;
    private String name;
    private int nBits;

    public RtlBitable(String type, String name, int nBits) {
        this.type = type;
        this.name = name;
        this.nBits = nBits;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int nBits() {
        return nBits;
    }

    public void setBits(int nBits) {
        this.nBits = nBits;
    }

    public String getDeclarationString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-5s ", type));
        if (nBits > 1) {
            sb.append(String.format("[%2d:%d] ", nBits - 1, 0));
        }
        sb.append(name);
        return sb.toString();
    }

    public static String getType(Bitable bitable) {
        if (bitable instanceof Reg) {
            return RTL_BITABLE_TYPE_REG;
        } else {
            return RTL_BITABLE_TYPE_WIRE;
        }
    }

    public static RtlBitable build(String direction, String name, Object object, Field field) throws IllegalAccessException {
        if (direction == null) {
            direction = ClassUtil.getFieldDirection(field);
        }

        if (name == null) {
            name = field.getName();
        }

        Bitable bitable = (Bitable) field.get(object);
        String type = getType(bitable);

        int nBits = bitable.nBits();
        if (nBits == 0) {
            nBits = ClassUtil.getFieldWidth(field);
        }

        RtlBitable rtlBitable = null;
        if (direction != null) {
            rtlBitable = new RtlPort(direction, type, name, nBits);
        } else {
            rtlBitable = new RtlBitable(type, name, nBits);
        }
        return rtlBitable;
    }

    public static RtlBitable build(Object object, Field field) throws IllegalAccessException {
        return build(null, null, object, field);
    }
}
