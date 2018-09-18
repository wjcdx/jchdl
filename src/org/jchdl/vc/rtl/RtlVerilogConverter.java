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
package org.jchdl.vc.rtl;

import org.jchdl.model.rtl.core.datatype.Bit;
import org.jchdl.model.rtl.core.datatype.Bits;
import org.jchdl.model.rtl.core.datatype.Reg;
import org.jchdl.model.rtl.core.meta.Module;
import org.jchdl.model.rtl.example.And2.Abc;
import org.jchdl.model.rtl.example.And2.And2;
import org.jchdl.model.rtl.example.AndAnd;
import org.jchdl.model.rtl.example.Mos6502.Cpu;
import org.jchdl.vc.rtl.m2.*;
import org.jchdl.vc.rtl.m2.RtlBitable;
import org.jchdl.vc.rtl.m2.block.RtlBlock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class RtlVerilogConverter {
    private RtlModule topRtlModule;
    private HashSet<Class<?>> moduleTranslated = new HashSet<>(16);

    public RtlVerilogConverter(Module module) throws IOException {
        this.topRtlModule = new RtlModule(module);
    }

    private boolean moduleTypeTranslated(RtlModule module) {
        Class clazz = module.getModelModule().getClass();
        return !moduleTranslated.add(clazz);
    }

    private void translateModuleHeader(RtlModule rtlModule) {
        System.out.println("module " + rtlModule.getName() + " (");
        ArrayList<RtlBitable> interfaces = rtlModule.getInterfaces();
        for (int i = 0; i < interfaces.size(); i++) {
            if (i != interfaces.size() - 1) {
                System.out.println("  " + interfaces.get(i).getDeclarationString() + ",");
            } else {
                System.out.println("  " + interfaces.get(i).getDeclarationString());
            }
        }
        System.out.println(");");
        System.out.println();
    }

    private void translateDeclarations(RtlModule rtlModule) {
        for (RtlBitable b : rtlModule.getDeclarations()) {
            System.out.println(b.getDeclarationString() + ";");
        }
        System.out.println();
    }

    private void translateModuleInstances(RtlModule rtlModule) {
        for (RtlModule c : rtlModule.getChildren()) {
            String name = c.getName();
            System.out.print(name + "(");
            ArrayList<String> parameters = c.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                if (i != parameters.size() - 1) {
                    System.out.print(" " + parameters.get(i) + ",");
                } else {
                    System.out.print(" " + parameters.get(i) + "");
                }
            }
            System.out.println(");");
        }
        System.out.println();
    }

    private void translateBlocks(RtlModule rtlModule) {
        for (RtlBlock rtlBlock : rtlModule.getBlocks()) {
            System.out.println(rtlBlock.translate());
        }
        System.out.println();
    }

    private void translateModuleFooter() {
        System.out.println("endmodule");
        System.out.println();
    }

    private void translateModule(RtlModule rtlModule) {
        translateModuleHeader(rtlModule);
        translateDeclarations(rtlModule);
        translateModuleInstances(rtlModule);
        translateBlocks(rtlModule);
        translateModuleFooter();
    }

    private void translate(RtlModule rtlModule) {
        if (moduleTypeTranslated(rtlModule)) return;
        translateModule(rtlModule);
        for (RtlModule m : rtlModule.getChildren()) {
            translate(m);
        }
    }

    private void setup() throws IllegalAccessException, IOException {
        topRtlModule.setupModuleHierarchy();
    }

    private void translate() {
        translate(topRtlModule);
    }

    public static void translate(Module module) {
        try {
            RtlVerilogConverter vc = new RtlVerilogConverter(module);
            vc.setup();
            vc.translate();
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static AndAnd getAA() {
        Bit a = new Bit(Bit.BIT_0);
        Bit b = new Bit(Bit.BIT_0);
        Bit c = new Bit(Bit.BIT_0);
        Bit r = new Bit();
        return AndAnd.inst(null, r, a, b, c);
    }

    public static Cpu getCpu() {
        Bit clk = new Bit();
        Bit reset = new Bit();
        Bits DI = new Bits(8, 0x00);
        Bit IRQ = new Bit();
        Bit NMI = new Bit();
        Bit RDY = new Bit();
        Reg AB = new Reg(16, 0x0000);
        Reg DO = new Reg(8, 0x00);
        Reg WE = new Reg(1, 0b0);

        return new Cpu(null, clk, reset, DI, IRQ, NMI, RDY, AB, DO, WE);
    }

    public static void main(String[] args) {
//        Cpu cpu = getCpu();
//        cpu.toVerilog();
//        AndAnd aa = getAA();
//        aa.toVerilog();
        And2 and2 = new And2(null, new Bit(), new Abc());
        and2.toVerilog();
    }
}
