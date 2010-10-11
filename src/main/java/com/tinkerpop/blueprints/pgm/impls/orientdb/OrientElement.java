package com.tinkerpop.blueprints.pgm.impls.orientdb;

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
    protected final OGraphElement raw;

    protected OrientElement(final OrientGraph graph, final OGraphElement rawElement) {
        this.graph = graph;
        this.raw = rawElement;

        graph.putElementInCache(this);
    }

    public void setProperty(final String key, final Object value) {
        final Object oldValue = raw.get(key);

        graph.beginTransaction();

        try {
            this.raw.set(key, value);
            this.save();

            if (oldValue != null)
                // REMOVE OLD INDEXED PROPERTY
                graph.getIndex().remove(key, oldValue, this);
            graph.getIndex().put(key, value, this);

            graph.commitTransaction();

        } catch (RuntimeException e) {

            graph.rollbackTransaction();
            throw e;
        }
    }

    public Object removeProperty(final String key) {
        graph.beginTransaction();

        try {
            final Object old = this.raw.remove(key);
            this.save();
            graph.getIndex().remove(key, old, this);
            graph.commitTransaction();

            return old;
        } catch (RuntimeException e) {

            graph.rollbackTransaction();
            throw e;
        }
    }

    public Object getProperty(final String key) {
        return this.raw.get(key);
    }

    public Set<String> getPropertyKeys() {
        final Set<String> set = this.raw.propertyNames();
        set.remove(LABEL);
        return set;
    }

    /**
     * Returns the Element Id assuring to save it if it's transient yet.
     */
    public Object getId() {
        ORID rid = this.raw.getId();
        this.save();
        return rid;
    }

    protected void delete() {
        graph.beginTransaction();

        try {
            graph.removeElementFromCache(this.raw.getId());
            this.raw.delete();
            graph.commitTransaction();

        } catch (RuntimeException e) {

            graph.rollbackTransaction();
            throw e;
        }
    }

    protected void save() {
        this.raw.save();
    }

    public OGraphElement getRaw() {
        return raw;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.raw == null) ? 0 : this.raw.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OrientElement other = (OrientElement) obj;
        if (this.raw == null) {
            if (other.raw != null)
                return false;
        } else if (!this.raw.equals(other.raw))
            return false;
        return true;
    }
}
