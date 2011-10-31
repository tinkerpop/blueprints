package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.EventVertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.impls.event.tx.Event;
import com.tinkerpop.blueprints.pgm.impls.event.tx.VertexPropertyChangedEvent;
import com.tinkerpop.blueprints.pgm.impls.event.tx.VertexPropertyRemovedEvent;

import java.util.List;

public class EventTransactionalVertex extends EventVertex implements Vertex {


    private final ThreadLocal<List<Event>> eventBuffer;

    public EventTransactionalVertex(final EventVertex vertex, final List<GraphChangedListener> listeners, final ThreadLocal<List<Event>> eventBuffer) {
        super(vertex.getRawVertex(), listeners);
        this.eventBuffer = eventBuffer;
    }

    @Override
    protected void onVertexPropertyChanged(Vertex vertex, String key, Object newValue) {
        eventBuffer.get().add(new VertexPropertyChangedEvent(vertex, key, newValue));
    }

    @Override
    protected void onVertexPropertyRemoved(Vertex vertex, String key, Object newValue) {
        eventBuffer.get().add(new VertexPropertyRemovedEvent(vertex, key, newValue));
    }
}

