package com.tinkerpop.blueprints.pgm.impls.datomic.util;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.datomic.DatomicEdge;
import com.tinkerpop.blueprints.pgm.impls.datomic.DatomicGraph;
import java.util.Iterator;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicEdgeSequence<T extends Edge> implements CloseableSequence<DatomicEdge> {

    private final Iterator<Object> ids;
    private final DatomicGraph graph;

    public DatomicEdgeSequence(final Iterable<Object> ids, final DatomicGraph graph) {
        this.graph = graph;
        this.ids = ids.iterator();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public DatomicEdge next() {
        return new DatomicEdge(this.graph, this.ids.next());
    }

    public boolean hasNext() {
        return this.ids.hasNext();
    }

    public Iterator<DatomicEdge> iterator() {
        return this;
    }

    public void close() {
    }
}