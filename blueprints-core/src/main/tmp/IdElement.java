package com.tinkerpop.blueprints.pgm.util.wrappers.id;

import com.tinkerpop.blueprints.pgm.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
abstract class IdElement implements Element {
    protected final Element base;

    public IdElement(Element base) {
        this.base = base;
    }

    public Object getProperty(String s) {
        if (s.equals(IdGraph.ID)) {
            return null;
        }

        return base.getProperty(s);
    }

    public Set<String> getPropertyKeys() {
        Set<String> keys = base.getPropertyKeys();

        // TODO: creating a new set for each call is expensive...
        // but so is maintaining a second set or doing comparisons on each read from the set
        Set<String> s = new HashSet<String>();
        s.addAll(keys);
        s.remove(IdGraph.ID);

        return s;
    }

    public void setProperty(String s, Object o) {
        if (s.equals(IdGraph.ID)) {
            throw new IllegalArgumentException("can't set value for reserved property '" + IdGraph.ID + "'");
        }

        base.setProperty(s, o);
    }

    public Object removeProperty(String s) {
        if (s.equals(IdGraph.ID)) {
            throw new IllegalArgumentException("can't remove value for reserved property '" + IdGraph.ID + "'");
        }

        return base.removeProperty(s);
    }

    public Object getId() {
        return base.getProperty(IdGraph.ID);
    }
}
