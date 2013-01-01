package com.tinkerpop.blueprints.impls.orient;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Element;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrientElementIterable<T extends Element> implements CloseableIterable<T> {

    private final Iterable<?> iterable;
    private final OrientBaseGraph graph;

    public OrientElementIterable(final OrientBaseGraph graph, final Iterable<?> iterable) {
        this.graph = graph;
        this.iterable = iterable;
    }

    public Iterator<T> iterator() {
        return new OrientElementIterator<T>(this.graph, this.iterable.iterator());
    }

    public void close() {

    }

}