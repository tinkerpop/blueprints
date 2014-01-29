package com.tinkerpop.blueprints.impls.neo4j2;


import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
abstract class Neo4j2Element implements Element {

    protected final Neo4j2Graph graph;
    protected PropertyContainer rawElement;

    public Neo4j2Element(final Neo4j2Graph graph) {
        this.graph = graph;
    }

    public <T> T getProperty(final String key) {
        this.graph.autoStartTransaction(false);
        if (this.rawElement.hasProperty(key))
            return (T) tryConvertCollectionToArrayList(this.rawElement.getProperty(key));
        else
            return null;
    }

    public void setProperty(final String key, final Object value) {
        ElementHelper.validateProperty(this, key, value);
        this.graph.autoStartTransaction(true);
        // attempts to take a collection and convert it to an array so that Neo4j can consume it
        this.rawElement.setProperty(key, tryConvertCollectionToArray(value));
    }

    public <T> T removeProperty(final String key) {
        if (!this.rawElement.hasProperty(key))
            return null;
        else {
            this.graph.autoStartTransaction(true);
            return (T) this.rawElement.removeProperty(key);
        }
    }

    public Set<String> getPropertyKeys() {
        this.graph.autoStartTransaction(false);
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
        this.graph.autoStartTransaction(false);
        if (this.rawElement instanceof Node) {
            return ((Node) this.rawElement).getId();
        } else {
            return ((Relationship) this.rawElement).getId();
        }
    }

    public void remove() {
        if (this instanceof Vertex)
            this.graph.removeVertex((Vertex) this);
        else
            this.graph.removeEdge((Edge) this);
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
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
            } catch (final ArrayStoreException ase) {
                // this fires off if the collection is not all of the same type
                return value;
            }
        } else {
            return value;
        }
    }

    private Object tryConvertCollectionToArrayList(final Object value) {
        if (value.getClass().isArray()) {
            // convert primitive array to an ArrayList.  
            try {
                ArrayList<Object> list = new ArrayList<Object>();
                int arrlength = Array.getLength(value);
                for (int i = 0; i < arrlength; i++) {
                    Object object = Array.get(value, i);
                    list.add(object);
                }
                return list;
            } catch (final Exception e) {
                // this fires off if the collection is not an array
                return value;
            }
        } else {
            return value;
        }
    }
}
