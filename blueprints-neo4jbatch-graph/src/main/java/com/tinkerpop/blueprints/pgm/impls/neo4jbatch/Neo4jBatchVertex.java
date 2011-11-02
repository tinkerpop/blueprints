package com.tinkerpop.blueprints.pgm.impls.neo4jbatch;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;

import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jBatchVertex extends Neo4jBatchElement implements Vertex {

    public Neo4jBatchVertex(final Neo4jBatchGraph graph, final Long id) {
        super(graph, id);
    }

    public Object removeProperty(final String key) {
        final Map<String, Object> properties = this.getPropertyMapClone();
        final Object value = properties.remove(key);
        this.graph.getRawGraph().setNodeProperties(this.id, properties);
        for (final Neo4jBatchAutomaticIndex index : this.graph.getAutomaticVertexIndices()) {
            index.autoUpdate(this, properties);
        }
        return value;

    }

    public void setProperty(final String key, final Object value) {
        final Map<String, Object> properties = this.getPropertyMapClone();
        properties.put(key, value);
        this.graph.getRawGraph().setNodeProperties(this.id, properties);
        for (final Neo4jBatchAutomaticIndex index : this.graph.getAutomaticVertexIndices()) {
            index.autoUpdate(this, properties);
        }
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Iterable<Edge> getOutEdges(final String... labels) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Iterable<Edge> getInEdges(final String... labels) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    protected Map<String, Object> getPropertyMap() {
        return this.graph.getRawGraph().getNodeProperties(this.id);
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }
}

