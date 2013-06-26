package com.tinkerpop.blueprints;

import java.util.Collection;

/**
 * @author Pierre De Wilde
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public enum Contains implements CompareRelation {

    IN, NOT_IN;

    public boolean compare(final Object first, final Object second) {
        if (second instanceof Collection) {
            return this.equals(IN) ? ((Collection) second).contains(first) : !((Collection) second).contains(first);
        } else {
            if (null == first)
                return this.equals(IN) ? null == second : null != second;
            else
                return this.equals(IN) ? first.equals(second) : !first.equals(second);
        }
    }

    public Contains opposite() {
        return this.equals(IN) ? NOT_IN : IN;
    }
}
