package com.tinkerpop.blueprints.pgm.util.wrappers.partition;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.StringFactory;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.util.PartitionEdgeIterable;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.util.PartitionVertexIterable;

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

    public long count(final String key, final Object value) {
        long counter = 0;
        for (Element element : this.get(key, value)) {
            counter++;
        }
        return counter;
    }

    public void remove(final String key, final Object value, final T element) {
        this.rawIndex.remove(key, value, (T) ((PartitionElement) element).getBaseElement());
    }

    public void put(final String key, final Object value, final T element) {
        this.rawIndex.put(key, value, (T) ((PartitionElement) element).getBaseElement());
    }

    public CloseableIterable<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass()))
            return (CloseableIterable<T>) new PartitionVertexIterable((Iterable<Vertex>) this.rawIndex.get(key, value), this.graph);
        else
            return (CloseableIterable<T>) new PartitionEdgeIterable((Iterable<Edge>) this.rawIndex.get(key, value), this.graph);
    }

    public CloseableIterable<T> query(final String key, final Object query) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return (CloseableIterable<T>) new PartitionVertexIterable((Iterable<Vertex>) this.rawIndex.query(key, query), this.graph);
        } else {
            return (CloseableIterable<T>) new PartitionEdgeIterable((Iterable<Edge>) this.rawIndex.query(key, query), this.graph);
        }
    }


    public String toString() {
        return StringFactory.indexString(this);
    }
}
