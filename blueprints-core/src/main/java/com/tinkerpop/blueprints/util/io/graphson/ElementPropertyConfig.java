package com.tinkerpop.blueprints.util.io.graphson;

import java.util.Set;

/**
 * Configure how the GraphSON utility treats edge and vertex properties.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class ElementPropertyConfig {

    public static enum ElementPropertiesRule {
        INCLUDE, EXCLUDE
    }

    private final Set<String> vertexPropertyKeys;
    private final Set<String> edgePropertyKeys;
    private final ElementPropertiesRule vertexPropertiesRule;
    private final ElementPropertiesRule edgePropertiesRule;

    /**
     * A configuration that includes all properties of vertices and edges.
     */
    public static final ElementPropertyConfig AllProperties = new ElementPropertyConfig(null, null,
            ElementPropertiesRule.INCLUDE, ElementPropertiesRule.INCLUDE);

    public ElementPropertyConfig(final Set<String> vertexPropertyKeys, final Set<String> edgePropertyKeys,
                                 final ElementPropertiesRule vertexPropertiesRule, final ElementPropertiesRule edgePropertiesRule) {
        this.vertexPropertiesRule = vertexPropertiesRule;
        this.vertexPropertyKeys = vertexPropertyKeys;
        this.edgePropertiesRule = edgePropertiesRule;
        this.edgePropertyKeys = edgePropertyKeys;
    }

    /**
     * Construct a configuration that includes the specified properties from both vertices and edges.
     */
    public static ElementPropertyConfig IncludeProperties(final Set<String> vertexPropertyKeys,
                                                          final Set<String> edgePropertyKeys) {
        return new ElementPropertyConfig(vertexPropertyKeys, edgePropertyKeys, ElementPropertiesRule.INCLUDE,
                ElementPropertiesRule.INCLUDE);
    }

    /**
     * Construct a configuration that excludes the specified properties from both vertices and edges.
     */
    public static ElementPropertyConfig ExcludeProperties(final Set<String> vertexPropertyKeys,
                                                          final Set<String> edgePropertyKeys) {
        return new ElementPropertyConfig(vertexPropertyKeys, edgePropertyKeys, ElementPropertiesRule.EXCLUDE,
                ElementPropertiesRule.EXCLUDE);
    }

    public Set<String> getVertexPropertyKeys() {
        return vertexPropertyKeys;
    }

    public Set<String> getEdgePropertyKeys() {
        return edgePropertyKeys;
    }

    public ElementPropertiesRule getVertexPropertiesRule() {
        return vertexPropertiesRule;
    }

    public ElementPropertiesRule getEdgePropertiesRule() {
        return edgePropertiesRule;
    }
}
