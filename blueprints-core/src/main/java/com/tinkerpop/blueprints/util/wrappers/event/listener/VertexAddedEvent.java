package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;

/**
 * Event that fires when a vertex is added to a graph.
 *
 * @author Stephen Mallette
 */
public class VertexAddedEvent implements Event {

    private final Vertex vertex;

    public VertexAddedEvent(final Vertex vertex) {
        this.vertex = vertex;
    }

    @Override
    public void fireEvent(final Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            eventListeners.next().vertexAdded(vertex);
        }
    }
}
