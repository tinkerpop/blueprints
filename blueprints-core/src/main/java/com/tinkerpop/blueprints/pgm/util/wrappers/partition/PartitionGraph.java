package com.tinkerpop.blueprints.pgm.util.wrappers.partition;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.WrappableGraph;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.util.PartitionEdgeSequence;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.util.PartitionVertexSequence;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionGraph<T extends Graph> implements Graph, WrappableGraph<T> {

    protected T rawGraph;
    private String writePartition;
    private Set<String> readPartitions = new HashSet<String>();
    private String partitionKey;

    public PartitionGraph(final T rawGraph, final String partitionKey, final String writePartition, final Set<String> readPartitions) {
        this.rawGraph = rawGraph;
        this.partitionKey = partitionKey;
        this.writePartition = writePartition;
        this.readPartitions.addAll(readPartitions);
    }

    public PartitionGraph(final T rawGraph, final String partitionKey, final String readWritePartition) {
        this(rawGraph, partitionKey, readWritePartition, new HashSet<String>(Arrays.asList(readWritePartition)));
    }

    public String getWritePartition() {
        return this.writePartition;
    }

    public void setWritePartition(final String writePartition) {
        this.writePartition = writePartition;
    }

    public Set<String> getReadPartitions() {
        return new HashSet<String>(this.readPartitions);
    }

    public void removeReadPartition(final String readPartition) {
        this.readPartitions.remove(readPartition);
    }

    public void addReadPartition(final String readPartition) {
        this.readPartitions.add(readPartition);
    }

    public void setPartitionKey(final String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public String getPartitionKey() {
        return this.partitionKey;
    }

    public boolean isInPartition(final Element element) {
        final String writePartition;
        if (element instanceof PartitionElement)
            writePartition = ((PartitionElement) element).getPartition();
        else
            writePartition = (String) element.getProperty(this.partitionKey);
        return (null == writePartition || this.readPartitions.contains(writePartition));
    }

    public void clear() {
        this.rawGraph.clear();
    }

    public void shutdown() {
        this.rawGraph.shutdown();
    }

    public Vertex addVertex(final Object id) {
        final PartitionVertex vertex = new PartitionVertex(this.rawGraph.addVertex(id), this);
        vertex.setPartition(this.writePartition);
        return vertex;
    }

    public Vertex getVertex(final Object id) {
        final Vertex vertex = this.rawGraph.getVertex(id);
        if (null == vertex)
            return null;
        else {
            if (isInPartition(vertex))
                return new PartitionVertex(vertex, this);
            else
                return null;
        }
    }

    public Iterable<Vertex> getVertices() {
        return new PartitionVertexSequence(this.rawGraph.getVertices().iterator(), this);
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        final PartitionEdge edge = new PartitionEdge(this.rawGraph.addEdge(id, ((PartitionVertex) outVertex).getRawVertex(), ((PartitionVertex) inVertex).getRawVertex(), label), this);
        edge.setPartition(this.writePartition);
        return edge;
    }

    public Edge getEdge(final Object id) {
        final Edge edge = this.rawGraph.getEdge(id);
        if (null == edge)
            return null;
        else
            return new PartitionEdge(edge, this);
    }

    public Iterable<Edge> getEdges() {
        return new PartitionEdgeSequence(this.rawGraph.getEdges().iterator(), this);
    }

    public void removeEdge(final Edge edge) {
        this.rawGraph.removeEdge(((PartitionEdge) edge).getRawEdge());
    }

    public void removeVertex(final Vertex vertex) {
        this.rawGraph.removeVertex(((PartitionVertex) vertex).getRawVertex());
    }

    @Override
    public T getRawGraph() {
        return this.rawGraph;
    }

    public String toString() {
        return StringFactory.graphString(this, this.rawGraph.toString());
    }
}
