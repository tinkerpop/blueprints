package com.tinkerpop.blueprints.pgm.impls.named;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.named.util.NamedEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.named.util.NamedVertexSequence;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NamedGraph implements Graph {

    protected Graph rawGraph;
    private String writeGraph;
    private Set<String> readGraphs = new HashSet<String>();
    private String writeGraphKey;

    public NamedGraph(final Graph rawGraph, final String writeGraphKey, final String writeGraph, final Set<String> readGraphs) {
        this.rawGraph = rawGraph;
        this.writeGraphKey = writeGraphKey;
        this.writeGraph = writeGraph;
        this.readGraphs.addAll(readGraphs);
    }

    public NamedGraph(final Graph rawGraph, final String writeGraphKey, final String readWriteGraph) {
        this(rawGraph, writeGraphKey, readWriteGraph, new HashSet<String>(Arrays.asList(readWriteGraph)));
    }

    public String getWriteGraph() {
        return this.writeGraph;
    }

    public void setWriteGraph(final String writeGraph) {
        this.writeGraph = writeGraph;
    }

    public Set<String> getReadGraphs() {
        return new HashSet<String>(this.readGraphs);
    }

    public void removeReadGraph(final String readGraph) {
        this.readGraphs.remove(readGraph);
    }

    public void addReadGraph(final String readGraph) {
        this.readGraphs.add(readGraph);
    }

    public void setWriteGraphKey(final String writeGraphKey) {
        this.writeGraphKey = writeGraphKey;
    }

    public String getWriteGraphKey() {
        return this.writeGraphKey;
    }

    public boolean isInGraph(final Element element) {
        final String writeGraph;
        if (element instanceof NamedElement)
            writeGraph = ((NamedElement) element).getWriteGraph();
        else
            writeGraph = (String) element.getProperty(this.writeGraphKey);
        return (null == writeGraph || this.readGraphs.contains(writeGraph));
    }

    public void clear() {
        this.rawGraph.clear();
    }

    public void shutdown() {
        this.rawGraph.shutdown();
    }

    public Vertex addVertex(final Object id) {
        final NamedVertex vertex = new NamedVertex(this.rawGraph.addVertex(id), this);
        vertex.setWriteGraph(this.writeGraph);
        return vertex;
    }

    public Vertex getVertex(final Object id) {
        final Vertex vertex = this.rawGraph.getVertex(id);
        if (null == vertex)
            return null;
        else {
            if (isInGraph(vertex))
                return new NamedVertex(vertex, this);
            else
                return null;
        }
    }

    public Iterable<Vertex> getVertices() {
        return new NamedVertexSequence(this.rawGraph.getVertices().iterator(), this);
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        final NamedEdge edge = new NamedEdge(this.rawGraph.addEdge(id, ((NamedVertex) outVertex).getRawVertex(), ((NamedVertex) inVertex).getRawVertex(), label), this);
        edge.setWriteGraph(this.writeGraph);
        return edge;
    }

    public Edge getEdge(final Object id) {
        final Edge edge = this.rawGraph.getEdge(id);
        if (null == edge)
            return null;
        else
            return new NamedEdge(edge, this);
    }

    public Iterable<Edge> getEdges() {
        return new NamedEdgeSequence(this.rawGraph.getEdges().iterator(), this);
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
