package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

import java.util.List;

/**
 * An edge with a GraphChangedListener attached.  Those listeners are notified when changes occur to
 * the properties of the edge.
 *
 * @author Stephen Mallette
 */
public class EventEdge extends EventElement implements Edge {

    protected EventEdge(final Edge rawEdge, final List<GraphChangedListener> graphChangedListeners,
                     final EventTrigger trigger) {
        super(rawEdge, graphChangedListeners, trigger);
    }

    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        return new EventVertex(this.getBaseEdge().getVertex(direction), this.graphChangedListeners, this.trigger);
    }

    public String getLabel() {
        return ((Edge) this.baseElement).getLabel();
    }

    public Edge getBaseEdge() {
        return (Edge) this.baseElement;
    }
}
