package com.tinkerpop.blueprints.pgm.pipes.sefs;

import com.tinkerpop.blueprints.pgm.pipes.Pipe;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface SideEffectPipe<S, E, T> extends Pipe<S, E> {

    public T getSideEffect();
}
