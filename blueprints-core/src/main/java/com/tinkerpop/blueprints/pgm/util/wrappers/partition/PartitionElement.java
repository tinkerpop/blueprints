package com.tinkerpop.blueprints.pgm.util.wrappers.partition;

import com.tinkerpop.blueprints.pgm.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionElement implements Element {

    protected Element rawElement;
    protected PartitionGraph graph;

    public PartitionElement(final Element rawElement, final PartitionGraph graph) {
        this.rawElement = rawElement;
        this.graph = graph;
    }

    public void setProperty(final String key, final Object value) {
        if (!key.equals(this.graph.getPartitionKey()))
            this.rawElement.setProperty(key, value);
    }

    public Object getProperty(final String key) {
        if (key.equals(this.graph.getPartitionKey()))
            return null;
        return this.rawElement.getProperty(key);
    }

    public Object removeProperty(final String key) {
        if (key.equals(this.graph.getPartitionKey()))
            return null;
        return this.rawElement.removeProperty(key);
    }

    public Set<String> getPropertyKeys() {
        final Set<String> keys = new HashSet<String>(this.rawElement.getPropertyKeys());
        keys.remove(this.graph.getPartitionKey());
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

    public String getPartition() {
        return (String) this.rawElement.getProperty(this.graph.getPartitionKey());
    }

    public void setPartition(final String partition) {
        this.rawElement.setProperty(this.graph.getPartitionKey(), partition);
    }

    public String toString() {
        return this.rawElement.toString();
    }
}
