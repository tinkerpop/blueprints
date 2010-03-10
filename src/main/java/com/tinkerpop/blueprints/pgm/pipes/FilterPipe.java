package com.tinkerpop.blueprints.pgm.pipes;

import java.util.Collection;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface FilterPipe<S,E,T> extends Pipe<S,E> {

    public boolean areEqual(T object1, T object2);
    public boolean doesContain(Collection<T> collection, T object);

}
