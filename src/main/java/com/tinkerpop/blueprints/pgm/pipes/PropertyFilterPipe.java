package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.pgm.Element;

import java.util.Collection;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PropertyFilterPipe<S extends Element, T> extends AbstractFilterPipe<S, S, T> {

    private final String key;
    private final Collection<T> values;
    private final boolean filter;

    public PropertyFilterPipe(final String key, final Collection<T> values, final boolean filter) {
        this.key = key;
        this.values = values;
        this.filter = filter;
    }

    protected void setNext() {
        while (this.starts.hasNext()) {
            S element = this.starts.next();
            if (this.filter) {
                if (!this.doesContain(this.values, (T) element.getProperty(this.key))) {
                    this.nextEnd = element;
                    return;
                }
            } else {
                if (this.doesContain(this.values, (T) element.getProperty(this.key))) {
                    this.nextEnd = element;
                    return;
                }
            }
        }
        this.done = true;
    }
}
