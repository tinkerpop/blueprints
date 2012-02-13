package com.tinkerpop.blueprints.pgm.impls;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Parameter<A, B> {

    private final A a;
    private final B b;

    public Parameter(final A a, final B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public boolean equals(Object object) {
        return (object.getClass().equals(Parameter.class) && ((Parameter) object).getA().equals(this.a) && ((Parameter) object).getB().equals(this.b));
    }

    public String toString() {
        return "parameter[" + a.toString() + "," + b.toString() + "]";
    }
}
