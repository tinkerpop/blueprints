package com.tinkerpop.blueprints.pgm.util.wrappers.wrapped;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.util.wrappers.wrapped.util.WrappedEdgeIterable;
import com.tinkerpop.blueprints.pgm.util.wrappers.wrapped.util.WrappedVertexIterable;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedIndex<T extends Element> implements Index<T> {

    protected Index<T> rawIndex;

    public WrappedIndex(final Index<T> rawIndex) {
        this.rawIndex = rawIndex;
    }

    public String getIndexName() {
        return this.rawIndex.getIndexName();
    }

    public Class<T> getIndexClass() {
        return this.rawIndex.getIndexClass();
    }

    public long count(final String key, final Object value) {
        return this.rawIndex.count(key, value);
    }

    public void remove(final String key, final Object value, final T element) {
        this.rawIndex.remove(key, value, (T) ((WrappedElement) element).getBaseElement());
    }

    public void put(final String key, final Object value, final T element) {
        this.rawIndex.put(key, value, (T) ((WrappedElement) element).getBaseElement());
    }

    public CloseableIterable<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass()))
            return (CloseableIterable<T>) new WrappedVertexIterable((Iterable<Vertex>) this.rawIndex.get(key, value));
        else
            return (CloseableIterable<T>) new WrappedEdgeIterable((Iterable<Edge>) this.rawIndex.get(key, value));
    }

    public CloseableIterable<T> query(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass()))
            return (CloseableIterable<T>) new WrappedVertexIterable((Iterable<Vertex>) this.rawIndex.query(key, value));
        else
            return (CloseableIterable<T>) new WrappedEdgeIterable((Iterable<Edge>) this.rawIndex.query(key, value));
    }

    public String toString() {
        return StringFactory.indexString(this);
    }
}
