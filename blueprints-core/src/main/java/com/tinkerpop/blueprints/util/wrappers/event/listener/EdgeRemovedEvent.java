package com.tinkerpop.blueprints.util.wrappers.event.listener;


import com.tinkerpop.blueprints.Edge;

import java.util.Iterator;

public class EdgeRemovedEvent implements Event {

    private final Edge edge;

    public EdgeRemovedEvent(Edge edge) {
        this.edge = edge;
    }

    @Override
    public void fireEvent(Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            eventListeners.next().edgeRemoved(edge);
        }
    }
}
