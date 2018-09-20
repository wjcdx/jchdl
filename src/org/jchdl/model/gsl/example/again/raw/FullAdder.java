package org.jchdl.model.gsl.example.again.raw;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.ni.Or;
import org.jchdl.model.gsl.core.gate.ni.atomic.And;
import org.jchdl.model.gsl.core.gate.ni.atomic.Xor;
import org.jchdl.model.gsl.core.meta.Node;
import org.jchdl.model.gsl.core.meta.PropagateManager;
import org.jchdl.model.gsl.core.value.Value;

public class FullAdder extends Node {
    private Wire a;
    private Wire b;
    private Wire ci;
    private Wire s;
    private Wire co;

    public FullAdder(Wire s, Wire co, Wire a, Wire b, Wire ci) {
        in(a);
        in(b);
        in(ci);
        out(s);
        out(co);
        construct();
    }

    @Override
    public void logic() {
        a = new Wire(in(0));
        b = new Wire(in(1));
        ci = new Wire(in(2));
        s = new Wire(out(0));
        co = new Wire(out(1));

        Wire o0 = new Wire();
        Xor.inst(o0, a, b);

        Wire o1 = new Wire();
        And.inst(o1, a, b);

        Wire o2 = new Wire();
        And.inst(o2, a, ci);

        Wire o3 = new Wire();
        And.inst(o3, b, ci);

        Xor.inst(s, o0, ci);
        Or.inst(co, o1, o2, o3);
    }

    public static FullAdder inst(Wire s, Wire co, Wire a, Wire b, Wire ci) {
        return new FullAdder(s, co, a, b, ci);
    }

    public static void main(String[] args) {
        Wire a = new Wire();
        Wire b = new Wire();
        Wire ci = new Wire();
        Wire s = new Wire();
        Wire co = new Wire();
        FullAdder fullAdder = FullAdder.inst(s, co, a, b, ci);

        a.assign(Value.V0);
        b.assign(Value.V0);
        ci.assign(Value.V0);
        PropagateManager.propagateParallel(a, b, ci);
        System.out.println("c_s: " + co.getValue().toString() + "_" + s.getValue().toString());

        a.assign(Value.V1);
        b.assign(Value.V0);
        ci.assign(Value.V0);
        PropagateManager.propagateParallel(a, b, ci);
        System.out.println("c_s: " + co.getValue().toString() + "_" + s.getValue().toString());

        a.assign(Value.V0);
        b.assign(Value.V1);
        ci.assign(Value.V0);
        PropagateManager.propagateParallel(a, b, ci);
        System.out.println("c_s: " + co.getValue().toString() + "_" + s.getValue().toString());

        a.assign(Value.V1);
        b.assign(Value.V1);
        ci.assign(Value.V0);
        PropagateManager.propagateParallel(a, b, ci);
        System.out.println("c_s: " + co.getValue().toString() + "_" + s.getValue().toString());

        a.assign(Value.V0);
        b.assign(Value.V0);
        ci.assign(Value.V1);
        PropagateManager.propagateParallel(a, b, ci);
        System.out.println("c_s: " + co.getValue().toString() + "_" + s.getValue().toString());

        a.assign(Value.V1);
        b.assign(Value.V1);
        ci.assign(Value.V1);
        PropagateManager.propagateParallel(a, b, ci);
        System.out.println("c_s: " + co.getValue().toString() + "_" + s.getValue().toString());

        fullAdder.toVerilog();
    }
}
