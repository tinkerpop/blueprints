package com.tinkerpop.blueprints.pgm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface IndexableGraph extends Graph {

    public final static String VERTICES = "vertices";
    public final static String EDGES = "edges";

    public enum Type {
        MANUAL, AUTOMATIC
    }

    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Type type);

    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass);

    public Iterable<Index> getIndices();

    public void dropIndex(String indexName);

}
