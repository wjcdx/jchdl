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
package org.jchdl.vc.rtl.m2;

import javafx.util.Pair;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.LocalVariableNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.analysis.Analyzer;
import jdk.internal.org.objectweb.asm.tree.analysis.AnalyzerException;
import org.jchdl.model.rtl.core.block.Always;
import org.jchdl.model.rtl.core.block.Assign;
import org.jchdl.model.rtl.core.block.Block;
import org.jchdl.model.rtl.core.datatype.Bit;
import org.jchdl.model.rtl.core.datatype.Bits;
import org.jchdl.model.rtl.core.datatype.Reg;
import org.jchdl.model.rtl.core.datatype.Structure;
import org.jchdl.model.rtl.core.meta.Bitable;
import org.jchdl.model.rtl.core.meta.Module;
import org.jchdl.vc.rtl.asm.RtlAnalyzer;
import org.jchdl.vc.rtl.asm.RtlInterpreter;
import org.jchdl.vc.rtl.asm.RtlValue;
import org.jchdl.vc.rtl.cfg.RtlStmt;
import org.jchdl.vc.rtl.m2.block.RtlAlways;
import org.jchdl.vc.rtl.m2.block.RtlAssign;
import org.jchdl.vc.rtl.m2.block.RtlBlock;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class RtlModule {
    private String name;
    private Module modelModule;
    private ClassNode classNode;
    private MethodNode initMethodNode;
    private MethodNode logicMethodNode;
    private LinkedHashMap<String, RtlBitable> bitableFields = new LinkedHashMap<>(32);
    private LinkedHashMap<String, RtlStructure> structureFields = new LinkedHashMap<>(32);
    private ArrayList<RtlBitable> bitableLocals = new ArrayList<>(32); // bitable local variables of method:logic

    /**
     * Input/Output declarations.
     */
    private ArrayList<RtlBitable> interfaces = new ArrayList<>(32);
    /**
     * Parameters provide by parent module to initiate this module.
     */
    private ArrayList<String> parameters = new ArrayList<>(32);
    /**
     * Inner bits & regs: declaration for fields and method:logic local variables.
     */
    private ArrayList<RtlBitable> declarations = new ArrayList<>(32);
    /**
     * Child modules.
     */
    private ArrayList<RtlModule> children = new ArrayList<>(32);
    /**
     * Always/Assign blocks of this module.
     */
    private ArrayList<RtlBlock> blocks = new ArrayList<>(32);

    // there may be multiple statements of a single insn, such as: bool ? a : b
    private ArrayList<Pair<Integer, RtlStmt>> statements = new ArrayList<>(32);

    public RtlModule(Module module) throws IOException {
        this.modelModule = module;
        this.name = getModuleName();
        this.classNode = getClassNode();
        this.initMethodNode = getMethodNode("<init>");
        this.logicMethodNode = getMethodNode("logic");
    }

    private String getModuleName() {
        String name = modelModule.getName();
        if (name == null) {
            name = modelModule.getClass().getSimpleName();
        }
        return name;
    }

    private ClassNode getClassNode() throws IOException {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(modelModule.getClass().getName());
        classReader.accept(classNode, 0);
        return classNode;
    }

    public String getName() {
        return name;
    }

    public Module getModelModule() {
        return modelModule;
    }

    public ArrayList<RtlBitable> getInterfaces() {
        return interfaces;
    }

    public ArrayList<String> getParameters() {
        return parameters;
    }

    public ArrayList<RtlModule> getChildren() {
        return children;
    }

    public ArrayList<RtlBlock> getBlocks() {
        return blocks;
    }

    public ArrayList<RtlBitable> getDeclarations() {
        return declarations;
    }

    public void setupModuleHierarchy() throws IllegalAccessException, IOException {
        fillModuleMetaRecursively(this);
    }

    private void fillModuleMetaRecursively(RtlModule rtlModule) throws IllegalAccessException, IOException {
        rtlModule.fillModuleMeta();
        for (RtlModule n : rtlModule.getChildren()) {
            fillModuleMetaRecursively(n);
        }
    }

    private void fillModuleMeta() throws IllegalAccessException, IOException {
        // fill with instance info
        fillModuleStructureFields();
        fillModuleBitableFields();
        fillModuleBitableLocals();
        fillModuleChildInstances();
        fillModuleBlockInstances();
        // fill with structure info
        fillModuleInterfaces();
        fillModuleDeclarations();
        fillModuleChildParameters();
        fillModuleBlockParameters();
        fillModuleBlockRunnables();
    }

    private void fillModuleStructureFields() throws IllegalAccessException {
        for (Field field : modelModule.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (Structure.class.isAssignableFrom(field.getType())) {
                String dir = ClassUtil.getFieldDirection(field);
                Structure structure = (Structure) field.get(modelModule);
                RtlStructure rtlStructure = new RtlStructure(dir, field.getName(), structure);
                this.structureFields.put(field.getName(), rtlStructure);
                this.structureFields.putAll(rtlStructure.getRtlStructures());
            }
        }
    }

    private void fillModuleBitableFields() throws IllegalAccessException {
        for (Field field : modelModule.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (Bitable.class.isAssignableFrom(field.getType())) {
                RtlBitable rtlBitable = RtlBitable.build(modelModule, field);
                this.bitableFields.put(field.getName(), rtlBitable);
            } else if (Structure.class.isAssignableFrom(field.getType())) {
                RtlStructure rtlStructure = structureFields.get(field.getName());
                this.bitableFields.putAll(rtlStructure.getRtlBitables());
            }
        }
    }

    // bitable local variables of method:logic
    private void fillModuleBitableLocals() {
        for (LocalVariableNode lvn : logicMethodNode.localVariables) {
            Class clazz = ClassUtil.getClassOfDesc(lvn.desc);
            if (clazz == Bit.class || clazz == Bits.class) {
                bitableLocals.add(new RtlBitable(RtlBitable.RTL_BITABLE_TYPE_WIRE, lvn.name, 0));
            } else if (clazz == Reg.class) {
                bitableLocals.add(new RtlBitable(RtlBitable.RTL_BITABLE_TYPE_REG, lvn.name, 0));
            } else {
                bitableLocals.add(null);// place holder
            }
        }
    }

    private void fillModuleChildInstances() throws IOException {
        for (Module module : modelModule.getChildren()) {
            children.add(new RtlModule(module));
        }
    }

    private void fillModuleBlockInstances() {
        for (Block block : modelModule.getBlocks()) {
            if (block instanceof Assign) {
                this.blocks.add(new RtlAssign(block));
            } else if (block instanceof Always) {
                this.blocks.add(new RtlAlways(block));
            }
        }
    }

    // get from constructor parameters
    private void fillModuleInterfaces() {
        for (int i = 0; i < initMethodNode.localVariables.size(); i++) {
            LocalVariableNode lvn = initMethodNode.localVariables.get(i);
            Class clazz = ClassUtil.getClassOfDesc(lvn.desc);
            if (Bitable.class.isAssignableFrom(clazz)) {
                RtlBitable rtlBitable = bitableFields.get(lvn.name);
                if (rtlBitable instanceof RtlPort) {
                    this.interfaces.add(rtlBitable);
                }
            } else if (Structure.class.isAssignableFrom(clazz)) {
                RtlStructure rtlStructure = structureFields.get(lvn.name);
                this.interfaces.addAll(rtlStructure.getRtlBitables().values());
            }
        }
    }

    // local variable declarations within logic.
    // get from:
    // 1. modelModule fields, exclude input/output fields;
    // 2. local variable table of method:logic;
    private void fillModuleDeclarations() throws IllegalAccessException {
        fillFieldDeclarations();
        fillLocalDeclarations();
    }

    // declarations declared as module fields, excludes input/output fields
    private void fillFieldDeclarations() throws IllegalAccessException {
        for (Map.Entry<String, RtlBitable> entry : bitableFields.entrySet()) {
            if (!(entry.getValue() instanceof RtlPort)) {
                this.declarations.add(entry.getValue());
            }
        }
    }

    // declarations declared inside method:logic
    private void fillLocalDeclarations() {
        // local: this
        if (logicMethodNode.localVariables.size() <= 1) {
            return;
        }
        Analyzer<RtlValue> analyzer = new Analyzer<>(
                new RtlInterpreter(this, logicMethodNode, new RtlInterpreter.Callbacks() {
                    @Override
                    public void notifyLocalBitable(int index, int nBits) {
                        RtlBitable rtlBitable = RtlModule.this.bitableLocals.get(index);
                        if (rtlBitable != null) {
                            rtlBitable.setBits(nBits);
                            declarations.add(rtlBitable);
                        }
                    }
                }));
        try {
            analyzer.analyze(classNode.name, logicMethodNode);
        } catch (AnalyzerException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private void fillModuleChildParameters() {
        Analyzer<RtlValue> analyzer = new Analyzer<>(
                new RtlInterpreter(this, logicMethodNode, new RtlInterpreter.Callbacks() {
                    @Override
                    public void notifyChildParameter(int index, String parameter) {
                        RtlModule child = children.get(index);
                        if (RtlStructure.hasPrefix(parameter)) {
                            RtlStructure rtlStructure = structureFields.get(RtlStructure.trimPrefix(parameter));
                            child.parameters.addAll(rtlStructure.getRtlBitables().keySet());
                        } else {
                            child.parameters.add(parameter);
                        }
                    }
                }));

        try {
            analyzer.analyze(classNode.name, logicMethodNode);
        } catch (AnalyzerException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private void fillModuleBlockParameters() {
        Analyzer<RtlValue> analyzer = new Analyzer<>(
                new RtlInterpreter(this, logicMethodNode, new RtlInterpreter.Callbacks() {
                    @Override
                    public void notifyAlwaysEvents(int index, String events) {
                        RtlAlways always = (RtlAlways) blocks.get(index);
                        String[] es = events.split(";");
                        for (String e : es) {
                            always.addEvent(e);
                        }
                    }

                    @Override
                    public void notifyAlwaysRunnable(int index, String methodName) {
                        RtlAlways always = (RtlAlways) blocks.get(index);
                        always.setMethodName(methodName);
                    }

                    @Override
                    public void notifyAssignTarget(int index, String target) {
                        RtlAssign assign = (RtlAssign) blocks.get(index);
                        assign.setTarget(target);
                    }

                    @Override
                    public void notifyAssignSources(int index, String sources) {
                        RtlAssign assign = (RtlAssign) blocks.get(index);
                        String[] ss = sources.split(";");
                        for (String s : ss) {
                            assign.addSource(s);
                        }
                    }

                    @Override
                    public void notifyAssignRunnable(int index, String methodName) {
                        RtlAssign assign = (RtlAssign) blocks.get(index);
                        assign.setMethodName(methodName);
                    }
                }));

        try {
            analyzer.analyze(classNode.name, logicMethodNode);
        } catch (AnalyzerException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private void fillModuleBlockRunnables() {
        for (RtlBlock rtlBlock : blocks) {
            decodeBlock(rtlBlock);
        }
    }

    private void decodeBlock(RtlBlock rtlBlock) {
        MethodNode mn = getMethodNode(rtlBlock.getMethodName());
        assert mn != null;

        if (rtlBlock.getMethodName().equals("updateTempLogic")) {
            System.out.println("stop for debugging");
        }

        RtlAnalyzer<RtlValue> analyzer = new RtlAnalyzer<>(
                new RtlInterpreter(this, mn, new RtlInterpreter.Callbacks() {
                    @Override
                    public void notifyStatement(int insnIndex, RtlStmt stmt) {
                        statements.add(new Pair<>(insnIndex, stmt));
                    }
                }));

        try {
            statements.clear();
            analyzer.analyze(classNode.name, mn);
        } catch (AnalyzerException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        for (Pair<Integer, RtlStmt> pair : statements) {
            analyzer.addStatement(pair.getKey(), pair.getValue());
        }
        analyzer.prepareControlFlowGraph();
        StringBuilder sb = new StringBuilder();
        analyzer.getRootCfgN().visit(sb);
        rtlBlock.setMethodBody(sb.toString());
    }

    private MethodNode getMethodNode(String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals(name)) {
                return methodNode;
            }
        }
        return null;
    }

    public int getFieldWidth(String fieldName) {
        RtlBitable rtlBitable = bitableFields.get(fieldName);
        if (rtlBitable != null) {
            return rtlBitable.nBits();
        }
        return 0;
    }

}
