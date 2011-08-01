package com.tinkerpop.blueprints.pgm.impls.readonly;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.readonly.util.ReadOnlyEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.readonly.util.ReadOnlyVertexSequence;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyIndex<T extends Element> implements Index<T> {

    protected final Index<T> index;

    public ReadOnlyIndex(Index<T> index) {
        this.index = index;
    }

    public void remove(final String key, final Object value, final T element) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public void put(final String key, final Object value, final T element) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public CloseableSequence<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return (CloseableSequence<T>) new ReadOnlyVertexSequence((Iterator<Vertex>) this.index.get(key, value).iterator());
        } else {
            return (CloseableSequence<T>) new ReadOnlyEdgeSequence((Iterator<Edge>) this.index.get(key, value).iterator());
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
        return "(readonly)" + this.index.toString();
    }

}
