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
package org.jchdl.model.gsl.core.datatype.net;

import org.jchdl.model.gsl.core.datatype.helper.WireVec;
import org.jchdl.model.gsl.core.gate.pull.PullDown;
import org.jchdl.model.gsl.core.gate.pull.PullUp;
import org.jchdl.model.gsl.core.io.Input;
import org.jchdl.model.gsl.core.io.Output;
import org.jchdl.model.gsl.core.meta.Assignable;
import org.jchdl.model.gsl.core.meta.Ground;
import org.jchdl.model.gsl.core.meta.Net;
import org.jchdl.model.gsl.core.meta.Port;
import org.jchdl.model.gsl.core.value.Value;

// the value of in and out can be different, such as Wand/Wor wire.
// but we do not care about this for now.
public class Wire extends Net implements Assignable {
    private String name;
    private WireVec vec;
    private int seq;

    private Input in = new Input(this);
    private Output out = new Output(this);

    public Wire() {
    }

    public Wire(Value value) {
        this.assign(value);
    }

    public Wire(Input in) {
        this.assign(in);
    }

    public Wire(Output output) {
        this.connect(output);
    }

    public Wire(WireVec vec) {
        this.vec = vec;
    }

    public Value getValue() {
        return this.out.value;
    }

    // Input direction: a wire assigns a value means the value is continuously assigned to this wire
    @Override
    public void assign(Value value) {
        this.in.value.v = value.v;
    }

    // Input direction: a wire assigns a port means the port's value is continuously assigned to this wire
    @Override
    public void assign(Port port) {
        port.connect(this.in);
    }

    // Output direction: a wire connects to a port means using its output to drive the port
    @Override
    public void connect(Port port) {
        this.out.connect(port);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WireVec getVec() {
        return vec;
    }

    public void setVec(WireVec vec) {
        this.vec = vec;
    }

    public int getVecIndex() {
        if (vec == null) {
            return 0;
        }
        for (int i = 0; i < vec.nBits(); i++) {
            if (vec.wire(i).equals(this)) {
                return i;
            }
        }
        return 0;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Input getInput() {
        return in;
    }

    public Output getOutput() {
        return out;
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    @Override
    public void propagate() {
        this.out.value.v = this.in.value.v;
        this.out.propagate();
    }

    @Override
    public void propagate(int steps) {
        this.out.value.v = this.in.value.v;
        this.out.propagate(--steps);
    }

    public static Wire pulledUp() {
        Wire wire = new Wire();
        PullUp.inst(wire);
        return wire;
    }

    public static Wire pulledDown() {
        Wire wire = new Wire();
        PullDown.inst(wire);
        return wire;
    }

    public static Wire toGround() {
        Wire wire = new Wire();
        Ground.inst(wire);
        return wire;
    }

    @Deprecated
    public static Wire[] array(Wire... wires) {
        return wires;
    }

    public static Wire[] arrayOf(Wire... wires) {
        return wires;
    }

}
