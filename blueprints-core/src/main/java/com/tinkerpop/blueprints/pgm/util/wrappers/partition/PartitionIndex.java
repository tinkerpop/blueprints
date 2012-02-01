package com.tinkerpop.blueprints.pgm.util.wrappers.partition;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.util.PartitionEdgeSequence;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.util.PartitionVertexSequence;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionIndex<T extends Element> implements Index<T> {

    protected Index<T> rawIndex;
    protected PartitionGraph graph;

    public PartitionIndex(final Index<T> rawIndex, final PartitionGraph graph) {
        this.rawIndex = rawIndex;
        this.graph = graph;
    }

    public String getIndexName() {
        return this.rawIndex.getIndexName();
    }

    public Class<T> getIndexClass() {
        return this.rawIndex.getIndexClass();
    }

    public Type getIndexType() {
        return this.rawIndex.getIndexType();
    }

    public long count(final String key, final Object value) {
        long counter = 0;
        for (Element element : this.get(key, value)) {
            counter++;
        }
        return counter;
    }

    public void remove(final String key, final Object value, final T element) {
        this.rawIndex.remove(key, value, (T) ((PartitionElement) element).getRawElement());
    }

    public void put(final String key, final Object value, final T element) {
        this.rawIndex.put(key, value, (T) ((PartitionElement) element).getRawElement());
    }

    public CloseableSequence<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass()))
            return (CloseableSequence<T>) new PartitionVertexSequence((Iterator<Vertex>) this.rawIndex.get(key, value).iterator(), this.graph);
        else
            return (CloseableSequence<T>) new PartitionEdgeSequence((Iterator<Edge>) this.rawIndex.get(key, value).iterator(), this.graph);
    }

    public String toString() {
        return StringFactory.indexString(this);
    }
}
