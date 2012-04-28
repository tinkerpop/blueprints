package com.tinkerpop.blueprints.pgm.util.wrappers;

import com.tinkerpop.blueprints.pgm.Graph;

/**
 * A WrapperGraph has an underlying graph object to which it delegates its operations.
 *
 * @author Jordan A. Lewis (http://jordanlewis.org)
 */
public interface WrapperGraph<T extends Graph> {
    /**
     * Get the graph this wrapper delegates to.
     *
     * @return the underlying graph that this WrapperGraph delegates its operations to.
     */
    public T getBaseGraph();
}
