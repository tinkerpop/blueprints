package com.tinkerpop.blueprints.pgm.impls.named;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.wrapped.util.WrappedEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.wrapped.util.WrappedVertexSequence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NamedGraph implements Graph {

    protected Graph rawGraph;

    public NamedGraph(final Graph rawGraph) {
        this.rawGraph = rawGraph;
    }

    public void clear() {
        this.rawGraph.clear();
    }

    public void shutdown() {
        this.rawGraph.shutdown();
    }

    public Vertex addVertex(final Object id) {
        return new NamedVertex(this.rawGraph.addVertex(id));
    }

    public Vertex getVertex(final Object id) {
        final Vertex vertex = this.rawGraph.getVertex(id);
        if (null == vertex)
            return null;
        else
            return new NamedVertex(vertex);
    }

    public Iterable<Vertex> getVertices() {
        return new WrappedVertexSequence(this.rawGraph.getVertices().iterator());
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        return new NamedEdge(this.rawGraph.addEdge(id, ((NamedVertex) outVertex).getRawVertex(), ((NamedVertex) inVertex).getRawVertex(), label));
    }

    public Edge getEdge(final Object id) {
        final Edge edge = this.rawGraph.getEdge(id);
        if (null == edge)
            return null;
        else
            return new NamedEdge(edge);
    }

    public Iterable<Edge> getEdges() {
        return new WrappedEdgeSequence(this.rawGraph.getEdges().iterator());
    }

    public void removeEdge(final Edge edge) {
        this.rawGraph.removeEdge(((NamedEdge) edge).getRawEdge());
    }

    public void removeVertex(final Vertex vertex) {
        this.rawGraph.removeVertex(((NamedVertex) vertex).getRawVertex());
    }

    public Graph getRawGraph() {
        return this.rawGraph;
    }

    public String toString() {
        return StringFactory.graphString(this, this.rawGraph.toString());
    }
}
