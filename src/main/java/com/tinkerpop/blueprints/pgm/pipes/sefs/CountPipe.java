package com.tinkerpop.blueprints.pgm.pipes.sefs;

import com.tinkerpop.blueprints.pgm.pipes.AbstractPipe;

import java.util.NoSuchElementException;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class CountPipe<S> extends AbstractPipe<S, S> implements SideEffectPipe<S, S, Long> {

    private Long counter = 0l;

    protected S processNextStart() {
        S tempEnd = this.starts.next();
        counter++;
        return tempEnd;
    }

    public Long getSideEffect() {
        return this.counter;
    }
}
