package com.tinkerpop.blueprints.pgm;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface KeyIndexableGraph extends Graph {

    /**
     * Remove any automatic indexing structure associated with indexing provided key for element class.
     *
     * @param key          the key to drop the index for
     * @param elementClass the element class that the index is for
     * @param <T>          the element class specification
     */
    public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass);

    /**
     * Create an automatic indexing structure for indexing provided key for element class.
     *
     * @param key          the key to create the index for
     * @param elementClass the element class that the index is for
     * @param <T>          the element class specification
     */
    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass);

    /**
     * Return all the index keys associated with a particular element class.
     *
     * @param elementClass the element class that the index is for
     * @param <T>          the element class specification
     * @return the indexed keys as a Set
     */
    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass);
}
