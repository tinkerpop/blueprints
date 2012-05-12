package com.tinkerpop.blueprints.pgm.impls.tg;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.util.ExceptionFactory;
import com.tinkerpop.blueprints.pgm.util.StringFactory;

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
    protected final TinkerGraph graph;

    protected TinkerElement(final String id, final TinkerGraph graph) {
        this.graph = graph;
        this.id = id;
    }

    public Set<String> getPropertyKeys() {
        return this.properties.keySet();
    }

    public Object getProperty(final String key) {
        return this.properties.get(key);
    }

    public void setProperty(final String key, final Object value) {
        if (key.equals(StringFactory.ID))
            throw ExceptionFactory.propertyKeyIdIsReserved();
        if (key.equals(StringFactory.LABEL) && this instanceof Edge)
            throw ExceptionFactory.propertyKeyLabelIsReservedForEdges();

        Object oldValue = this.properties.put(key, value);
        if (this instanceof TinkerVertex)
            this.graph.vertexKeyIndex.autoUpdate(key, value, oldValue, (TinkerVertex) this);
        else
            this.graph.edgeKeyIndex.autoUpdate(key, value, oldValue, (TinkerEdge) this);
    }

    public Object removeProperty(final String key) {
        Object oldValue = this.properties.remove(key);
        if (this instanceof TinkerVertex)
            this.graph.vertexKeyIndex.autoRemove(key, oldValue, (TinkerVertex) this);
        else
            this.graph.edgeKeyIndex.autoRemove(key, oldValue, (TinkerEdge) this);
        return oldValue;
    }


    public int hashCode() {
        return this.id.hashCode();
    }

    public String getId() {
        return this.id;
    }
}
