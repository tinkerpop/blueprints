package com.tinkerpop.blueprints.pgm.util.wrappers.id;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Query;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.DefaultQuery;
import com.tinkerpop.blueprints.pgm.util.wrappers.id.util.IdEdgeIterable;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdVertex extends IdElement implements Vertex {

    public IdVertex(final Vertex baseVertex) {
        super(baseVertex);
    }

    public Vertex getBaseVertex() {
        return (Vertex) this.baseElement;
    }

    public Iterable<Edge> getOutEdges(final String... filters) {
        return new IdEdgeIterable(((Vertex) this.baseElement).getOutEdges(filters));
    }

    public Iterable<Edge> getInEdges(final String... filters) {
        return new IdEdgeIterable(((Vertex) this.baseElement).getInEdges(filters));
    }

    @Override
    public Query query() {
        return new DefaultQuery(this);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof IdVertex && ((Vertex) other).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return 274703 + getId().hashCode();
    }
}
