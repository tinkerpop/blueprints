package com.tinkerpop.blueprints.pgm.util.wrappers.batch;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Features;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.WrapperGraph;

/**
 * BufferGraph is a wrapper that allows for bulk writing/deleting of vertices or edges to a graph.
 * Every addVertex(), addEdge(), removeVertex(), removeEdge() call will increment a mutation counter.
 * When that mutation counter reaches the size of the buffer, the underlying TransactionalGraph has its transaction committed.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class BufferGraph<T extends TransactionalGraph> implements TransactionalGraph, WrapperGraph<T> {
    private final T baseGraph;
    private int bufferSize;
    private int mutationCounter = 0;

    private final Features features;

    public BufferGraph(final T baseGraph, final int bufferSize) {
        this.baseGraph = baseGraph;
        this.bufferSize = bufferSize;
        this.features = this.baseGraph.getFeatures().copyFeatures();
        this.features.isWrapper = true;
    }

    /**
     * Set the size of the buffer.
     *
     * @param bufferSize the size of the buffer
     */
    public void setBufferSize(final int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Get the size of the buffer.
     *
     * @return the size of the buffer
     */
    public int getBufferSize() {
        return this.bufferSize;
    }

    /**
     * Get the current number of mutations in the buffer.
     *
     * @return the number of mutations in the buffer
     */
    public int getMutationCounter() {
        return this.mutationCounter;
    }

    /**
     * Increments the mutation counter by one.
     * If that number is equal to or greater than the buffer size, a commit occurs.
     *
     * @param id the recommended object identifier
     * @return newly created vertex
     */
    public Vertex addVertex(final Object id) {
        final Vertex vertex = this.baseGraph.addVertex(id);
        this.incrBuffer();
        return vertex;
    }

    /**
     * Increments the mutation counter by one.
     * If that number is equal to or greater than the buffer size, a commit occurs.
     *
     * @param vertex the vertex to remove from the graph
     */
    public void removeVertex(final Vertex vertex) {
        this.baseGraph.removeVertex(vertex);
        this.incrBuffer();

    }

    /**
     * Increments the mutation counter by one.
     * If that number is equal to or greater than the buffer size, a commit occurs.
     *
     * @param id        the recommended object identifier
     * @param outVertex the vertex on the tail of the edge
     * @param inVertex  the vertex on the head of the edge
     * @param label     the label associated with the edge
     * @return the newly created edge
     */
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        final Edge edge = this.baseGraph.addEdge(id, outVertex, inVertex, label);
        this.incrBuffer();
        return edge;
    }

    /**
     * Increments the mutation counter by one.
     * If that number is equal to or greater than the buffer size, a commit occurs.
     *
     * @param edge the edge to remove from the graph
     */
    public void removeEdge(final Edge edge) {
        this.baseGraph.removeEdge(edge);
        this.incrBuffer();
    }


    public T getBaseGraph() {
        return this.baseGraph;
    }

    private void incrBuffer() {
        if (++this.mutationCounter >= this.bufferSize) {
            this.baseGraph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            this.mutationCounter = 0;
        }
    }

    public Features getFeatures() {
        return this.features;
    }

    public void shutdown() {
        this.baseGraph.shutdown();
    }

    public Iterable<Edge> getEdges() {
        return this.baseGraph.getEdges();
    }

    public Iterable<Edge> getEdges(final String key, final Object value) {
        return this.baseGraph.getEdges(key, value);
    }

    public Iterable<Vertex> getVertices() {
        return this.baseGraph.getVertices();
    }

    public Iterable<Vertex> getVertices(final String key, final Object value) {
        return this.baseGraph.getVertices(key, value);
    }

    public Edge getEdge(final Object id) {
        return this.baseGraph.getEdge(id);
    }

    public Vertex getVertex(final Object id) {
        return this.baseGraph.getVertex(id);
    }

    public void stopTransaction(final Conclusion conclusion) {
        this.baseGraph.stopTransaction(conclusion);
    }

    public void startTransaction() {
        this.baseGraph.startTransaction();
    }

}
