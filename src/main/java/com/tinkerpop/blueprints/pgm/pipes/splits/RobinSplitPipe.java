package com.tinkerpop.blueprints.pgm.pipes.splits;

import com.tinkerpop.blueprints.pgm.pipes.splits.AbstractSplitPipe;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RobinSplitPipe<S> extends AbstractSplitPipe<S> {

    protected int currentSplit = 0;

    public RobinSplitPipe(final int numberOfSplits) {
        super(numberOfSplits);
    }

    public void fillNext(int splitNumber) {
        if (!done) {
            for (int i = 0; i < numberOfSplits; i++) {
                if (this.hasNext()) {
                    this.splits[currentSplit].add(this.next());
                    currentSplit = ++currentSplit % numberOfSplits;
                } else {
                    break;
                }
            }
        }
    }


}
