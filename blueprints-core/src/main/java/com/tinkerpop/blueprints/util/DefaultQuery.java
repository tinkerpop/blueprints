package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class DefaultQuery implements Query {

    private static final String[] EMPTY_LABELS = new String[]{};

    public Direction direction = Direction.BOTH;
    public String[] labels = EMPTY_LABELS;
    public long maximum = Long.MAX_VALUE;
    public long minimum = 0l;
    public List<HasContainer> hasContainers = new ArrayList<HasContainer>();

    public Query has(final String key, final Object value) {
        this.hasContainers.add(new HasContainer(key, Compare.EQUAL, value));
        return this;
    }

    public <T extends Comparable<T>> Query has(final String key, final T value, final Compare compare) {
        return this.has(key, compare, value);
    }

    public <T extends Comparable<T>> Query has(final String key, final Compare compare, final T... values) {
        this.hasContainers.add(new HasContainer(key, compare, values));
        return this;
    }

    public <T extends Comparable<T>> Query interval(final String key, final T startValue, final T endValue) {
        this.hasContainers.add(new HasContainer(key, Compare.GREATER_THAN_EQUAL, startValue));
        this.hasContainers.add(new HasContainer(key, Compare.LESS_THAN, endValue));
        return this;
    }

    public Query limit(final long max) {
        this.maximum = max;
        return this;
    }

    public Query limit(final long min, final long max) {
        this.minimum = min;
        if (min == 0)
            this.maximum = max;
        else
            this.maximum = max + (min - 1);
        return this;
    }

    ////////////////////


    protected class HasContainer {
        public String key;
        public Object[] values;
        public Compare compare;

        public HasContainer(final String key, final Compare compare, final Object... values) {
            this.key = key;
            this.values = values;
            this.compare = compare;
        }

        public HasContainer(final String key, final Compare compare, final Object value) {
            this(key, compare, new Object[]{value});
        }

        public boolean isLegal(final Element element) {
            final Object elementValue = element.getProperty(key);
            switch (compare) {
                case EQUAL:
                    if (null == elementValue) {
                        for (final Object value : values) {
                            if (value == null)
                                return true;
                        }
                    } else {
                        for (final Object value : values) {
                            if (elementValue.equals(value))
                                return true;
                        }
                    }
                    return false;
                case NOT_EQUAL:
                    if (null == elementValue) {
                        for (final Object value : values) {
                            if (value != null)
                                return true;
                        }
                    } else {
                        for (final Object value : values) {
                            if (!elementValue.equals(value))
                                return true;
                        }
                    }
                    return false;
                case GREATER_THAN:
                    if (null == elementValue)
                        return false;
                    else {
                        for (final Object value : values) {
                            if (((Comparable) elementValue).compareTo((value)) >= 1)
                                return true;
                        }
                    }
                    return false;
                case LESS_THAN:
                    if (null == elementValue)
                        return false;
                    else {
                        for (final Object value : values) {
                            if (((Comparable) elementValue).compareTo((value)) <= -1)
                                return true;
                        }
                    }
                    return false;
                case GREATER_THAN_EQUAL:
                    if (null == elementValue)
                        return false;
                    else {
                        for (final Object value : values) {
                            if (((Comparable) elementValue).compareTo((value)) >= 0)
                                return true;
                        }
                    }
                    return false;
                case LESS_THAN_EQUAL:
                    if (null == elementValue)
                        return false;
                    else {
                        for (final Object value : values) {
                            if (((Comparable) elementValue).compareTo((value)) <= 0)
                                return true;
                        }
                    }
                    return false;
                default:
                    throw new IllegalArgumentException("Invalid state as no valid filter was provided");
            }
        }
    }
}
