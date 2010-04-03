package com.tinkerpop.blueprints.pgm.pipes.merges;

import com.tinkerpop.blueprints.pgm.pipes.AbstractPipe;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ExhaustiveMergePipe<S> extends AbstractPipe<Iterator<S>, S> {

    protected Iterator<S> currentEnds;

    protected S processNextStart() {
        if (null != this.currentEnds && this.currentEnds.hasNext()) {
            return this.currentEnds.next();
        } else {
            if ((null == this.currentEnds || !this.currentEnds.hasNext()) && this.starts.hasNext()) {
                this.currentEnds = this.starts.next();
                return processNextStart();
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
