package com.tinkerpop.blueprints.impls.orient;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.Element;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class OrientElementIterator<T extends Element> implements Iterator<T> {

    private final Iterator itty;
    private final OrientBaseGraph graph;

    public OrientElementIterator(final OrientBaseGraph graph, final Iterator itty) {
        this.itty = itty;
        this.graph = graph;
    }

    public boolean hasNext() {
        return this.itty.hasNext();
    }

    @SuppressWarnings("unchecked")
    public T next() {
        OrientElement currentElement = null;

        if (!hasNext())
            throw new NoSuchElementException();

        Object current = itty.next();

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
}