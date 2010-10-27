package com.tinkerpop.blueprints.pgm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface IndexableGraph extends Graph {

    public final static String VERTICES = "vertices";
    public final static String EDGES = "edges";

    public void addIndex(Index index);

    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass);

    public Iterable<Index> getIndices();

    public void dropIndex(String indexName);

}
