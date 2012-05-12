package com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.EventVertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.Event;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.VertexPropertyChangedEvent;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.VertexPropertyRemovedEvent;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.util.EventTransactionalEdgeIterable;

import java.util.List;

public class EventTransactionalVertex extends EventVertex implements Vertex {

    private final ThreadLocal<List<Event>> eventBuffer;

    public EventTransactionalVertex(final Vertex vertex, final List<GraphChangedListener> listeners,
                                    final ThreadLocal<List<Event>> eventBuffer) {
        super(vertex, listeners);
        this.eventBuffer = eventBuffer;
    }

    @Override
    public Iterable<Edge> getInEdges(final String... labels) {
        return new EventTransactionalEdgeIterable(this.getBaseVertex().getInEdges(labels), this.graphChangedListeners, eventBuffer);
    }

    @Override
    public Iterable<Edge> getOutEdges(final String... labels) {
        return new EventTransactionalEdgeIterable(this.getBaseVertex().getOutEdges(labels), this.graphChangedListeners, eventBuffer);
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
