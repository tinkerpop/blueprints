package com.tinkerpop.blueprints.pgm.pipes.pgms;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.pipes.filters.AbstractFilterPipe;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PropertyFilterPipe<S extends Element, T> extends AbstractFilterPipe<S, T> {

    private final String key;
    private final Collection<T> values;
    private final boolean filter;

    public PropertyFilterPipe(final String key, final Collection<T> values, final boolean filter) {
        this.key = key;
        this.values = values;
        this.filter = filter;
    }

    protected S processNextStart() {
        while (this.starts.hasNext()) {
            S element = this.starts.next();
            if (this.filter) {
                if (!this.doesContain(this.values, (T) element.getProperty(this.key))) {
                    return element;
                }
            } else {
                if (this.doesContain(this.values, (T) element.getProperty(this.key))) {
                   return element;
                }
            }
        }
        throw new NoSuchElementException();
    }
}
