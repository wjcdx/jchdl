package org.jchdl.model.gsl.example.again;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;
import org.jchdl.model.gsl.operator.conditional.Mux;

public class Mux4 extends Node {
    private Wire i0;
    private Wire i1;
    private Wire i2;
    private Wire i3;
    private Wire s1;
    private Wire s0;
    private Wire out;

    private Wire o0 = new Wire();
    private Wire o1 = new Wire();

    public Mux4(Wire out, Wire i0, Wire i1, Wire i2, Wire i3, Wire s1, Wire s0) {
        in(Wire.array(i0, i1, i2, i3, s1, s0));
        out(out);
        construct();
    }

    @Override
    public void logic() {
        i0 = new Wire(in(0));
        i1 = new Wire(in(1));
        i2 = new Wire(in(2));
        i3 = new Wire(in(3));
        s1 = new Wire(in(4));
        s0 = new Wire(in(5));
        out = new Wire(out(0));

        Mux.inst(o0, i0, i1, s0);
        Mux.inst(o1, i2, i3, s0);
        Mux.inst(out, o0, o1, s1);
    }

    public static Mux4 inst(Wire out, Wire i0, Wire i1, Wire i2, Wire i3, Wire s1, Wire s0) {
        return new Mux4(out, i0, i1, i2, i3, s1, s0);
    }

    public static void main(String[] args) {
        Wire i0 = new Wire(Value.V0);
        Wire i1 = new Wire(Value.V1);
        Wire i2 = new Wire(Value.V0);
        Wire i3 = new Wire(Value.V1);
        Wire s1 = new Wire(Value.V0);
        Wire s0 = new Wire(Value.V0);
        Wire out = new Wire();
        Mux4 mux4 = Mux4.inst(out, i0, i1, i2, i3, s1, s0);

        PropagateManager.propagateParallel(i0, i1, i2, i3, s1, s0);
        System.out.println("out: " + out.getValue().toString());

        s1.assign(Value.V0);
        s0.assign(Value.V1);
        PropagateManager.propagateParallel(s1, s0);
        System.out.println("out: " + out.getValue().toString());

        s1.assign(Value.V1);
        s0.assign(Value.V0);
        PropagateManager.propagateParallel(s1, s0);
        System.out.println("out: " + out.getValue().toString());

        s1.assign(Value.V1);
        s0.assign(Value.V1);
        PropagateManager.propagateParallel(s1, s0);
        System.out.println("out: " + out.getValue().toString());

        mux4.toVerilog();
    }
}
