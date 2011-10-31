package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.Iterator;

public interface Event {

    public void fireEvent(Iterator<GraphChangedListener> eventListeners);

}
