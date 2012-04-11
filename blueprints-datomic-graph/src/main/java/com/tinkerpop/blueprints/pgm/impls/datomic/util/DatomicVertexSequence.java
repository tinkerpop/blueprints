package com.tinkerpop.blueprints.pgm.impls.datomic.util;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.datomic.DatomicGraph;
import com.tinkerpop.blueprints.pgm.impls.datomic.DatomicVertex;
import java.util.Iterator;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicVertexSequence<T extends Vertex> implements CloseableSequence<DatomicVertex> {

    private final Iterator<Object> ids;
    private final DatomicGraph graph;

    public DatomicVertexSequence(final Iterable<Object> ids, final DatomicGraph graph) {
        this.graph = graph;
        this.ids = ids.iterator();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public DatomicVertex next() {
        return new DatomicVertex(this.graph, this.ids.next());
    }

    public boolean hasNext() {
        return this.ids.hasNext();
    }

    public Iterator<DatomicVertex> iterator() {
        return this;
    }

    public void close() {
    }
}