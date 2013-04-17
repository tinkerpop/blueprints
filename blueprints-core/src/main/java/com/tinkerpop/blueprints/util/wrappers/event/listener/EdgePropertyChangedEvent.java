package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Edge;

public class EdgePropertyChangedEvent extends EdgePropertyEvent {

    public EdgePropertyChangedEvent(final Edge edge, final String key, final Object oldValue, final Object newValue) {
        super(edge, key, oldValue, newValue);
    }

    @Override
    void fire(final GraphChangedListener listener, final Edge edge, final String key, final Object oldValue, final Object newValue) {
        listener.edgePropertyChanged(edge, key, oldValue, newValue);
    }
}
