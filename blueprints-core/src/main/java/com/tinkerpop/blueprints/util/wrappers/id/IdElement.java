package com.tinkerpop.blueprints.util.wrappers.id;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class IdElement implements Element {
    protected final Element baseElement;

    protected final IdGraph idGraph;

    protected final boolean propertyBased;

    protected IdElement(final Element baseElement,
                        final IdGraph idGraph,
                        final boolean propertyBased) {
        this.baseElement = baseElement;
        this.idGraph = idGraph;
        this.propertyBased = propertyBased;
    }

    public <T> T getProperty(final String key) {
        if (propertyBased && key.equals(IdGraph.ID)) {
            return null;
        } else {
            return baseElement.getProperty(key);
        }
    }

    public Set<String> getPropertyKeys() {
        if (propertyBased) {
            final Set<String> keys = baseElement.getPropertyKeys();
            final Set<String> s = new HashSet<String>();
            s.addAll(keys);
            s.remove(IdGraph.ID);
            return s;
        } else {
            return baseElement.getPropertyKeys();
        }
    }

    public void setProperty(final String key, final Object value) {
        if (propertyBased && key.equals(IdGraph.ID)) {
            throw new IllegalArgumentException("Unable to set value for reserved property " + IdGraph.ID);
        }

        baseElement.setProperty(key, value);
    }

    public <T> T removeProperty(final String key) {
        if (propertyBased) {
            if (key.equals(IdGraph.ID)) {
                throw new IllegalArgumentException("Unable to remove value for reserved property " + IdGraph.ID);
            }
        }

        return baseElement.removeProperty(key);
    }

    public Object getId() {
        return propertyBased
                ? baseElement.getProperty(IdGraph.ID)
                : baseElement.getId();
    }

    public int hashCode() {
        return this.baseElement.hashCode();
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    public void remove() {
        if (this instanceof Vertex) {
            this.idGraph.removeVertex((Vertex) this);
        } else {
            this.idGraph.removeEdge((Edge) this);
        }
    }
}
