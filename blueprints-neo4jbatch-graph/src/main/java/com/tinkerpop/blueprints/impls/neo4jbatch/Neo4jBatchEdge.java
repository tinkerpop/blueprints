package com.tinkerpop.blueprints.impls.neo4jbatch;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;

import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class Neo4jBatchEdge extends Neo4jBatchElement implements Edge {

    private final String label;

    public Neo4jBatchEdge(final Neo4jBatchGraph graph, final Long id, final String label) {
        super(graph, id);
        this.label = label;
    }

    public Object removeProperty(final String key) {
        final Map<String, Object> properties = this.getPropertyMapClone();
        final Object value = properties.remove(key);
        this.graph.getRawGraph().setRelationshipProperties(this.id, properties);
        return value;

    }

    public void setProperty(final String key, final Object value) {
        if (key.equals(StringFactory.ID))
            throw ExceptionFactory.propertyKeyIdIsReserved();
        if (key.equals(StringFactory.LABEL))
            throw ExceptionFactory.propertyKeyLabelIsReservedForEdges();
        if (key.equals(StringFactory.EMPTY_STRING))
            throw ExceptionFactory.elementKeyCanNotBeEmpty();

        final Map<String, Object> properties = this.getPropertyMapClone();
        properties.put(key, value);
        this.graph.getRawGraph().setRelationshipProperties(this.id, properties);
    }

    public Map<String, Object> getPropertyMap() {
        return this.graph.getRawGraph().getRelationshipProperties(this.id);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Vertex getVertex(final Direction direction) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public String getLabel() {
        return this.label;
    }

    public String toString() {
        return "e[" + this.id + "][?-" + this.label + "->?]";
    }

}