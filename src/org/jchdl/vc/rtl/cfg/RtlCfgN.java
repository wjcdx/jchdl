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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

// control flow graph node
public class RtlCfgN {
    public static final int TYPE_QUOTE_NORMAL = 0x00;
    public static final int TYPE_QUOTE_COND_TOP = 0x01;
    public static final int TYPE_QUOTE_COND_MIDDLE = 0x02;
    public static final int TYPE_QUOTE_COND_EXPR = 0x03;

    public static final int TYPE_CHILD = 0x00;
    public static final int TYPE_SIBLING = 0x01;
    public static final int TYPE_RETURN = 0x02;

    public int start; // the index of first insn
    public ArrayList<Integer> instructions = new ArrayList<>(32);
    public ArrayList<RtlStmt> statements = new ArrayList<>(8);
    // size=2: the last statement of parent node is a conditional statement.
    // size>2: the last statement of parent node is a switch statement.
    public ArrayList<RtlCfgN> children = new ArrayList<>(32);
    public int level; // least nested level can be visited, for wide-first-visit. default to 0.
    public int quote;
    public RtlAssignStmt quoteAssignStmt;
    public int type;

    public RtlCfgN(int start) {
        this.start = start;
        instructions.add(start);
        this.level = 0;
        this.quote = TYPE_QUOTE_NORMAL;
    }


    @SuppressWarnings("unchecked")
    public void visit(StringBuilder sb) {
        String prefix = getStatementPrefix();
        for (RtlStmt stmt : statements) {
            if (stmt instanceof RtlCondStmt) { // assert: cond stmt should be the last stmt
                visitCondStmt(sb, prefix, (RtlCondStmt) stmt);
            } else if (stmt instanceof RtlSwitchStmt) {
                visitSwitchStmt(sb, prefix, (RtlSwitchStmt) stmt);
            } else if (stmt instanceof RtlAssignStmt) {
                visitAssignStmt(sb, prefix, (RtlAssignStmt) stmt);
            } else if (stmt instanceof RtlMatchStmt) {
                visitMatchStmt(sb, prefix, (RtlMatchStmt) stmt);
            } else {
                sb.append(prefix).append(stmt.sentence).append("\n");
            }
        }
        for (RtlCfgN n : children) {
            if (shouldVisitSibling(n)) {
                n.visit(sb);
            }
        }
    }

    private void visitCondStmt(StringBuilder sb, String prefix, RtlCondStmt stmt) {
        if (quote == RtlCfgN.TYPE_QUOTE_NORMAL) {
            visitCondNormal(sb, prefix, stmt);
        } else if (quote == RtlCfgN.TYPE_QUOTE_COND_TOP) {
            visitCondQuoteTop(sb, prefix, stmt);
        } else if (quote == RtlCfgN.TYPE_QUOTE_COND_MIDDLE) {
            visitCondQuoteMiddle(sb, prefix, stmt);
        }
    }

    private void visitCondNormal(StringBuilder sb, String prefix, RtlCondStmt stmt) {
        // using negative judgement
        if (stmt.valueRight.equals("0") && (stmt.condition == RtlCondStmt.RTL_COND_STMT_IFEQ)) {
            sb.append(prefix).append("if (")
                    .append(stmt.valueLeft)
                    .append(") begin\n");
        } else if (stmt.valueRight.equals("0") && (stmt.condition == RtlCondStmt.RTL_COND_STMT_IFNE)) {
            sb.append(prefix).append("if (!(")
                    .append(stmt.valueLeft)
                    .append(")) begin\n");
        } else {
            sb.append(prefix).append("if (")
                    .append(stmt.valueLeft)
                    .append(stmt.getNegCondExpr())
                    .append(stmt.valueRight)
                    .append(") begin\n");
        }

        RtlCfgN negTrueBranch = children.get(0);
        RtlCfgN negFalseBranch = children.get(1);
        if (shouldVisitChild(negTrueBranch)) {
            // child 0 is the true branch of negative judgement
            negTrueBranch.visit(sb);
        }
        sb.append(prefix).append("end\n");

        if (shouldVisitChild(negFalseBranch)) {
            sb.append(prefix).append("else begin\n");
            // child 1 is the false branch of negative judgement
            negFalseBranch.visit(sb); // true branch
            sb.append(prefix).append("end\n");
        }
    }

    private void visitCondQuoteTop(StringBuilder sb, String prefix, RtlCondStmt stmt) {
        sb.append(prefix).append(quoteAssignStmt.modifier).append(" ")
                .append(quoteAssignStmt.target).append(" = ");
        // using negative judgement
        if (stmt.valueRight.equals("0") && (stmt.condition == RtlCondStmt.RTL_COND_STMT_IFEQ)) {
            sb.append("(")
                    .append(stmt.valueLeft)
                    .append(") ? (");
        } else if (stmt.valueRight.equals("0") && (stmt.condition == RtlCondStmt.RTL_COND_STMT_IFNE)) {
            sb.append("(!(")
                    .append(stmt.valueLeft)
                    .append(")) ? (");
        } else {
            sb.append("(")
                    .append(stmt.valueLeft)
                    .append(stmt.getNegCondExpr())
                    .append(stmt.valueRight)
                    .append(") ? (");
        }

        RtlCfgN negTrueBranch = children.get(0);
        RtlCfgN negFalseBranch = children.get(1);
        if (shouldVisitChild(negTrueBranch)) {
            // child 0 is the true branch of negative judgement
            negTrueBranch.visit(sb);
        }
        sb.append(") ");
        if (shouldVisitChild(negFalseBranch)) {
            sb.append(": (");
            // child 1 is the false branch of negative judgement
            negFalseBranch.visit(sb); // true branch
            sb.append(");\n");
        }
    }

    private void visitCondQuoteMiddle(StringBuilder sb, String prefix, RtlCondStmt stmt) {
        // using negative judgement
        if (stmt.valueRight.equals("0") && (stmt.condition == RtlCondStmt.RTL_COND_STMT_IFEQ)) {
            sb.append("(")
                    .append(stmt.valueLeft)
                    .append(") ? (");
        } else if (stmt.valueRight.equals("0") && (stmt.condition == RtlCondStmt.RTL_COND_STMT_IFNE)) {
            sb.append("(!(")
                    .append(stmt.valueLeft)
                    .append(")) ? (");
        } else {
            sb.append("(")
                    .append(stmt.valueLeft)
                    .append(stmt.getNegCondExpr())
                    .append(stmt.valueRight)
                    .append(") ? (");
        }

        RtlCfgN negTrueBranch = children.get(0);
        RtlCfgN negFalseBranch = children.get(1);
        if (shouldVisitChild(negTrueBranch)) {
            // child 0 is the true branch of negative judgement
            negTrueBranch.visit(sb);
        }
        sb.append(") ");
        if (shouldVisitChild(negFalseBranch)) {
            sb.append(": (");
            // child 1 is the false branch of negative judgement
            negFalseBranch.visit(sb); // true branch
            sb.append(")");
        }
    }

    private void visitMatchStmt(StringBuilder sb, String prefix, RtlMatchStmt stmt) {
        if (stmt.type == RtlMatchStmt.TYPE_TOP) {
            sb.append(prefix).append("case (").append(stmt.target).append(")\n");
        }

        for (int i = 0; i < stmt.matches.size(); i++) {
            String m = stmt.matches.get(i);
            sb.append(prefix).append(getBitLiteral(m));
            if (i == stmt.matches.size() - 1) {
                sb.append(":\n");
            } else {
                sb.append(",\n");
            }
        }

        RtlCfgN leftBranch = children.get(0);
        RtlCfgN rightBranch = children.get(1);
        if (shouldVisitChild(leftBranch)) {
            leftBranch.visit(sb);
        }

        if (shouldVisitMatch(rightBranch)) {
            if (!rightBranch.hasMatchStmt()) {
                sb.append(prefix).append(RtlSwitchStmt.DEFAULT_LABEL).append(":\n");
            }
            rightBranch.visit(sb);
        }
    }

    private void visitSwitchStmt(StringBuilder sb, String prefix, RtlSwitchStmt switchStmt) {
        sb.append(prefix).append("case (").append(switchStmt.target).append(")\n");
        LinkedHashMap<RtlCfgN, ArrayList<String>> blockCases = new LinkedHashMap<>(8);

        // child 0 is default case, which should be the last one.
        for (int i = 1, c = 0; c < children.size(); i++, i %= children.size(), c++) {
            RtlCfgN n = children.get(i);
            ArrayList<String> cases = blockCases.computeIfAbsent(n, k -> new ArrayList<>(8));
            cases.add(switchStmt.getCase(i));
        }
        for (RtlCfgN n : blockCases.keySet()) {
            ArrayList<String> cases = blockCases.get(n);
            if (cases.contains(RtlSwitchStmt.DEFAULT_LABEL)) {
                cases.clear();
                cases.add(RtlSwitchStmt.DEFAULT_LABEL);
                blockCases.remove(n);
                blockCases.put(n, cases);
                break;
            }
        }

        for (Map.Entry<RtlCfgN, ArrayList<String>> entry : blockCases.entrySet()) {
            RtlCfgN n = entry.getKey();
            if (shouldVisitChild(n)) {
                ArrayList<String> cases = entry.getValue();
                for (int i = 0; i < cases.size(); i++) {
                    String c = cases.get(i);
                    sb.append(prefix).append(c);
                    if (i != cases.size() - 1) {
                        sb.append(",\n");
                    } else {
                        sb.append(":\n");
                    }
                }
                n.visit(sb);
            }
        }
    }

    private void visitAssignStmt(StringBuilder sb, String prefix, RtlAssignStmt stmt) {
        if (quote == RtlCfgN.TYPE_QUOTE_NORMAL) {
            visitAssignNormal(sb, prefix, stmt);
        } else if (quote == RtlCfgN.TYPE_QUOTE_COND_EXPR) {
            visitAssignQuoteExpr(sb, prefix, stmt);
        }
    }

    private void visitAssignNormal(StringBuilder sb, String prefix, RtlAssignStmt stmt) {
        sb.append(prefix).append(stmt.modifier).append(" ")
                .append(stmt.target)
                .append(" = ")
                .append(stmt.expr)
                .append(";\n");
    }

    private void visitAssignQuoteExpr(StringBuilder sb, String prefix, RtlAssignStmt stmt) {
        sb.append(stmt.expr);
    }

    private boolean shouldVisitChild(RtlCfgN child) {
        if (child == null) {
            return false;
        }
        if (child.type == TYPE_RETURN) {
            return false;
        }
        if (child.type == TYPE_SIBLING) {
            return false;
        }
        return child.level > level;
    }

    private boolean shouldVisitSibling(RtlCfgN sibling) {
        if (sibling == null) {
            return false;
        }
        if (sibling.type != TYPE_SIBLING) {
            return false;
        }
        return sibling.level == level;
    }

    private boolean shouldVisitMatch(RtlCfgN child) {
        if (child == null) {
            return false;
        }
        if (child.type == TYPE_RETURN) {
            return false;
        }
        if (child.type == TYPE_SIBLING) {
            return false;
        }
        return true;
    }

    private String getStatementPrefix() {
        StringBuilder prefix = new StringBuilder("   ");
        for (int i = 0; i < level; i++) {
            prefix.append("   ");
        }
        return prefix.toString();
    }

    public boolean hasMatchStmt() {
        for (RtlStmt stmt : statements) {
            if (stmt instanceof RtlMatchStmt) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCondStmt() {
        for (RtlStmt stmt : statements) {
            if (stmt instanceof RtlCondStmt) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSwitchStmt() {
        for (RtlStmt stmt : statements) {
            if (stmt instanceof RtlSwitchStmt) {
                return true;
            }
        }
        return false;
    }

    private String getBitLiteral(String match) {
        String b1 = match.replace("0b", "");
        String b2 = b1.replace("_", "");
        return b2.length() + "'b" + b1;
    }

    public int getChildrenNumber() {
        int count = 0;
        for (RtlCfgN n : children) {
            if (n != null && n.type == RtlCfgN.TYPE_CHILD) {
                count++;
            }
        }
        return count;
    }
}
