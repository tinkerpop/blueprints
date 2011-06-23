package com.tinkerpop.blueprints.pgm.impls.rexster.util;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterGraph;
import com.tinkerpop.blueprints.pgm.impls.rexster.RexsterTokens;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class RexsterElementSequence<T extends Element> implements CloseableSequence<T> {

    protected int start = 0;
    protected int end;

    protected final Queue<T> queue = new LinkedList<T>();
    protected final RexsterGraph graph;
    protected final String uri;


    public RexsterElementSequence(final String uri, final RexsterGraph graph) {
        this.graph = graph;
        this.uri = uri;
        this.end = graph.getBufferSize();
        this.fillBuffer();
    }

    public boolean hasNext() {
        if (!queue.isEmpty())
            return true;
        else {
            if (this.end > this.start) // last buffer if start == end
                fillBuffer();
            return !queue.isEmpty();
        }
    }

    public T next() {
        if (!queue.isEmpty()) {
            return queue.remove();
        } else {
            if (this.end > this.start) // last buffer if start == end
                fillBuffer();
            if (!queue.isEmpty()) {
                return queue.remove();
            } else
                throw new NoSuchElementException();
        }
    }

    public Iterator<T> iterator() {
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public long size() {
        return queue.size();
    }

    protected abstract void fillBuffer();

    protected String createSeparator() {
        if (this.uri.contains(RexsterTokens.QUESTION))
            return RexsterTokens.AND;
        else
            return RexsterTokens.QUESTION;
    }

    public void close() {
    }
}
