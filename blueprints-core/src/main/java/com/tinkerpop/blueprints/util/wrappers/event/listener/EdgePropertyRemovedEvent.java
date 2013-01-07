package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Edge;

public class EdgePropertyRemovedEvent extends EdgePropertyEvent {

    public EdgePropertyRemovedEvent(Edge vertex, String key, Object oldValue) {
        super(vertex, key, oldValue, null);
    }

    @Override
    void fire(GraphChangedListener listener, Edge edge, String key, Object oldValue, Object newValue) {
        listener.edgePropertyRemoved(edge, key, oldValue);
    }
}
