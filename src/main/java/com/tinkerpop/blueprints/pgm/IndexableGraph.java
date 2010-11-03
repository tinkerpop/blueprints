package com.tinkerpop.blueprints.pgm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface IndexableGraph extends Graph {

    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Index.Type type);

    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass);

    public Iterable<Index<?>> getIndices();

    public void dropIndex(String indexName);

}
