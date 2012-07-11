package com.tinkerpop.blueprints.util.io.graphson;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * A factory responsible for creating graph elements.  Abstracts the way that graph elements are created. In
 * most cases a Graph is responsible for element creation, but there are cases where more control over
 * how vertices and edges are constructed.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public interface ElementFactory<V extends Vertex, E extends Edge> {
    /**
     * Creates a new Edge instance.
     */
    E createEdge(final Object id, final V out, final V in, final String label);

    /**
     * Creates a new Vertex instance.
     */
    V createVertex(final Object id);
}
