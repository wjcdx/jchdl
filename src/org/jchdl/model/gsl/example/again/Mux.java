package org.jchdl.model.gsl.example.again;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.ni.atomic.And;
import org.jchdl.model.gsl.core.gate.ni.atomic.Or;
import org.jchdl.model.gsl.core.gate.no.Not;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;

public class Mux extends Node {

    private Wire in0;
    private Wire in1;
    private Wire sel;
    private Wire out;

    private Wire selNot;
    private Wire o0;
    private Wire o1;

    public Mux(Wire out, Wire in0, Wire in1, Wire sel) {
        in(in0);
        in(in1);
        in(sel);
        out(out);
        construct();
    }

    @Override
    public void logic() {
        in0 = new Wire(in(0));
        in1 = new Wire(in(1));
        sel = new Wire(in(2));
        out = new Wire(out(0));

        selNot = new Wire();
        Not.inst(selNot, sel);

        o0 = new Wire();
        And.inst(o0, in0, selNot);

        o1 = new Wire();
        And.inst(o1, in1, sel);

        Or.inst(out, o0, o1);
    }

    public static Mux inst(Wire out, Wire in0, Wire in1, Wire sel) {
        return new Mux(out, in0, in1, sel);
    }

    public static void main(String[] args) {
        Wire in0 = new Wire(Value.V0);
        Wire in1 = new Wire(Value.V1);
        Wire sel = new Wire(Value.V0);
        Wire out = new Wire();
        Mux mux = Mux.inst(out, in0, in1, sel);

        System.out.println("out: " + out.getValue().toString());

        PropagateManager.propagateParallel(in0, in1, sel);
        System.out.println("out: " + out.getValue().toString());

        sel.assign(Value.V1);
        PropagateManager.propagateParallel(sel);
        System.out.println("out: " + out.getValue().toString());

        mux.toVerilog();
    }
}


