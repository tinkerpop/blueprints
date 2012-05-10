package com.tinkerpop.blueprints.pgm;

import java.util.Set;

/**
 * An IndexableGraph is a graph that supports the indexing of its elements.
 * An index is typically some sort of tree structure that allows for the fast lookup of elements by key/value pairs.
 * There are two types of indices: manual and automatic.
 * Manual indices have an Index object associated with them and allow the user to specify the putting and getting of elements into the index.
 * Automatic indices are based on the key/value properties of an element.
 * Manual indices are not typical of most graph databases while automatic indices are.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface IndexableGraph extends Graph {

    /**
     * Generate a manual index with a particular name for a particular class.
     *
     * @param indexName       the name of the manual index
     * @param indexClass      the element class that this index is indexing (can be base class)
     * @param indexParameters a collection of parameters for the underlying index implementation
     * @param <T>             the element class that this index is indexing (can be base class)
     * @return the index created
     */
    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Parameter... indexParameters);

    /**
     * Get a manual index from the graph by its name and index class. An index is unique up to name.
     *
     * @param indexName  the name of the index to retrieve
     * @param indexClass the class of the elements being indexed (can be base class)
     * @param <T>        the class of the elements being indexed (can be base class)
     * @return the retrieved index
     */
    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass);

    /**
     * Get all the manual indices maintained by the graph.
     *
     * @return the indices associated with the graph
     */
    public Iterable<Index<? extends Element>> getIndices();

    /**
     * Remove a manual index associated with the graph.
     *
     * @param indexName the name of the index to drop
     */
    public void dropIndex(String indexName);

}
