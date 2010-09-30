package com.tinkerpop.blueprints.pgm.impls.tg;


import com.tinkerpop.blueprints.pgm.Element;

import com.tinkerpop.blueprints.pgm.Graph;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class TinkerElement implements Element {

    protected Map<String, Object> properties = new HashMap<String, Object>();
    protected final String id;
    protected final TinkerIndex index;
    private final Graph graph;

    protected TinkerElement(final String id, final TinkerIndex index, final TinkerGraph graph) {
        this.index = index;
        this.id = id;
        this.graph = graph;
    }

    public Set<String> getPropertyKeys() {
        return this.properties.keySet();
    }

    public void setProperty(final String key, final Object value) {
        this.index.remove(key, this.getProperty(key), this);
        this.properties.put(key, value);
        this.index.put(key, value, this);
    }

    public Object getProperty(final String key) {
        return this.properties.get(key);
    }

    public Object removeProperty(final String key) {
        this.index.remove(key, this.getProperty(key), this);
        return this.properties.remove(key);
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public String getId() {
        return this.id;
    }

    public Graph getGraph() {
        return this.graph;
    }
}
