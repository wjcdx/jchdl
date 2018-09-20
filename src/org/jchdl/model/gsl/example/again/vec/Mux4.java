package org.jchdl.model.gsl.example.again.vec;

import org.jchdl.model.gsl.core.datatype.helper.WireVec;
import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.operator.conditional.Mux;

public class Mux4 extends Node {
    private int nDatBits = 0;
    private WireVec dat;
    private WireVec sel;
    private Wire out;

    private Wire o0 = new Wire();
    private Wire o1 = new Wire();

    public Mux4(Wire out, WireVec dat, WireVec sel) {
        nDatBits = dat.nBits();
        in(dat.wires());
        in(sel.wires());
        out(out);
        construct();
    }

    @Override
    public void logic() {
        dat = new WireVec(inputs(0, nDatBits));
        sel = new WireVec(inputs(nDatBits));
        out = new Wire(out(0));

        Mux.inst(o0, dat.wire(0), dat.wire(1), sel.wire(0));
        Mux.inst(o1, dat.wire(2), dat.wire(3), sel.wire(0));
        Mux.inst(out, o0, o1, sel.wire(1));
    }

    public static Mux4 inst(Wire out, WireVec dat, WireVec sel) {
        return new Mux4(out, dat, sel);
    }

    public static void main(String[] args) {
        WireVec dat = new WireVec(4);
        WireVec sel = new WireVec(2);
        Wire out = new Wire();
        Mux4 mux4 = Mux4.inst(out, dat, sel);

        dat.assign(new Value[]{Value.V0, Value.V1, Value.V0, Value.V1});
        sel.assign(new Value[]{Value.V0, Value.V0});// values: bit0, bit1
        PropagateManager.propagateParallel(dat, sel);
        System.out.println("out: " + out.getValue().toString());

        sel.assign(new Value[]{Value.V1, Value.V0});// values: bit0, bit1
        PropagateManager.propagateParallel(sel);
        System.out.println("out: " + out.getValue().toString());

        sel.assign(new Value[]{Value.V0, Value.V1});// values: bit0, bit1
        PropagateManager.propagateParallel(sel);
        System.out.println("out: " + out.getValue().toString());

        sel.assign(new Value[]{Value.V1, Value.V1});// values: bit0, bit1
        PropagateManager.propagateParallel(sel);
        System.out.println("out: " + out.getValue().toString());

        mux4.toVerilog();
    }
}
