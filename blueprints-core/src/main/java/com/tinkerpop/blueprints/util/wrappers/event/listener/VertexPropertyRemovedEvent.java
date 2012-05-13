package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Vertex;

public class VertexPropertyRemovedEvent extends VertexPropertyEvent {

    public VertexPropertyRemovedEvent(Vertex vertex, String key, Object newValue) {
        super(vertex, key, newValue);
    }

    @Override
    void fire(GraphChangedListener listener, Vertex vertex, String key, Object newValue) {
        listener.vertexPropertyRemoved(vertex, key, newValue);
    }
}
