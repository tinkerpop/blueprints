package com.tinkerpop.blueprints.pgm.impls.orientdb.util;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientEdge;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientElement;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientVertex;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientElementSequence<T extends Element> implements CloseableSequence<T> {
    private final Iterator<?> underlying;
    private final OrientGraph graph;

    public OrientElementSequence(final OrientGraph graph, final Iterator<?> rawIterator) {
        this.graph = graph;
        this.underlying = rawIterator;
    }

    public boolean hasNext() {
        return this.underlying.hasNext();
    }

    @SuppressWarnings("unchecked")
    public T next() {
        OrientElement currentElement = null;
        Object current = this.underlying.next();

        if (null == current)
            throw new NoSuchElementException();

        if (current instanceof ORID)
            current = graph.getRawGraph().load((ORID) current);

        if (current instanceof ODocument) {
            final ODocument currentDocument = (ODocument) current;

            if (currentDocument.getInternalStatus() == ODocument.STATUS.NOT_LOADED)
                currentDocument.load();

            if (currentDocument.getSchemaClass().isSubClassOf(graph.getRawGraph().getEdgeBaseClass()))
                currentElement = new OrientEdge(graph, currentDocument);
            else
                currentElement = new OrientVertex(graph, currentDocument);
        }

        return (T) currentElement;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Iterator<T> iterator() {
        return this;
    }

    public void close() {

    }
}