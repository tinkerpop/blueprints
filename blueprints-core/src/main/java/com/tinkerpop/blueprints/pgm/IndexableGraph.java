package com.tinkerpop.blueprints.pgm;

import java.util.Set;

/**
 * An indexable graph is a graph that supports the indexing of its elements.
 * An index is typically some sort of tree structure that allows for the fast lookup of elements by key/value pairs.
 * All indexable graphs are initially constructed with two automatic indices called "vertices" and "edges."
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface IndexableGraph extends Graph {

    /**
     * Generate a manual index with a particular name for a particular class.
     *
     * @param indexName  the name of the manual index
     * @param indexClass the element class that this index is indexing (can be base class)
     * @param <T>        the element class that this index is indexing (can be base class)
     * @return the index created
     */
    public <T extends Element> Index<T> createManualIndex(String indexName, Class<T> indexClass);

    /**
     * Generate an automatic index with a particular name, for a particular class, and of a particular type.
     *
     * @param indexName  the name of the automatic index
     * @param indexClass the element class that this index is indexing (can be base class)
     * @param indexKeys  the keys to automatically index
     * @param <T>        the element class that this index is indexing (can be base class)
     * @return the index created
     */
    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(String indexName, Class<T> indexClass, Set<String> indexKeys);

    /**
     * Get an index from the graph by its name and index class. An index is unique up to name.
     *
     * @param indexName  the name of the index to retrieve
     * @param indexClass the class of the elements being indexed (can be base class)
     * @param <T>        the class of the elements being indexed (can be base class)
     * @return the retrieved index
     */
    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass);

    /**
     * Get all the indices maintained by the graph.
     *
     * @return the indices associated with the graph
     */
    public Iterable<Index<? extends Element>> getIndices();

    /**
     * Remove an index associated with the graph.
     *
     * @param indexName the name of the index to drop
     */
    public void dropIndex(String indexName);

}
