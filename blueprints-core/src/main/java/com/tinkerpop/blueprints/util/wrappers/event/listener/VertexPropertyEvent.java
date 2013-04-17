package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;

/**
 * Base class for property changed events.
 *
 * @author Stephen Mallette
 */
public abstract class VertexPropertyEvent implements Event {

    private final Vertex vertex;
    private final String key;
    private final Object oldValue;
    private final Object newValue;

    public VertexPropertyEvent(final Vertex vertex, final String key, final Object oldValue, final Object newValue) {

        this.vertex = vertex;
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    abstract void fire(final GraphChangedListener listener, final Vertex vertex, final String key, final Object oldValue, final Object newValue);

    @Override
    public void fireEvent(final Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            fire(eventListeners.next(), vertex, key, oldValue, newValue);
        }
    }
}
