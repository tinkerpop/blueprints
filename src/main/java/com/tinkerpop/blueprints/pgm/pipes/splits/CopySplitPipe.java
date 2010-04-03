package com.tinkerpop.blueprints.pgm.pipes.splits;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class CopySplitPipe<S> extends AbstractSplitPipe<S> {

    public CopySplitPipe(final int numberOfSplits) {
        super(numberOfSplits);
    }

    public void fillNext(int splitNumber) {
        if (this.hasNext()) {
            S item = this.next();
            for (int i = 0; i < this.numberOfSplits; i++) {
                this.splits[i].add(item);
            }
        }
    }
}