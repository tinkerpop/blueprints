package com.tinkerpop.blueprints.util.wrappers;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;

/**
 * A WrapperQuery is useful for wrapping the construction and results of a Vertex.query().
 * Any necessary Iterable wrapping must occur when Vertex.vertices() or Vertex.edges() is called.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class WrapperVertexQuery implements VertexQuery {

    protected VertexQuery query;

    public WrapperVertexQuery(final VertexQuery query) {
        this.query = query;
    }

    public VertexQuery has(final String key, final Object value) {
        this.query = this.query.has(key, value);
        return this;
    }

    public <T extends Comparable<T>> VertexQuery has(final String key, final T value, final Compare compare) {
        return this.has(key, compare, value);
    }

    public <T extends Comparable<T>> VertexQuery has(final String key, final Compare compare, final T value) {
        this.query = this.query.has(key, compare, value);
        return this;
    }

    public <T extends Comparable<T>> VertexQuery interval(final String key, final T startValue, final T endValue) {
        this.query = this.query.interval(key, startValue, endValue);
        return this;
    }

    public VertexQuery direction(final Direction direction) {
        this.query = this.query.direction(direction);
        return this;
    }

    public VertexQuery limit(final long limit) {
        this.query = this.query.limit(limit);
        return this;
    }

    public VertexQuery labels(final String... labels) {
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