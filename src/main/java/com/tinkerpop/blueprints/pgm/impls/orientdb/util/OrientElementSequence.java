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
    private final Iterator<? extends OGraphElement> rawElements;
    private final OrientGraph graph;

    public OrientElementSequence(final OrientGraph graph, final Iterator<? extends OGraphElement> rawElements) {
        this.graph = graph;
        this.rawElements = rawElements;
    }

    public boolean hasNext() {
        return this.rawElements.hasNext();
    }

    public T next() {
        OGraphElement rawElement = this.rawElements.next();
        if (null == rawElement)
            throw new NoSuchElementException();

        if (rawElement instanceof OGraphEdge)
            return (T) new OrientEdge(graph, (OGraphEdge) rawElement);
        else
            return (T) new OrientVertex(graph, (OGraphVertex) rawElement);
    }

    public void remove() {
        this.rawElements.remove();
    }

    public Iterator<T> iterator() {
        return this;
    }
}
