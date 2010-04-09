package com.tinkerpop.blueprints.pgm.pipes;

import java.util.Iterator;
import java.util.List;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Pipeline<S, E> implements Pipe<S, E> {

    private Pipe<S, ?> startPipe;
    private Pipe<?, E> endPipe;

    public Pipeline(final List<Pipe> pipes) {
        this.setPipes(pipes);
    }

    public Pipeline() {
    }

    public void setPipes(final List<Pipe> pipes) {
        this.startPipe = pipes.get(0);
        this.endPipe = pipes.get(pipes.size() - 1);
        for (int i = 1; i < pipes.size(); i++) {
            pipes.get(i).setStarts(pipes.get(i - 1));
        }
    }

    public void setStarts(final Iterator<S> starts) {
        startPipe.setStarts(starts);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return endPipe.hasNext();
    }

    public E next() {
        return endPipe.next();
    }
}
