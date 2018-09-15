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

import org.jchdl.vc.gsl.GslVerilogConverter;
import org.jchdl.model.gsl.core.io.Input;
import org.jchdl.model.gsl.core.io.Output;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Node implements Propagatable {
    private String name;
    private ArrayList<Net> netI = new ArrayList<>(16);
    private ArrayList<Net> netO = new ArrayList<>(16);
    private Input[] ioI;
    private Output[] ioO;

    protected void in(Net net) {
        netI.add(net);
    }

    protected void in(Net nets[]) {
        netI.addAll(Arrays.asList(nets));
    }

    /**
     * -1 = -9 % 8         7 = 7 % 8
     * 7 = -1 + 8;        15 = 7 + 8
     * 7 =  7 % 8;         7 = 15 % 8
     */
    protected Input in(int index) {
        int length = ioI.length;
        int i = index;
        i %= length;
        i += length;
        i %= length;
        return ioI[i];
    }

    protected int nIn() {
        return ioI.length;
    }

    protected Input[] inputs() {
        return ioI;
    }

    protected Input[] inputs(int from) {
        return Arrays.copyOfRange(ioI, from, ioI.length);
    }

    // range: [from, to)
    protected Input[] inputs(int from, int to) {
        return Arrays.copyOfRange(ioI, from, to);
    }

    protected void out(Net net) {
        netO.add(net);
    }

    protected void out(Net nets[]) {
        netO.addAll(Arrays.asList(nets));
    }

    protected Output out(int index) {
        int length = ioO.length;
        int i = index;
        i %= length;
        i += length;
        i %= length;
        return ioO[i];
    }

    protected int nOut() {
        return ioO.length;
    }

    protected Output[] outputs() {
        return ioO;
    }

    protected Output[] outputs(int from) {
        return Arrays.copyOfRange(ioO, from, ioO.length);
    }

    // range: [from, to)
    protected Output[] outputs(int from, int to) {
        return Arrays.copyOfRange(ioO, from, to);
    }

    // construct logic and connect to external wires
    protected void construct() {
        // -1. the logic diffs according to the number of input wires
        prepare();
        // 0. construct logic first
        logic();
        // 1. connect node ports to external wires then
        connect();
    }

    // Node logic may differs because of different input/output port numbers
    private void prepare() {
        ioI = new Input[netI.size()];
        for (int i = 0; i < netI.size(); i++) {
            ioI[i] = new Input(this);
        }
        ioO = new Output[netO.size()];
        for (int i = 0; i < netO.size(); i++) {
            ioO[i] = new Output(this);
        }
    }

    public abstract void logic();

    public void connect() {
        for (int i = 0; i < netI.size(); i++) {
            Net net = netI.get(i);
            Input input = ioI[i];
            net.connect(input);
        }
        for (int i = 0; i < netO.size(); i++) {
            Net net = netO.get(i);
            Output output = ioO[i];
            net.assign(output);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Input[] getInputs() {
        return ioI;
    }

    public Output[] getOutputs() {
        return ioO;
    }

    private void propagateInputs() {
        for (Input input : ioI) {
            input.propagate();
        }
    }

    protected void propagateOutputs() {
        for (Output output : ioO) {
            output.propagate();
        }
    }

    private void propagateInputs(int steps) {
        for (Input input : ioI) {
            input.propagate(steps);
        }
    }

    protected void propagateOutputs(int steps) {
        for (Output output : ioO) {
            output.propagate(steps);
        }
    }

    @Override
    public void propagate() {
        propagateInputs();
    }

    @Override
    public void propagate(int steps) {
        propagateInputs(steps);
    }

    public void toVerilog() {
        GslVerilogConverter.translate(this);
    }
}
