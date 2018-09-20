package org.jchdl.model.gsl.example.again;

import org.jchdl.model.gsl.core.datatype.net.Wire;
import org.jchdl.model.gsl.core.gate.ni.atomic.Or;
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
        in(Wire.array(a, b, ci));
        out(Wire.array(s, co));
        construct();
    }

    @Override
    public void logic() {
        a = new Wire(in(0));
        b = new Wire(in(1));
        ci = new Wire(in(2));
        s = new Wire(out(0));
        co = new Wire(out(1));

        Wire s1 = new Wire();
        Wire c1 = new Wire();
        HalfAdder.inst(s1, c1, a, b);

        Wire c2 = new Wire();
        HalfAdder.inst(s, c2, s1, ci);

        Or.inst(co, c1, c2);
    }

    public static FullAdder inst(Wire s, Wire co, Wire a, Wire b, Wire ci) {
        return new FullAdder(s, co, a, b, ci);
    }

    public static void main(String args[]) {
        Wire a = new Wire();
        Wire b = new Wire();
        Wire ci = new Wire();
        Wire s = new Wire();
        Wire co = new Wire();

        FullAdder fullAdder = FullAdder.inst(s, co, a, b, ci);

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
