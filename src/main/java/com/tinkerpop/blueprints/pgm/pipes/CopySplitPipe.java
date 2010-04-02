package com.tinkerpop.blueprints.pgm.pipes;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class CopySplitPipe<S> extends AbstractSplitPipe<S> {

    public CopySplitPipe(final int numberOfSplits) {
        super(numberOfSplits);
    }

    public void fillNext() {
        if (!done && this.hasNext()) {
            S item = this.next();
            for (int i = 0; i < this.numberOfSplits; i++) {
                this.splits[i].add(item);
            }
        }
    }
}