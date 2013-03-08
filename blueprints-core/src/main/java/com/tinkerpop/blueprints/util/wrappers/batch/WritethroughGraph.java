package com.tinkerpop.blueprints.util.wrappers.batch;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

/**
 * This is a naive wrapper to make a non-transactional graph transactional by simply writing all mutations
 * directly through to the wrapped graph and not supporting transactional failures.
 * <br />
 * Hence, this is not meant as a functional implementation of a {@link TransactionalGraph} but rather as a means
 * to using non-transactional graphs where transactional graphs are expected and transactional failure can be
 * excluded. {@link com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph} is one such case.
 * <br />
 * Note, the constructor will throw an exception if the given graph already supports transactions.
 *
 * @author Matthias Broecheler (http://www.matthiasb.com)
 */

class WritethroughGraph<T extends Graph> implements WrapperGraph<T>, TransactionalGraph {

    private final T graph;

    WritethroughGraph(final T graph) {
        if (graph == null) throw new IllegalArgumentException("Graph expected");
        if (graph instanceof TransactionalGraph)
            throw new IllegalArgumentException("Can only wrap non-transactional graphs");
        this.graph = graph;
    }


    /**
     * Only supports successful termination of a transaction.
     *
     * @param conclusion whether or not the current transaction was successful or not
     */
    @Override
    public void stopTransaction(Conclusion conclusion) {
        if (Conclusion.SUCCESS == conclusion)
            commit();
        else
            rollback();
    }

    public void rollback() {
        throw new IllegalStateException("Transactions can not be rolled back");
    }

    public void commit() {

    }

    /**
     * @return The features of the underlying graph but with transactions supported.
     */
    @Override
    public Features getFeatures() {
        Features f = graph.getFeatures().copyFeatures();
        f.isWrapper = true;
        f.supportsTransactions = true;
        return f;
    }

    @Override
    public Vertex addVertex(final Object id) {
        return graph.addVertex(id);
    }

    @Override
    public Vertex getVertex(final Object id) {
        return graph.getVertex(id);
    }

    @Override
    public void removeVertex(final Vertex vertex) {
        graph.removeVertex(vertex);
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return graph.getVertices();
    }

    @Override
    public Iterable<Vertex> getVertices(final String key, final Object value) {
        return graph.getVertices(key, value);
    }

    @Override
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        return graph.addEdge(id, outVertex, inVertex, label);
    }

    @Override
    public Edge getEdge(final Object id) {
        return graph.getEdge(id);
    }

    @Override
    public void removeEdge(final Edge edge) {
        graph.removeEdge(edge);
    }

    @Override
    public Iterable<Edge> getEdges() {
        return graph.getEdges();
    }

    @Override
    public GraphQuery query() {
        return graph.query();
    }

    @Override
    public Iterable<Edge> getEdges(final String key, final Object value) {
        return graph.getEdges(key, value);
    }

    @Override
    public void shutdown() {
        graph.shutdown();
    }

    @Override
    public T getBaseGraph() {
        return graph;
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this, this.graph.toString());
    }

}
