package com.tinkerpop.blueprints.pgm.util.wrappers.readonly;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.StringFactory;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyIndex<T extends Element> implements Index<T> {

    protected final Index<T> rawIndex;

    public ReadOnlyIndex(Index<T> rawIndex) {
        this.rawIndex = rawIndex;
    }

    public void remove(final String key, final Object value, final T element) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public void put(final String key, final Object value, final T element) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public CloseableIterable<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return (CloseableIterable<T>) new ReadOnlyVertexIterable((Iterable<Vertex>) this.rawIndex.get(key, value));
        } else {
            return (CloseableIterable<T>) new ReadOnlyEdgeIterable((Iterable<Edge>) this.rawIndex.get(key, value));
        }
    }

    public CloseableIterable<T> query(final String key, final Object query) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return (CloseableIterable<T>) new ReadOnlyVertexIterable((Iterable<Vertex>) this.rawIndex.query(key, query));
        } else {
            return (CloseableIterable<T>) new ReadOnlyEdgeIterable((Iterable<Edge>) this.rawIndex.query(key, query));
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
