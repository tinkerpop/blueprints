package com.tinkerpop.blueprints.pgm.impls.orientdb.util;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.ORecord.STATUS;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientEdge;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientElement;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientVertex;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientElementSequence<T extends Element> implements Iterator<T>, Iterable<T> {
    private final Iterator<?> underlying;
    private final OrientGraph graph;
    private ODocument currentDocument;
    private OrientElement currentElement;

    public OrientElementSequence(final OrientGraph graph, final Iterator<?> iUnderlying) {
        this.graph = graph;
        this.underlying = iUnderlying;
    }

    public boolean hasNext() {
        return this.underlying.hasNext();
    }

    @SuppressWarnings("unchecked")
    public T next() {
        currentDocument = (ODocument) this.underlying.next();
        if (null == currentDocument)
            throw new NoSuchElementException();

        if (currentDocument.getInternalStatus() == STATUS.NOT_LOADED)
            currentDocument.load();

        currentElement = this.graph.getCache().get(currentDocument.getIdentity());
        if (currentElement != null)
            return (T) currentElement;

        if (currentDocument.getSchemaClass().isSubClassOf(graph.getRawGraph().getEdgeBaseClass()))
            currentElement = new OrientEdge(graph, currentDocument);
        else
            currentElement = new OrientVertex(graph, currentDocument);

        this.graph.getCache().put((ORecordId) currentElement.getId(), currentElement);

        return (T) currentElement;
    }

    public void remove() {
        if (currentElement != null) {
            if (currentElement instanceof Edge)
                graph.removeEdge((Edge) currentElement);
            else
                graph.removeVertex((Vertex) currentElement);
            currentElement = null;
            currentDocument = null;
        }
    }

    public Iterator<T> iterator() {
        return this;
    }
}
