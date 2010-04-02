package com.tinkerpop.blueprints.pgm.pipes.filters;

import com.tinkerpop.blueprints.pgm.pipes.AbstractPipe;
import com.tinkerpop.blueprints.pgm.pipes.filters.FilterPipe;

import java.util.Collection;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class AbstractFilterPipe<S, T> extends AbstractPipe<S, S> implements FilterPipe<S, T> {

    public boolean areEqual(T object1, T object2) {
        return object1.equals(object2);
    }

    public boolean doesContain(Collection<T> collection, T object) {
        return collection.contains(object);
    }
}
