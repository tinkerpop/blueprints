package com.tinkerpop.blueprints.pgm.impls.neo4jbatch;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

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
        for (Neo4jBatchAutomaticIndex index : this.graph.getAutoIndices()) {
            index.autoUpdate(this, properties);
        }
        return value;

    }

    public void setProperty(final String key, final Object value) {
        final Map<String, Object> properties = this.getPropertyMapClone();
        properties.put(key, value);
        this.graph.getRawGraph().setNodeProperties(this.id, properties);
        for (Neo4jBatchAutomaticIndex index : this.graph.getAutoIndices()) {
            index.autoUpdate(this, properties);
        }
    }

    public Iterable<Edge> getOutEdges(String... labels) {
        throw new UnsupportedOperationException();
    }

    public Iterable<Edge> getInEdges(String... labels) {
        throw new UnsupportedOperationException();
    }

    protected Map<String, Object> getPropertyMap() {
        return this.graph.getRawGraph().getNodeProperties(this.id);
    }
}

