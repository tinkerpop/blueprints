package com.tinkerpop.blueprints.pgm.impls.event.tx;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

/**
 * Created by IntelliJ IDEA.
 * User: toby.orourke
 * Date: 28/10/2011
 * Time: 15:31
 * To change this template use File | Settings | File Templates.
 */
class VertexPropertyRemovedEvent extends VertexPropertyEvent {

    public VertexPropertyRemovedEvent(Vertex vertex, String key, Object newValue) {
        super(vertex, key, newValue);
    }

    @Override
    void fire(GraphChangedListener listener, Vertex vertex, String key, Object newValue) {
        listener.vertexPropertyRemoved(vertex, key, newValue);
    }
}
