package com.tinkerpop.blueprints.pgm.pipes.splits;

import com.tinkerpop.blueprints.pgm.pipes.Pipe;

import java.util.Iterator;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface SplitPipe<S> extends Pipe<S,S> {

    public Iterator<S> getSplit(int splitNumber);

    public void fillNext(int splitNumber);

}
