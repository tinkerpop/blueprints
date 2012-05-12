package com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.EventIndex;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.Event;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.util.EventTransactionalEdgeIterable;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.util.EventTransactionalVertexIterable;

import java.util.List;

public class EventTransactionalIndex<T extends Element> extends EventIndex<T> {
    private final ThreadLocal<List<Event>> eventBuffer;

    public EventTransactionalIndex(final Index<T> rawIndex, final List<GraphChangedListener> graphChangedListeners,
                                   final ThreadLocal<List<Event>> eventBuffer) {
        super(rawIndex, graphChangedListeners);
        this.eventBuffer = eventBuffer;
    }

    public CloseableIterable<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return (CloseableIterable<T>) new EventTransactionalVertexIterable((Iterable<Vertex>) this.rawIndex.get(key, value), this.graphChangedListeners, this.eventBuffer);
        } else {
            return (CloseableIterable<T>) new EventTransactionalEdgeIterable((Iterable<Edge>) this.rawIndex.get(key, value), this.graphChangedListeners, this.eventBuffer);
        }
    }

    public CloseableIterable<T> query(final String key, final Object query) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return (CloseableIterable<T>) new EventTransactionalVertexIterable((Iterable<Vertex>) this.rawIndex.query(key, query), this.graphChangedListeners, this.eventBuffer);
        } else {
            return (CloseableIterable<T>) new EventTransactionalEdgeIterable((Iterable<Edge>) this.rawIndex.query(key, query), this.graphChangedListeners, this.eventBuffer);
        }
    }


}
