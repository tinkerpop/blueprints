package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Vertex;

public class VertexPropertyChangedEvent extends VertexPropertyEvent {

    public VertexPropertyChangedEvent(Vertex vertex, String key, Object oldValue, Object newValue) {
        super(vertex, key, oldValue, newValue);
    }

    @Override
    void fire(GraphChangedListener listener, Vertex vertex, String key, Object oldValue, Object newValue) {
        listener.vertexPropertyChanged(vertex, key, oldValue, newValue);
    }
}
