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
package org.jchdl.model.gsl.core.datatype.helper;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.pull.PullDown;
import org.jchdl.model.gsl.core.gate.pull.PullUp;
import org.jchdl.model.gsl.core.io.Input;
import org.jchdl.model.gsl.core.io.Output;
import org.jchdl.model.gsl.core.meta.Ground;
import org.jchdl.model.gsl.core.meta.Port;
import org.jchdl.model.gsl.core.meta.Propagatable;
import org.jchdl.model.gsl.core.value.Value;

import java.util.Arrays;

public class WireVec implements Propagatable {
    private Wire[] wires;
    private String name;
    private int seq;

    public WireVec(int nBits) {
        wires = new Wire[nBits];
        for (int i = 0; i < nBits; i++) {
            wires[i] = new Wire(this);
        }
    }

    public WireVec(int nBits, Value value) {
        this(nBits);
        assign(value);
    }

    public WireVec(Input[] inputs) {
        this(inputs.length);
        assign(inputs);
    }

    public WireVec(Output[] outputs) {
        this(outputs.length);
        connect(outputs);
    }

    public Wire wire(int index) {
        int length = wires.length;
        int i = index;
        i %= length;
        i += length;
        i %= length;
        return wires[i];
    }

    public Wire[] wires() {
        return wires;
    }

    public Wire[] wires(int from) {
        return Arrays.copyOfRange(wires, from, wires.length);
    }

    // [from, to)
    public Wire[] wires(int from, int to) {
        return Arrays.copyOfRange(wires, from, to);
    }

    public int nBits() {
        return wires.length;
    }

    public void connect(Port[] ports) {
        for (int i = 0; i < wires.length; i++) {
            wires[i].connect(ports[i]);
        }
    }

    public void assign(Value value) {
        for (Wire wire : wires) {
            wire.assign(value);
        }
    }

    public void assign(Value[] values) {
        for (int i = 0; i < wires.length; i++) {
            wires[i].assign(values[i]);
        }
    }

    public void assign(Port[] ports) {
        for (int i = 0; i < wires.length; i++) {
            wires[i].assign(ports[i]);
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = wires.length - 1; i >= 0; i--) {
            s.append(wire(i).getValue().toString());
        }
        return s.toString();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    @Override
    public void propagate() {
        for (Wire wire : wires) {
            wire.propagate();
        }
    }

    @Override
    public void propagate(int steps) {
        for (Wire wire : wires) {
            wire.propagate(steps);
        }
    }

    public static WireVec pulledUp(int nBits) {
        WireVec vec = new WireVec(nBits);
        for (Wire wire : vec.wires) {
            PullUp.inst(wire);
        }
        return vec;
    }

    public static WireVec pulledDown(int nBits) {
        WireVec vec = new WireVec(nBits);
        for (Wire wire : vec.wires) {
            PullDown.inst(wire);
        }
        return vec;
    }

    public static WireVec toGround(int nBits) {
        WireVec vec = new WireVec(nBits);
        for (Wire wire : vec.wires) {
            Ground.inst(wire);
        }
        return vec;
    }
}
