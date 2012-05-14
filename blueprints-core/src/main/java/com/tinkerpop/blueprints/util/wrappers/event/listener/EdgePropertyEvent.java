package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Edge;

import java.util.Iterator;

public abstract class EdgePropertyEvent implements Event {

    private final Edge edge;
    private final String key;
    private final Object newValue;

    public EdgePropertyEvent(Edge edge, String key, Object newValue) {
        this.edge = edge;
        this.key = key;
        this.newValue = newValue;
    }

    abstract void fire(GraphChangedListener listener, Edge edge, String key, Object newValue);

    @Override
    public void fireEvent(Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            fire(eventListeners.next(), edge, key, newValue);
        }
    }
}
