package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.pgm.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Darrick Wiebe (http://ofallpossibleworlds.wordpress.com)
 * @author Marko Rodriguez (http://markorodriguez.com)
 */
public class IndexHelper {

    /**
     * Add an element to an automatic index.
     *
     * @param autoIndex automatic index to add the element to
     * @param element   element to be added
     */
    public static void autoIndexElement(AutomaticIndex autoIndex, Element element) {
        for (String key : IndexHelper.indexKeys(autoIndex, element)) {
            Object value = IndexHelper.indexKeyValue(autoIndex, element, key);
            if (value != null) {
                autoIndex.put(key, value, element);
            }
        }
    }

    /**
     * Add an element to the automatic indicies of an indexable graph.
     *
     * @param graph   the indexable graph maintaining the element
     * @param element element to be indexed in all automatic indices
     */
    public static void autoIndexElement(IndexableGraph graph, Element element) {
        for (Index index : graph.getIndices()) {
            if (index instanceof AutomaticIndex) {
                if (index.getIndexClass().isAssignableFrom(element.getClass())) {
                    IndexHelper.autoIndexElement((AutomaticIndex) index, element);
                }
            }
        }
    }

    /**
     * Remove an element from an automatic index.
     *
     * @param autoIndex automatic index to remove the element from
     * @param element   element to be removed
     */

    public static void unAutoIndexElement(AutomaticIndex autoIndex, Element element) {
        for (String key : IndexHelper.indexKeys(autoIndex, element)) {
            Object value = IndexHelper.indexKeyValue(autoIndex, element, key);
            if (value != null) {
                autoIndex.remove(key, value, element);
            }
        }
    }

    /**
     * Remove an element from the automatic indices of an indexable graph.
     *
     * @param graph   the indexable graph maintaining the element
     * @param element element to be unidexed in all automatic indices
     */
    public static void unAutoIndexElement(IndexableGraph graph, Element element) {
        for (Index index : graph.getIndices()) {
            if (index instanceof AutomaticIndex) {
                if (index.getIndexClass().isAssignableFrom(element.getClass())) {
                    IndexHelper.unAutoIndexElement((AutomaticIndex) index, element);
                }
            }
        }
    }

    /**
     * Reindex the provided elements in the automatic indicies of the indexable graph.
     * The elements are first removed from the indices and then added to the indices.
     * The properties indexed are determined by the automatic index keys at the moment of reindex.
     *
     * @param graph    the indexeable graph to reindex
     * @param elements the elements to reindex
     */
    public static void reAutoIndex(IndexableGraph graph, Iterable<Element> elements) {
        /*for (Index index : graph.getIndices()) {
            if (index instanceof AutomaticIndex) {
                graph.dropIndex(index.getIndexName());
                graph.createIndex(index.getIndexName(), index.getIndexClass(), index.getIndexType());
            }
        }*/
        for (Element element : elements) {
            IndexHelper.unAutoIndexElement(graph, element);
            IndexHelper.autoIndexElement(graph, element);
        }
    }

    private static Set<String> indexKeys(AutomaticIndex autoIndex, Element element) {
        Set<String> keys = autoIndex.getAutoIndexKeys();
        if (keys == null) {
            keys = new HashSet<String>(element.getPropertyKeys());
            if (Edge.class.isAssignableFrom(autoIndex.getIndexClass()))
                keys.add(AutomaticIndex.LABEL);
        } else {
            boolean addBackLabel = keys.contains(AutomaticIndex.LABEL);
            keys.retainAll(element.getPropertyKeys());
            if (addBackLabel)
                keys.add(AutomaticIndex.LABEL);
        }
        return keys;
    }

    private static Object indexKeyValue(AutomaticIndex autoIndex, Element element, String key) {
        if (Edge.class.isAssignableFrom(autoIndex.getIndexClass()) && (key.equals(AutomaticIndex.LABEL)))
            return ((Edge) element).getLabel();
        else
            return element.getProperty(key);
    }

}
