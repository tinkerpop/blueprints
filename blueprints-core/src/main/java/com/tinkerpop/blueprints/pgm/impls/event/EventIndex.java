package com.tinkerpop.blueprints.pgm.impls.event;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.impls.event.util.EventEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.event.util.EventVertexSequence;

import java.util.Iterator;
import java.util.List;

/**
 * An index that wraps graph elements in the "evented" way.
 */
public class EventIndex<T extends Element> implements Index<T> {
    protected final Index<T> index;
    protected final List<GraphChangedListener> graphChangedListeners;

    public EventIndex(Index<T> index, List<GraphChangedListener> graphChangedListeners) {
        this.index = index;
        this.graphChangedListeners = graphChangedListeners;
    }

    public void remove(final String key, final Object value, final T element) {
        this.index.remove(key, value, element);
    }

    public void put(final String key, final Object value, final T element) {
        this.index.put(key, value, element);
    }

    public CloseableSequence<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return (CloseableSequence<T>) new EventVertexSequence((Iterator<Vertex>) this.index.get(key, value).iterator(), this.graphChangedListeners);
        } else {
            return (CloseableSequence<T>) new EventEdgeSequence((Iterator<Edge>) this.index.get(key, value).iterator(), this.graphChangedListeners);
        }
    }

    public long count(final String key, final Object value) {
        return this.index.count(key, value);
    }

    public Index.Type getIndexType() {
        return Index.Type.MANUAL;
    }

    public String getIndexName() {
        return this.index.getIndexName();
    }

    public Class<T> getIndexClass() {
        return this.index.getIndexClass();
    }

    public String toString() {
        return "(event)" + this.index.toString();
    }

}
