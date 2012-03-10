package com.tinkerpop.blueprints.pgm.util.wrappers;

import com.tinkerpop.blueprints.pgm.Graph;

/**
 * A GraphWrapper is a Blueprints Graph that has an underlying Graph object to which it delegates
 * its operations.
 *
 * @author Jordan A. Lewis (http://jordanlewis.org)
 */
public interface GraphWrapper extends Graph {
    /**
     * Get the graph this wrapper delegates to.
     * @return the underlying Blueprints graph that this GraphWrapper delegates its operations to.
     */
    public Graph getRawGraph();
}
