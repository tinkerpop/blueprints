package com.tinkerpop.blueprints.impls.neo4j2.batch;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;

import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class Neo4j2BatchEdge extends Neo4j2BatchElement implements Edge {

    private final String label;

    public Neo4j2BatchEdge(final Neo4j2BatchGraph graph, final Long id, final String label) {
        super(graph, id);
        this.label = label;
    }

    public <T> T removeProperty(final String key) {
        final Map<String, Object> properties = this.getPropertyMapClone();
        final Object value = properties.remove(key);
        this.graph.getRawGraph().setRelationshipProperties(this.id, properties);
        return (T) value;

    }

    public void setProperty(final String key, final Object value) {
        if (key.isEmpty())
            throw ExceptionFactory.propertyKeyCanNotBeEmpty();
        if (key.equals(StringFactory.ID))
            throw ExceptionFactory.propertyKeyIdIsReserved();
        if (key.equals(StringFactory.LABEL))
            throw ExceptionFactory.propertyKeyLabelIsReservedForEdges();

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