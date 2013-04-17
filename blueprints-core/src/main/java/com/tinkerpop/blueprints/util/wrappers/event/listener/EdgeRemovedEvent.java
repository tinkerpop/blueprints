package com.tinkerpop.blueprints.util.wrappers.event.listener;


import com.tinkerpop.blueprints.Edge;

import java.util.Iterator;

/**
 * Event fired when an edge is removed.
 *
 * @author Stephen Mallette
 */
public class EdgeRemovedEvent implements Event {

    private final Edge edge;

    public EdgeRemovedEvent(final Edge edge) {
        this.edge = edge;
    }

    @Override
    public void fireEvent(final Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            eventListeners.next().edgeRemoved(edge);
        }
    }
}
