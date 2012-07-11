package com.tinkerpop.blueprints.util.wrappers.partition;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class PartitionIndexIterable<T extends Element> implements Iterable<Index<T>> {

    protected Iterable<Index<T>> iterable;
    private final PartitionGraph graph;

    public PartitionIndexIterable(final Iterable<Index<T>> iterable, final PartitionGraph graph) {
        this.iterable = iterable;
        this.graph = graph;
    }

    public Iterator<Index<T>> iterator() {
        return new Iterator<Index<T>>() {
            private final Iterator<Index<T>> itty = iterable.iterator();

            public void remove() {
                this.itty.remove();
            }

            public boolean hasNext() {
                return this.itty.hasNext();
            }

            public Index<T> next() {
                return new PartitionIndex<T>(this.itty.next(), graph);
            }
        };
    }
}
