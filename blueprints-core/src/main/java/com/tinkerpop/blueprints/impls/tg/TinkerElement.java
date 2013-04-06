package com.tinkerpop.blueprints.impls.tg;


import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
abstract class TinkerElement implements Element, Serializable {

    protected Map<String, Object> properties = new HashMap<String, Object>();
    protected final String id;
    protected final TinkerGraph graph;

    protected TinkerElement(final String id, final TinkerGraph graph) {
        this.graph = graph;
        this.id = id;
    }

    public Set<String> getPropertyKeys() {
        return new HashSet<String>(this.properties.keySet());
    }

    public <T> T getProperty(final String key) {
        return (T) this.properties.get(key);
    }

    public void setProperty(final String key, final Object value) {
        ElementHelper.validateProperty(this, key, value);
        Object oldValue = this.properties.put(key, value);
        if (this instanceof TinkerVertex)
            this.graph.vertexKeyIndex.autoUpdate(key, value, oldValue, (TinkerVertex) this);
        else
            this.graph.edgeKeyIndex.autoUpdate(key, value, oldValue, (TinkerEdge) this);
    }

    public <T> T removeProperty(final String key) {
        Object oldValue = this.properties.remove(key);
        if (this instanceof TinkerVertex)
            this.graph.vertexKeyIndex.autoRemove(key, oldValue, (TinkerVertex) this);
        else
            this.graph.edgeKeyIndex.autoRemove(key, oldValue, (TinkerEdge) this);
        return (T) oldValue;
    }


    public int hashCode() {
        return this.id.hashCode();
    }

    public String getId() {
        return this.id;
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    public void remove() {
        if (this instanceof Vertex)
            this.graph.removeVertex((Vertex) this);
        else
            this.graph.removeEdge((Edge) this);
    }
}
