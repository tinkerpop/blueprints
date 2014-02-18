package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Predicate;
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
    public int limit = Integer.MAX_VALUE;
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
        this.hasContainers.add(new HasContainer(key, com.tinkerpop.blueprints.Compare.EQUAL, null));
        return this;
    }

    public Query has(final String key) {
        this.hasContainers.add(new HasContainer(key, com.tinkerpop.blueprints.Compare.NOT_EQUAL, null));
        return this;
    }

    public <T extends Comparable<T>> Query has(final String key, final T value, final Compare compare) {
        return this.has(key, compare, value);
    }

    public Query has(final String key, final Predicate predicate, final Object value) {
        this.hasContainers.add(new HasContainer(key, predicate, value));
        return this;
    }

    public <T extends Comparable<?>> Query interval(final String key, final T startValue, final T endValue) {
        this.hasContainers.add(new HasContainer(key, com.tinkerpop.blueprints.Compare.GREATER_THAN_EQUAL, startValue));
        this.hasContainers.add(new HasContainer(key, com.tinkerpop.blueprints.Compare.LESS_THAN, endValue));
        return this;
    }

    public Query limit(final int count) {
        this.limit = count;
        return this;
    }

    ////////////////////


    protected class HasContainer {
        public String key;
        public Object value;
        public Predicate predicate;

        public HasContainer(final String key, final Predicate predicate, final Object value) {
            this.key = key;
            this.value = value;
            this.predicate = predicate;
        }

        public boolean isLegal(final Element element) {
            if (this.key.equals(StringFactory.ID)) {
                return this.predicate.evaluate(element.getId(), this.value);
            } else if (this.key.equals(StringFactory.LABEL) && element instanceof Edge) {
                return this.predicate.evaluate(((Edge) element).getLabel(), this.value);
            } else {
                return this.predicate.evaluate(element.getProperty(this.key), this.value);
            }
        }
    }
}
