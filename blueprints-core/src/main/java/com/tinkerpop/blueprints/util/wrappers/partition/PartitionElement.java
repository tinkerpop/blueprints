package com.tinkerpop.blueprints.util.wrappers.partition;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class PartitionElement implements Element {

    protected Element baseElement;
    protected PartitionGraph graph;

    protected PartitionElement(final Element baseElement, final PartitionGraph partitionGraph) {
        this.baseElement = baseElement;
        this.graph = partitionGraph;
    }

    public void setProperty(final String key, final Object value) {
        if (!key.equals(this.graph.getPartitionKey()))
            this.baseElement.setProperty(key, value);
    }

    public <T> T getProperty(final String key) {
        if (key.equals(this.graph.getPartitionKey()))
            return null;
        return this.baseElement.getProperty(key);
    }

    public <T> T removeProperty(final String key) {
        if (key.equals(this.graph.getPartitionKey()))
            return null;
        return this.baseElement.removeProperty(key);
    }

    public Set<String> getPropertyKeys() {
        final Set<String> keys = new HashSet<String>(this.baseElement.getPropertyKeys());
        keys.remove(this.graph.getPartitionKey());
        return keys;
    }

    public Object getId() {
        return this.baseElement.getId();
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    public int hashCode() {
        return this.baseElement.hashCode();
    }

    public Element getBaseElement() {
        return this.baseElement;
    }

    public String getPartition() {
        return (String) this.baseElement.getProperty(this.graph.getPartitionKey());
    }

    public void setPartition(final String partition) {
        this.baseElement.setProperty(this.graph.getPartitionKey(), partition);
    }

    public void remove() {
        if (this instanceof Vertex)
            this.graph.removeVertex((Vertex) this);
        else
            this.graph.removeEdge((Edge) this);
    }

    public String toString() {
        return this.baseElement.toString();
    }
}
