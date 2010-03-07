package com.tinkerpop.blueprints.pgm.pipes;

import java.util.Iterator;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class AbstractPipe<S, E> implements Pipe<S, E> {

    protected Iterator<S> starts;
    protected E nextEnd;

    public void setStarts(final Iterator<S> starts) {
        this.starts = starts;
        this.setNext();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public E next() {
        E end = this.nextEnd;
        this.setNext();
        return end;
    }

    public boolean hasNext() {
        return this.nextEnd != null;
    }

    protected void setNext() {
        throw new RuntimeException("Override this method in the child class");
    }

}

