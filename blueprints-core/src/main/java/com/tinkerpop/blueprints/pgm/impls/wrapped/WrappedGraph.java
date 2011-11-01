package com.tinkerpop.blueprints.pgm.impls.wrapped;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.wrapped.util.WrappedEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.wrapped.util.WrappedVertexSequence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedGraph implements Graph {

    protected Graph rawGraph;

    public WrappedGraph(final Graph rawGraph) {
        this.rawGraph = rawGraph;
    }

    public void clear() {
        this.rawGraph.clear();
    }

    public void shutdown() {
        this.rawGraph.shutdown();
    }

    public Vertex addVertex(final Object id) {
        return new WrappedVertex(this.rawGraph.addVertex(id));
    }

    public Vertex getVertex(final Object id) {
        final Vertex vertex = this.rawGraph.getVertex(id);
        if (null == vertex)
            return null;
        else
            return new WrappedVertex(vertex);
    }

    public Iterable<Vertex> getVertices() {
        return new WrappedVertexSequence(this.rawGraph.getVertices().iterator());
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        return new WrappedEdge(this.rawGraph.addEdge(id, ((WrappedVertex) outVertex).getRawVertex(), ((WrappedVertex) inVertex).getRawVertex(), label));
    }

    public Edge getEdge(final Object id) {
        final Edge edge = this.rawGraph.getEdge(id);
        if (null == edge)
            return null;
        else
            return new WrappedEdge(edge);
    }

    public Iterable<Edge> getEdges() {
        return new WrappedEdgeSequence(this.rawGraph.getEdges().iterator());
    }

    public void removeEdge(final Edge edge) {
        this.rawGraph.removeEdge(((WrappedEdge) edge).getRawEdge());
    }

    public void removeVertex(final Vertex vertex) {
        this.rawGraph.removeVertex(((WrappedVertex) vertex).getRawVertex());
    }

    public Graph getRawGraph() {
        return this.rawGraph;
    }

    public String toString() {
        return StringFactory.graphString(this, this.rawGraph.toString());
    }
}
