package com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.GraphChangedListener;

public class EdgePropertyChangedEvent extends EdgePropertyEvent {

    public EdgePropertyChangedEvent(Edge edge, String key, Object newValue) {
        super(edge, key, newValue);
    }

    @Override
    void fire(GraphChangedListener listener, Edge edge, String key, Object newValue) {
        listener.edgePropertyChanged(edge, key, newValue);
    }
}
