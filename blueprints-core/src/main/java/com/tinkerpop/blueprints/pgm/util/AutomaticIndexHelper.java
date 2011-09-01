package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;

import java.util.Set;

/**
 * A collection of AutomaticIndex helper methods.
 *
 * @author Darrick Wiebe (http://ofallpossibleworlds.wordpress.com)
 * @author Marko Rodriguez (http://markorodriguez.com)
 */
public class AutomaticIndexHelper {

    /**
     * Add an element to an automatic index by its key/value properties.
     *
     * @param index   automatic index to add the element to
     * @param element element to be added
     */
    public static void addElement(final AutomaticIndex index, final Element element) {
        final Set<String> indexKeys = index.getAutoIndexKeys();
        if (indexKeys == null || indexKeys.contains(AutomaticIndex.LABEL)) {
            if (element instanceof Edge)
                index.put(AutomaticIndex.LABEL, ((Edge) element).getLabel(), element);
        }
        for (final String key : element.getPropertyKeys()) {
            if (indexKeys == null || indexKeys.contains(key)) {
                index.put(key, element.getProperty(key), element);
            }
        }
    }

    /**
     * Add an element to the automatic indicies of an indexable graph by its key/value properties.
     *
     * @param graph   the indexable graph maintaining the element
     * @param element element to be indexed in all automatic indices
     */
    public static void addElement(final IndexableGraph graph, final Element element) {
        for (final Index index : graph.getIndices()) {
            if (index instanceof AutomaticIndex) {
                if (index.getIndexClass().isAssignableFrom(element.getClass())) {
                    AutomaticIndexHelper.addElement((AutomaticIndex) index, element);
                }
            }
        }
    }

    /**
     * Remove an element from an automatic index by its key/value properties.
     *
     * @param index   automatic index to remove the element from
     * @param element element to be removed
     */
    public static void removeElement(final AutomaticIndex index, final Element element) {
        final Set<String> indexKeys = index.getAutoIndexKeys();
        if (indexKeys == null || indexKeys.contains(AutomaticIndex.LABEL)) {
            if (element instanceof Edge)
                index.remove(AutomaticIndex.LABEL, ((Edge) element).getLabel(), element);
        }
        for (final String key : element.getPropertyKeys()) {
            if (indexKeys == null || indexKeys.contains(key)) {
                index.remove(key, element.getProperty(key), element);
            }
        }
    }

    /**
     * Remove an element from the automatic indices of an indexable graph by its key/value properties.
     *
     * @param graph   the indexable graph maintaining the element
     * @param element element to be unidexed in all automatic indices
     */
    public static void removeElement(final IndexableGraph graph, final Element element) {
        for (final Index index : graph.getIndices()) {
            if (index instanceof AutomaticIndex) {
                if (index.getIndexClass().isAssignableFrom(element.getClass())) {
                    AutomaticIndexHelper.removeElement((AutomaticIndex) index, element);
                }
            }
        }
    }

    /**
     * Reindex the provided elements in the automatic indicies of the indexable graph.
     * The elements are first removed from the indices and then added to the indices.
     * The properties indexed are determined by the automatic index keys of the index.
     *
     * @param graph    the indexable graph to reindex
     * @param elements the elements to reindex
     */
    public static void reIndexElements(final IndexableGraph graph, final Iterable<Element> elements) {
        for (final Element element : elements) {
            AutomaticIndexHelper.removeElement(graph, element);
            AutomaticIndexHelper.addElement(graph, element);
        }
    }

    /**
     * Reindex the provided elements in the provided automatic index.
     * The elements are first removed from the index and then added to the index.
     * The properties indexed are determined by the automatic index keys of the index.
     *
     * @param graph    the indexable graph to reindex
     * @param index    the automatic index to reindex
     * @param elements the elements to reindex
     * @return a new version of the AutomaticIndex
     */
    public static <T extends Element> AutomaticIndex<T> reIndexElements(final IndexableGraph graph, AutomaticIndex<T> index, final Iterable<Element> elements) {
        final String indexName = index.getIndexName();
        final Class<T> indexClass = index.getIndexClass();
        final Set<String> indexKeys = index.getAutoIndexKeys();

        graph.dropIndex(indexName);
        index = graph.createAutomaticIndex(indexName, indexClass, indexKeys);

        for (final Element element : elements) {
            AutomaticIndexHelper.addElement(index, element);

        }
        return index;
    }

}
