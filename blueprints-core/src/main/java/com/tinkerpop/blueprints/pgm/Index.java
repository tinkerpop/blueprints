package com.tinkerpop.blueprints.pgm;

/**
 * An index maintains a mapping between some key/value pair and an element.
 * A manual index requires that the developers code explicitly put elements of the graph into the index.
 * A the key/value pair need not be specific to the element properties.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Index<T extends Element> {

    /**
     * For IndexableGraphs that support vertex indexing, an AutomaticIndex must exist at construction named "vertices."
     */
    public static final String VERTICES = "vertices";
    /**
     * For IndexableGraphs that support edge indexing, an AutomaticIndex must exist at construction named "edges."
     */
    public static final String EDGES = "edges";

    /**
     * An Index is either manual or automatic. Automatic types must implement AutomaticIndex.
     */
    enum Type {
        MANUAL, AUTOMATIC
    }

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
     * Get the type of the index. This can be determined using instanceof on the interface names as well.
     *
     * @return the index type
     */
    public Type getIndexType();

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
    public CloseableSequence<T> get(String key, Object value);

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
