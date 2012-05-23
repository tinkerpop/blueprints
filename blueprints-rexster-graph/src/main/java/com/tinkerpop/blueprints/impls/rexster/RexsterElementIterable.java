package com.tinkerpop.blueprints.impls.rexster;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Element;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
abstract class RexsterElementIterable<T extends Element> implements CloseableIterable<T> {

    protected final RexsterGraph graph;
    protected final String uri;

    public RexsterElementIterable(final String uri, final RexsterGraph graph) {
        this.graph = graph;
        this.uri = uri;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private final Queue<T> queue = new LinkedList<T>();
            private int start = 0;
            private int end = graph.getBufferSize();

            public boolean hasNext() {
                if (!queue.isEmpty())
                    return true;
                else {
                    if (end > start) {
                        // last buffer if start == end
                        fillBuffer(queue, start, end);
                        this.update();
                    }
                    return !queue.isEmpty();
                }
            }


            public void remove() {
                throw new NotImplementedException();
            }

            public T next() {
                if (!queue.isEmpty()) {
                    return queue.remove();
                } else {
                    if (end > start) {
                        // last buffer if start == end
                        fillBuffer(queue, start, end);
                        this.update();
                    }

                    if (!queue.isEmpty()) {
                        return queue.remove();
                    } else
                        throw new NoSuchElementException();
                }
            }

            private void update() {
                final int bufferSize = graph.getBufferSize();
                if (this.queue.size() == bufferSize) { // next buffer if full
                    this.start = this.start + bufferSize;
                    this.end = this.end + bufferSize;
                } else { // last buffer
                    this.start = this.end;
                }
            }
        };
    }

    protected abstract void fillBuffer(final Queue<T> queue, final int start, final int end);

    protected String createSeparator() {
        if (this.uri.contains(RexsterTokens.QUESTION))
            return RexsterTokens.AND;
        else
            return RexsterTokens.QUESTION;
    }

    public void close() {
    }
}
