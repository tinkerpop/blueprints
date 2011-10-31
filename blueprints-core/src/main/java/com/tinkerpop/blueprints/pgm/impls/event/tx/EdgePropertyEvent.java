package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

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
