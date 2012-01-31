package com.tinkerpop.blueprints.pgm.impls.named;

import com.tinkerpop.blueprints.pgm.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NamedElement implements Element {

    protected Element rawElement;
    protected NamedGraph graph;

    public NamedElement(final Element rawElement, final NamedGraph graph) {
        this.rawElement = rawElement;
        this.graph = graph;
    }

    public void setProperty(final String key, final Object value) {
        if (!key.equals(this.graph.getWriteGraphKey()))
            this.rawElement.setProperty(key, value);
    }

    public Object getProperty(final String key) {
        if (key.equals(this.graph.getWriteGraphKey()))
            return null;
        return this.rawElement.getProperty(key);
    }

    public Object removeProperty(final String key) {
        if (key.equals(this.graph.getWriteGraphKey()))
            return null;
        return this.rawElement.removeProperty(key);
    }

    public Set<String> getPropertyKeys() {
        final Set<String> keys = new HashSet<String>(this.rawElement.getPropertyKeys());
        keys.remove(this.graph.getWriteGraphKey());
        return keys;
    }

    public Object getId() {
        return this.rawElement.getId();
    }

    public boolean equals(final Object object) {
        return null != object && this.getClass().equals(object.getClass()) && this.getId().equals(((Element) object).getId());
    }

    public int hashCode() {
        return this.rawElement.hashCode();
    }

    public Element getRawElement() {
        return this.rawElement;
    }

    public String getWriteGraph() {
        return (String) this.rawElement.getProperty(this.graph.getWriteGraphKey());
    }

    public void setWriteGraph(final String writeGraph) {
        this.rawElement.setProperty(this.graph.getWriteGraphKey(), writeGraph);
    }

    public String toString() {
        return this.rawElement.toString();
    }
}
