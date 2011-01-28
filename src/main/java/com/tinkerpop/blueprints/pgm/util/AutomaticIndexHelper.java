package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.pgm.*;

import java.util.HashSet;
import java.util.Set;

/**
 * A collection of AutomaticIndex helper methods.
 *
 * @author Darrick Wiebe (http://ofallpossibleworlds.wordpress.com)
 * @author Marko Rodriguez (http://markorodriguez.com)
 */
public class AutomaticIndexHelper {

    /**
     * Add an element to an automatic index.
     *
     * @param index   automatic index to add the element to
     * @param element element to be added
     */
    public static void indexElement(final AutomaticIndex index, final Element element) {
        for (String key : AutomaticIndexHelper.indexKeys(index, element)) {
            Object value = AutomaticIndexHelper.indexKeyValue(index, element, key);
            if (value != null) {
                index.put(key, value, element);
            }
        }
    }

    /**
     * Add an element to the automatic indicies of an indexable graph.
     *
     * @param graph   the indexable graph maintaining the element
     * @param element element to be indexed in all automatic indices
     */
    public static void indexElement(final IndexableGraph graph, final Element element) {
        for (Index index : graph.getIndices()) {
            if (index instanceof AutomaticIndex) {
                if (index.getIndexClass().isAssignableFrom(element.getClass())) {
                    AutomaticIndexHelper.indexElement((AutomaticIndex) index, element);
                }
            }
        }
    }

    /**
     * Remove an element from an automatic index.
     *
     * @param index   automatic index to remove the element from
     * @param element element to be removed
     */
    public static void unindexElement(final AutomaticIndex index, final Element element) {
        for (String key : AutomaticIndexHelper.indexKeys(index, element)) {
            Object value = AutomaticIndexHelper.indexKeyValue(index, element, key);
            if (value != null) {
                index.remove(key, value, element);
            }
        }
    }

    /**
     * Remove an element from the automatic indices of an indexable graph.
     *
     * @param graph   the indexable graph maintaining the element
     * @param element element to be unidexed in all automatic indices
     */
    public static void unIndexElement(final IndexableGraph graph, final Element element) {
        for (Index index : graph.getIndices()) {
            if (index instanceof AutomaticIndex) {
                if (index.getIndexClass().isAssignableFrom(element.getClass())) {
                    AutomaticIndexHelper.unindexElement((AutomaticIndex) index, element);
                }
            }
        }
    }

    /**
     * Reindex the provided elements in the automatic indicies of the indexable graph.
     * The elements are first removed from the indices and then added to the indices.
     * The properties indexed are determined by the automatic index keys at the moment of reindex.
     *
     * @param graph    the indexable graph to reindex
     * @param elements the elements to reindex
     */
    public static void reIndex(final IndexableGraph graph, final Iterable<Element> elements) {
        for (Element element : elements) {
            AutomaticIndexHelper.unIndexElement(graph, element);
            AutomaticIndexHelper.indexElement(graph, element);
        }
    }

    private static Set<String> indexKeys(final AutomaticIndex index, final Element element) {
        Set<String> keys = index.getAutoIndexKeys();
        if (keys == null) {
            keys = new HashSet<String>(element.getPropertyKeys());
            if (Edge.class.isAssignableFrom(index.getIndexClass()))
                keys.add(AutomaticIndex.LABEL);
        } else {
            boolean addBackLabel = keys.contains(AutomaticIndex.LABEL);
            keys.retainAll(element.getPropertyKeys());
            if (addBackLabel)
                keys.add(AutomaticIndex.LABEL);
        }
        return keys;
    }

    private static Object indexKeyValue(final AutomaticIndex index, final Element element, final String key) {
        if (Edge.class.isAssignableFrom(index.getIndexClass()) && (key.equals(AutomaticIndex.LABEL)))
            return ((Edge) element).getLabel();
        else
            return element.getProperty(key);
    }

}
