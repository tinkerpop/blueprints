package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.tinkerpop.blueprints.pgm.Graph;
import com.orientechnologies.orient.core.db.graph.OGraphElement;
import com.orientechnologies.orient.core.id.ORID;
import com.tinkerpop.blueprints.pgm.Element;

import java.util.Set;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public abstract class OrientElement implements Element {

    protected static final String LABEL = "label";
    protected final OrientGraph graph;
    protected final OGraphElement rawElement;

    protected OrientElement(final OrientGraph graph, final OGraphElement rawElement) {
        this.graph = graph;
        this.rawElement = rawElement;
    }

    public void setProperty(final String key, final Object value) {
        final Object oldValue = rawElement.get(key);

        this.rawElement.set(key, value);
        this.save();

        if (oldValue != null)
            this.graph.getIndex().remove(key, oldValue, this);

        this.graph.getIndex().put(key, value, this);
    }

    public Object removeProperty(final String key) {
        final Object old = this.rawElement.remove(key);
        this.save();
        this.graph.getIndex().remove(key, old, this);
        return old;
    }

    public Object getProperty(final String key) {
        return this.rawElement.get(key);
    }

    public Set<String> getPropertyKeys() {
        final Set<String> set = this.rawElement.propertyNames();
        set.remove(LABEL);
        return set;
    }

    /**
     * Returns the Element Id assuring to save it if it's transient yet.
     */
    public Object getId() {
        ORID rid = this.rawElement.getId();
        this.save();
        return rid;
    }

    protected void delete() {
        this.rawElement.delete();
    }

    protected void save() {
        this.rawElement.save();
    }

    public OGraphElement getRawElement() {
        return rawElement;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.rawElement == null) ? 0 : this.rawElement.hashCode());
        return result;
    }

    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (getClass() != object.getClass())
            return false;
        final OrientElement other = (OrientElement) object;
        if (this.rawElement == null) {
            if (other.rawElement != null)
                return false;
        } else if (!this.rawElement.equals(other.rawElement))
            return false;
        return true;
    }

    public Graph getGraph() {
      return this.graph;
    }
}
