package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Vertex;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Darrick Wiebe (http://ofallpossibleworlds.wordpress.com)
 */
public class IndexHelper {
    /*
     * Add an element to an automatic index.
     *
     * @param index The automatic index to add the element to
     * @param element The element to be added
     */
    public static void indexElement(AutomaticIndex index, Element element) {
        for (String key: IndexHelper.indexKeys(index, element)) {
            Object value = IndexHelper.indexKeyValue(index, element, key);
            if (value != null) {
                index.put(key, value, element);
            }
        }
    }

    /*
     * Remove an element from an automatic index.
     *
     * @param index The automatic index to remove the element from
     * @param element The element to be removed
     */
    public static void unIndexElement(AutomaticIndex index, Element element) {
        for (String key: IndexHelper.indexKeys(index, element)) {
            Object value = IndexHelper.indexKeyValue(index, element, key);
            if (value != null) {
                index.remove(key, value, element);
            }
        }
    }

    /*
     * Remove an element from all of its automatic indices.
     *
     * @param graph The element's graph
     * @param element The element that should be unindexed
     */
    public static void unIndexElement(IndexableGraph graph, Element element) {
        for (Index index : graph.getIndices()) {
            if (index instanceof AutomaticIndex) {
                if (element.getClass().isAssignableFrom(index.getIndexClass())) {
                    IndexHelper.unIndexElement((AutomaticIndex) index, element);
                }
            }
        }
    }

    /*
     * Return the keys for this element that are indexed in an automatic index
     *
     * @param The index to check
     * @param The element whose keys may be indexed
     */
    public static Set<String> indexKeys(AutomaticIndex index, Element element) {
        Set<String> keys = index.getAutoIndexKeys();
        if (keys == null) {
            keys = new HashSet<String>(element.getPropertyKeys());
            if (Edge.class.isAssignableFrom(index.getIndexClass()))
                keys.add(AutomaticIndex.LABEL);
        }
        return keys;
    }

    public static Object indexKeyValue(AutomaticIndex index, Element element, String key) {
        if (Edge.class.isAssignableFrom(index.getIndexClass()) && (key == AutomaticIndex.LABEL))
            return ((Edge) element).getLabel();
        else
            return element.getProperty(key);
    }
}
