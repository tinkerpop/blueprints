package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * An edge with a GraphChangedListener attached.  Those listeners are notified when changes occur to
 * the properties of the edge.
 *
 * @author Stephen Mallette
 */
public class EventEdge extends EventElement implements Edge {

    public EventEdge(final Edge rawEdge, final EventGraph eventGraph) {
        super(rawEdge, eventGraph);
    }

    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        return new EventVertex(this.getBaseEdge().getVertex(direction), this.eventGraph);
    }

    public String getLabel() {
        return ((Edge) this.baseElement).getLabel();
    }

    public Edge getBaseEdge() {
        return (Edge) this.baseElement;
    }
}
