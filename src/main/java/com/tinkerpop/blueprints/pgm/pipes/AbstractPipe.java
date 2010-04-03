package com.tinkerpop.blueprints.pgm.pipes;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class AbstractPipe<S, E> implements Pipe<S, E> {

    protected Iterator<S> starts;
    private E nextEnd;
    private boolean available = false;

    public void setStarts(final Iterator<S> starts) {
        this.starts = starts;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public E next() {
        if (this.available) {
            this.available = false;
            return this.nextEnd;
        } else {
            this.nextEnd = processNextStart();
            this.available = true;
            return this.next();
        }
    }

    public boolean hasNext() {
        if (this.available)
            return true;
        else {
            try {
                this.nextEnd = processNextStart();
                this.available = true;
                return this.hasNext();
            } catch (NoSuchElementException e) {
                return false;
            }
        }
    }

    protected E processNextStart() throws NoSuchElementException {
        throw new RuntimeException("Override this method in the child class");
    }

}

