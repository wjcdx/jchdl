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

import org.jchdl.model.rtl.core.datatype.Structure;
import org.jchdl.model.rtl.core.meta.Bitable;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

public class RtlStructure {
    public static final String PREFIX = "Structure:";
    public static final String DELIMITER = "_";

    public String prefix;
    public Structure structure;
    public String direction;
    private LinkedHashMap<String, RtlBitable> rtlBitables = new LinkedHashMap<>(32);
    private LinkedHashMap<String, RtlStructure> rtlStructures = new LinkedHashMap<>(32);

    public RtlStructure(String direction, String prefix, Structure structure) throws IllegalAccessException {
        this.prefix = prefix;
        this.structure = structure;
        this.direction = direction;
        buildStructureHierarchy();
    }

    /**
     * Structure fields should be public.
     * @throws IllegalAccessException
     */
    private void buildStructureHierarchy() throws IllegalAccessException {
        for (Field field : structure.getClass().getDeclaredFields()) {
            String key = getFieldKey(field);
            if (Bitable.class.isAssignableFrom(field.getType())) {
                RtlBitable rtlBitable = RtlBitable.build(direction, key, structure, field);
                rtlBitables.put(key, rtlBitable);
            } else if (Structure.class.isAssignableFrom(field.getType())) {
                RtlStructure rs = new RtlStructure(direction, key, (Structure) field.get(structure));
                rtlStructures.put(key, rs);
                rtlStructures.putAll(rs.getRtlStructures());
                rtlBitables.putAll(rs.getRtlBitables());
            }
        }
    }

    private String getFieldKey(Field field) {
        return prefix + DELIMITER + field.getName();
    }

    public LinkedHashMap<String, RtlBitable> getRtlBitables() {
        return rtlBitables;
    }

    public LinkedHashMap<String, RtlStructure> getRtlStructures() {
        return rtlStructures;
    }

    public static String trimPrefix(String key) {
        return key.substring(PREFIX.length());
    }

    public static boolean hasPrefix(String key) {
        return key.indexOf(PREFIX) == 0;
    }
}
