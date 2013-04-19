package com.tinkerpop.blueprints.util.wrappers.event.listener;


import com.tinkerpop.blueprints.Edge;

import java.util.Iterator;
import java.util.Map;

/**
 * Event fired when an edge is removed.
 *
 * @author Stephen Mallette
 */
public class EdgeRemovedEvent implements Event {

    private final Edge edge;
    private final Map<String, Object> props;

    public EdgeRemovedEvent(final Edge edge, final Map<String, Object> props) {
        this.edge = edge;
        this.props = props;
    }

    @Override
    public void fireEvent(final Iterator<GraphChangedListener> eventListeners) {
        while (eventListeners.hasNext()) {
            eventListeners.next().edgeRemoved(edge, props);
        }
    }
}
