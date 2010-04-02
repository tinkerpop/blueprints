package com.tinkerpop.blueprints.pgm.pipes.util;

import com.tinkerpop.blueprints.pgm.pipes.SplitPipe;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SplitQueue<S> implements Iterator<S> {

    private Queue<S> queue = new LinkedList<S>();
    private SplitPipe<S> splitPipe;

    public SplitQueue(SplitPipe<S> splitPipe) {
        this.splitPipe = splitPipe;
    }

    public synchronized void add(S element) {
        this.queue.add(element);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public synchronized boolean hasNext() {
        return !queue.isEmpty();
    }

    public synchronized S next() {
        S temp = queue.remove();
        this.splitPipe.fillNext();
        return temp;
    }

}
