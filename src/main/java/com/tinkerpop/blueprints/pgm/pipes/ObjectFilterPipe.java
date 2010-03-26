package com.tinkerpop.blueprints.pgm.pipes;

import java.util.Collection;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ObjectFilterPipe<S> extends AbstractFilterPipe<S, S, S> {

    private final Collection<S> objects;
    private final boolean filter;

    public ObjectFilterPipe(final Collection<S> objects, final boolean filter) {
        this.objects = objects;
        this.filter = filter;
    }

    protected void setNext() {
        while (this.starts.hasNext()) {
            S object = this.starts.next();
            if (this.filter) {
                if (!this.doesContain(this.objects, object)) {
                    this.nextEnd = object;
                    return;
                }
            } else {
                if (this.doesContain(this.objects, object)) {
                    this.nextEnd = object;
                    return;
                }
            }
        }
        this.done = true;
    }

}
