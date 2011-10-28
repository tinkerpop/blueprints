package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.Vertex;
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
abstract class VertexPropertyEvent extends Event {

    private final Vertex vertex;
    private final String key;
    private final Object newValue;

    public VertexPropertyEvent(Vertex vertex, String key, Object newValue) {

        this.vertex = vertex;
        this.key = key;
        this.newValue = newValue;
    }

    abstract void fire(GraphChangedListener listener, Vertex vertex, String key, Object newValue);

    @Override
    public void fireEvent(Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()){
            fire(eventListeners.next(), vertex, key, newValue);
        }
    }
}
