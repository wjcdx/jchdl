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
package org.jchdl.model.gsl.core.meta;

import org.jchdl.model.gsl.core.value.Value;

import java.util.ArrayList;

// Note: One output port can driver multiple input ports.
public abstract class Port implements Propagatable {
    public Value value = new Value(Value.VALUE_X);
    private Port upstream;
    private ArrayList<Port> downstreams = new ArrayList<>(16);
    protected Propagatable owner;

    public Port(Propagatable owner) {
        this.owner = owner;
    }

    public Propagatable getOwner() {
        return owner;
    }

    public Port getDownstream(int i) {
        return downstreams.get(i);
    }

    public ArrayList<Port> getDownstreams() {
        return downstreams;
    }

    public Port getUpstream() {
        return upstream;
    }

    public void setUpstream(Port upstream) {
        this.upstream = upstream;
    }

    // direction:
    // upstream ---connect---> downstream
    public void connect(Port port) {
        downstreams.add(port);
        port.setUpstream(this);
    }

    // in ports of AtomicNode has no downstream ports.
    public boolean hasNoDownstreams() {
        return downstreams.isEmpty();
    }

    protected void propagateDownstreams() {
        for (Port p : downstreams) {
            if (!p.value.equals(value)) {
                p.value.v = value.v;
                p.propagate();
            }
        }
    }

    protected void propagateDownstreams(int steps) {
        for (Port p : downstreams) {
            if (!p.value.equals(value)) {
                p.value.v = value.v;
                p.propagate(steps);
            }
        }
    }

}
