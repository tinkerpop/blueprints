package com.tinkerpop.blueprints.util.wrappers.batch;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

import java.util.Set;

/**
 * This is a naive wrapper to make a non-transactional graph transactional by simply writing all mutations
 * directly through to the wrapped graph and not supporting transactional failures.
 * <br />
 * Hence, this is not meant as a functional implementation of a {@link Graph} but rather as a means
 * to using non-transactional graphs where transactional graphs are expected and transactional failure can be
 * excluded. {@link com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph} is one such case.
 * <br />
 * Note, the constructor will throw an exception if the given graph already supports transactions.
 *
 * @author Matthias Broecheler (http://www.matthiasb.com)
 */

class WritethroughGraph<T extends Graph> implements WrapperGraph<T>, Graph {

    private final T baseGraph;

    WritethroughGraph(final T baseGraph) {
        if (baseGraph == null) throw new IllegalArgumentException("Graph expected");
        if (baseGraph.getFeatures().supportsTransactions)
            throw new IllegalArgumentException("Can only wrap non-transactional graphs");
        this.baseGraph = baseGraph;
    }


    public void rollback() {
        throw new IllegalStateException("Transactions can not be rolled back");
    }

    public void commit() {

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

    /**
     * @return The features of the underlying graph but with transactions supported.
     */
    @Override
    public Features getFeatures() {
        Features f = baseGraph.getFeatures().copyFeatures();
        f.isWrapper = true;
        f.supportsTransactions = true;
        return f;
    }

    @Override
    public Vertex addVertex(final Object id) {
        return baseGraph.addVertex(id);
    }

    @Override
    public Vertex getVertex(final Object id) {
        return baseGraph.getVertex(id);
    }

    @Override
    public void removeVertex(final Vertex vertex) {
        baseGraph.removeVertex(vertex);
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return baseGraph.getVertices();
    }

    @Override
    public Iterable<Vertex> getVertices(final String key, final Object value) {
        return baseGraph.getVertices(key, value);
    }

    @Override
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        return baseGraph.addEdge(id, outVertex, inVertex, label);
    }

    @Override
    public Edge getEdge(final Object id) {
        return baseGraph.getEdge(id);
    }

    @Override
    public void removeEdge(final Edge edge) {
        baseGraph.removeEdge(edge);
    }

    @Override
    public Iterable<Edge> getEdges() {
        return baseGraph.getEdges();
    }

    @Override
    public GraphQuery query() {
        return baseGraph.query();
    }

    @Override
    public Iterable<Edge> getEdges(final String key, final Object value) {
        return baseGraph.getEdges(key, value);
    }

    @Override
    public void shutdown() {
        baseGraph.shutdown();
    }

    @Override
    public T getBaseGraph() {
        return baseGraph;
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this, this.baseGraph.toString());
    }

}
