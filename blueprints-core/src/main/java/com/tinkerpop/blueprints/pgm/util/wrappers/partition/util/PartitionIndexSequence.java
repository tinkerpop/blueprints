package com.tinkerpop.blueprints.pgm.util.wrappers.partition.util;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.PartitionAutomaticIndex;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.PartitionGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.PartitionIndex;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionIndexSequence<T extends Element> implements Iterator<Index<T>>, Iterable<Index<T>> {

    protected Iterator<Index<T>> itty;
    private final PartitionGraph graph;

    public PartitionIndexSequence(final Iterator<Index<T>> itty, final PartitionGraph graph) {
        this.itty = itty;
        this.graph = graph;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return this.itty.hasNext();
    }

    public Index<T> next() {
        final Index<T> index = itty.next();
        if (index.getIndexType().equals(Index.Type.MANUAL)) {
            return new PartitionIndex<T>(index, this.graph);
        } else {
            return new PartitionAutomaticIndex<T>((AutomaticIndex<T>) index, this.graph);
        }
    }

    public Iterator<Index<T>> iterator() {
        return this;
    }
}
