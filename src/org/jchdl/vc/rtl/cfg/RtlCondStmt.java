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

public class RtlCondStmt extends RtlStmt {
    /**
     * condition statement:
     * if (bool) {
     *     a = value1;
     * } else {
     *     a = value2;
     * }
     */
    public static final int RTL_COND_TYPE_STMT = 0x00;
    /**
     * quote expression: a = bool ? value1 : value2
     */
    public static final int RTL_COND_TYPE_EXPR = 0x01;

    public static final int RTL_COND_STMT_IFEQ = 0x00;
    public static final int RTL_COND_STMT_IFNE = 0x01;
    public static final int RTL_COND_STMT_IFGT = 0x02;
    public static final int RTL_COND_STMT_IFGE = 0x03;
    public static final int RTL_COND_STMT_IFLT = 0x04;
    public static final int RTL_COND_STMT_IFLE = 0x05;
    public int condType;
    public String valueLeft;
    public String valueRight;
    public int condition;
    public RtlCfgN trueBranch;
    public RtlCfgN falseBranch;

    public RtlCondStmt(int condition, String valueLeft, String valueRight) {
        super(RTL_STMT_TYPE_COND);
        this.condType = RTL_COND_TYPE_STMT;
        this.valueLeft = valueLeft;
        this.valueRight = valueRight;
        this.condition = condition;
    }

    public void setQuoteExpr() {
        this.condType = RTL_COND_TYPE_EXPR;
    }

    public boolean isQuoteExpr() {
        return condType == RTL_COND_TYPE_EXPR;
    }

    public String getCondExpr() {
        String expr = "<>";
        switch (condition) {
            case RTL_COND_STMT_IFEQ:
                expr = "==";
                break;
            case RTL_COND_STMT_IFNE:
                expr = "!=";
                break;
            case RTL_COND_STMT_IFGT:
                expr = ">";
                break;
            case RTL_COND_STMT_IFGE:
                expr = ">=";
                break;
            case RTL_COND_STMT_IFLT:
                expr = "<";
                break;
            case RTL_COND_STMT_IFLE:
                expr = "<=";
                break;
        }
        return expr;
    }

    public String getNegCondExpr() {
        String expr = "<>";
        switch (condition) {
            case RTL_COND_STMT_IFEQ:
                expr = "!=";
                break;
            case RTL_COND_STMT_IFNE:
                expr = "==";
                break;
            case RTL_COND_STMT_IFGT:
                expr = "<=";
                break;
            case RTL_COND_STMT_IFGE:
                expr = "<";
                break;
            case RTL_COND_STMT_IFLT:
                expr = ">=";
                break;
            case RTL_COND_STMT_IFLE:
                expr = ">";
                break;
        }
        return expr;
    }

    @Override
    public void attach(RtlCfgN cfgN) {
        trueBranch = cfgN.children.get(1);
        falseBranch = cfgN.children.get(0);
    }

}
