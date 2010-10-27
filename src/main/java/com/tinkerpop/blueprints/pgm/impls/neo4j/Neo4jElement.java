package com.tinkerpop.blueprints.pgm.impls.neo4j;


import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class Neo4jElement implements Element {

    protected final Neo4jGraph graph;
    protected PropertyContainer element;

    public Neo4jElement(final Neo4jGraph graph) {
        this.graph = graph;
    }

    public Object getProperty(final String key) {
        if (this.element.hasProperty(key))
            return this.element.getProperty(key);
        else
            return null;
    }

    public void setProperty(final String key, final Object value) {

        Object oldValue = this.getProperty(key);

        for (AutomaticIndex autoIndex : this.graph.getAutoIndices()) {
            if (autoIndex.doAutoIndex(key, this.getClass())) {
                if (null != oldValue)
                    autoIndex.remove(key, oldValue, this);
                autoIndex.put(key, value, this);
            }
        }

        this.element.setProperty(key, value);
        this.graph.stopStartTransaction();
    }

    public Object removeProperty(final String key) {
        try {
            Object oldValue = this.getProperty(key);
            if (null != oldValue) {
                for (AutomaticIndex autoIndex : this.graph.getAutoIndices()) {
                    if (autoIndex.doAutoIndex(key, this.getClass()))
                        autoIndex.remove(key, oldValue, this);
                }
            }

            Object value = this.element.removeProperty(key);
            this.graph.stopStartTransaction();
            return value;
        } catch (NotFoundException e) {
            return null;
        }
    }

    public Set<String> getPropertyKeys() {
        final Set<String> keys = new HashSet<String>();
        for (final String key : this.element.getPropertyKeys()) {
            keys.add(key);
        }
        return keys;
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public PropertyContainer getRawElement() {
        return this.element;
    }

    public Object getId() {
        if (this.element instanceof Node) {
            return ((Node) this.element).getId();
        } else {
            return ((Relationship) this.element).getId();
        }
    }

    public boolean equals(Object object) {
        return (this.getClass().equals(object.getClass()) && this.getId().equals(((Element) object).getId()));
    }
}
