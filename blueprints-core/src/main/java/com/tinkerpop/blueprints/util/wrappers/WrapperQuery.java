package com.tinkerpop.blueprints.util.wrappers;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;

/**
 * A WrapperQuery is useful for wrapping the construction and results of a Vertex.query().
 * Any necessary Iterable wrapping must occur when Vertex.vertices() or Vertex.edges() is called.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class WrapperQuery implements Query {

    protected Query query;

    public WrapperQuery(final Query query) {
        this.query = query;
    }

    public Query has(final String key, final Object value) {
        this.query = this.query.has(key, value);
        return this;
    }

    public <T extends Comparable<T>> Query has(final String key, final T value, final Compare compare) {
        this.query = this.query.has(key, value, compare);
        return this;
    }

    public <T extends Comparable<T>> Query interval(final String key, final T startValue, final T endValue) {
        this.query = this.query.interval(key, startValue, endValue);
        return this;
    }

    public Query direction(final Direction direction) {
        this.query = this.query.direction(direction);
        return this;
    }

    public Query limit(final long limit) {
        this.query = this.query.limit(limit);
        return this;
    }

    public Query labels(final String... labels) {
        this.query = this.query.labels(labels);
        return this;
    }

    public long count() {
        return this.query.count();
    }

    public Object vertexIds() {
        return this.query.vertexIds();
    }

    public abstract Iterable<Edge> edges();

    public abstract Iterable<Vertex> vertices();

}