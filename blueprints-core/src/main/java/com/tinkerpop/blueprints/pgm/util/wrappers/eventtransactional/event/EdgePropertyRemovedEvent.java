package com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.GraphChangedListener;

public class EdgePropertyRemovedEvent extends EdgePropertyEvent {

    public EdgePropertyRemovedEvent(Edge vertex, String key, Object newValue) {
        super(vertex, key, newValue);
    }

    @Override
    void fire(GraphChangedListener listener, Edge edge, String key, Object newValue) {
        listener.edgePropertyRemoved(edge, key, newValue);
    }
}
