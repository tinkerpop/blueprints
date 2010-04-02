package com.tinkerpop.blueprints.pgm.pipes.sefs;

import com.tinkerpop.blueprints.pgm.pipes.AbstractPipe;
import com.tinkerpop.blueprints.pgm.pipes.sefs.SideEffectPipe;

import java.util.NoSuchElementException;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class CountPipe<S> extends AbstractPipe<S, S> implements SideEffectPipe<S, S, Long> {

    private Long counter = 0l;

    protected void setNext() {
        if (this.starts.hasNext()) {
            this.nextEnd = this.starts.next();
        } else {
            this.done = true;
        }
    }

    public S next() {
        if (this.done) {
            throw new NoSuchElementException();
        } else {
            S end = this.nextEnd;
            this.setNext();
            counter++;
            return end;
        }
    }

    public Long getSideEffect() {
        return this.counter;
    }
}
