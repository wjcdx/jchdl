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
package org.jchdl.model.gsl.sequential;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.meta.AtomicNode;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;

public class Clock extends AtomicNode {
    private long period;

    public Clock(Wire out) {
        out(out);
        construct();
    }

    @Override
    public void atomic() {
        try {
            Thread.sleep(period);
            out(0).value.v = Value.VALUE_0;

            Thread.sleep(period);
            out(0).value.v = Value.VALUE_1;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String primitive() {
        return "clk";
    }

    public static void tick(Wire clk, Wire q, Wire nq, int n) throws InterruptedException {
        long millis = 1000;
        for (int i = 0; i < n; i++) {
            Thread.sleep(millis);

            clk.assign(Value.V0);
            PropagateManager.propagateParallel(clk);
            System.out.println("c = 0, q => " + q.getValue().toString() + " nq => " + nq.getValue().toString());

            Thread.sleep(millis);

            clk.assign(Value.V1);
            PropagateManager.propagateParallel(clk);
            System.out.println("c = 1, q => " + q.getValue().toString() + " nq => " + nq.getValue().toString());
        }
    }

    public static void tick(Wire clk, int n) throws InterruptedException {
        for (int i = 0; i < n; i++) {
            clk.assign(Value.V0);
            PropagateManager.propagateParallel(clk);

            clk.assign(Value.V1);
            PropagateManager.propagateParallel(clk);
        }
    }
}
