package com.tinkerpop.blueprints.pgm.impls.tg;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class TinkerElement implements Element, Serializable {

    protected Map<String, Object> properties = new HashMap<String, Object>();
    protected final String id;
    private final int hashCode;
    protected final TinkerGraph graph;

    protected TinkerElement(final String id, final TinkerGraph graph) {
        this.graph = graph;
        this.id = id;
        this.hashCode = id.hashCode();
    }

    public Set<String> getPropertyKeys() {
        return this.properties.keySet();
    }

    public Object getProperty(final String key) {
        return this.properties.get(key);
    }

    public void setProperty(final String key, final Object value) {
        if (key.equals(StringFactory.ID) || (key.equals(StringFactory.LABEL) && this instanceof Edge))
            throw new RuntimeException(key + StringFactory.PROPERTY_EXCEPTION_MESSAGE);

        Object oldValue = this.properties.put(key, value);
        if (this instanceof TinkerVertex)
            this.graph.vertexIndex.autoUpdate(key, value, oldValue, (TinkerVertex) this);
        else
            this.graph.edgeIndex.autoUpdate(key, value, oldValue, (TinkerEdge) this);
    }

    public Object removeProperty(final String key) {
        Object oldValue = this.properties.remove(key);
        if (this instanceof TinkerVertex)
            this.graph.vertexIndex.autoRemove(key, oldValue, (TinkerVertex) this);
        else
            this.graph.edgeIndex.autoRemove(key, oldValue, (TinkerEdge) this);
        return oldValue;
    }


    public int hashCode() {
        return this.hashCode;
    }

    public String getId() {
        return this.id;
    }
}
