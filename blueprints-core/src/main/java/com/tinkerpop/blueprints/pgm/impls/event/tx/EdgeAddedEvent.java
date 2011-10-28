package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.event.Event;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: toby.orourke
 * Date: 28/10/2011
 * Time: 15:32
 * To change this template use File | Settings | File Templates.
 */
class EdgeAddedEvent extends Event {

    private final Edge edge;

    public EdgeAddedEvent(Edge edge) {
        this.edge = edge;
    }

    @Override
    public void fireEvent(Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            eventListeners.next().edgeAdded(edge);
        }
    }
}
