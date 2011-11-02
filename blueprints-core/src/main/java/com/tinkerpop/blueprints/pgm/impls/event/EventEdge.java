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

    public EventEdge(final Edge rawEdge, final List<GraphChangedListener> graphChangedListeners) {
        super(rawEdge, graphChangedListeners);
    }

    public Vertex getOutVertex() {
        return new EventVertex(((Edge) this.rawElement).getOutVertex(), this.graphChangedListeners);
    }

    public Vertex getInVertex() {
        return new EventVertex(((Edge) this.rawElement).getInVertex(), this.graphChangedListeners);
    }

    public String getLabel() {
        return ((Edge) this.rawElement).getLabel();
    }

    public Edge getRawEdge() {
        return (Edge) this.rawElement;
    }
}
