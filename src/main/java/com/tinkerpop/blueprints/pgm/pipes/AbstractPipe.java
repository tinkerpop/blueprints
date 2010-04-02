package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.pgm.pipes.Pipe;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class AbstractPipe<S, E> implements Pipe<S, E> {

    protected Iterator<S> starts;
    protected E nextEnd;
    protected boolean done = false;

    public void setStarts(final Iterator<S> starts) {
        this.starts = starts;
        this.setNext();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public E next() {
        if (this.done) {
            throw new NoSuchElementException();
        } else {
            E end = this.nextEnd;
            this.setNext();
            return end;
        }
    }

    public boolean hasNext() {
        return !done;
    }

    protected void setNext() {
        throw new RuntimeException("Override this method in the child class");
    }

}

