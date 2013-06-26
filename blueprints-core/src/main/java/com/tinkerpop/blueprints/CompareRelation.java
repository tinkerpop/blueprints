package com.tinkerpop.blueprints;

/**
 * A general interface for all enumerations that represent comparable operations.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public interface CompareRelation {

    /**
     * If the underlying graph does not support the push down comparator, then an in-memory comparison can be done.
     *
     * @param first  the left hand side of the comparator
     * @param second the right hand side of the comparator
     * @return whether the first compares with the second given the semantics of the compare relation
     */
    public boolean compare(final Object first, final Object second);
}
