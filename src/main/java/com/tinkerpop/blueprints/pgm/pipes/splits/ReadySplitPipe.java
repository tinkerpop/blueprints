package com.tinkerpop.blueprints.pgm.pipes.splits;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadySplitPipe<S> extends AbstractSplitPipe<S> {

    public ReadySplitPipe(final int numberOfSplits) {
        super(numberOfSplits);
    }

    public void fillNext(int splitNumber) {
        if (this.hasNext()) {
            this.splits[splitNumber].add(this.next());
        }
    }
}
