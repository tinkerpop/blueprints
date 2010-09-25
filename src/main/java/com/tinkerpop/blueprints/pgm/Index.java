package com.tinkerpop.blueprints.pgm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Index {

    /**
     * Put an element into the index according to a particular key and value.
     *
     * @param key     denoting the sub-index to use
     * @param value   the value of element for that key
     * @param element the element to store
     */
    public void put(String key, Object value, Element element);

    /**
     * Get all elements with a particular key/value pair.
     * The value need not be a direct object match, but can be,
     * according to the underlying implementation,
     * a text search, regular expression, logical expression, etc.
     *
     * @param key   denoting the sub-index to search
     * @param value the value to search for
     * @return the collection of elements that meet that criteria
     */
    public Iterable<Element> get(String key, Object value);

    /**
     * Get the number of elements with a particular key/value pair.
     * The semantics are the same as the get method.
     *
     * @param key   denoting the sub-index to search
     * @param value the value to search for
     * @return the number of elements that meet that criteria
     */
    public long count(String key, Object value);

    /**
     * Remove an element with a particular key/value from the index
     *
     * @param key     denoting the particular sub-index to remove the element from
     * @param value   denote the value that the element is stored under
     * @param element the element to remove from that key/value slot
     */
    public void remove(String key, Object value, Element element);

    /**
     * Automatically index all properties for all elements added to the graph.
     *
     * @param indexAll index all properties for all elements added to the graph
     */
    public void indexAll(boolean indexAll);

    /**
     * When indexAll is false, denote which keys to index of the elements.
     *
     * @param key a key to index
     */
    public void addIndexKey(String key);

    /**
     * When indexAll is false, remove a key to index.
     *
     * @param key a key not to index
     */
    public void removeIndexKey(String key);
}
