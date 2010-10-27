package com.tinkerpop.blueprints.pgm.impls.tg;


import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class TinkerElement implements Element {

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
        Object oldValue = this.properties.put(key, value);
        for (AutomaticIndex index : this.graph.getAutoIndices()) {
            if (this.getClass().isAssignableFrom(index.getIndexClass()) && index.doAutoIndex(key)) {
                index.remove(key, oldValue, this);
                index.put(key, value, this);
            }
        }
    }

    public Object removeProperty(final String key) {

        Object value = this.properties.remove(key);
        for (AutomaticIndex index : this.graph.getAutoIndices()) {
            if (this.getClass().isAssignableFrom(index.getIndexClass())  && index.doAutoIndex(key)) {
                index.remove(key, value, this);
            }
        }
        return value;
    }


    public int hashCode() {
        return this.getId().hashCode();
    }

    public String getId() {
        return this.id;
    }
}
