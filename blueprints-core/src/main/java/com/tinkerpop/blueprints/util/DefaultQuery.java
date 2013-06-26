package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.CompareRelation;
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
    public long limit = Long.MAX_VALUE;
    public List<HasContainer> hasContainers = new ArrayList<HasContainer>();

    public Query has(final String key, final Object value) {
        this.hasContainers.add(new HasContainer(key, com.tinkerpop.blueprints.Compare.EQUAL, value));
        return this;
    }

    public Query hasNot(final String key, final Object value) {
        this.hasContainers.add(new HasContainer(key, com.tinkerpop.blueprints.Compare.NOT_EQUAL, value));
        return this;
    }

    public Query hasNot(final String key) {
        this.hasContainers.add(new HasContainer(key, com.tinkerpop.blueprints.Compare.EQUAL, new Object[]{null}));
        return this;
    }

    public Query has(final String key) {
        this.hasContainers.add(new HasContainer(key, com.tinkerpop.blueprints.Compare.NOT_EQUAL, new Object[]{null}));
        return this;
    }

    public <T extends Comparable<T>> Query has(final String key, final T value, final Compare compare) {
        return this.has(key, compare, value);
    }

    public Query has(final String key, final CompareRelation compare, final Object... values) {
        this.hasContainers.add(new HasContainer(key, compare, values));
        return this;
    }

    public <T extends Comparable<T>> Query interval(final String key, final T startValue, final T endValue) {
        this.hasContainers.add(new HasContainer(key, com.tinkerpop.blueprints.Compare.GREATER_THAN_EQUAL, startValue));
        this.hasContainers.add(new HasContainer(key, com.tinkerpop.blueprints.Compare.LESS_THAN, endValue));
        return this;
    }

    public Query limit(final long count) {
        this.limit = count;
        return this;
    }

    ////////////////////


    protected class HasContainer {
        public String key;
        public Object[] values;
        public com.tinkerpop.blueprints.Compare compare;

        public HasContainer(final String key, final CompareRelation compare, final Object... values) {
            this.key = key;
            this.values = values;
            if (!(compare instanceof com.tinkerpop.blueprints.Compare))
                throw new IllegalArgumentException("The provided CompareRelation is not supported by DefaultQuery: " + compare.getClass());
            this.compare = (com.tinkerpop.blueprints.Compare) compare;
        }

        public boolean isLegal(final Element element) {
            final Object elementValue = element.getProperty(this.key);
            switch (compare) {
                case EQUAL:
                    if (null == elementValue) {
                        for (final Object value : this.values) {
                            if (value == null)
                                return true;
                        }
                    } else {
                        for (final Object value : this.values) {
                            if (elementValue.equals(value))
                                return true;
                        }
                    }
                    return false;
                case NOT_EQUAL:
                    if (null == elementValue) {
                        for (final Object value : this.values) {
                            if (value != null)
                                return true;
                        }
                    } else {
                        for (final Object value : this.values) {
                            if (!elementValue.equals(value))
                                return true;
                        }
                    }
                    return false;
                case GREATER_THAN:
                    if (null == elementValue)
                        return false;
                    else {
                        for (final Object value : this.values) {
                            if (((Comparable) elementValue).compareTo((value)) >= 1)
                                return true;
                        }
                    }
                    return false;
                case LESS_THAN:
                    if (null == elementValue)
                        return false;
                    else {
                        for (final Object value : this.values) {
                            if (((Comparable) elementValue).compareTo((value)) <= -1)
                                return true;
                        }
                    }
                    return false;
                case GREATER_THAN_EQUAL:
                    if (null == elementValue)
                        return false;
                    else {
                        for (final Object value : this.values) {
                            if (((Comparable) elementValue).compareTo((value)) >= 0)
                                return true;
                        }
                    }
                    return false;
                case LESS_THAN_EQUAL:
                    if (null == elementValue)
                        return false;
                    else {
                        for (final Object value : this.values) {
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
