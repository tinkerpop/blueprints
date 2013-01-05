package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Edge;

public class EdgePropertyChangedEvent extends EdgePropertyEvent {

    public EdgePropertyChangedEvent(Edge edge, String key, Object oldValue, Object newValue) {
        super(edge, key, oldValue, newValue);
    }

    @Override
    void fire(GraphChangedListener listener, Edge edge, String key, Object oldValue, Object newValue) {
        listener.edgePropertyChanged(edge, key, oldValue, newValue);
    }
}
