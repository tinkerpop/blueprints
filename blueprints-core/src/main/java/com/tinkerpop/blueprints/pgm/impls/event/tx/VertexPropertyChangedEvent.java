package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

public class VertexPropertyChangedEvent extends VertexPropertyEvent {

    public VertexPropertyChangedEvent(Vertex vertex, String key, Object newValue) {
        super(vertex, key, newValue);
    }

    @Override
    void fire(GraphChangedListener listener, Vertex vertex, String key, Object newValue) {
        listener.vertexPropertyChanged(vertex, key, newValue);
    }
}
