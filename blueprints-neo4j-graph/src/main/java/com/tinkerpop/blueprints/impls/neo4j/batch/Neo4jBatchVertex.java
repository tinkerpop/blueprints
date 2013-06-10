package com.tinkerpop.blueprints.impls.neo4j.batch;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;

import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class Neo4jBatchVertex extends Neo4jBatchElement implements Vertex {

    public Neo4jBatchVertex(final Neo4jBatchGraph graph, final Long id) {
        super(graph, id);
    }

    public <T> T removeProperty(final String key) {
        final Map<String, Object> properties = this.getPropertyMapClone();
        final Object value = properties.remove(key);
        this.graph.getRawGraph().setNodeProperties(this.id, properties);
        return (T) value;

    }

    public void setProperty(final String key, final Object value) {
        if (key.isEmpty())
            throw ExceptionFactory.propertyKeyCanNotBeEmpty();
        if (key.equals(StringFactory.ID))
            throw ExceptionFactory.propertyKeyIdIsReserved();

        final Map<String, Object> properties = this.getPropertyMapClone();
        properties.put(key, value);
        this.graph.getRawGraph().setNodeProperties(this.id, properties);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Iterable<Edge> getEdges(final Direction direction, final String... labels) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Edge addEdge(final String label, final Vertex vertex) {
        return this.graph.addEdge(null, this, vertex, label);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public VertexQuery query() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public Map<String, Object> getPropertyMap() {
        return this.graph.getRawGraph().getNodeProperties(this.id);
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }
}

