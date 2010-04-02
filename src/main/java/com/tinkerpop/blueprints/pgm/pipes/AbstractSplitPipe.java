package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.pgm.pipes.util.SplitQueue;

import java.util.Iterator;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class AbstractSplitPipe<S> extends AbstractPipe<S, S> implements SplitPipe<S> {

    protected SplitQueue<S>[] splits;
    protected final int numberOfSplits;

    public AbstractSplitPipe(int numberOfSplits) {
        this.numberOfSplits = numberOfSplits;
        this.splits = new SplitQueue[numberOfSplits];
        for (int i = 0; i < numberOfSplits; i++) {
            this.splits[i] = new SplitQueue<S>(this);
        }
    }

    public Iterator<S> getSplit(final int number) {
        return this.splits[number];
    }

    public void setStarts(final Iterator<S> starts) {
        super.setStarts(starts);
        this.fillNext();
    }

    protected void setNext() {
        if (this.starts.hasNext()) {
            this.nextEnd = this.starts.next();
        } else {
            this.done = true;
        }
    }
}
