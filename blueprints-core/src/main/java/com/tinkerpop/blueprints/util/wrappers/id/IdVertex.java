package com.tinkerpop.blueprints.util.wrappers.id;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.WrapperVertexQuery;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdVertex extends IdElement implements Vertex {

    protected IdVertex(final Vertex baseVertex,
                       final IdGraph idGraph) {
        super(baseVertex, idGraph, idGraph.getSupportVertexIds());
    }

    public Vertex getBaseVertex() {
        return (Vertex) this.baseElement;
    }

    public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
        return new IdEdgeIterable(((Vertex) this.baseElement).getEdges(direction, labels), this.idGraph);
    }

    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return new IdVertexIterable(((Vertex) this.baseElement).getVertices(direction, labels), this.idGraph);
    }

    public VertexQuery query() {
        return new WrapperVertexQuery(((Vertex) this.baseElement).query()) {
            @Override
            public Iterable<Vertex> vertices() {
                return new IdVertexIterable(this.query.vertices(), idGraph);
            }

            @Override
            public Iterable<Edge> edges() {
                return new IdEdgeIterable(this.query.edges(), idGraph);
            }
        };
    }

    public void setProperty(final String key, final Object value) {
        super.setProperty(key, value);
    }

    public Edge addEdge(final String label, final Vertex vertex) {
        return this.idGraph.addEdge(null, this, vertex, label);
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }
}
