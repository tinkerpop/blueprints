package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.wrappers.WrapperVertexQuery;

/**
 * An vertex with a GraphChangedListener attached.  Those listeners are notified when changes occur to
 * the properties of the vertex.
 *
 * @author Stephen Mallette
 */
public class EventVertex extends EventElement implements Vertex {
    public EventVertex(final Vertex rawVertex, final EventGraph eventGraph) {
        super(rawVertex, eventGraph);
    }

    public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
        return new EventEdgeIterable(((Vertex) this.baseElement).getEdges(direction, labels), this.eventGraph);
    }

    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return new EventVertexIterable(((Vertex) this.baseElement).getVertices(direction, labels), this.eventGraph);
    }

    public VertexQuery query() {
        return new WrapperVertexQuery(((Vertex) this.baseElement).query()) {
            @Override
            public Iterable<Vertex> vertices() {
                return new EventVertexIterable(this.query.vertices(), eventGraph);
            }

            @Override
            public Iterable<Edge> edges() {
                return new EventEdgeIterable(this.query.edges(), eventGraph);
            }
        };
    }

    public Edge addEdge(final String label, final Vertex vertex) {
        return this.eventGraph.addEdge(null, this, vertex, label);
    }

    public Vertex getBaseVertex() {
        return (Vertex) this.baseElement;
    }
}
