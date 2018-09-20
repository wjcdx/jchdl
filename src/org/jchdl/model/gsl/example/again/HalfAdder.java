package org.jchdl.model.gsl.example.again;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.ni.atomic.And;
import org.jchdl.model.gsl.core.gate.ni.atomic.Xor;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;

public class HalfAdder extends Node {
    private Wire a;
    private Wire b;
    private Wire s;
    private Wire c;

    public HalfAdder(Wire s, Wire c, Wire a, Wire b) {
        in(a);
        in(b);
        out(s);
        out(c);
        construct();
    }

    @Override
    public void logic() {
        a = new Wire(in(0));
        b = new Wire(in(1));
        s = new Wire(out(0));
        c = new Wire(out(1));

        Xor.inst(s, a, b);
        And.inst(c, a, b);
    }

    public static HalfAdder inst(Wire s, Wire c, Wire a, Wire b) {
        return new HalfAdder(s, c, a, b);
    }

    public static void main(String[] args) {
        Wire a = new Wire();
        Wire b = new Wire();
        Wire s = new Wire();
        Wire c = new Wire();
        HalfAdder halfAdder = HalfAdder.inst(s, c, a, b);

        a.assign(Value.V0);
        b.assign(Value.V0);
        PropagateManager.propagateParallel(a, b);
        System.out.println("out: " + c.getValue().toString() + "_" + s.getValue().toString());

        a.assign(Value.V0);
        b.assign(Value.V1);
        PropagateManager.propagateParallel(a, b);
        System.out.println("out: " + c.getValue().toString() + "_" + s.getValue().toString());

        a.assign(Value.V1);
        b.assign(Value.V0);
        PropagateManager.propagateParallel(a, b);
        System.out.println("out: " + c.getValue().toString() + "_" + s.getValue().toString());

        a.assign(Value.V1);
        b.assign(Value.V1);
        PropagateManager.propagateParallel(a, b);
        System.out.println("out: " + c.getValue().toString() + "_" + s.getValue().toString());

        halfAdder.toVerilog();
    }
}
