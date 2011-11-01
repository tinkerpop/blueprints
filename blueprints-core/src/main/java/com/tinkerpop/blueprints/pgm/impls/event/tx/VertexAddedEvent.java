package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.Iterator;

public class VertexAddedEvent implements Event {

    private final Vertex vertex;

    public VertexAddedEvent(Vertex vertex) {
        this.vertex = vertex;
    }

    @Override
    public void fireEvent(Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            eventListeners.next().vertexAdded(vertex);
        }
    }
}
