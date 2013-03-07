package com.tinkerpop.blueprints;

import java.util.Set;

/**
 * A KeyIndexableGraph is a graph that supports basic index functionality around the key/value pairs of the elements of the graph.
 * By creating key indices for a particular property key, that key is indexed on all the elements of the graph.
 * This has ramifications for quick lookups on methods like getVertices(String, Object) and getEdges(String, Object).
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface KeyIndexableGraph extends Graph {

    /**
     * Remove an automatic indexing structure associated with indexing provided key for element class.
     *
     * @param key          the key to drop the index for
     * @param elementClass the element class that the index is for
     * @param <T>          the element class specification
     */
    public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass);

    /**
     * Create an automatic indexing structure for indexing provided key for element class.
     *
     * @param key             the key to create the index for
     * @param elementClass    the element class that the index is for
     * @param indexParameters a collection of parameters for the underlying index implementation
     * @param <T>             the element class specification
     */
    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, final Parameter... indexParameters);

    /**
     * Return all the index keys associated with a particular element class.
     *
     * @param elementClass the element class that the index is for
     * @param <T>          the element class specification
     * @return the indexed keys as a Set
     */
    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass);
}
