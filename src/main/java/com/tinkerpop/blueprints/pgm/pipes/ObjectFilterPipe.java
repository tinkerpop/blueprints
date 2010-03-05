package com.tinkerpop.blueprints.pgm.pipes;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ObjectFilterPipe<S> implements Pipe<S, S> {

    private Iterator<S> starts;
    private S nextLegal;
    private final Collection<S> objects;
    private final boolean filter;

    public ObjectFilterPipe(final Collection<S> objects, final boolean filter) {
        this.objects = objects;
        this.filter = filter;
    }


    public void setStarts(Iterator<S> starts) {
        this.starts = starts;
        this.setNext();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public S next() {
        S object = this.nextLegal;
        this.setNext();
        return object;
    }

    public boolean hasNext() {
        return null != this.nextLegal;
    }

    private void setNext() {
        while (starts.hasNext()) {
            S object = this.starts.next();
            if (this.filter) {
                if (!objects.contains(object)) {
                    this.nextLegal = object;
                    return;
                }
            } else {
                if (objects.contains(object)) {
                    this.nextLegal = object;
                    return;
                }
            }
        }
        this.nextLegal = null;
    }

}
