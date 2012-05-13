package com.tinkerpop.blueprints;

/**
 * A CloseableIterable is required where it is necessary to deallocate resources from an Iterable.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface CloseableIterable<T> extends Iterable<T> {

    /**
     * Release the resources of the iterator.
     */
    public void close();
}
