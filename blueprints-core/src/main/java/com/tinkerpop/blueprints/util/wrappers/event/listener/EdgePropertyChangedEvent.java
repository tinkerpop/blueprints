package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Edge;

public class EdgePropertyChangedEvent extends EdgePropertyEvent {

    public EdgePropertyChangedEvent(Edge edge, String key, Object newValue) {
        super(edge, key, newValue);
    }

    @Override
    void fire(GraphChangedListener listener, Edge edge, String key, Object newValue) {
        listener.edgePropertyChanged(edge, key, newValue);
    }
}
