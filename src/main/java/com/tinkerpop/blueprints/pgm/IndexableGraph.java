package com.tinkerpop.blueprints.pgm;

/**
 * An indexable graph is a graph that supports the indexing of its elements.
 * An index is typically some sort of tree structure that allows for the fast lookup of elements by key/value pairs.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface IndexableGraph extends Graph {

    /**
     * Generate an index with a particular name, for a particular class, and of a particular type.
     *
     * @param indexName  the name of the index
     * @param indexClass the element class that this index is indexing
     * @param type       whether the index is a manual or automatic index
     * @param <T>        the element class that this index is indexing
     * @return the index created
     */
    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Index.Type type);

    /**
     * Get an index from the graph by its name and index class. An index is unique up to name.
     *
     * @param indexName  the name of the index to retrieve
     * @param indexClass the class of the elements being indexed
     * @param <T>        the class of the elements being indexed
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
