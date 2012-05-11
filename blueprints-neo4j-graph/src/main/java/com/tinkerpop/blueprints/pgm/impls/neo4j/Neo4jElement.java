package com.tinkerpop.blueprints.pgm.impls.neo4j;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class Neo4jElement implements Element {

    protected final Neo4jGraph graph;
    protected PropertyContainer rawElement;

    public Neo4jElement(final Neo4jGraph graph) {
        this.graph = graph;
    }

    public Object getProperty(final String key) {
        if (this.rawElement.hasProperty(key))
            return this.rawElement.getProperty(key);
        else
            return null;
    }

    public void setProperty(final String key, final Object value) {
        if (key.equals(StringFactory.ID) || (key.equals(StringFactory.LABEL) && this instanceof Edge))
            throw new RuntimeException(key + StringFactory.PROPERTY_EXCEPTION_MESSAGE);
        try {
            // attempts to take a collection and convert it to an array so that Neo4j can consume it
            this.graph.autoStartTransaction();
            this.rawElement.setProperty(key, tryConvertCollectionToArray(value));
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Object removeProperty(final String key) {
        try {
            this.graph.autoStartTransaction();
            return this.rawElement.removeProperty(key);
        } catch (NotFoundException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Set<String> getPropertyKeys() {
        final Set<String> keys = new HashSet<String>();
        for (final String key : this.rawElement.getPropertyKeys()) {
            keys.add(key);
        }
        return keys;
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public PropertyContainer getRawElement() {
        return this.rawElement;
    }

    public Object getId() {
        if (this.rawElement instanceof Node) {
            return ((Node) this.rawElement).getId();
        } else {
            return ((Relationship) this.rawElement).getId();
        }
    }

    public boolean equals(final Object object) {
        return (null != object) && (this.getClass().equals(object.getClass()) && this.getId().equals(((Element) object).getId()));
    }

    private Object tryConvertCollectionToArray(final Object value) {
        if (value instanceof Collection<?>) {
            // convert this collection to an array.  the collection must
            // be all of the same type.
            try {
                final Collection<?> collection = (Collection<?>) value;
                Object[] array = null;
                final Iterator<?> objects = collection.iterator();
                for (int i = 0; objects.hasNext(); i++) {
                    Object object = objects.next();
                    if (array == null) {
                        array = (Object[]) Array.newInstance(object.getClass(), collection.size());
                    }

                    array[i] = object;
                }
                return array;
            } catch (ArrayStoreException ase) {
                // this fires off if the collection is not all of the same type
                return value;
            }
        } else {
            return value;
        }
    }
}
