package com.tinkerpop.blueprints.pgm.pipes.filters;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ObjectFilterPipe<S> extends AbstractFilterPipe<S, S> {

    private final Collection<S> objects;
    private final boolean filter;

    public ObjectFilterPipe(final Collection<S> objects, final boolean filter) {
        this.objects = objects;
        this.filter = filter;
    }

    protected S processNextStart() {
        while (this.starts.hasNext()) {
            S tempEnd = this.starts.next();
            if (this.filter) {
                if (!this.doesContain(this.objects, tempEnd)) {
                    return tempEnd;
                }
            } else {
                if (this.doesContain(this.objects, tempEnd)) {
                    return tempEnd;
                }
            }
        }
        throw new NoSuchElementException();
    }

}
