package com.tinkerpop.blueprints.pgm.impls.neo4jbatch;

import com.tinkerpop.blueprints.pgm.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class Neo4jBatchElement implements Element {

    protected final Neo4jBatchGraph graph;
    protected final Long id;

    protected Neo4jBatchElement(final Neo4jBatchGraph graph, final Long id) {
        this.graph = graph;
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    protected abstract Map<String, Object> getPropertyMap();

    public Set<String> getPropertyKeys() {
        return this.getPropertyMap().keySet();
    }

    public Object getProperty(final String key) {
        return this.getPropertyMap().get(key);
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

    public boolean equals(final Object object) {
        return (null != object) && (this.getClass().equals(object.getClass()) && this.getId().equals(((Element) object).getId()));
    }
}
