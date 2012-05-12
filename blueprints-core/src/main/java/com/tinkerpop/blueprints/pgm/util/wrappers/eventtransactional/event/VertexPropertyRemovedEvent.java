package com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.GraphChangedListener;

public class VertexPropertyRemovedEvent extends VertexPropertyEvent {

    public VertexPropertyRemovedEvent(Vertex vertex, String key, Object newValue) {
        super(vertex, key, newValue);
    }

    @Override
    void fire(GraphChangedListener listener, Vertex vertex, String key, Object newValue) {
        listener.vertexPropertyRemoved(vertex, key, newValue);
    }
}
