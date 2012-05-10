package com.tinkerpop.blueprints.pgm.impls.orientdb.util;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.CloseableIterable;
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
public class OrientElementIterable<T extends Element> implements CloseableIterable<T>, Iterator<T> {
    private final Iterator<?> rawIterator;
    private final OrientGraph graph;

    public OrientElementIterable(final OrientGraph graph, final Iterator<?> rawIterator) {
        this.graph = graph;
        this.rawIterator = rawIterator;
    }

    public boolean hasNext() {
        return this.rawIterator.hasNext();
    }

    @SuppressWarnings("unchecked")
    public T next() {
        OrientElement currentElement = null;
        
        if (!hasNext())
            throw new NoSuchElementException();

        Object current = this.rawIterator.next();

        if (null == current)
            throw new NoSuchElementException();

        if (current instanceof OIdentifiable)
            current = ((OIdentifiable) current).getRecord();

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