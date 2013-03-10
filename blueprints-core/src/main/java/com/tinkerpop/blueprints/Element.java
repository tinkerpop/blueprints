package com.tinkerpop.blueprints;

import java.util.Set;

/**
 * An Element is the base class for both vertices and edges.
 * An element has an identifier that must be unique to its inheriting classes (vertex or edges).
 * An element can maintain a collection of key/value properties.
 * Keys are always Strings and values can be any Object.
 * Particular implementations can reduce the space of objects that can be used as values.
 * Typically, objects are Java primitives (e.g. String, long, int, boolean, etc.)
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract interface Element {

    /**
     * Return the object value associated with the provided string key.
     * If no value exists for that key, return null.
     *
     * @param key the key of the key/value property
     * @return the object value related to the string key
     */
    public <T> T getProperty(String key);

    /**
     * Return all the keys associated with the element.
     *
     * @return the set of all string keys associated with the element
     */
    public Set<String> getPropertyKeys();

    /**
     * Assign a key/value property to the element.
     * If a value already exists for this key, then the previous key/value is overwritten.
     *
     * @param key   the string key of the property
     * @param value the object value o the property
     */
    public void setProperty(String key, Object value);

    /**
     * Un-assigns a key/value property from the element.
     * The object value of the removed property is returned.
     *
     * @param key the key of the property to remove from the element
     * @return the object value associated with that key prior to removal
     */
    public <T> T removeProperty(String key);

    /**
     * Remove the element from the graph.
     */
    public void remove();

    /**
     * An identifier that is unique to its inheriting class.
     * All vertices of a graph must have unique identifiers.
     * All edges of a graph must have unique identifiers.
     *
     * @return the identifier of the element
     */
    public Object getId();

}
