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
import org.jchdl.model.gsl.core.gate.pull.PullDown;
import org.jchdl.model.gsl.core.gate.pull.PullUp;
import org.jchdl.model.gsl.core.io.Input;
import org.jchdl.model.gsl.core.io.Output;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.Port;
import org.jchdl.model.gsl.core.meta.Propagatable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public class GslNode {
    private String name;
    private Node modelNode;
    private ArrayList<Node> modelSubNodes = new ArrayList<>(16);
    private ArrayList<Propagatable> modelWires = new ArrayList<>(16);
    private ArrayList<Wire> modelPullupWires = new ArrayList<>(16);
    private ArrayList<Wire> modelPulldownWires = new ArrayList<>(16);

    private ArrayList<GslNode> children = new ArrayList<>(16);
    private ArrayList<GslWire> inputs = new ArrayList<>(16);
    private ArrayList<GslWire> outputs = new ArrayList<>(16);
    private ArrayList<GslWire> parameters = new ArrayList<>(16);
    private ArrayList<GslWire> declarations = new ArrayList<>(16);
    private ArrayList<GslWire> pullups = new ArrayList<>(16);
    private ArrayList<GslWire> pulldowns = new ArrayList<>(16);

    public GslNode(Node modelNode) {
        this.modelNode = modelNode;
        this.name = getModelNodeName();
    }

    private String getModelNodeName() {
        String name = modelNode.getName();
        if (name == null) {
            name = modelNode.getClass().getSimpleName();
        }
        return name;
    }

    public String getName() {
        return name;
    }

    public Node getModelNode() {
        return modelNode;
    }

    private void addChild(GslNode gslNode) {
        children.add(gslNode);
    }

    public ArrayList<GslNode> getChildren() {
        return children;
    }

    public ArrayList<GslWire> getInputs() {
        return inputs;
    }

    public ArrayList<GslWire> getOutputs() {
        return outputs;
    }

    public ArrayList<GslWire> getParameters() {
        return parameters;
    }

    public ArrayList<GslWire> getDeclarations() {
        return declarations;
    }

    public ArrayList<GslWire> getPullups() {
        return pullups;
    }

    public ArrayList<GslWire> getPulldowns() {
        return pulldowns;
    }

    public void setupNodeHierarchy() throws IllegalAccessException {
        // setup node hierarchy
        fillNodeHierarchyRecursively(this);
        // fill meta data: Wire & WireVec field names of all Nodes to provide meta information of generated modules
        fillNodeMetaNameRecursively(this);
        // collect inputs and outputs from Node's inner Wire and WireVec fields
        fillNodeInputsOutputsRecursively(this);
        // fill meta data: sub-node inst/local variable declarations/pulled wires
        fillNodeMetaModelRecursively(this);
    }

    private ArrayList<Port> getNodeOutputsArrayList(Node node) {
        return new ArrayList<>(Arrays.asList(node.getOutputs()));
    }

    private void addModelWire(Wire wire, ArrayList<Propagatable> wires) {
        Propagatable propagatable = wire;
        if (wire.getVec() != null) {
            propagatable = wire.getVec();
        }
        if (!wires.contains(propagatable)) {
            wires.add(propagatable);
        }
    }

    // port: root element, exclusive
    private void traverseTree(Port top, Node parent, ArrayList<Node> nodes, ArrayList<Propagatable> wires) {
        ArrayList<Port> borderPorts = getNodeOutputsArrayList(parent);
        if (borderPorts.contains(top)) return;

        // get & iterate downstream ports
        ArrayList<Port> downstreams = new ArrayList<>(32);
        Propagatable owner = top.getOwner();
        if (owner instanceof Node) {
            Node n = (Node) owner;
            if (n == parent) {
                // assert: port is input
                downstreams.addAll(top.getDownstreams());
            } else {
                if (!nodes.contains(n)) {
                    nodes.add(n);
                    ArrayList<Port> nOutputs = getNodeOutputsArrayList(n);
                    // assert: output connect to a wire
                    for (Port np : nOutputs) {
                        Port wi = np.getDownstream(0);
                        downstreams.add(wi);
                        addModelWire((Wire) wi.getOwner(), wires);
                    }
                }
            }
        } else if (owner instanceof Wire) {
            Wire wire = (Wire) owner;
            downstreams.addAll(wire.getOutput().getDownstreams());
            addModelWire(wire, wires);
        }

        for (Port p : downstreams) {
            traverseTree(p, parent, nodes, wires);
        }
    }

    private void traverseInsideNode() {
        Input[] inputs = modelNode.getInputs();
        for (Input input : inputs) {
            traverseTree(input, modelNode, modelSubNodes, modelWires);
        }
    }

    private void fillNode() {
        traverseInsideNode();
        for (Node n : modelSubNodes) {
            addChild(new GslNode(n));
        }
    }

    private void backtraceForPulls(Node n, ArrayList<Wire> pullupWires, ArrayList<Wire> pulldownWires) {
        Input[] inputs = n.getInputs();
        for (Input input : inputs) {
            //assert: input driven by a wire
            Wire wire = (Wire) input.getUpstream().getOwner();
            Propagatable upOwner = wire.getInput().getUpstream().getOwner();
            if (upOwner instanceof PullUp) {
                if (!pullupWires.contains(wire)) {
                    pullupWires.add(wire);
                }
            } else if (upOwner instanceof PullDown) {
                if (!pulldownWires.contains(wire)) {
                    pulldownWires.add(wire);
                }
            }
        }
    }

    private void backtrace() {
        for (Node n : modelSubNodes) {
            backtraceForPulls(n, modelPullupWires, modelPulldownWires);
        }
        for (Wire wire : modelPullupWires) {
            addModelWire(wire, modelWires);
        }
        for (Wire wire : modelPulldownWires) {
            addModelWire(wire, modelWires);
        }
    }

    private void fillNodeHierarchyRecursively(GslNode gslNode) {
        gslNode.fillNode();
        gslNode.backtrace();
        for (GslNode n : gslNode.getChildren()) {
            fillNodeHierarchyRecursively(n);
        }
    }

    private void fillWireFieldNameOfClass(Class clazz, Object object) throws IllegalAccessException {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == Wire.class) {
                field.setAccessible(true);
                Wire w = (Wire) field.get(object);
                w.setName(field.getName());
            } else if (field.getType() == WireVec.class) {
                field.setAccessible(true);
                WireVec vec = (WireVec) field.get(object);
                vec.setName(field.getName());
            }
        }
    }

    private void fillWireFieldName(Node node) throws IllegalAccessException {
        Class clazz = node.getClass();
        while (clazz != Node.class) {
            fillWireFieldNameOfClass(clazz, node);
            clazz = clazz.getSuperclass();
        }
    }

    private void fillNodeMetaNameRecursively(GslNode gslNode) throws IllegalAccessException {
        fillWireFieldName(gslNode.modelNode);
        for (GslNode n : gslNode.children) {
            fillNodeMetaNameRecursively(n);
        }
    }

    private void addDeclaration(GslWire gslWire) {
        // if the wire was already added or declared as a input/output
        if (gslWire.isNameContainedIn(this.declarations)
                || gslWire.isNameContainedIn(this.inputs)
                || gslWire.isNameContainedIn(this.outputs)) {
            return;
        }
        // add local wire declaration
        this.declarations.add(gslWire);
    }

    private void fillNodeLocalWireDeclarations() {
        for (Propagatable p : modelWires) {
            if (p instanceof Wire) {
                addDeclaration(new GslWire((Wire) p));
            } else if (p instanceof WireVec) {
                addDeclaration(new GslWire((WireVec) p));
            }
        }
    }

    private GslWire getGslWire(Wire wire) {
        GslWire gslWire = null;
        if (wire.getVec() == null) {
            gslWire = new GslWire(wire);
        } else {
            WireVec wireVec = wire.getVec();
            gslWire = new GslWire(wireVec);
        }
        return gslWire;
    }

    private GslWire getGslWire(Wire wire, int length) {
        GslWire gslWire = null;
        if (length == 1) {
            if (wire.getVec() == null) {
                gslWire = new GslWire(wire);
            } else {
                int index = wire.getVecIndex();
                gslWire = new GslWire(wire.getVec(), index, index + 1);
            }
        } else {
            // assert: output connect to a wire which belongs to a WireVec
            int from = wire.getVecIndex();
            int to = from + length;
            gslWire = new GslWire(wire.getVec(), from, to);
        }
        return gslWire;
    }

    private void fillNodeOutputParameters(GslNode gslNode) {
        Output[] outputs = gslNode.modelNode.getOutputs();
        int outputIndex = 0;
        for (GslWire gslWire : gslNode.outputs) {
            Output output = outputs[outputIndex];
            // assert: output connect to a wire
            Wire owner = (Wire) output.getDownstream(0).getOwner();
            GslWire parameter = getGslWire(owner, gslWire.nBits());
            gslNode.parameters.add(parameter);
            outputIndex += gslWire.nBits();
        }
    }

    private void fillNodeInputParameters(GslNode gslNode) {
        Input[] inputs = gslNode.modelNode.getInputs();
        int inputIndex = 0;
        for (GslWire gslWire : gslNode.inputs) {
            Input input = inputs[inputIndex];
            // assert: input driven by a wire
            Wire owner = (Wire) input.getUpstream().getOwner();
            GslWire parameter = getGslWire(owner, gslWire.nBits());
            gslNode.parameters.add(parameter);
            inputIndex += gslWire.nBits();
        }
    }

    private void fillNodeParameters() {
        for (GslNode n : children) {
            fillNodeOutputParameters(n);
            fillNodeInputParameters(n);
        }
    }

    private void fillNodeMetaModel(GslNode gslNode) {
        // local wire seq for sub-node inst should start from 1
        GslWire.setSeq(GslWire.NET_SEQUENCE_DECLARATION);
        // declarations of used local wires
        gslNode.fillNodeLocalWireDeclarations();
        // assert: parameters should be provided according to the inputs and outputs
        gslNode.fillNodeParameters();
        // fill pulled wires after all names generated
        gslNode.fillNodePulls();
    }

    private void fillNodePulls() {
        for (Wire wire : modelPullupWires) {
            GslWire gslWire = getGslWire(wire, 1);
            pullups.add(gslWire);
        }
        for (Wire wire : modelPulldownWires) {
            GslWire gslWire = getGslWire(wire, 1);
            pulldowns.add(gslWire);
        }
    }

    private void fillNodeMetaModelRecursively(GslNode gslNode) {
        fillNodeMetaModel(gslNode);
        for (GslNode n : gslNode.getChildren()) {
            fillNodeMetaModelRecursively(n);
        }
    }

    private void fillNodeOutputs(GslNode gslNode) {
        Output[] outputs = gslNode.modelNode.getOutputs();
        for (int i = 0; i < outputs.length; i++) {
            Output output = outputs[i];
            // atomic node
            if (output.getUpstream() == null) {
                // we setup a wire for module generation
                Wire wireNotReal = new Wire();
                wireNotReal.setName("wo" + i);
                gslNode.outputs.add(new GslWire(wireNotReal));
                continue;
            }
            // assert: output driven by a wire
            Wire owner = (Wire) output.getUpstream().getOwner();
            GslWire gslWire = getGslWire(owner);
            gslNode.outputs.add(gslWire);
            i += gslWire.nBits() - 1; // add extra wires consumed
        }
    }

    private void fillNodeInputs(GslNode gslNode) {
        Input[] inputs = gslNode.modelNode.getInputs();
        for (int i = 0; i < inputs.length; i++) {
            Input input = inputs[i];
            // atomic node
            if (input.hasNoDownstreams()) {
                // we setup a wire for module generation
                Wire wireNotReal = new Wire();
                wireNotReal.setName("wi" + i);
                gslNode.inputs.add(new GslWire(wireNotReal));
                continue;
            }
            // assert: input connect to a wire
            Wire owner = (Wire) input.getDownstream(0).getOwner();
            GslWire gslWire = getGslWire(owner);
            gslNode.inputs.add(gslWire);
            i += gslWire.nBits() - 1; // add extra wires consumed
        }
    }

    private void fillNodeInputsOutputs(GslNode gslNode) {
        // collect inputs and outputs from Node's inner Wire and WireVec fields
        GslWire.setSeq(GslWire.NET_SEQUENCE_OUTPUT);
        fillNodeOutputs(gslNode);
        GslWire.setSeq(GslWire.NET_SEQUENCE_INPUT);
        fillNodeInputs(gslNode);
    }

    private void fillNodeInputsOutputsRecursively(GslNode gslNode) {
        fillNodeInputsOutputs(gslNode);
        for (GslNode n : gslNode.getChildren()) {
            fillNodeInputsOutputsRecursively(n);
        }
    }
}
