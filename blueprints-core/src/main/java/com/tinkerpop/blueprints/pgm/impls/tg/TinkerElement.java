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
        for (TinkerAutomaticIndex index : this.graph.getAutoIndices()) {
            index.autoUpdate(key, value, oldValue, this);
        }
    }

    public Object removeProperty(final String key) {
        Object oldValue = this.properties.remove(key);
        for (TinkerAutomaticIndex index : this.graph.getAutoIndices()) {
            index.autoRemove(key, oldValue, this);
        }
        return oldValue;
    }


    public int hashCode() {
        return this.hashCode;
    }

    public String getId() {
        return this.id;
    }
}
