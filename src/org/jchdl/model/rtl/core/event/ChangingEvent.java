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
package org.jchdl.model.rtl.core.event;

import org.jchdl.model.rtl.core.datatype.Bit;
import org.jchdl.model.rtl.core.datatype.Bits;
import org.jchdl.model.rtl.core.meta.Bitable;

/**
 * @author wjcdx
 */
public class ChangingEvent implements Event {
    private int nBits;
    private Bit[] bits;
    private int[] origValue;

    private ChangingEvent(Bitable bitable) {
        this.nBits = bitable.nBits();
        this.bits = bitable.bits();
        this.origValue = new int[this.nBits];
        saveValue();
    }

    private void saveValue() {
        for (int i = 0; i < nBits; i++) {
            origValue[i] = bits[i].value;
        }
    }

    @Override
    public boolean happened() {
        for (int i = 0; i < nBits; i++) {
            if (origValue[i] != bits[i].value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        saveValue();
    }

    public static ChangingEvent of(Bitable bitable) {
        return new ChangingEvent(bitable);
    }

    public static ChangingEvent[] of(Bitable... bitables) {
        ChangingEvent[] events = new ChangingEvent[bitables.length];
        for (int i = 0; i < events.length; i++) {
            events[i] = ChangingEvent.of(bitables[i]);
        }
        return events;
    }

    public static void main(String[] args) {
        Bits bits = new Bits(2, 0b00);
        System.out.println(bits);

        ChangingEvent event = ChangingEvent.of(bits);

        bits.assign(new Bits(2, 0b11));
        System.out.println(bits);

        System.out.println(event.happened());
        event.clear();
        System.out.println(event.happened());

        bits.bit(0).assign(Bit.BIT_0);
        System.out.println(bits);

        System.out.println(event.happened());
        event.clear();
        System.out.println(event.happened());
    }
}
