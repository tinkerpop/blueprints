package com.tinkerpop.blueprints.impls.neo4j2.batch;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.util.ElementHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
abstract class Neo4j2BatchElement implements Element {

    protected final Neo4j2BatchGraph graph;
    protected final Long id;

    protected Neo4j2BatchElement(final Neo4j2BatchGraph graph, final Long id) {
        this.graph = graph;
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    public abstract Map<String, Object> getPropertyMap();

    public Set<String> getPropertyKeys() {
        return this.getPropertyMap().keySet();
    }

    public <T> T getProperty(final String key) {
        return (T) this.getPropertyMap().get(key);
    }

    protected Map<String, Object> getPropertyMapClone() {
        final Map<String, Object> clone = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : this.getPropertyMap().entrySet()) {
            clone.put(entry.getKey(), entry.getValue());
        }
        return clone;

    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public void remove() {
        throw new UnsupportedOperationException(Neo4j2BatchTokens.DELETE_OPERATION_MESSAGE);
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }
}
