package com.tinkerpop.blueprints;

/**
 * A general interface for all enumerations that represent comparable operations.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public interface Predicate {

    /**
     * If the underlying graph does not support the push down predicate, then an in-memory comparison can be done.
     *
     * @param first  the left hand side of the predicate
     * @param second the right hand side of the predicate
     * @return whether the first compares with the second given the semantics of the predicate
     */
    public boolean compare(final Object first, final Object second);
}
