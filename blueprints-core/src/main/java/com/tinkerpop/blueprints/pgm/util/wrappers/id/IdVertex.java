package com.tinkerpop.blueprints.pgm.util.wrappers.id;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Query;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.DefaultQuery;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class IdVertex extends IdElement implements Vertex {
    public IdVertex(Vertex base) {
        super(base);
    }

    public Vertex getBase() {
        return (Vertex) base;
    }

    public Iterable<Edge> getOutEdges(String... filters) {
        return new IdEdgeIterable(((Vertex) base).getOutEdges(filters));
    }

    public Iterable<Edge> getInEdges(String... filters) {
        return new IdEdgeIterable(((Vertex) base).getInEdges(filters));
    }

    @Override
    public Query query() {
        return new DefaultQuery(this);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IdVertex
                && ((Vertex) other).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return 274703 + getId().hashCode();
    }
}
