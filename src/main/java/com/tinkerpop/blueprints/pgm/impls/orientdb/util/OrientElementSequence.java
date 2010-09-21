package com.tinkerpop.blueprints.pgm.impls.orientdb.util;

import com.orientechnologies.orient.core.db.graph.OGraphEdge;
import com.orientechnologies.orient.core.db.graph.OGraphElement;
import com.orientechnologies.orient.core.db.graph.OGraphVertex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientEdge;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientVertex;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientElementSequence<T extends Element> implements Iterator<T>, Iterable<T> {
    private final Iterator<? extends OGraphElement> elements;
    private final OrientGraph graph;

    public OrientElementSequence(final OrientGraph iGraph, final Iterator<? extends OGraphElement> edges) {
        this.graph = iGraph;
        this.elements = edges;
    }

    public boolean hasNext() {
        return this.elements.hasNext();
    }

    public T next() {
        OGraphElement e = this.elements.next();
        if (null == e)
            throw new NoSuchElementException();

        // TRY IN CACHE
        final Element el = this.graph.getCachedElement(e.getDocument().getIdentity());
        if (el != null)
            return (T) el;

        if (e instanceof OGraphEdge)
            return (T) new OrientEdge(graph, (OGraphEdge) e);
        else
            return (T) new OrientVertex(graph, (OGraphVertex) e);
    }

    public void remove() {
        this.elements.remove();
    }

    public Iterator<T> iterator() {
        return this;
    }
}
