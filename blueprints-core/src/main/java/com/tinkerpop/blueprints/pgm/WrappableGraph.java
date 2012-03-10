package com.tinkerpop.blueprints.pgm;

/**
 * A WrappableGraph has an underlying graph object to which it delegates its operations.
 *
 * @author Jordan A. Lewis (http://jordanlewis.org)
 */
public interface WrappableGraph<T> {
    /**
     * Get the graph this wrapper delegates to.
     *
     * @return the underlying graph that this WrappableGraph delegates its operations to.
     */
    public T getRawGraph();
}
