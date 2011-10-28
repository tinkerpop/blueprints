package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: toby.orourke
 * Date: 28/10/2011
 * Time: 15:32
 * To change this template use File | Settings | File Templates.
 */
class VertexAddedEvent extends Event {

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
