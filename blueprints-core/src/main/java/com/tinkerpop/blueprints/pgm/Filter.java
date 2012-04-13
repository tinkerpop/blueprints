package com.tinkerpop.blueprints.pgm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Filter {

    public enum Compare {EQUAL, NOT_EQUAL, GREATER_THAN, GREATER_THAN_EQUAL, LESS_THAN, LESS_THAN_EQUAL}

    public final String key;
    public final Object value;
    public final Compare compare;
    public final Class<? extends Element> forClass;

    public Filter(final String key, final Object value, final Compare compare, final Class<? extends Element> forClass) {
        this.key = key;
        this.value = value;
        this.compare = compare;
        this.forClass = forClass;

    }

    public Filter(final String key, final Object value, final Compare compare) {
        this(key, value, compare, Edge.class);
    }

    public Filter(final String key, final Object value) {
        this(key, value, Compare.EQUAL);
    }
}
