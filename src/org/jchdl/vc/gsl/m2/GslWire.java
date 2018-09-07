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
package org.jchdl.vc.gsl.m2;

import org.jchdl.model.gsl.core.datatype.helper.WireVec;
import org.jchdl.model.gsl.core.datatype.net.Wire;

import java.util.ArrayList;
import java.util.Objects;

public class GslWire {
    private static int NET_SEQUENCE_NUMBER = 0;
    public static final int NET_SEQUENCE_INPUT = 10000;
    public static final int NET_SEQUENCE_OUTPUT = 20000;
    public static final int NET_SEQUENCE_DECLARATION = 0;

    private String name;
    private boolean generatedName = false;

    private int nBits;
    // for single wire
    public Wire wire;
    // for WireVec
    private WireVec wireVec;
    private int indexFrom; // inclusive
    private int indexTo;   // exclusive

    public GslWire(Wire wire) {
        this.wire = wire;
        this.nBits = 1;
        this.indexFrom = 0;
        this.indexTo = 0;
        this.name = getWireName(wire);
    }

    public GslWire(WireVec wireVec) {
        this.wireVec = wireVec;
        this.nBits = wireVec.nBits();
        this.indexFrom = 0;
        this.indexTo = wireVec.nBits();
        this.name = getWireVecName(wireVec);
    }

    // GslWire of partial WireVec[from, to)
    public GslWire(WireVec wireVec, int from, int to) {
        this.wireVec = wireVec;
        this.nBits = to - from;
        this.indexFrom = from;
        this.indexTo = to;
        this.name = getWireVecName(wireVec);
    }

    private String getFormatNameSufix(int seq) {
        String name = "";
        if (seq > NET_SEQUENCE_OUTPUT) {
            name = String.format("o%d", (seq - NET_SEQUENCE_OUTPUT));
        } else if (seq > NET_SEQUENCE_INPUT) {
            name = String.format("i%d", (seq - NET_SEQUENCE_INPUT));
        } else if (seq > NET_SEQUENCE_DECLARATION) {
            name = String.format("%d", (seq - NET_SEQUENCE_DECLARATION));
        }
        return name;
    }

    private String getWireName(Wire wire) {
        String n = wire.getName();
        if (n == null) {
            if (wire.getSeq() == 0) {
                wire.setSeq(++NET_SEQUENCE_NUMBER);
            }
            n = "w" + getFormatNameSufix(wire.getSeq());
            generatedName = true;
        }
        return n;
    }

    private String getWireVecName(WireVec vec) {
        String n = vec.getName();
        if (n == null) {
            if (vec.getSeq() == 0) {
                vec.setSeq(++NET_SEQUENCE_NUMBER);
            }
            n = "wv" + getFormatNameSufix(vec.getSeq());
            generatedName = true;
        }
        return n;
    }

    public String getName() {
        return name;
    }

    public boolean isGeneratedName() {
        return generatedName;
    }

    public boolean isNameContainedIn(ArrayList<GslWire> gslWires) {
        for (GslWire w : gslWires) {
            if (w.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public int nBits() {
        return nBits;
    }

    public static void setSeq(int seq) {
        NET_SEQUENCE_NUMBER = seq;
    }

    public String getDeclarationString() {
        StringBuilder sb = new StringBuilder("wire ");
        if (nBits > 1) {
            sb.append("[").append(nBits - 1).append(":").append(0).append("] ");
        }
        sb.append(name);
        return sb.toString();
    }

    public String getParameterString() {
        StringBuilder sb = new StringBuilder(name);
        if (indexTo > 0) { // ref to a WireVec
            if (wireVec.nBits() != nBits) { // not entire vector
                sb.append("[");
                if (indexTo - 1 > indexFrom) {
                    sb.append(indexTo - 1).append(":");
                }
                sb.append(indexFrom).append("]");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("wire ");
        if (indexTo > 0) {
            sb.append("[");
            if (indexTo - 1 > indexFrom) {
                sb.append(indexTo - 1).append(":");
            }
            sb.append(indexFrom).append("] ");
        }
        sb.append(name);
        return sb.toString();
    }

}
