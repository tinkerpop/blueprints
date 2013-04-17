package com.tinkerpop.blueprints.util.wrappers.event.listener;


import com.tinkerpop.blueprints.Edge;

import java.util.Iterator;

public class EdgeAddedEvent implements Event {

    private final Edge edge;

    public EdgeAddedEvent(final Edge edge) {
        this.edge = edge;
    }

    @Override
    public void fireEvent(final Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            eventListeners.next().edgeAdded(edge);
        }
    }
}
