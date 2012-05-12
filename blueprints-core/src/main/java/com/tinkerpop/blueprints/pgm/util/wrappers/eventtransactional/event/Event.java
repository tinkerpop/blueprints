package com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event;

import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.GraphChangedListener;

import java.util.Iterator;

public interface Event {

    public void fireEvent(Iterator<GraphChangedListener> eventListeners);

}
