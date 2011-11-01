package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.event.EventEdge;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.impls.event.tx.EdgePropertyChangedEvent;
import com.tinkerpop.blueprints.pgm.impls.event.tx.EdgePropertyRemovedEvent;
import com.tinkerpop.blueprints.pgm.impls.event.tx.Event;

import java.util.List;

public class EventTransactionalEdge extends EventEdge implements Edge {
    private final ThreadLocal<List<Event>> eventBuffer;

    public EventTransactionalEdge(EventEdge eventEdge, List<GraphChangedListener> graphChangedListeners, ThreadLocal<List<Event>> eventBuffer) {
        super(eventEdge.getRawEdge(), graphChangedListeners);
        this.eventBuffer = eventBuffer;
    }

    @Override
    protected void onEdgePropertyChanged(Edge edge, String key, Object newValue) {
        eventBuffer.get().add(new EdgePropertyChangedEvent(edge, key, newValue));
    }

    @Override
    protected void onEdgePropertyRemoved(Edge edge, String key, Object newValue) {
        eventBuffer.get().add(new EdgePropertyRemovedEvent(edge, key, newValue));
    }
}

