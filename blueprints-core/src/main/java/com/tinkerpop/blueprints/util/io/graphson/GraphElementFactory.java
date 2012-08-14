package com.tinkerpop.blueprints.util.io.graphson;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

/**
 * The standard factory used for most graph element creation.  It uses an actual
 * Graph implementation to construct vertices and edges
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GraphElementFactory implements ElementFactory<Vertex, Edge> {

    private final Graph graph;

    public GraphElementFactory(final Graph g) {
        this.graph = g;
    }

    @Override
    public Edge createEdge(final Object id, final Vertex out, final Vertex in, final String label) {
        return this.graph.addEdge(id, out, in, label);
    }

    @Override
    public Vertex createVertex(final Object id) {
        return this.graph.addVertex(id);
    }
}
