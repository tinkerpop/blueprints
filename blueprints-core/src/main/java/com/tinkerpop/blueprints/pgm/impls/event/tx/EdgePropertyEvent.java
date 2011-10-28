package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.event.Event;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: toby.orourke
 * Date: 28/10/2011
 * Time: 15:31
 * To change this template use File | Settings | File Templates.
 */
abstract class EdgePropertyEvent extends Event {

    private final Edge edge;
    private final String key;
    private final Object newValue;

    public EdgePropertyEvent(Edge edge, String key, Object newValue) {
        this.edge = edge;
        this.key = key;
        this.newValue = newValue;
    }

    abstract void fire(GraphChangedListener listener, Edge edge, String key, Object newValue);

    @Override
    public void fireEvent(Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            fire(eventListeners.next(), edge, key, newValue);
        }
    }
}
