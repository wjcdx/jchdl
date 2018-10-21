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
package org.jchdl.model.gsl.assign;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.meta.Node;

// There's only continuous assignment for wires.
// Node input connects directly to its output can not be translated correctly.
// Use this Assign node as a replacement.
public class Assign extends Node {

    private Assign(Wire out, Wire in) {
        in(in);
        out(out);
        construct();
    }

    @Override
    public void logic() {
        Wire in = new Wire(in(0));
        in.connect(out(0));
    }

    public static Assign inst(Wire out, Wire in) {
        return new Assign(out, in);
    }

    public static Assign[] inst(Wire[] out, Wire[] in) {
        Assign[] assigns = new Assign[in.length];
        for (int i = 0; i < in.length; i++) {
            assigns[i] = Assign.inst(out[i], in[i]);
        }
        return assigns;
    }
}
