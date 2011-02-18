package com.tinkerpop.blueprints.pgm.impls.readonly;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.readonly.util.ReadOnlyEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.readonly.util.ReadOnlyVertexSequence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyGraph implements Graph {

    protected final Graph graph;

    public ReadOnlyGraph(final Graph graph) {
        this.graph = graph;
    }

    public void removeVertex(final Vertex vertex) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public Vertex getVertex(final Object id) {
        return new ReadOnlyVertex(this.graph.getVertex(id));
    }

    public void removeEdge(final Edge edge) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public Iterable<Edge> getEdges() {
        return new ReadOnlyEdgeSequence(this.graph.getEdges().iterator());
    }

    public Edge getEdge(final Object id) {
        return new ReadOnlyEdge(this.graph.getEdge(id));
    }

    public Iterable<Vertex> getVertices() {
        return new ReadOnlyVertexSequence(this.graph.getVertices().iterator());
    }

    public Vertex addVertex(final Object id) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public void clear() {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public void shutdown() {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public String toString() {
        return "(readonly)" + this.graph.toString();
    }

}
