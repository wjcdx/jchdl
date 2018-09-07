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

import java.util.ArrayList;
import java.util.Arrays;

public class PropagateManager {
    private static ArrayList<Propagatable> propagatables = new ArrayList<>(128);

    public static void add(Propagatable p1, Propagatable... ps) {
        propagatables.add(p1);
        propagatables.addAll(Arrays.asList(ps));
    }

    public static void propagate() {
        for (Propagatable propagatable : propagatables) {
            propagatable.propagate();
        }
    }

    // steps >= 1
    public static void propagate(int steps) {
        assert steps >= 1;
        ArrayList<Propagatable> ps = new ArrayList<>(propagatables);
        propagatables.clear();
        for (Propagatable propagatable : ps) {
            propagatable.propagate(steps);
        }
    }

    public static void propagateParallel() {
        while (!propagatables.isEmpty()) {
            ArrayList<Propagatable> ps = new ArrayList<>(propagatables);
            propagatables.clear();
            for (Propagatable p : ps) {
                p.propagate(1);
            }
        }
    }

    public static void propagateParallel(Propagatable p1, Propagatable... ps) {
        assert propagatables.size() == 0;
        add(p1, ps);
        propagateParallel();
    }
}
