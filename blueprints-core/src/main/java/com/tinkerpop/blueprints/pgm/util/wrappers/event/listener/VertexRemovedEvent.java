package com.tinkerpop.blueprints.pgm.util.wrappers.event.listener;

import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Iterator;

public class VertexRemovedEvent implements Event {

    private final Vertex vertex;

    public VertexRemovedEvent(Vertex vertex) {
        this.vertex = vertex;
    }

    @Override
    public void fireEvent(Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            eventListeners.next().vertexRemoved(vertex);
        }
    }
}
