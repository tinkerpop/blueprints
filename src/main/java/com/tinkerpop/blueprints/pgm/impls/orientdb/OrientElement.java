package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphElement;
import com.orientechnologies.orient.core.id.ORID;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;
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

        graph.beginTransaction();

        try {
            this.rawElement.set(key, value);
            this.save();

            for (AutomaticIndex autoIndex : this.graph.getAutoIndices()) {
                if (autoIndex.getIndexClass().isAssignableFrom(this.getClass()) && autoIndex.doAutoIndex(key)) {
                    if (null != oldValue)
                        autoIndex.remove(key, oldValue, this);
                    autoIndex.put(key, value, this);
                }
            }

            graph.commitTransaction();

        } catch (RuntimeException e) {
            graph.rollbackTransaction();
            throw e;
        }
    }

    public Object removeProperty(final String key) {
        graph.beginTransaction();

        try {
            final Object oldValue = this.rawElement.remove(key);
            this.save();

            if (null != oldValue) {
                for (AutomaticIndex autoIndex : this.graph.getAutoIndices()) {
                    if (autoIndex.getIndexClass().isAssignableFrom(this.getClass()) && autoIndex.doAutoIndex(key))
                        autoIndex.remove(key, oldValue, this);
                }
            }
            graph.commitTransaction();
            return oldValue;

        } catch (RuntimeException e) {

            graph.rollbackTransaction();
            throw e;
        }
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
        graph.beginTransaction();

        try {
            this.rawElement.delete();
            graph.commitTransaction();
        } catch (RuntimeException e) {
            graph.rollbackTransaction();
            throw e;
        }
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

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OrientElement other = (OrientElement) obj;
        if (this.rawElement == null) {
            if (other.rawElement != null)
                return false;
        } else if (!this.rawElement.equals(other.rawElement))
            return false;
        return true;
    }
}
