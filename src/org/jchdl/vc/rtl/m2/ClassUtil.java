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

import jdk.internal.org.objectweb.asm.Type;
import org.jchdl.model.rtl.core.datatype.annotation.Range;
import org.jchdl.model.rtl.core.datatype.annotation.Width;
import org.jchdl.model.rtl.core.io.annotation.Input;
import org.jchdl.model.rtl.core.io.annotation.Output;

import java.lang.reflect.Field;

public class ClassUtil {

    public static Class<?> getClassOfName(String internalName) {
        try {
            return Class.forName(internalName.replace('/', '.'));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public static Class<?> getClassOfType(Type t) {
        if (t.getSort() == Type.OBJECT) {
            return getClassOfName(t.getInternalName());
        }
        return getClassOfName(t.getDescriptor());
    }

    public static Class<?> getClassOfDesc(String desc) {
        return getClassOfType(Type.getType(desc));
    }

    public static String getFieldDirection(Field field) {
        if (field.getAnnotation(Input.class) != null) {
            return RtlPort.RTL_PORT_TYPE_INPUT;
        } else if (field.getAnnotation(Output.class) != null) {
            return RtlPort.RTL_PORT_TYPE_OUTPUT;
        }
        return null;
    }

    public static int getFieldWidth(Field field) {
        Width width = field.getAnnotation(Width.class);
        if (width != null) {
            return width.value();
        }
        Range range = field.getAnnotation(Range.class);
        if (range != null) {
            return range.msb() - range.lsb() + 1;
        }
        return 0;
    }

}
