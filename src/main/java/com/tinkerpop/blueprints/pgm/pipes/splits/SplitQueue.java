package com.tinkerpop.blueprints.pgm.pipes.splits;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SplitQueue<S> implements Iterator<S> {

    private final Queue<S> queue = new LinkedList<S>();
    private final SplitPipe<S> splitPipe;
    private final int splitNumber;

    public SplitQueue(final SplitPipe<S> splitPipe, final int splitNumber) {
        this.splitPipe = splitPipe;
        this.splitNumber = splitNumber;
    }

    public synchronized void add(final S element) {
        this.queue.add(element);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public synchronized boolean hasNext() {
        if (!queue.isEmpty()) {
            return true;
        } else {
            if (this.splitPipe.hasNext()) {
                this.splitPipe.fillNext(this.splitNumber);
                return true;
            } else
                return false;
        }
    }

    public synchronized S next() {
        S temp = queue.remove();
        this.splitPipe.fillNext(this.splitNumber);
        return temp;
    }

}
