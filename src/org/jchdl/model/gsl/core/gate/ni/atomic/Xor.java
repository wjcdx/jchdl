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
package org.jchdl.model.gsl.core.gate.ni.atomic;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.io.Input;
import org.jchdl.model.gsl.core.io.Output;
import org.jchdl.model.gsl.core.value.Value;

public class Xor extends Gate2i {

    public Xor(Wire out, Wire in1, Wire in2) {
        super(out, in1, in2);
    }

    /**
     * The real logic is implemented with hardware. The method here can be used for simulation.
     * implementing with truth-table
     * xor 0 1 x z
     * 0  0 1 x x
     * 1  1 0 x x
     * x  x x x x
     * z  x x x x
     */
    @Override
    public void truth(Output out, Input in1, Input in2) {
        switch (in1.value.v) {
            case Value.VALUE_0:
                switch (in2.value.v) {
                    case Value.VALUE_0:
                        out.value.v = Value.VALUE_0;
                        break;
                    case Value.VALUE_1:
                        out.value.v = Value.VALUE_1;
                        break;
                    case Value.VALUE_X:
                        out.value.v = Value.VALUE_X;
                        break;
                    case Value.VALUE_Z:
                        out.value.v = Value.VALUE_X;
                        break;
                }
                break;
            case Value.VALUE_1:
                switch (in2.value.v) {
                    case Value.VALUE_0:
                        out.value.v = Value.VALUE_1;
                        break;
                    case Value.VALUE_1:
                        out.value.v = Value.VALUE_0;
                        break;
                    case Value.VALUE_X:
                        out.value.v = Value.VALUE_X;
                        break;
                    case Value.VALUE_Z:
                        out.value.v = Value.VALUE_X;
                        break;
                }
                break;
            case Value.VALUE_X:
                switch (in2.value.v) {
                    case Value.VALUE_0:
                        out.value.v = Value.VALUE_X;
                        break;
                    case Value.VALUE_1:
                        out.value.v = Value.VALUE_X;
                        break;
                    case Value.VALUE_X:
                        out.value.v = Value.VALUE_X;
                        break;
                    case Value.VALUE_Z:
                        out.value.v = Value.VALUE_X;
                        break;
                }
                break;
            case Value.VALUE_Z:
                switch (in2.value.v) {
                    case Value.VALUE_0:
                        out.value.v = Value.VALUE_X;
                        break;
                    case Value.VALUE_1:
                        out.value.v = Value.VALUE_X;
                        break;
                    case Value.VALUE_X:
                        out.value.v = Value.VALUE_X;
                        break;
                    case Value.VALUE_Z:
                        out.value.v = Value.VALUE_X;
                        break;
                }
                break;
        }
    }

    public static Xor inst(Wire out, Wire in1, Wire in2) {
        return new Xor(out, in1, in2);
    }

    @Override
    public String primitive() {
        return "xor";
    }
}