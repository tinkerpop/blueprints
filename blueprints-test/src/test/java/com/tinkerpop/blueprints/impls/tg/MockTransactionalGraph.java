package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;

/**
 * Mocking TinkerGraph as a transactional graph for testing purposes. This implementation does not actually
 * implement transactional behavior but only counts transaction starts, successes and failures so that
 * these can be compared to expected behavior.
 * This class is only meant for testing.
 * <br />
 * <br />
 *
 * @author Matthias Broecheler (http://www.matthiasb.com)
 */

public class MockTransactionalGraph implements TransactionalGraph {

    private int numTransactionsCommitted = 0;
    private int numTransactionsAborted = 0;

    private final Graph graph;

    public MockTransactionalGraph(final Graph graph) {
        this.graph = graph;
    }

    @Override
    public void stopTransaction(Conclusion conclusion) {
        switch (conclusion) {
            case SUCCESS:
                numTransactionsCommitted++;
                break;
            case FAILURE:
                numTransactionsAborted++;
                break;
            default:
                throw new IllegalArgumentException("Unrecognized conclusion: " + conclusion);
        }
    }

    public void rollback() {
        numTransactionsAborted++;
    }

    public void commit() {
        numTransactionsCommitted++;
    }

    public int getNumTransactionsCommitted() {
        return numTransactionsCommitted;
    }

    public int getNumTransactionsAborted() {
        return numTransactionsAborted;
    }

    public boolean allSuccessful() {
        return numTransactionsAborted == 0;
    }

    @Override
    public Features getFeatures() {
        Features f = graph.getFeatures().copyFeatures();
        f.supportsTransactions = true;
        return f;
    }

    @Override
    public Vertex addVertex(Object id) {
        return graph.addVertex(id);
    }

    @Override
    public Vertex getVertex(Object id) {
        return graph.getVertex(id);
    }

    @Override
    public void removeVertex(Vertex vertex) {
        graph.removeVertex(vertex);
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return graph.getVertices();
    }

    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
        return graph.getVertices(key, value);
    }

    @Override
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        return graph.addEdge(id, outVertex, inVertex, label);
    }

    @Override
    public Edge getEdge(Object id) {
        return graph.getEdge(id);
    }

    @Override
    public void removeEdge(Edge edge) {
        graph.removeEdge(edge);
    }

    @Override
    public Iterable<Edge> getEdges() {
        return graph.getEdges();
    }

    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        return graph.getEdges(key, value);
    }

    @Override
    public void shutdown() {
        graph.shutdown();
    }

    @Override
    public GraphQuery query() {
        return graph.query();
    }

}
