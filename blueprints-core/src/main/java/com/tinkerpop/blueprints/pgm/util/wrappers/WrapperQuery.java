package com.tinkerpop.blueprints.pgm.util.wrappers;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Query;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * A WrapperQuery is useful for wrapping the construction and results of a Vertex.query().
 * Any necessary Iterable wrapping must occur when Vertex.vertices() or Vertex.edges() is called.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class WrapperQuery implements Query {

    protected final Query query;

    public WrapperQuery(final Query query) {
        this.query = query;
    }

    public Query has(final String key, final Object value) {
        this.query.has(key, value);
        return this;
    }

    public Query has(final String key, final Object value, final Compare compare) {
        this.query.has(key, value, compare);
        return this;
    }

    public Query interval(final String key, final Object startValue, final Object endValue) {
        this.query.interval(key, startValue, endValue);
        return this;
    }

    public Query direction(final Direction direction) {
        this.query.direction(direction);
        return this;
    }

    public Query limit(final long limit) {
        this.query.limit(limit);
        return this;
    }

    public Query labels(final String... labels) {
        this.query.labels(labels);
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