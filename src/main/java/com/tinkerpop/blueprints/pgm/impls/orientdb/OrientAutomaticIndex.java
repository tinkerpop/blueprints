package com.tinkerpop.blueprints.pgm.impls.orientdb.util;

import com.orientechnologies.orient.core.record.ORecord.STATUS;
import com.orientechnologies.orient.core.record.impl.ODocument;
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
public class OrientElementSequence<T extends Element> implements Iterator<T>, Iterable<T> {
    private final Iterator<?> underlying;
    private final OrientGraph graph;

    public OrientElementSequence(final OrientGraph graph, final Iterator<?> iUnderlying) {
        this.graph = graph;
        this.underlying = iUnderlying;
    }

    public boolean hasNext() {
        return this.underlying.hasNext();
    }

    @SuppressWarnings("unchecked")
    public T next() {
        OrientElement currentElement=null;
        final Object current = this.underlying.next();
        if( current instanceof ODocument ){
	        final ODocument currentDocument = (ODocument) current;
	        if (null == currentDocument)
	            throw new NoSuchElementException();
	
	        if (currentDocument.getInternalStatus() == STATUS.NOT_LOADED)
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
}
