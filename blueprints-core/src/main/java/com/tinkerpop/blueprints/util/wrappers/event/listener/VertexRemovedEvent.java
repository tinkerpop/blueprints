package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;
import java.util.Map;

public class VertexRemovedEvent implements Event {

    private final Vertex vertex;
    private final Map<String, Object> props;

    public VertexRemovedEvent(Vertex vertex, Map<String, Object> props) {
        this.vertex = vertex;
        this.props = props;
    }

    @Override
    public void fireEvent(Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            eventListeners.next().vertexRemoved(vertex, props);
        }
    }
}
