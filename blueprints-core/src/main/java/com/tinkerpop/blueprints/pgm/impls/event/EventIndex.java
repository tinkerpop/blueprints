package com.tinkerpop.blueprints.pgm.impls.event;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.impls.event.util.EventEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.event.util.EventVertexSequence;

import java.util.Iterator;
import java.util.List;

/**
 * An index that wraps graph elements in the "evented" way. This class does not directly raise graph events, but
 * passes the GraphChangedListener to the edges and vertices returned from indices so that they may raise graph
 * events.
 *
 * @author Stephen Mallette
 */
public class EventIndex<T extends Element> implements Index<T> {
    protected final Index<T> rawIndex;
    protected final List<GraphChangedListener> graphChangedListeners;

    public EventIndex(final Index<T> rawIndex, final List<GraphChangedListener> graphChangedListeners) {
        this.rawIndex = rawIndex;
        this.graphChangedListeners = graphChangedListeners;
    }

    public void remove(final String key, final Object value, final T element) {
        this.rawIndex.remove(key, value, (T) ((EventElement) element).getRawElement());
    }

    public void put(final String key, final Object value, final T element) {
        this.rawIndex.put(key, value, (T) ((EventElement) element).getRawElement());
    }

    public CloseableSequence<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return (CloseableSequence<T>) new EventVertexSequence((Iterator<Vertex>) this.rawIndex.get(key, value).iterator(), this.graphChangedListeners);
        } else {
            return (CloseableSequence<T>) new EventEdgeSequence((Iterator<Edge>) this.rawIndex.get(key, value).iterator(), this.graphChangedListeners);
        }
    }

    public long count(final String key, final Object value) {
        return this.rawIndex.count(key, value);
    }

    public Index.Type getIndexType() {
        return Index.Type.MANUAL;
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
