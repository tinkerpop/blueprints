package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Vertex;

public class VertexPropertyRemovedEvent extends VertexPropertyEvent {

    public VertexPropertyRemovedEvent(Vertex vertex, String key, Object removedValue) {
        super(vertex, key, removedValue, null);
    }

    @Override
    void fire(GraphChangedListener listener, Vertex vertex, String key, Object oldValue, Object newValue) {
        listener.vertexPropertyRemoved(vertex, key, oldValue);
    }
}
