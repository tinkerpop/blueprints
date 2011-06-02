package com.tinkerpop.blueprints.pgm;

import java.util.Iterator;

/**
 * A CloseableSequence is required where it is necessary to deallocate resources.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface CloseableSequence<T> extends Iterator<T>, Iterable<T> {

    /**
     * Release the resources of the iterator.
     */
    public void close();
}
