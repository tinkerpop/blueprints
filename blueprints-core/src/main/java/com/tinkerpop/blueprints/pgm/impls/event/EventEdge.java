package com.tinkerpop.blueprints.pgm.impls.event;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.List;

/**
 * An edge with a GraphChangedListener attached.  Those listeners are notified when changes occur to
 * the properties of the edge.
 *
 * @author Stephen Mallette
 */
public class EventEdge extends EventElement implements Edge {

    public EventEdge(final Edge edge, final List<GraphChangedListener> graphChangedListeners) {
        super(edge, graphChangedListeners);
    }

    public Vertex getOutVertex() {
        return new EventVertex(((Edge) this.element).getOutVertex(), this.graphChangedListeners);
    }

    public Vertex getInVertex() {
        return new EventVertex(((Edge) this.element).getInVertex(), this.graphChangedListeners);
    }

    public String getLabel() {
        return ((Edge) this.element).getLabel();
    }

    public Edge getRawEdge() {
        return (Edge) this.element;
    }
}
