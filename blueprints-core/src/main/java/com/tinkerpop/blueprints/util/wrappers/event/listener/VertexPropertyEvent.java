package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;

public abstract class VertexPropertyEvent implements Event {

    private final Vertex vertex;
    private final String key;
    private final Object oldValue;
    private final Object newValue;

    public VertexPropertyEvent(Vertex vertex, String key, Object oldValue, Object newValue) {

        this.vertex = vertex;
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    abstract void fire(GraphChangedListener listener, Vertex vertex, String key, Object oldValue, Object newValue);

    @Override
    public void fireEvent(Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            fire(eventListeners.next(), vertex, key, oldValue, newValue);
        }
    }
}
