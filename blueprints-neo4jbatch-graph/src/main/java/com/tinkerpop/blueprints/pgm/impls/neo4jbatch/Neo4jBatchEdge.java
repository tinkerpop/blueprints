package com.tinkerpop.blueprints.pgm.impls.neo4jbatch;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jBatchEdge extends Neo4jBatchElement implements Edge {

    private final String label;

    public Neo4jBatchEdge(final Neo4jBatchGraph graph, final Long id, final String label) {
        super(graph, id);
        this.label = label;
    }

    public Object removeProperty(final String key) {
        final Map<String, Object> properties = this.getPropertyMapClone();
        final Object value = properties.remove(key);
        this.graph.getRawGraph().setRelationshipProperties(this.id, properties);
        for (final Neo4jBatchAutomaticIndex index : this.graph.getAutomaticEdgeIndices()) {
            index.autoUpdate(this, properties);
        }
        return value;

    }

    public void setProperty(final String key, final Object value) {
        final Map<String, Object> properties = this.getPropertyMapClone();
        properties.put(key, value);
        this.graph.getRawGraph().setRelationshipProperties(this.id, properties);
        for (final Neo4jBatchAutomaticIndex index : this.graph.getAutomaticEdgeIndices()) {
            index.autoUpdate(this, properties);
        }
    }


    protected Map<String, Object> getPropertyMap() {
        return this.graph.getRawGraph().getRelationshipProperties(this.id);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Vertex getOutVertex() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Vertex getInVertex() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public String getLabel() {
        return this.label;
    }

    public String toString() {
        return "e[" + this.id + "][?-" + this.label + "->?]";
    }

}