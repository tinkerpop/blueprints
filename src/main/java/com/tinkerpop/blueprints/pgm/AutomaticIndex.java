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
     * Get all the element property keys currently being indexed.
     * If what is returned is null, then all keys are currently being indexed (i.e. null is wildcard)
     *
     * @return the set of element property keys being indexed
     */
    public Set<String> getAutoIndexKeys();
}
