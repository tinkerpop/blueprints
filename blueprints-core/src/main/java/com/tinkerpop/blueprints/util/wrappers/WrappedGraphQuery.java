package com.tinkerpop.blueprints.util.wrappers;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class WrappedGraphQuery implements GraphQuery {

    protected GraphQuery query;

    public WrappedGraphQuery(final GraphQuery query) {
        this.query = query;
    }

    public GraphQuery has(final String key) {
        this.query = this.query.has(key);
        return this;
    }

    public GraphQuery hasNot(final String key) {
        this.query = this.query.hasNot(key);
        return this;
    }

    public GraphQuery has(final String key, final Object... values) {
        this.query = this.query.has(key, values);
        return this;
    }

    public <T extends Comparable<T>> GraphQuery has(final String key, final T value, final Compare compare) {
        return this.has(key, compare, value);
    }

    public <T extends Comparable<T>> GraphQuery has(final String key, final Compare compare, final T... values) {
        this.query = this.query.has(key, compare, values);
        return this;
    }

    public <T extends Comparable<T>> GraphQuery interval(final String key, final T startValue, final T endValue) {
        this.query = this.query.interval(key, startValue, endValue);
        return this;
    }

    public GraphQuery limit(final long total) {
        this.query = this.query.limit(total);
        return this;
    }

    public GraphQuery limit(final long skip, final long total) {
        this.query = this.query.limit(skip, total);
        return this;
    }

    public abstract Iterable<Edge> edges();

    public abstract Iterable<Vertex> vertices();
}
