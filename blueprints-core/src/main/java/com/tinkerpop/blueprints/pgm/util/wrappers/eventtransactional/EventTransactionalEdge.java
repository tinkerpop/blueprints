package com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.EventEdge;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.EdgePropertyChangedEvent;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.EdgePropertyRemovedEvent;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.Event;

import java.util.List;

public class EventTransactionalEdge extends EventEdge implements Edge {
    private final ThreadLocal<List<Event>> eventBuffer;

    public EventTransactionalEdge(Edge eventEdge, List<GraphChangedListener> graphChangedListeners, ThreadLocal<List<Event>> eventBuffer) {
        super(eventEdge, graphChangedListeners);
        this.eventBuffer = eventBuffer;
    }

    @Override
    public Vertex getOutVertex() {
        return new EventTransactionalVertex(this.getBaseEdge().getOutVertex(), this.graphChangedListeners, eventBuffer);
    }

    @Override
    public Vertex getInVertex() {
        return new EventTransactionalVertex(this.getBaseEdge().getInVertex(), this.graphChangedListeners, eventBuffer);
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
