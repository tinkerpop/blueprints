package com.tinkerpop.blueprints.pgm.pipes.splits;

import com.tinkerpop.blueprints.pgm.pipes.splits.AbstractSplitPipe;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadySplitPipe<S> extends AbstractSplitPipe<S> {

    protected int currentSplit = 0;

    public ReadySplitPipe(final int numberOfSplits) {
        super(numberOfSplits);
    }

    public void fillNext(int splitNumber) {
        if (!done) {
            if (this.hasNext()) {
                this.splits[splitNumber].add(this.next());
            }
        }
    }
}
