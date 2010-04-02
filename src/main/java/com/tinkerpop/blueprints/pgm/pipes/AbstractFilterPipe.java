package com.tinkerpop.blueprints.pgm.pipes;

import com.tinkerpop.blueprints.pgm.pipes.FilterPipe;

import java.util.Collection;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class AbstractFilterPipe<S, E, T> extends AbstractPipe<S, E> implements FilterPipe<S, E, T> {

    public boolean areEqual(T object1, T object2) {
        return object1.equals(object2);
    }

    public boolean doesContain(Collection<T> collection, T object) {
        return collection.contains(object);
    }
}
