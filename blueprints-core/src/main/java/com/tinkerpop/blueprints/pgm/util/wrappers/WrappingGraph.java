package com.tinkerpop.blueprints.pgm.util.wrappers;

/**
 * A WrappingGraph has an underlying Graph object to which it delegates its operations.
 *
 * @author Jordan A. Lewis (http://jordanlewis.org)
 */
public interface WrappingGraph<T> {
    /**
     * Get the graph this wrapper delegates to.
     *
     * @return the underlying graph that this WrappingGraph delegates its operations to.
     */
    public T getRawGraph();
}
