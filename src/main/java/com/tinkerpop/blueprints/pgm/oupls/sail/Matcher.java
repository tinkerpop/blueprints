package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.Edge;

import java.util.Iterator;

/**
 * An object which retrieves statements from the triple store based on a given triple pattern.
 * For example, an "soc" matcher expects the subject, object, and context components to be specified; it will then retrieve
 * all matching statements (where the unspecified component of the pattern -- the predicate -- may vary).
 * In an "o" matcher, only the object is specified, and subject, predicate and context may vary, etc.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class Matcher {
    protected final boolean s, p, o, c;

    /**
     * Create a new matcher based on the given triple pattern.
     *
     * @param s whether the subject is specified
     * @param p whether the predicate is specified
     * @param o whether the object is specified
     * @param c whether the context is specified
     */
    public Matcher(final boolean s, final boolean p, final boolean o, final boolean c) {
        this.s = s;
        this.p = p;
        this.o = o;
        this.c = c;
    }

    /**
     * Retrieve matching statements based on this matcher's triple pattern as well as the provided values.
     * If a component such as subject or object is specified in the pattern, a non-null value must be provided to this method.
     * Non-null values for unspecified components may be provided, but they will not be used.
     *
     * @param subject   the subject value of matching statements
     * @param predicate the predicate value of matching statements
     * @param object    the object of matching statements
     * @param context   the context of matching statements
     * @return an iterator over all matching statements
     */
    public abstract Iterator<Edge> match(final String subject, final String predicate, final String object, final String context);

    public String toString() {
        StringBuilder sb = new StringBuilder("matcher[");
        if (s) {
            sb.append("s");
        }
        if (p) {
            sb.append("p");
        }
        if (o) {
            sb.append("o");
        }
        if (c) {
            sb.append("c");
        }
        sb.append("]");
        return sb.toString();
    }
}