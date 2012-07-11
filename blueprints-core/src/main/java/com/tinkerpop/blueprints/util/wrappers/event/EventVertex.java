package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.WrapperQuery;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

import java.util.List;

/**
 * An vertex with a GraphChangedListener attached.  Those listeners are notified when changes occur to
 * the properties of the vertex.
 *
 * @author Stephen Mallette
 */
public class EventVertex extends EventElement implements Vertex {
    protected EventVertex(final Vertex rawVertex, final List<GraphChangedListener> graphChangedListeners,
                       final EventTrigger trigger) {
        super(rawVertex, graphChangedListeners, trigger);
    }

    public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
        return new EventEdgeIterable(((Vertex) this.baseElement).getEdges(direction, labels), this.graphChangedListeners, trigger);
    }

    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return new EventVertexIterable(((Vertex) this.baseElement).getVertices(direction, labels), this.graphChangedListeners, trigger);
    }

    public Query query() {
        return new WrapperQuery(((Vertex) this.baseElement).query()) {
            @Override
            public Iterable<Vertex> vertices() {
                return new EventVertexIterable(this.query.vertices(), graphChangedListeners, trigger);
            }

            @Override
            public Iterable<Edge> edges() {
                return new EventEdgeIterable(this.query.edges(), graphChangedListeners, trigger);
            }
        };
    }

    public Vertex getBaseVertex() {
        return (Vertex) this.baseElement;
    }
}
