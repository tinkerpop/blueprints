package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;

import java.util.Set;

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

public class MockTransactionalGraph implements Graph {

    private int numTransactionsCommitted = 0;
    private int numTransactionsAborted = 0;

    private final Graph baseGraph;

    public MockTransactionalGraph(final Graph baseGraph) {
        this.baseGraph = baseGraph;
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
        Features f = baseGraph.getFeatures().copyFeatures();
        f.supportsTransactions = true;
        return f;
    }

    @Override
    public Vertex addVertex(Object id) {
        return baseGraph.addVertex(id);
    }

    @Override
    public Vertex getVertex(Object id) {
        return baseGraph.getVertex(id);
    }

    @Override
    public void removeVertex(Vertex vertex) {
        baseGraph.removeVertex(vertex);
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return baseGraph.getVertices();
    }

    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
        return baseGraph.getVertices(key, value);
    }

    @Override
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        return baseGraph.addEdge(id, outVertex, inVertex, label);
    }

    @Override
    public Edge getEdge(Object id) {
        return baseGraph.getEdge(id);
    }

    @Override
    public void removeEdge(Edge edge) {
        baseGraph.removeEdge(edge);
    }

    @Override
    public Iterable<Edge> getEdges() {
        return baseGraph.getEdges();
    }

    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        return baseGraph.getEdges(key, value);
    }

    @Override
    public void shutdown() {
        baseGraph.shutdown();
    }

    @Override
    public GraphQuery query() {
        return baseGraph.query();
    }

    public <T extends Element> void createIndex(String key, Class<T> elementClass, final Parameter... indexParameters) {
        this.baseGraph.createIndex(key, elementClass, indexParameters);
    }

    public <T extends Element> void dropIndex(String key, Class<T> elementClass) {
        this.baseGraph.dropIndex(key, elementClass);
    }

    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        return this.baseGraph.getIndexedKeys(elementClass);
    }


    public Graph newTransaction() {
        return this.baseGraph.newTransaction();
    }

}
