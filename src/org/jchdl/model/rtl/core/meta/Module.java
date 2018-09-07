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
package org.jchdl.model.rtl.core.meta;

import org.jchdl.vc.rtl.RtlVerilogConverter;
import org.jchdl.model.rtl.core.block.Always;
import org.jchdl.model.rtl.core.block.Assign;
import org.jchdl.model.rtl.core.block.Block;
import org.jchdl.model.rtl.core.datatype.Bits;
import org.jchdl.model.rtl.core.datatype.Bit;
import org.jchdl.model.rtl.core.event.Event;

import java.util.ArrayList;

/**
 * @author wjcdx
 */
public abstract class Module implements Propagatable {
    private String name;
    private Module parent;
    private ArrayList<Module> children = new ArrayList<>(8);
    private ArrayList<Block> blocks = new ArrayList<>(8);

    public Module(Module parent) {
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    private void addChild(Module child) {
        children.add(child);
    }

    public ArrayList<Module> getChildren() {
        return children;
    }

    public ArrayList<Block> getBlocks() {
        return blocks;
    }

    public String getName() {
        return name;
    }

    /**
     * purpose:
     * 1. building the design model;
     * 2. translate inputs to outputs while simulating;
     */
    public abstract void logic();

    public void construct() {
        logic();
    }

    protected Always when(Event... events) {
        Always block = new Always(events);
        this.addBlock(block);
        return block;
    }

    protected Assign assign(Bit bit) {
        Assign block = new Assign(bit);
        this.addBlock(block);
        return block;
    }

    protected Assign assign(Bits bits) {
        Assign block = new Assign(bits);
        this.addBlock(block);
        return block;
    }

    private void addBlock(Block block) {
        blocks.add(block);
    }

    @Override
    public void prepare() {
        for (Block block : blocks) {
            block.prepare();
        }
        for (Module c : children) {
            c.prepare();
        }
    }

    @Override
    public void prepared() {
        for (Block block : blocks) {
            block.prepared();
        }
        for (Module c : children) {
            c.prepared();
        }
    }

    @Override
    public void propagate() {
        // delegated to blocks.
    }

    public void toVerilog() {
        RtlVerilogConverter.translate(this);
    }
}
