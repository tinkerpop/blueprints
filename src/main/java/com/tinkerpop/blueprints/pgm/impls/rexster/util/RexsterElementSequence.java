package com.tinkerpop.blueprints.pgm.impls.rexster.util;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterGraph;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class RexsterElementSequence<T extends Element> implements Iterable<T>, Iterator<T> {

    protected final int bufferSize = 100;
    protected int start = 0;
    protected int end = bufferSize;

    protected final Queue<T> queue = new LinkedList<T>();
    protected final RexsterGraph graph;
    protected final String uri;

    public RexsterElementSequence(final String uri, final RexsterGraph graph) {
        this.graph = graph;
        this.uri = uri;
        this.fillBuffer();
    }

    public boolean hasNext() {
        if (!queue.isEmpty())
            return true;
        else {
            fillBuffer();
            return !queue.isEmpty();
        }
    }

    public T next() {
        if (!queue.isEmpty())
            return queue.remove();
        else {
            fillBuffer();
            if (!queue.isEmpty())
                return queue.remove();
            else
                throw new NoSuchElementException();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Iterator<T> iterator() {
        return this;
    }

    protected abstract void fillBuffer();
}
