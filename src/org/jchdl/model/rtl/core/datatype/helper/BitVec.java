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
package org.jchdl.model.rtl.core.datatype.helper;

import org.jchdl.model.rtl.core.datatype.Bit;
import org.jchdl.model.rtl.core.meta.Bitable;

import java.util.ArrayList;
import java.util.Arrays;

public class BitVec implements Bitable {
    /**
     * Number of bits in this vector.
     */
    protected int nBits = 0;

    /**
     * Bit at index 0 is the LSB.
     */
    protected Bit[] bits;
    /**
     * Name of this vector.
     */
    private String name;

    public BitVec(Bit[] bits) {
        this.nBits = bits.length;
        this.bits = bits;
    }

    public BitVec(int nBits) {
        this.nBits = nBits;
        this.bits = new Bit[nBits];
        for (int i = 0; i < nBits; i++) {
            bits[i] = new Bit();
        }
    }

    public BitVec(int nBits, int value) {
        this(nBits);
        set(value);
    }

    public BitVec(int nBits, long value) {
        this(nBits);
        set(value);
    }

    @Override
    public int nBits() {
        return nBits;
    }

    public Bit bit(int index) {
        return bits[index];
    }

    @Override
    public Bit[] bits() {
        return bits;
    }

    /**
     * Return partial bits [msb, lsb]. If lsb > msb, then the result bits
     * are returned in reversed order.
     *
     * @param lsb the initial index of the range to be copied, inclusive
     * @param msb the final index of the range to be copied, inclusive.
     */
    public Bit[] bits(int msb, int lsb) {
        if (lsb > msb) {
            return reverse(Arrays.copyOfRange(bits, msb, lsb + 1));
        }
        return Arrays.copyOfRange(bits, lsb, msb + 1);
    }

    public void set(Bit[] bits) {
        int n = Math.min(nBits, bits.length);
        for (int i = 0; i < n; i++) {
            this.bits[i].set(bits[i]);
        }
    }

    public void set(int value) {
        set(intToBits(value, nBits));
    }

    public void set(long value) {
        set(longToBits(value, nBits));
    }

    @Override
    public boolean boolVal() {
        return (intVal() != 0);
    }

    @Override
    public int intVal() {
        return bitsToInt(bits());
    }

    public int intVal(int msb, int lsb) {
        return bitsToInt(bits(msb, lsb));
    }

    public Bit eq(int value) {
        return Bit.inst(intVal() == value);
    }

    public Bit ne(int value) {
        return Bit.inst(intVal() != value);
    }

    public Bit lt(int value) {
        return Bit.inst(intVal() < value);
    }

    public Bit le(int value) {
        return Bit.inst(intVal() <= value);
    }

    public Bit gt(int value) {
        return Bit.inst(intVal() > value);
    }

    public Bit ge(int value) {
        return Bit.inst(intVal() >= value);
    }

    @Override
    public long longVal() {
        return bitsToLong(bits());
    }

    public long longVal(int msb, int lsb) {
        return bitsToLong(bits(msb, lsb));
    }

    public Bit eq(long value) {
        return Bit.inst(longVal() == value);
    }

    public Bit ne(long value) {
        return Bit.inst(longVal() != value);
    }

    public Bit lt(long value) {
        return Bit.inst(longVal() < value);
    }

    public Bit le(long value) {
        return Bit.inst(longVal() <= value);
    }

    public Bit gt(long value) {
        return Bit.inst(longVal() > value);
    }

    public Bit ge(long value) {
        return Bit.inst(longVal() >= value);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = nBits - 1; i >= 0; i--) {
            s.append(bit(i).toString());
        }
        return s.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // 0b10xx_x010: char ordered from msb to lsb
    public boolean match(String binaryString) {
        String s = binaryString.trim();
        s = s.replace("_", "");
        s = s.replace("0b", "");

        char[] ca = s.toCharArray();
        if (ca.length == 0 || ca.length != nBits) {
            return false;
        }

        for (int i = 0; i < nBits; i++) {
            int c = nBits - 1 - i;
            switch (ca[c]) {
                case '0':
                    if (bit(i).boolVal()) {
                        return false;
                    }
                    break;
                case '1':
                    if (!bit(i).boolVal()) {
                        return false;
                    }
                    break;
                case 'x':
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    // return true at least one matched.
    public boolean match(String... binaryStrings) {
        for (String s : binaryStrings) {
            if (match(s)) {
                return true;
            }
        }
        return false;
    }

    // bitables: bits in a vec are ordered from lsb to msb,
    //          arrays are ordered from msb to lsb.
    protected Bit[] vecConcat(Bitable... bitables) {
        ArrayList<Bit> bitList = new ArrayList<>(32);
        for (int i = bitables.length - 1; i >= 0; i--) {
            Bit[] bits = bitables[i].bits();
            bitList.addAll(Arrays.asList(bits));
        }
        bitList.addAll(Arrays.asList(this.bits));

        int n = bitList.size();
        Bit[] union = new Bit[n];
        for (int i = 0; i < n; i++) {
            union[i] = bitList.get(i);
        }
        return union;
    }

    protected Bit[] vecAND(Bit[] bits) {
        Bit[] result = new Bit[nBits];
        for (int i = 0; i < nBits; i++) {
            result[i] = bit(i).and(bits[i]);
        }
        return result;
    }

    protected Bit[] vecOR(Bit[] bits) {
        Bit[] result = new Bit[nBits];
        for (int i = 0; i < nBits; i++) {
            result[i] = bit(i).or(bits[i]);
        }
        return result;
    }

    protected Bit[] vecNOT() {
        Bit[] result = new Bit[nBits];
        for (int i = 0; i < nBits; i++) {
            result[i] = bit(i).not();
        }
        return result;
    }

    protected Bit[] vecXOR(Bit[] bits) {
        Bit[] result = new Bit[nBits];
        for (int i = 0; i < nBits; i++) {
            result[i] = bit(i).xor(bits[i]);
        }
        return result;
    }

    protected Bit[] vecAdd(Bit[] bits) {
        int add1 = bitsToInt(this.bits);
        int add2 = bitsToInt(bits);
        int sum = add1 + add2;
        return intToBits(sum, nBits);
    }

    public Bit[] add(int v) {
        int orig = bitsToInt(this.bits);
        int sum = orig + v;
        return intToBits(sum, nBits);
    }

    public Bit[] add(long v) {
        long orig = bitsToLong(this.bits);
        long sum = orig + v;
        return longToBits(sum, nBits);
    }

    private static Bit[] intToBits(int value, int nBits) {
        int nEffective = Integer.SIZE - Integer.numberOfLeadingZeros(value);
        nEffective = Math.max(nEffective, nBits);
        Bit[] bits = new Bit[nEffective];
        for (int i = 0; i < nEffective; i++) {
            bits[i] = new Bit(((value >> i) & 0x01) != 0);
        }
        return bits;
    }

    private static int bitsToInt(Bit[] bits) {
        int result = 0;
        for (int i = 0; i < bits.length; i++) {
            result |= (bits[i].value << i);
        }
        return result;
    }

    private static Bit[] longToBits(long value, int nBits) {
        int nEffective = Long.SIZE - Long.numberOfLeadingZeros(value);
        nEffective = Math.max(nEffective, nBits);
        Bit[] bits = new Bit[nEffective];
        for (int i = 0; i < nEffective; i++) {
            bits[i] = new Bit(((value >> i) & 0x01) == 0 ? 0 : 1);
        }
        return bits;
    }

    private static long bitsToLong(Bit[] bits) {
        long result = 0;
        for (int i = 0; i < bits.length; i++) {
            result |= (bits[i].value << i);
        }
        return result;
    }

    private static Bit[] reverse(Bit[] bits) {
        for (int s = 0, e = bits.length - 1; s < e; s++, e--) {
            Bit bit = bits[e];
            bits[e] = bits[s];
            bits[s] = bit;
        }
        return bits;
    }
}
