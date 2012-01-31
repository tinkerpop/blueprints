package com.tinkerpop.blueprints.pgm.impls.named.util;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.impls.named.NamedAutomaticIndex;
import com.tinkerpop.blueprints.pgm.impls.named.NamedGraph;
import com.tinkerpop.blueprints.pgm.impls.named.NamedIndex;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NamedIndexSequence<T extends Element> implements Iterator<Index<T>>, Iterable<Index<T>> {

    protected Iterator<Index<T>> itty;
    private final NamedGraph graph;

    public NamedIndexSequence(final Iterator<Index<T>> itty, final NamedGraph graph) {
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
            return new NamedIndex<T>(index, this.graph);
        } else {
            return new NamedAutomaticIndex<T>((AutomaticIndex<T>) index, this.graph);
        }
    }

    public Iterator<Index<T>> iterator() {
        return this;
    }
}
