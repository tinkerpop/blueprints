package com.tinkerpop.blueprints;

/**
 * An Index maintains a mapping between some key/value pair and an element.
 * An index requires that the developer explicitly put elements of the graph into the index.
 * The key/value pair need not be specific to the element properties.
 * There is a query method to support index lookups beyond exact matches.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Index<T extends Element> {

    /**
     * Get the name of the index.
     *
     * @return the name of the index
     */
    public String getIndexName();

    /**
     * Get the class that this index is indexing.
     *
     * @return the class this index is indexing
     */
    public Class<T> getIndexClass();

    /**
     * Index an element by a key and a value.
     *
     * @param key     the key to index the element by
     * @param value   the value to index the element by
     * @param element the element to index
     */
    public void put(String key, Object value, T element);

    /**
     * Get all elements that are indexed by the provided key/value.
     *
     * @param key   the key of the indexed elements
     * @param value the value of the indexed elements
     * @return an iterable of elements that have a particular key/value in the index
     */
    public CloseableIterable<T> get(String key, Object value);

    /**
     * Get all the elements that are indexed by the provided key and specified query object.
     * This is useful for graph implementations that support complex query capabilities.
     * If querying is not supported, simply throw an UnsupportedOperationException.
     *
     * @param key   the key of the indexed elements
     * @param query the query object for the indexed elements' keys
     * @return an iterable of elements that have a particular key/value in the index that match the query object
     */
    public CloseableIterable<T> query(String key, Object query);

    /**
     * Get a count of elements with a particular key/value pair.
     * The semantics are the same as the get method.
     *
     * @param key   denoting the sub-index to search
     * @param value the value to search for
     * @return the collection of elements that meet that criteria
     */
    public long count(String key, Object value);

    /**
     * Remove an element indexed by a particular key/value.
     *
     * @param key     the key of the indexed element
     * @param value   the value of the indexed element
     * @param element the element to remove given the key/value pair
     */
    public void remove(String key, Object value, T element);

}
