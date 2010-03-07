package com.tinkerpop.blueprints.pgm.pipes;

import java.util.Iterator;
import java.util.List;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Pipeline<S, E> implements Pipe<S, E> {

    private final List<Pipe> pipes;
    private final Pipe endPipe;

    public Pipeline(final List<Pipe> pipes) {
        this.pipes = pipes;
        this.endPipe = pipes.get(this.pipes.size()-1);
    }

    public void setStarts(final Iterator<S> starts) {
        pipes.get(0).setStarts(starts);
        for (int i = 1; i < pipes.size(); i++) {
            pipes.get(i).setStarts(pipes.get(i - 1));
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return endPipe.hasNext();
    }

    // TODO: implement a validate method to check pipe types?

    public E next() {
        return (E) endPipe.next();
    }


}
