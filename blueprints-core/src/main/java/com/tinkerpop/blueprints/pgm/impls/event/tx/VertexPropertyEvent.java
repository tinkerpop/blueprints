package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.Iterator;

public abstract class VertexPropertyEvent implements Event {

    private final Vertex vertex;
    private final String key;
    private final Object newValue;

    public VertexPropertyEvent(Vertex vertex, String key, Object newValue) {

        this.vertex = vertex;
        this.key = key;
        this.newValue = newValue;
    }

    abstract void fire(GraphChangedListener listener, Vertex vertex, String key, Object newValue);

    @Override
    public void fireEvent(Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()){
            fire(eventListeners.next(), vertex, key, newValue);
        }
    }
}
