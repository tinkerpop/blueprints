package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Edge;

import java.util.Iterator;

public abstract class EdgePropertyEvent implements Event {

    private final Edge edge;
    private final String key;
    private final Object oldValue;
    private final Object newValue;

    public EdgePropertyEvent(final Edge edge, final String key, final Object oldValue, final Object newValue) {
        this.edge = edge;
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    abstract void fire(final GraphChangedListener listener, final Edge edge, final String key, final Object oldValue, final Object newValue);

    @Override
    public void fireEvent(final Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            fire(eventListeners.next(), edge, key, oldValue, newValue);
        }
    }
}
