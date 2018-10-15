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
package org.jchdl.vc.gsl;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.meta.AtomicNode;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.example.FullAdder;
import org.jchdl.model.gsl.assign.Assign;
import org.jchdl.vc.gsl.m2.GslNode;
import org.jchdl.vc.gsl.m2.GslWire;

import java.util.ArrayList;
import java.util.HashSet;

public class GslVerilogConverter {
    private static boolean TRANSLATE_PULL_INTO_ASSIGN = false;
    private GslNode topGslNode;
    private HashSet<String> moduleTranslated = new HashSet<>(16);

    private GslVerilogConverter(Node node) {
        this.topGslNode = new GslNode(node);
    }

    private boolean nodeTypeTranslated(GslNode gslNode) {
        return !moduleTranslated.add(gslNode.getName());
    }

    public static void setTranslatePullIntoAssign(boolean translatePullIntoAssign) {
        TRANSLATE_PULL_INTO_ASSIGN = translatePullIntoAssign;
    }

    private void translateModuleHeader(GslNode gslNode) {
        System.out.println("module " + gslNode.getName() + " (");
        for (GslWire w : gslNode.getOutputs()) {
            System.out.println("  output " + w.getDeclarationString() + ",");
        }
        ArrayList<GslWire> inputs = gslNode.getInputs();
        for (GslWire w : inputs) {
            if (w != inputs.get(inputs.size() - 1)) {
                System.out.println("  input  " + w.getDeclarationString() + ",");
            } else {
                System.out.println("  input  " + w.getDeclarationString());
            }
        }
        System.out.println(");");
        System.out.println();
    }

    private void translateDeclarations(GslNode gslNode) {
        ArrayList<GslWire> declarations = gslNode.getDeclarations();
        if (declarations.isEmpty()) return;

        for (GslWire w : declarations) {
            System.out.println(w.getDeclarationString() + ";");
        }
        System.out.println();
    }

    private void translatePulls(GslNode gslNode) {
        ArrayList<GslWire> pullups = gslNode.getPullups();
        ArrayList<GslWire> pulldowns = gslNode.getPulldowns();
        if (pullups.isEmpty() && pulldowns.isEmpty()) return;

        for (GslWire w : pullups) {
            System.out.println("pullup(" + w.getParameterString() + ");");
        }
        for (GslWire w : pulldowns) {
            System.out.println("pulldown(" + w.getParameterString() + ");");
        }
        System.out.println();
    }

    private void translatePullsToAssign(GslNode gslNode) {
        ArrayList<GslWire> pullups = gslNode.getPullups();
        ArrayList<GslWire> pulldowns = gslNode.getPulldowns();
        if (pullups.isEmpty() && pulldowns.isEmpty()) return;

        for (GslWire w : pullups) {
            System.out.println("assign " + w.getParameterString() + " = 1;");
        }
        for (GslWire w : pulldowns) {
            System.out.println("assign " + w.getParameterString() + " = 0;");
        }
        System.out.println();
    }

    private void translateModuleInstances(GslNode gslNode) {
        for (GslNode c : gslNode.getChildren()) {
            String name = c.getName();
            if (c.getChildren().isEmpty()) {
                if (c.getModelNode() instanceof AtomicNode) {
                    name = ((AtomicNode) c.getModelNode()).primitive();
                    if (name.length() == 0) {
                        continue;
                    }
                } else if (c.getModelNode() instanceof Assign) {
                    translateAssign(c);
                    continue;
                }
            }
            System.out.print(name + "(");
            ArrayList<GslWire> parameters = c.getParameters();
            for (GslWire w : parameters) {
                if (w != parameters.get(parameters.size() - 1)) {
                    System.out.print(" " + w.getParameterString() + ",");
                } else {
                    System.out.print(" " + w.getParameterString() + " ");
                }
            }
            System.out.println(");");
        }

        System.out.println();
    }

    private void translateAssign(GslNode gslNode) {
        System.out.println("assign " + gslNode.getParameters().get(0).getParameterString()
                + " = " + gslNode.getParameters().get(1).getParameterString()
                + ";"
        );
    }

    private void translateModuleFooter() {
        System.out.println("endmodule");
        System.out.println();
    }

    private void translateNode(GslNode gslNode) {
        translateModuleHeader(gslNode);
        translateDeclarations(gslNode);
        if (TRANSLATE_PULL_INTO_ASSIGN) {
            translatePullsToAssign(gslNode);
        } else {
            translatePulls(gslNode);
        }
        translateModuleInstances(gslNode);
        translateModuleFooter();
    }

    private void translate(GslNode gslNode) {
        if (nodeTypeTranslated(gslNode)) return;
        translateNode(gslNode);
        for (GslNode n : gslNode.getChildren()) {
            // AtomicNode should be implemented with a primitive.
            if (!n.getChildren().isEmpty()) {
                translate(n);
            }
        }
    }

    private void translate() {
        translate(topGslNode);
    }

    private void setup() {
        try {
            topGslNode.setupNodeHierarchy();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void translate(Node node) {
        GslVerilogConverter vc = new GslVerilogConverter(node);
        vc.setup();
        vc.translate();
    }

    public static void main(String[] args) {
        Wire a = new Wire();
        Wire b = new Wire();
        Wire cin = new Wire();
        Wire sum = new Wire();
        Wire cout = new Wire();
        FullAdder.inst(sum, cout, a, b, cin).toVerilog();
    }
}
