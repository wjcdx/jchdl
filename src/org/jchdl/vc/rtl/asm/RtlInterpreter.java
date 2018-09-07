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
package org.jchdl.vc.rtl.asm;

import org.jchdl.model.rtl.core.block.Always;
import org.jchdl.model.rtl.core.block.Assign;
import org.jchdl.model.rtl.core.datatype.Bit;
import org.jchdl.model.rtl.core.datatype.Bits;
import org.jchdl.model.rtl.core.datatype.Reg;
import org.jchdl.model.rtl.core.event.ChangingEvent;
import org.jchdl.model.rtl.core.event.NegEdgeEvent;
import org.jchdl.model.rtl.core.event.PosEdgeEvent;
import org.jchdl.model.rtl.core.event.expression.Expression;
import org.jchdl.model.rtl.core.meta.Bitable;
import org.jchdl.model.rtl.core.meta.Module;
import org.jchdl.vc.rtl.cfg.*;
import org.jchdl.vc.rtl.m2.RtlModule;
import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.tree.*;
import jdk.internal.org.objectweb.asm.tree.analysis.AnalyzerException;
import jdk.internal.org.objectweb.asm.tree.analysis.Interpreter;

import java.util.List;

public class RtlInterpreter extends Interpreter<RtlValue> implements Opcodes {
    private int nModule = 0;
    private int nBlock = 0;

    private RtlModule rtlModule;
    private MethodNode methodNode;
    private Callbacks callbacks;

    public RtlInterpreter(RtlModule rtlModule, MethodNode methodNode, Callbacks callbacks) {
        super(ASM5);
        this.rtlModule = rtlModule;
        this.methodNode = methodNode;
        this.callbacks = callbacks;
    }

    /**
     * Constructs a new {@link Interpreter}.
     *
     * @param api the ASM API version supported by this interpreter. Must be one of {@link
     *            Opcodes#ASM4}, {@link Opcodes#ASM5}, {@link
     *            Opcodes#ASM6} or {@link Opcodes#ASM7_EXPERIMENTAL}.
     */
    protected RtlInterpreter(int api) {
        super(api);
    }

    @Override
    public RtlValue newValue(Type type) {
        if (type == Type.VOID_TYPE) {
            return null;
        }
        return new RtlValue(type, RtlValue.UNINITIALIZED_VALUE);
    }

    @Override
    public RtlValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
        switch (insn.getOpcode()) {
            case ACONST_NULL:
                return newValue(null);
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
                return new RtlValue(insn.getOpcode() - ICONST_0);
            case LCONST_0:
            case LCONST_1:
                return new RtlValue((long) (insn.getOpcode() - LCONST_0));
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
                throw new AnalyzerException(insn, "float operations not supported");
            case DCONST_0:
            case DCONST_1:
                throw new AnalyzerException(insn, "double operations not supported");
            case BIPUSH:
            case SIPUSH:
                return new RtlValue(((IntInsnNode) insn).operand);
            case LDC:
                Object value = ((LdcInsnNode) insn).cst;
                if (value instanceof Integer) {
                    return new RtlValue((Integer) value);
                } else if (value instanceof Float) {
                    throw new AnalyzerException(insn, "float operations not supported");
                } else if (value instanceof Long) {
                    return new RtlValue((Long) value);
                } else if (value instanceof Double) {
                    throw new AnalyzerException(insn, "double operations not supported");
                } else if (value instanceof String) {
                    return new RtlValue(Type.getObjectType("java/lang/String"), (String) value);
                } else if (value instanceof Type) {
                    int sort = ((Type) value).getSort();
                    if (sort == Type.OBJECT || sort == Type.ARRAY) {
                        return newValue(Type.getObjectType("java/lang/Class"));
                    } else if (sort == Type.METHOD) {
                        return newValue(Type.getObjectType("java/lang/invoke/MethodType"));
                    } else {
                        throw new AnalyzerException(insn, "Illegal LDC value " + value);
                    }
                } else if (value instanceof Handle) {
                    return newValue(Type.getObjectType("java/lang/invoke/MethodHandle"));
//                } else if (value instanceof ConstantDynamic) {
//                    return newValue(Type.getType(((ConstantDynamic) value).getDescriptor()));
                } else {
                    throw new AnalyzerException(insn, "Illegal LDC value " + value);
                }
            case JSR:
                throw new AnalyzerException(insn, "JSR instruction not supported");
            case GETSTATIC:
                FieldInsnNode fin = (FieldInsnNode) insn;
                Class ownerClass = getClass(fin.owner);
                if (Bit.class.isAssignableFrom(ownerClass)) {
                    if (fin.name.equals("B0")) {
                        return new RtlValue(Type.getType(fin.desc), "0");
                    } else if (fin.name.equals("B1")) {
                        return new RtlValue(Type.getType(fin.desc), "1");
                    }
                }
                return new RtlValue(Type.getType(fin.desc), fin.name);
            case NEW:
                return newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
            default:
                throw new AssertionError();
        }
    }

    @Override
    public RtlValue copyOperation(AbstractInsnNode insn, RtlValue value) throws AnalyzerException {
        switch (insn.getOpcode()) {
            case ILOAD:
            case ISTORE:
                return value;
            case FLOAD:
            case FSTORE:
                throw new AnalyzerException(insn, "float operations not supported");
            case LLOAD:
            case LSTORE:
                return value;
            case DLOAD:
            case DSTORE:
                throw new AnalyzerException(insn, "double operations not supported");
            case ALOAD:
                int i = ((VarInsnNode) insn).var;
                String name = methodNode.localVariables.get(i).name;
                value.setExpr(value.getExpr().replace(RtlValue.UNINITIALIZED_VALUE, name));
                return value;
            case ASTORE:
                i = ((VarInsnNode) insn).var;
                name = methodNode.localVariables.get(i).name;
                value.setExpr(value.getExpr().replace(RtlValue.UNINITIALIZED_VALUE, name));
                callbacks.notifyLocalBitable(i, value.nBits);
                return value;
            default:
                return value;
        }
    }

    @Override
    public RtlValue unaryOperation(AbstractInsnNode insn, RtlValue value) throws AnalyzerException {
        switch (insn.getOpcode()) {
            case INEG:
                value.setExpr("-" + value.getExpr());
                return value;
            case IINC:
                value.setExpr(value.getExpr() + "+1");
                return value;
            case L2I:
            case F2I:
            case D2I:
            case I2B:
            case I2C:
            case I2S:
                return value;
            case FNEG:
                throw new AnalyzerException(insn, "float/double operations not supported");
            case I2F:
            case L2F:
                return value;
            case D2F:
                throw new AnalyzerException(insn, "float/double operations not supported");
            case LNEG:
                value.setExpr("-" + value.getExpr());
                return value;
            case I2L:
                return value;
            case F2L:
            case D2L:
            case DNEG:
            case I2D:
            case L2D:
            case F2D:
                throw new AnalyzerException(insn, "float/double operations not supported");
            case IFEQ: // match the sequence of method:traverseControlFlowGraph.
                if (value.getExpr().indexOf("MATCH") == 0) {
                    RtlMatchStmt stmt = new RtlMatchStmt();
                    String[] parts = value.getExpr().split(":");
                    assert parts.length == 3;
                    stmt.target = parts[1];
                    for (String m : parts[2].split(";")) {
                        if (m.length() > 0) {
                            stmt.matches.add(m);
                        }
                    }
                    callbacks.notifyStatement(getInsnIndex(insn), stmt);
                } else {
                    RtlStmt stmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFEQ,
                            value.getExpr(), "0");
                    callbacks.notifyStatement(getInsnIndex(insn), stmt);
                }

                return null;
            case IFNE:
                RtlStmt stmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFNE,
                        value.getExpr(), "0");
                callbacks.notifyStatement(getInsnIndex(insn), stmt);
                return null;
            case IFLT:
                stmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFLT,
                        value.getExpr(), "0");
                callbacks.notifyStatement(getInsnIndex(insn), stmt);
                return null;
            case IFGE:
                stmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFGE,
                        value.getExpr(), "0");
                callbacks.notifyStatement(getInsnIndex(insn), stmt);
                return null;
            case IFGT:
                stmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFGT,
                        value.getExpr(), "0");
                callbacks.notifyStatement(getInsnIndex(insn), stmt);
                return null;
            case IFLE:
                stmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFLE,
                        value.getExpr(), "0");
                callbacks.notifyStatement(getInsnIndex(insn), stmt);
                return null;
            case TABLESWITCH:
                stmt = new RtlSwitchStmt(value.getExpr(), (TableSwitchInsnNode) insn);
                callbacks.notifyStatement(getInsnIndex(insn), stmt);
                return null;
            case LOOKUPSWITCH:
                stmt = new RtlSwitchStmt(value.getExpr(), (LookupSwitchInsnNode) insn);
                callbacks.notifyStatement(getInsnIndex(insn), stmt);
                return null;
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case PUTSTATIC:
                return null;
            case GETFIELD: // stack: objref
                RtlValue v = null;
                FieldInsnNode fin = (FieldInsnNode) insn;
                Class ownerClass = getClass(fin.owner);
                if (Module.class.isAssignableFrom(ownerClass)) {
                    v = new RtlValue(Type.getType(fin.desc), fin.name);
                    v.nBits = rtlModule.getFieldWidth(fin.name);
                } else if (Bit.class.isAssignableFrom(ownerClass)) {
                    if (fin.name.equals("value")) {
                        v = new RtlValue(Type.getType(fin.desc), value.getExpr());
                    }
                }
                return v;
            case NEWARRAY:
                switch (((IntInsnNode) insn).operand) {
                    case T_BOOLEAN:
                        return newValue(Type.getType("[Z"));
                    case T_CHAR:
                        return newValue(Type.getType("[C"));
                    case T_BYTE:
                        return newValue(Type.getType("[B"));
                    case T_SHORT:
                        return newValue(Type.getType("[S"));
                    case T_INT:
                        return newValue(Type.getType("[I"));
                    case T_FLOAT:
                        return newValue(Type.getType("[F"));
                    case T_DOUBLE:
                        return newValue(Type.getType("[D"));
                    case T_LONG:
                        return newValue(Type.getType("[J"));
                    default:
                        break;
                }
                throw new AnalyzerException(insn, "Invalid array type");
            case ANEWARRAY:
                return newValue(Type.getType("[" + Type.getObjectType(((TypeInsnNode) insn).desc)));
            case ARRAYLENGTH:
                throw new AnalyzerException(insn, "arraylength instruction not supported");
            case ATHROW:
                return null;
            case CHECKCAST:
                return newValue(Type.getObjectType(((TypeInsnNode) insn).desc));
            case INSTANCEOF:
                throw new AnalyzerException(insn, "instanceof instruction not supported");
            case MONITORENTER:
            case MONITOREXIT:
            case IFNULL:
            case IFNONNULL:
                return null;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public RtlValue binaryOperation(AbstractInsnNode insn, RtlValue value1, RtlValue value2) throws AnalyzerException {
        switch (insn.getOpcode()) {
            case IALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "[" + value2.getExpr() + "]");
            case IADD:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "+" + value2.getExpr());
            case ISUB:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "-" + value2.getExpr());
            case IMUL:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "*" + value2.getExpr());
            case IDIV:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "/" + value2.getExpr());
            case IREM:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "%" + value2.getExpr());
            case ISHL:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "<<" + value2.getExpr());
            case ISHR:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + ">>" + value2.getExpr());
            case IUSHR:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + ">>" + value2.getExpr());
            case IAND:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "&" + value2.getExpr());
            case IOR:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "|" + value2.getExpr());
            case IXOR:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "^" + value2.getExpr());
            case FALOAD:
            case FADD:
            case FSUB:
            case FMUL:
            case FDIV:
            case FREM:
                throw new AnalyzerException(insn, "float/double operations not supported");
            case LALOAD:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "[" + value2.getExpr() + "]");
            case LADD:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "+" + value2.getExpr());
            case LSUB:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "-" + value2.getExpr());
            case LMUL:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "*" + value2.getExpr());
            case LDIV:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "/" + value2.getExpr());
            case LREM:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "%" + value2.getExpr());
            case LSHL:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "<<" + value2.getExpr());
            case LSHR:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + ">>" + value2.getExpr());
            case LUSHR:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + ">>" + value2.getExpr());
            case LAND:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "&" + value2.getExpr());
            case LOR:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "|" + value2.getExpr());
            case LXOR:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "^" + value2.getExpr());
            case DALOAD:
            case DADD:
            case DSUB:
            case DMUL:
            case DDIV:
            case DREM:
                throw new AnalyzerException(insn, "float/double operations not supported");
            case AALOAD:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "[" + value2.getExpr() + "]");
            case LCMP:
                return new RtlValue(Type.INT_TYPE, value1.getExpr() + "==" + value2.getExpr());
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG:
                throw new AnalyzerException(insn, "float/double operations not supported");
            case IF_ICMPEQ: // match the sequence of method:traverseControlFlowGraph
                RtlCondStmt condStmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFEQ,
                        value1.getExpr(), value2.getExpr());
                callbacks.notifyStatement(getInsnIndex(insn), condStmt);
                return null;
            case IF_ICMPNE:
                condStmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFNE,
                        value1.getExpr(), value2.getExpr());
                callbacks.notifyStatement(getInsnIndex(insn), condStmt);
                return null;
            case IF_ICMPLT:
                condStmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFLT,
                        value1.getExpr(), value2.getExpr());
                callbacks.notifyStatement(getInsnIndex(insn), condStmt);
                return null;
            case IF_ICMPGE:
                condStmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFGE,
                        value1.getExpr(), value2.getExpr());
                callbacks.notifyStatement(getInsnIndex(insn), condStmt);
                return null;
            case IF_ICMPGT:
                condStmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFGT,
                        value1.getExpr(), value2.getExpr());
                callbacks.notifyStatement(getInsnIndex(insn), condStmt);
                return null;
            case IF_ICMPLE:
                condStmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFLE,
                        value1.getExpr(), value2.getExpr());
                callbacks.notifyStatement(getInsnIndex(insn), condStmt);
                return null;
            case IF_ACMPEQ:
                condStmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFEQ,
                        value1.getExpr(), value2.getExpr());
                callbacks.notifyStatement(getInsnIndex(insn), condStmt);
                return null;
            case IF_ACMPNE:
                condStmt = new RtlCondStmt(RtlCondStmt.RTL_COND_STMT_IFNE,
                        value1.getExpr(), value2.getExpr());
                callbacks.notifyStatement(getInsnIndex(insn), condStmt);
                return null;
            case PUTFIELD:
                return null;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public RtlValue ternaryOperation(AbstractInsnNode insn, RtlValue value1, RtlValue value2, RtlValue value3) throws AnalyzerException {
        switch (insn.getOpcode()) {
            case IASTORE:
                break;
            case BASTORE:
                break;
            case CASTORE:
                break;
            case SASTORE:
                break;
            case LASTORE:
                break;
            case FASTORE:
            case DASTORE:
                throw new AnalyzerException(insn, "float/double operations not supported");
            case AASTORE: // arrayref, index, value
                String a = value1.getExpr();
                if (a.equals(RtlValue.UNINITIALIZED_VALUE)) {
                    value1.setExpr(value3.getExpr());
                } else {
                    value1.setExpr(a + ";" + value3.getExpr()); // concat events into a string
                }
                break;
            default:
                throw new AssertionError();
        }
        return null;
    }

    /**
     * Interprets a bytecode instruction with a variable number of arguments. This method is called
     * for the following opcodes:
     *
     * <p>INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE, MULTIANEWARRAY and
     * INVOKEDYNAMIC
     *
     * @param insn   the bytecode instruction to be interpreted.
     * @param values the arguments of the instruction to be interpreted.
     * @return the result of the interpretation of the given instruction.
     * @throws AnalyzerException if an error occurred during the interpretation.
     */
    @Override
    public RtlValue naryOperation(AbstractInsnNode insn, List<? extends RtlValue> values) throws AnalyzerException {
        int opcode = insn.getOpcode();
        if (opcode == MULTIANEWARRAY) {
            return newValue(Type.getType(((MultiANewArrayInsnNode) insn).desc));
        } else if (opcode == INVOKEDYNAMIC) {
            InvokeDynamicInsnNode dyn = (InvokeDynamicInsnNode) insn;
            Object handle = dyn.bsmArgs[1];
            if (handle instanceof Handle) {
                Type type = Type.getReturnType(dyn.desc);
                Class returnClass = getClass(type);
                String expr = ((Handle) handle).getName();
                if (returnClass == Expression.class) {
                    expr = "expr:" + expr;
                }
                return new RtlValue(Type.getReturnType(dyn.desc), expr);
            } else {
                return newValue(Type.getReturnType(dyn.desc));
            }
        } else if (opcode == INVOKESTATIC) {
            MethodInsnNode min = (MethodInsnNode) insn;
            Type type = Type.getReturnType(min.desc);
            switch (min.name) {
                case "from":// Bits.from
                    return values.get(0);
                case "to": // Reg.to(Bit), Reg.to(Bits)
                    return values.get(0);
                case "inst":
                    Class ownerClass = getClass(min.owner);
                    if (ownerClass == Bit.class) { // Bit.inst(set), stack: set
                        RtlValue set = values.get(0);
                        RtlValue v = new RtlValue(type, "(" + set.getExpr() + ")");
                        v.nBits = 1;
                        return v;
                    } else if (ownerClass == Bits.class || ownerClass == Reg.class) {// stack: nBits, [initValue]
                        int nBits = Integer.parseInt(values.get(0).getExpr());
                        int initValue = 0;
                        if (values.size() == 2) {
                            initValue = Integer.parseInt(values.get(1).getExpr());
                        }
                        RtlValue v = new RtlValue(type, String.format("%d'h%02x", nBits, initValue));
                        v.nBits = nBits;
                        return v;
                    } else if (Module.class.isAssignableFrom(ownerClass)) {
                        // skip first parameter: parent
                        for (int i = 1; i < values.size(); i++) {
                            callbacks.notifyChildParameter(nModule, values.get(i).getExpr());
                        }
                        nModule++;
                    }
                    break;
                case "of":
                    ownerClass = getClass(min.owner);
                    if (ownerClass == ChangingEvent.class) { // stack: bitable|bitables
                        return new RtlValue(type, values.get(0).getExpr()); // do not change, to be used by method:when
                    } else if (ownerClass == PosEdgeEvent.class) { // stack: bit
                        return new RtlValue(type, "posedge " + values.get(0).getExpr());
                    } else if (ownerClass == NegEdgeEvent.class) { // stack: bit
                        return new RtlValue(type, "negedge " + values.get(0).getExpr());
                    } else if (ownerClass == Bits.class) { // stack: bit
                        return new RtlValue(type, values.get(0).getExpr());
                    }
                    break;
                default:
                    break;
            }
            return newValue(type);
        } else {
            MethodInsnNode min = (MethodInsnNode) insn;
            Type type = Type.getReturnType(min.desc);
            RtlValue objref = values.get(0);
            switch (min.name) {
                case "bit": //vec.bit(0)
                    int nBits = objref.nBits;
                    String n = values.get(1).getExpr();
                    if (nBits > 1) {
                        return new RtlValue(type, objref.getExpr() + "[" + n + "]");
                    }
                    return objref;
                case "bits": // stack: objref, [msb, lsb]
                    if (values.size() == 3) {
                        return new RtlValue(type, objref.getExpr() + "["
                                + values.get(1).getExpr()
                                + ":"
                                + values.get(2).getExpr()
                                + "]");
                    } else {
                        return objref;
                    }
                case "part":
                    Class ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) {
                        String bit = values.get(0).getExpr();
                        if (values.size() == 2) { // stack: objref, msb
                            String msb = values.get(1).getExpr();
                            return new RtlValue(type, bit + "[" + msb + "]");
                        } else if (values.size() == 3) { // stack: objref, msb, lsb
                            String msb = values.get(1).getExpr();
                            String lsb = values.get(2).getExpr();
                            return new RtlValue(type, bit + "[" + msb + ":" + lsb + "]");
                        }
                    }
                    return newValue(type);
                case "boolVal":
                    return objref;
                case "intVal": // stack: objref, msb, lsb
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) {
                        if (values.size() == 3) {
                            String msb = values.get(1).getExpr();
                            String lsb = values.get(2).getExpr();
                            return new RtlValue(type, objref.getExpr() + "[" + msb + ":" + lsb + "]");
                        } else {
                            return objref;
                        }
                    } else { // stack: objref,
                        return objref;
                    }

                case "longVal": // stack: objref, msb, lsb
                    if (values.size() == 3) {
                        String msb = values.get(1).getExpr();
                        String lsb = values.get(2).getExpr();
                        return new RtlValue(type, objref.getExpr() + "[" + msb + ":" + lsb + "]");
                    } else {
                        return objref;
                    }
                case "concat": // stack: objref, bitable|bitables
                    return new RtlValue(type, "{ "
                            + objref.getExpr() + ", "
                            + values.get(1).getExpr().replace(";", ", ")
                            + " }"
                    );
                case "<init>":
                    // Stack before <init> call: ..., objref, objref, arg1, arg2
                    // The first objref is pushed by new, and the second by dup.
                    // By reuse a single RtlValue object, we can change the expr
                    // of the first objref, by changing the expr of the second objref.
                    ownerClass = getClass(min.owner);
                    if (ownerClass == Bit.class) {
                        objref.nBits = 1;
                        return objref;
                    } else if (ownerClass == Bits.class || ownerClass == Reg.class) {
                        objref.nBits = Integer.parseInt(values.get(1).getExpr());
                        return objref;
                    } else if (Module.class.isAssignableFrom(ownerClass)) {
                        // skip first two parameters: objref, parent
                        for (int i = 2; i < values.size(); i++) {
                            callbacks.notifyChildParameter(nModule, values.get(i).getExpr());
                        }
                        nModule++;
                        return objref;
                    }
                    break;
                case "eq": // stack: objref, arg0
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) {
                        return new RtlValue(type, "(" + objref.getExpr() + "==" + values.get(1).getExpr() + ")");
                    }
                    break;
                case "ne": // stack: objref, arg0
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) {
                        return new RtlValue(type, "(" + objref.getExpr() + "!=" + values.get(1).getExpr() + ")");
                    }
                    break;
                case "lt": // stack: objref, arg0
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) {
                        return new RtlValue(type, "(" + objref.getExpr() + "<" + values.get(1).getExpr() + ")");
                    }
                    break;
                case "le": // stack: objref, arg0
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) {
                        return new RtlValue(type, "(" + objref.getExpr() + "<=" + values.get(1).getExpr() + ")");
                    }
                    break;
                case "gt": // stack: objref, arg0
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) {
                        return new RtlValue(type, "(" + objref.getExpr() + ">" + values.get(1).getExpr() + ")");
                    }
                    break;
                case "ge": // stack: objref, arg0
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) {
                        return new RtlValue(type, "(" + objref.getExpr() + ">=" + values.get(1).getExpr() + ")");
                    }
                    break;
                case "and": // stack: objref, arg0
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) {
                        return new RtlValue(type, objref.getExpr() + "&" + values.get(1).getExpr());
                    }
                    break;
                case "or":
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) { // stack: objref, arg0
                        return new RtlValue(type, objref.getExpr() + "|" + values.get(1).getExpr());
                    } else if (Always.class.isAssignableFrom(ownerClass)) { // stack: objref, event
                        callbacks.notifyAlwaysEvents(nBlock, values.get(1).getExpr());
                    }
                    break;
                case "xor": // stack: objref, arg0
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) {
                        return new RtlValue(type, objref.getExpr() + "^" + values.get(1).getExpr());
                    }
                    break;
                case "not": // stack: objref
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) {
                        return new RtlValue(type, "(~(" + objref.getExpr() + "))");
                    }
                    break;
                case "add": // stack: objref, arg0
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) {
                        return new RtlValue(type, objref.getExpr() + "+" + values.get(1).getExpr());
                    }
                    break;
                case "when": // stack: objref, events
                    ownerClass = getClass(min.owner);
                    if (Module.class.isAssignableFrom(ownerClass)) {
                        callbacks.notifyAlwaysEvents(nBlock, values.get(1).getExpr());
                    }
                    break;
                case "run"://stack: objref, methodName
                    ownerClass = getClass(min.owner);
                    if (Always.class.isAssignableFrom(ownerClass)) {
                        callbacks.notifyAlwaysRunnable(nBlock, values.get(1).getExpr());
                        nBlock++;
                    }
                    break;
                case "assign": // stack: objref, bit|bits
                    ownerClass = getClass(min.owner);
                    if (Module.class.isAssignableFrom(ownerClass)) {
                        callbacks.notifyAssignTarget(nBlock, values.get(1).getExpr());
                    } else if (Bitable.class.isAssignableFrom(ownerClass)) { // stack: objref, bitable
                        RtlAssignStmt stmt = new RtlAssignStmt(RtlAssignStmt.STMT_MODIFIER_ASSIGN,
                                objref.getExpr(), values.get(1).getExpr());
                        callbacks.notifyStatement(getInsnIndex(insn), stmt);
                    }
                    break;
                case "from"://stack: objref, bitables|expr
                    ownerClass = getClass(min.owner);
                    if (Assign.class.isAssignableFrom(ownerClass)) {
                        callbacks.notifyAssignSources(nBlock, values.get(1).getExpr());
                    }
                    break;
                case "with"://stack: objref, methodName
                    ownerClass = getClass(min.owner);
                    if (Assign.class.isAssignableFrom(ownerClass)) {
                        callbacks.notifyAssignRunnable(nBlock, values.get(1).getExpr());
                        nBlock++;
                    }
                    break;
                case "set":
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) { // stack: objref, bitable|iconst
                        RtlAssignStmt stmt = new RtlAssignStmt(RtlAssignStmt.STMT_MODIFIER_SET,
                                objref.getExpr(), values.get(1).getExpr());
                        callbacks.notifyStatement(getInsnIndex(insn), stmt);
                    }
                    break;
                case "match":
                    ownerClass = getClass(min.owner);
                    if (Bitable.class.isAssignableFrom(ownerClass)) { // stack: objref, matches
                        return new RtlValue(type, "MATCH:" + objref.getExpr() + ":" + values.get(1).getExpr());
                    }
                    break;
                default:
                    break;
            }
            return newValue(type);
        }
    }

    @Override
    public void returnOperation(AbstractInsnNode insn, RtlValue value, RtlValue expected) throws AnalyzerException {
        // nothing to do
    }

    @Override
    public RtlValue merge(RtlValue value1, RtlValue value2) {
        if (!value1.equals(value2)) {
            return value2;
        }
        return value1;
    }

    private static Class<?> getClass(String desc) {
        try {
            return Class.forName(desc.replace('/', '.'));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private static Class<?> getClass(Type t) {
        if (t.getSort() == Type.OBJECT) {
            return getClass(t.getInternalName());
        }
        return getClass(t.getDescriptor());
    }

    private int getInsnIndex(AbstractInsnNode insnNode) {
        return methodNode.instructions.indexOf(insnNode);
    }

    public interface Callbacks {
        default void notifyLocalBitable(int index, int nBits) {
        }

        default void notifyChildParameter(int index, String parameter) {
        }

        //events: separated with ';'.
        default void notifyAlwaysEvents(int index, String events) {
        }

        default void notifyAlwaysRunnable(int index, String methodName) {
        }

        // 1. ChangingEvent.of: bitable,
        // 2. ExprEvent.of: expr:expression
        default void notifyAssignTarget(int index, String target) {
        }

        default void notifyAssignSources(int index, String sources) {
        }

        default void notifyAssignRunnable(int index, String methodName) {
        }

        default void notifyStatement(int insnIndex, RtlStmt statement) {
        }
    }

    public class RtlCallbacks implements Callbacks {

    }
}
