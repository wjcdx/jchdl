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
package org.jchdl.vc.rtl.cfg;

import jdk.internal.org.objectweb.asm.tree.LookupSwitchInsnNode;
import jdk.internal.org.objectweb.asm.tree.TableSwitchInsnNode;

import java.util.ArrayList;

public class RtlSwitchStmt extends RtlStmt{
    public static final String DEFAULT_LABEL = "default";
    public String target;
    public ArrayList<String> cases = new ArrayList<>(32);
    private TableSwitchInsnNode tableSwitchInsnNode;

    public RtlSwitchStmt(String target, TableSwitchInsnNode tableSwitchInsnNode) {
        super(RTL_STMT_TYPE_SWITCH);
        this.target = target;
        // as the sequence of ControlFlowNode.children
        this.cases.add("default");
        for (int i = 0; i < tableSwitchInsnNode.labels.size(); i++) {
            this.cases.add("" + (i + tableSwitchInsnNode.min));
        }
    }

    public RtlSwitchStmt(String target, LookupSwitchInsnNode lookupSwitchInsnNode) {
        super(RTL_STMT_TYPE_SWITCH);
        this.target = target;
        // as the sequence of ControlFlowNode.children
        this.cases.add(DEFAULT_LABEL);
        for (Integer key : lookupSwitchInsnNode.keys) {
            this.cases.add("" + key);
        }
    }

    public String getCase(int index) {
        return cases.get(index);
    }
}
