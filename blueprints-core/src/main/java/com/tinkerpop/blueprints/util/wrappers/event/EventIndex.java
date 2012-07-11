package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

import java.util.List;

/**
 * An index that wraps graph elements in the "evented" way. This class does not directly raise graph events, but
 * passes the GraphChangedListener to the edges and vertices returned from indices so that they may raise graph
 * events.
 *
 * @author Stephen Mallette
 */
class EventIndex<T extends Element> implements Index<T> {
    protected final Index<T> rawIndex;
    protected final List<GraphChangedListener> graphChangedListeners;

    private final EventTrigger trigger;

    public EventIndex(final Index<T> rawIndex, final List<GraphChangedListener> graphChangedListeners,
                      final EventTrigger trigger) {
        this.rawIndex = rawIndex;
        this.graphChangedListeners = graphChangedListeners;
        this.trigger = trigger;
    }

    public void remove(final String key, final Object value, final T element) {
        this.rawIndex.remove(key, value, (T) ((EventElement) element).getBaseElement());
    }

    public void put(final String key, final Object value, final T element) {
        this.rawIndex.put(key, value, (T) ((EventElement) element).getBaseElement());
    }

    public CloseableIterable<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return (CloseableIterable<T>) new EventVertexIterable((Iterable<Vertex>) this.rawIndex.get(key, value),
                    this.graphChangedListeners, this.trigger);
        } else {
            return (CloseableIterable<T>) new EventEdgeIterable((Iterable<Edge>) this.rawIndex.get(key, value),
                    this.graphChangedListeners, this.trigger);
        }
    }

    public CloseableIterable<T> query(final String key, final Object query) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return (CloseableIterable<T>) new EventVertexIterable((Iterable<Vertex>) this.rawIndex.query(key, query),
                    this.graphChangedListeners, this.trigger);
        } else {
            return (CloseableIterable<T>) new EventEdgeIterable((Iterable<Edge>) this.rawIndex.query(key, query),
                    this.graphChangedListeners, this.trigger);
        }
    }

    public long count(final String key, final Object value) {
        return this.rawIndex.count(key, value);
    }

    public String getIndexName() {
        return this.rawIndex.getIndexName();
    }

    public Class<T> getIndexClass() {
        return this.rawIndex.getIndexClass();
    }

    public String toString() {
        return StringFactory.indexString(this);
    }

}
