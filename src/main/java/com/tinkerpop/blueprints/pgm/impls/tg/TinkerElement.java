package com.tinkerpop.blueprints.pgm.impls.tg;


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

        for (TinkerAutomaticIndex index : this.graph.getAutoIndices()) {
            index.autoUpdate(key, value, this);
        }
        this.properties.put(key, value);
    }

    public Object removeProperty(final String key) {

        for (TinkerAutomaticIndex index : this.graph.getAutoIndices()) {
            index.autoRemove(key, this);
        }
        return this.properties.remove(key);
    }


    public int hashCode() {
        return this.getId().hashCode();
    }

    public String getId() {
        return this.id;
    }
}
