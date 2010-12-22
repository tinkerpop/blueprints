package com.tinkerpop.blueprints.pgm;

import java.util.Set;

/**
 * An automatic index will automatically maintain an index of element properties as element properties mutate.
 * If an element is removed from the graph, then it is also automatically removed from the automatic index.
 * The key/value pairs that are automatically monitored are element properties and their values.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface AutomaticIndex<T extends Element> extends Index<T> {

    /**
     * The automatic index key for edge labels is "label"
     */
    public static final String LABEL = "label";

    /**
     * Add an element property key that should be indexed.
     * If null is provided as the key, then all properties are indexed (i.e. null is wildcard)
     *
     * @param key the element property key to be indexed
     */
    public void addAutoIndexKey(String key);

    /**
     * Remove an element property key from being indexed.
     *
     * @param key the element property to key to not be indexed
     */
    public void removeAutoIndexKey(String key);

    /**
     * Get all the element property keys currently being indexed.
     * If what is returned is null, then all keys are currently being indexed (i.e. null is wildcard)
     *
     * @return the set of element property keys being indexed
     */
    public Set<String> getAutoIndexKeys();


    /**
     * Add each indexed property of an element to this index.
     *
     * @param element the element to be indexed
     */
    public void addElement(T element);

    /**
     * Remove each indexed property of an element from this index.
     *
     * @param element the element to be deindexed
     */
    public void removeElement(T element);
}
