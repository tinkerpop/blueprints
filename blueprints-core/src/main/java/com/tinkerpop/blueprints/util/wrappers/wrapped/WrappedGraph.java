package com.tinkerpop.blueprints.util.wrappers.wrapped;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.WrappedGraphQuery;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

/**
 * WrappedGraph serves as a template for writing a wrapper graph.
 * The intention is that the code in this template is copied and adjusted accordingly.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedGraph<T extends Graph> implements Graph, WrapperGraph<T> {

    protected T baseGraph;
    private final Features features;

    public WrappedGraph(final T baseGraph) {
        this.baseGraph = baseGraph;
        this.features = this.baseGraph.getFeatures().copyFeatures();
        this.features.isWrapper = true;
    }

    public void shutdown() {
        this.baseGraph.shutdown();
    }

    public Vertex addVertex(final Object id) {
        return new WrappedVertex(this.baseGraph.addVertex(id));
    }

    public Vertex getVertex(final Object id) {
        final Vertex vertex = this.baseGraph.getVertex(id);
        if (null == vertex)
            return null;
        else
            return new WrappedVertex(vertex);
    }

    public Iterable<Vertex> getVertices() {
        return new WrappedVertexIterable(this.baseGraph.getVertices());
    }

    public Iterable<Vertex> getVertices(final String key, final Object value) {
        return new WrappedVertexIterable(this.baseGraph.getVertices(key, value));
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        return new WrappedEdge(this.baseGraph.addEdge(id, ((WrappedVertex) outVertex).getBaseVertex(), ((WrappedVertex) inVertex).getBaseVertex(), label));
    }

    public Edge getEdge(final Object id) {
        final Edge edge = this.baseGraph.getEdge(id);
        if (null == edge)
            return null;
        else
            return new WrappedEdge(edge);
    }

    public Iterable<Edge> getEdges() {
        return new WrappedEdgeIterable(this.baseGraph.getEdges());
    }

    public Iterable<Edge> getEdges(final String key, final Object value) {
        return new WrappedEdgeIterable(this.baseGraph.getEdges(key, value));
    }

    public void removeEdge(final Edge edge) {
        this.baseGraph.removeEdge(((WrappedEdge) edge).getBaseEdge());
    }

    public void removeVertex(final Vertex vertex) {
        this.baseGraph.removeVertex(((WrappedVertex) vertex).getBaseVertex());
    }

    @Override
    public T getBaseGraph() {
        return this.baseGraph;
    }

    public GraphQuery query() {
        return new WrappedGraphQuery(this.baseGraph.query()) {
            @Override
            public Iterable<Edge> edges() {
                return new WrappedEdgeIterable(this.query.edges());
            }

            @Override
            public Iterable<Vertex> vertices() {
                return new WrappedVertexIterable(this.query.vertices());
            }
        };
    }

    public String toString() {
        return StringFactory.graphString(this, this.baseGraph.toString());
    }

    public Features getFeatures() {
        return this.features;
    }
}
