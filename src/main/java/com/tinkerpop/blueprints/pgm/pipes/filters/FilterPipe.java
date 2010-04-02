package com.tinkerpop.blueprints.pgm.pipes.filters;

import com.tinkerpop.blueprints.pgm.pipes.Pipe;

import java.util.Collection;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface FilterPipe<S, T> extends Pipe<S, S> {

    public boolean areEqual(T object1, T object2);

    public boolean doesContain(Collection<T> collection, T object);

}
