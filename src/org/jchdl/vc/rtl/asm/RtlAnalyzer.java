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

import org.jchdl.vc.rtl.cfg.*;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.JumpInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.analysis.*;

import java.util.ArrayList;
import java.util.Comparator;

public class RtlAnalyzer<V extends Value> extends Analyzer<V> {

    private InsnList insnList;

    private ArrayList<Integer>[] ancestors;
    /**
     * The successors of the currently analyzed method instructions (one per instruction index).
     */
    private ArrayList<Integer>[] successors;

    private RtlCfgN[] rtlCfgNs;

    private ArrayList<Integer> startPoints = new ArrayList<>(32);

    private int[] nStatements;

    public RtlAnalyzer(Interpreter<V> interpreter) {
        super(interpreter);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void init(String owner, MethodNode method) throws AnalyzerException {
        insnList = method.instructions;
        successors = (ArrayList<Integer>[]) new ArrayList<?>[insnList.size()];
        ancestors = (ArrayList<Integer>[]) new ArrayList<?>[insnList.size()];
        rtlCfgNs = new RtlCfgN[insnList.size()];
        nStatements = new int[insnList.size()];
    }

    @Override
    protected void newControlFlowEdge(int insnIndex, int successorIndex) {
        addSuccessor(insnIndex, successorIndex);
        addAncestor(insnIndex, successorIndex);
        addCfgNEdge(insnIndex, successorIndex);
    }

    @Override
    public Frame<V>[] analyze(String owner, MethodNode method) throws AnalyzerException {
        Frame<V>[] frames = super.analyze(owner, method);
        fillStartPoints();
        mergeBasicBlocks();
        return frames;
    }

    @SuppressWarnings("unchecked")
    private void addSuccessor(int insnIndex, int successorIndex) {
        ArrayList<Integer> list = getSuccessor(insnIndex);
        if (!list.contains(successorIndex)) {
            list.add(successorIndex);
        }
    }

    @SuppressWarnings("unchecked")
    private void addAncestor(int ancestorIndex, int insnIndex) {
        ArrayList<Integer> list = getAncestor(insnIndex);
        if (!list.contains(ancestorIndex)) {
            list.add(ancestorIndex);
        }
    }

    private void addCfgNEdge(int insnIndex, int successorIndex) {
        RtlCfgN i = getCfgN(insnIndex);
        RtlCfgN s = getCfgN(successorIndex);
        i.children.add(s);
    }

    private void fillStartPoints() {
        startPoints.add(0);
        for (int i = 0; i < successors.length; i++) {
            ArrayList<Integer> list = successors[i];
            if (list != null && (list.size() > 1 || isGotoInsn(i))) {
                for (Integer s : list) {
                    if (!startPoints.contains(s)) {
                        startPoints.add(s);
                    }
                }
            }
        }
        startPoints.sort(Comparator.comparingInt(o -> o));
    }

    private void mergeBasicBlocks() {
        RtlCfgN current = null;
        for (int i = 0; i < rtlCfgNs.length; i++) {
            if (current == null || startPoints.contains(i)) {
                current = getCfgN(i);
                continue;
            }
            RtlCfgN n = getCfgN(i);
            current.children.remove(n);
            current.instructions.addAll(n.instructions);
            current.children.addAll(n.children);
            rtlCfgNs[i] = current;
        }
    }

    private void markCfgLevel(RtlCfgN cfgN, int level, ArrayList<RtlCfgN> visitedCfgNs) {
        if (visitedCfgNs.contains(cfgN)) {
            if (level < cfgN.level) {
                cfgN.level = level;
            }
            return;
        }
        visitedCfgNs.add(cfgN);
        cfgN.level = level;
        for (RtlCfgN n : cfgN.children) {
            if (cfgN.hasMatchStmt() && n.hasMatchStmt()) {
                markCfgLevel(n, level, visitedCfgNs);
            } else {
                markCfgLevel(n, level + 1, visitedCfgNs);
            }
        }
    }

    private void markCfgLevels() {
        ArrayList<RtlCfgN> visitedCfgNs = new ArrayList<>(32);
        markCfgLevel(getRootCfgN(), 0, visitedCfgNs);
    }

    public void prepareControlFlowGraph() {
        markCfgLevels();
        markReturnBranch();
        markSiblingBranch();
        markQuoteCfgNs();
        markMatchTopStmts();
    }

    private void markReturnBranch() {
        ArrayList<RtlCfgN> visitedCfgNs = new ArrayList<>(32);
        for (int i = 0; i < rtlCfgNs.length; i++) {
            RtlCfgN cfgN = getCfgN(i);
            if (visitedCfgNs.contains(cfgN)) {
                continue;
            }
            visitedCfgNs.add(cfgN);
            for (int j = 0; j < cfgN.children.size(); j++) {
                RtlCfgN n = cfgN.children.get(j);
                if (hasNoReturnInsn(n.instructions)) {
                    continue;
                }
                n.type = RtlCfgN.TYPE_RETURN;
            }
        }
    }

    /**
     * eg 1:
     * if (bool) {
     * a = 0;
     * }
     * b = a;
     * ---
     * where b = a is not the child of "if (bool)".
     * There's no goto insn to jump over "b = a;" after "a = 0;".
     */
    private boolean oneCondBranchDominating(RtlCfgN left, RtlCfgN right) {
        return left.children.contains(right) && hasNoJumpInsn(left.instructions);
    }

    /**
     * eg 1:
     * if (bool) {
     * a = 0;
     * } else {
     * a = 1;
     * }
     * b = a;
     * ---
     * where b = a is not the child of "if (bool)".
     * There's no goto insn to jump over "b = a;" after "a = 1;".
     */
    private boolean twoCondBranchDominating(RtlCfgN left, RtlCfgN right) {
        if (left.children.size() != 1 || right.children.size() != 1) {
            return false;
        }
        return left.children.get(0) == right.children.get(0);
    }

    private void markCondSiblingBranch() {
        ArrayList<RtlCfgN> visitedCfgNs = new ArrayList<>(32);
        for (int i = 0; i < rtlCfgNs.length; i++) {
            RtlCfgN cfgN = getCfgN(i);
            if (visitedCfgNs.contains(cfgN)) {
                continue;
            }
            visitedCfgNs.add(cfgN);
            if (!cfgN.hasCondStmt()) {
                continue;
            }
            RtlCfgN left = cfgN.children.get(0);
            RtlCfgN right = cfgN.children.get(1);
            if (right.type == RtlCfgN.TYPE_RETURN) {
                continue;
            }
            if (oneCondBranchDominating(left, right)) {
                markCfgNSibling(cfgN, right);
            } else if (twoCondBranchDominating(left, right)) {
                RtlCfgN s = left.children.get(0);
                markCfgNSibling(cfgN, s);
            }
        }
    }

    /**
     * eg 2:
     * switch (a) {
     * case 1:  v = 0; break;
     * case 2:  v = 1; break;
     * }
     * c = 0;
     * ---
     * where c = 0 will be treated as the default case of switch stmt.
     */
    private boolean dftSwitchBranchDominating(RtlCfgN dft, RtlCfgN swt) {
        for (int i = 1; i < swt.children.size(); i++) {
            RtlCfgN c = swt.children.get(i);
            if (c.children.get(0) != dft) {
                return false;
            }
        }
        return true;
    }

    /**
     * eg 2:
     * switch (a) {
     * case 1:  v = 0; break;
     * case 2:  v = 1; break;
     * default: v = 2; break;
     * }
     * c = 0;
     * ---
     * where c = 0 will be marked as the sibling of switch stmt.
     */
    private boolean allSwitchBranchDominating(RtlCfgN dft, RtlCfgN swt) {
        for (int i = 1; i < swt.children.size(); i++) {
            RtlCfgN c = swt.children.get(i);
            if (c.children.size() == 0) {
                System.out.println("");
            }
            if (c.children.get(0) != dft.children.get(0)) {
                return false;
            }
        }
        return true;
    }

    private void markSwitchSiblingBranch() {
        ArrayList<RtlCfgN> visitedCfgNs = new ArrayList<>(32);
        for (int i = 0; i < rtlCfgNs.length; i++) {
            RtlCfgN cfgN = getCfgN(i);
            if (visitedCfgNs.contains(cfgN)) {
                continue;
            }
            visitedCfgNs.add(cfgN);

            if (!cfgN.hasSwitchStmt()) {
                continue;
            }
            RtlCfgN dft = cfgN.children.get(0);
            if (dft.type == RtlCfgN.TYPE_RETURN) {
                continue;
            }
            if (dftSwitchBranchDominating(dft, cfgN)) {
                markCfgNSibling(cfgN, dft);
            } else if (allSwitchBranchDominating(dft, cfgN)) {
                RtlCfgN s = dft.children.get(0);
                markCfgNSibling(cfgN, s);
            }
        }
    }

    private void markCfgNSibling(RtlCfgN cfgN, RtlCfgN sibling) {
        sibling.type = RtlCfgN.TYPE_SIBLING;
        markCfgLevel(sibling, cfgN.level, new ArrayList<>(32));
    }

    private void markSiblingBranch() {
        markCondSiblingBranch();
        markSwitchSiblingBranch();
    }

    private void markQuoteCfgNs() {
        markQuoteExprs();
        markQuoteConds();
    }

    private void markQuoteConds() {
        ArrayList<RtlCfgN> visitedCfgNs = new ArrayList<>(32);
        boolean marked = true;
        while (marked) {
            marked = false;
            visitedCfgNs.clear();
            for (int i = 0; i < rtlCfgNs.length; i++) {
                RtlCfgN cfgN = getCfgN(i);
                if (visitedCfgNs.contains(cfgN)) {
                    continue;
                }
                visitedCfgNs.add(cfgN);

                int nChildren = cfgN.getChildrenNumber();
                for (RtlCfgN n : cfgN.children) {
                    if (n.quote == RtlCfgN.TYPE_QUOTE_NORMAL) {
                        // nothing to do;
                    } else if (n.quote == RtlCfgN.TYPE_QUOTE_COND_EXPR) {
                        if (cfgN.quote == RtlCfgN.TYPE_QUOTE_NORMAL) {
                            cfgN.quote = RtlCfgN.TYPE_QUOTE_COND_TOP;
                            cfgN.quoteAssignStmt = n.quoteAssignStmt;
                            marked = true;
                        }
                    } else if (n.quote == RtlCfgN.TYPE_QUOTE_COND_TOP) {
                        if (n.statements.size() != 1) { // n should have only one condition statement
                            continue;
                        }
                        RtlStmt lastStmt = cfgN.statements.get(cfgN.statements.size() - 1);
                        if (!(lastStmt instanceof RtlCondStmt)) {
                            continue;
                        }
                        if (nChildren < 2) {
                            continue;
                        }
                        cfgN.quote = RtlCfgN.TYPE_QUOTE_COND_TOP;
                        cfgN.quoteAssignStmt = n.quoteAssignStmt;
                        n.quote = RtlCfgN.TYPE_QUOTE_COND_MIDDLE;
                        marked = true;
                    } else if (n.quote == RtlCfgN.TYPE_QUOTE_COND_MIDDLE) {
                        // not valid case.
                    }
                }
            }
        }
    }

    private void markQuoteExprs() {
        for (int i = 0; i < nStatements.length; i++) {
            int nr = nStatements[i];
            if (nr <= 1) continue;

            RtlCfgN cfgN = getCfgN(i);
            assert cfgN.type == RtlCfgN.TYPE_SIBLING;
            // insn:i may reside inside cfgN, and have only one ancestor.
            ArrayList<Integer> ancestorList = ancestors[cfgN.start];
            assert ancestorList != null && ancestorList.size() > 1;
            for (int j = 0; j < ancestorList.size(); j++) {
                // assert: ancestors.size == statements.size
                RtlStmt stmt = cfgN.statements.get(j);
                if (stmt instanceof RtlAssignStmt) {
                    int ancestor = ancestorList.get(j);
                    RtlCfgN n = getCfgN(ancestor);
                    n.quote = RtlCfgN.TYPE_QUOTE_COND_EXPR;
                    n.statements.add(stmt);
                    n.quoteAssignStmt = (RtlAssignStmt) stmt;
                } else if (stmt instanceof RtlCondStmt) {
                    // TODO: handle Bit.inst(!bool) expr.
                    throw new IllegalArgumentException("Bit.inst(!BoolExpr) cannot be handled correctly,  please use " +
                            "Bit.inst(NotBoolExpr) instead. Such as using Reg.ne() instead of !Reg.equals().");
                }
            }
            // remove first n AssignStmts, others left, if any.
            for (int j = 0; j < ancestorList.size(); j++) {
                RtlStmt stmt = cfgN.statements.get(0);
                if (stmt instanceof RtlAssignStmt) {
                    cfgN.statements.remove(stmt);
                }
            }
        }
    }

    private void markMatchTopStmt(RtlCfgN cfgN, ArrayList<RtlCfgN> visitedCfgNs) {
        if (visitedCfgNs.contains(cfgN)) {
            return;
        }
        visitedCfgNs.add(cfgN);

        for (RtlStmt stmt : cfgN.statements) {
            if (stmt instanceof RtlMatchStmt) {
                RtlMatchStmt matchStmt = (RtlMatchStmt) stmt;
                matchStmt.type = RtlMatchStmt.TYPE_TOP;
                visitedCfgNs.addAll(cfgN.children);
                return;
            }
        }

        for (RtlCfgN n : cfgN.children) {
            if (n != null) {
                markMatchTopStmt(n, visitedCfgNs);
            }
        }
    }

    private void markMatchTopStmts() {
        ArrayList<RtlCfgN> visitedCfgNs = new ArrayList<>(32);
        markMatchTopStmt(getRootCfgN(), visitedCfgNs);
    }

    public void addStatement(int insnIndex, RtlStmt statement) {
        RtlCfgN rtlCfgN = getCfgN(insnIndex);
        rtlCfgN.statements.add(statement);
        nStatements[insnIndex]++;
    }

    public RtlCfgN getRootCfgN() {
        return rtlCfgNs[0];
    }

    private ArrayList<Integer> getSuccessor(int insnIndex) {
        ArrayList<Integer> list = successors[insnIndex];
        if (list == null) {
            successors[insnIndex] = new ArrayList<>(8);
            list = successors[insnIndex];
        }
        return list;
    }

    private ArrayList<Integer> getAncestor(int insnIndex) {
        ArrayList<Integer> list = ancestors[insnIndex];
        if (list == null) {
            ancestors[insnIndex] = new ArrayList<>(8);
            list = ancestors[insnIndex];
        }
        return list;
    }

    private RtlCfgN getCfgN(int insnIndex) {
        RtlCfgN cfgN = rtlCfgNs[insnIndex];
        if (cfgN == null) {
            rtlCfgNs[insnIndex] = new RtlCfgN(insnIndex);
            cfgN = rtlCfgNs[insnIndex];
        }
        return cfgN;
    }

    private boolean isGotoInsn(int insnIndex) {
        return insnList.get(insnIndex).getOpcode() == GOTO;
    }

    private boolean hasNoReturnInsn(ArrayList<Integer> insns) {
        for (int i : insns) {
            AbstractInsnNode insn = insnList.get(i);
            if (insn.getOpcode() != -1 && (insn.getOpcode() < IRETURN || insn.getOpcode() > RETURN)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNoJumpInsn(ArrayList<Integer> insns) {
        for (int i : insns) {
            AbstractInsnNode insn = insnList.get(i);
            if (insn instanceof JumpInsnNode) {
                return false;
            }
        }
        return true;
    }

}
