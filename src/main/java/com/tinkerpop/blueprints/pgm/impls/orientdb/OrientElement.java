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
    protected OGraphElement raw;

    protected OrientElement(final OGraphElement element) {
        this.raw = element;
    }

    public void setProperty(final String key, final Object value) {
        this.raw.set(key, value);
        save();
    }

    public Object removeProperty(final String key) {
        final Object old = this.raw.remove(key);
        this.save();
        return old;
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
        this.raw.delete();
    }

    protected void save() {
        this.raw.save();
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.raw == null) ? 0 : raw.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OrientElement other = (OrientElement) obj;
        if (this.raw == null) {
            if (other.raw != null)
                return false;
        } else if (!this.raw.equals(other.raw))
            return false;
        return true;
    }
}
