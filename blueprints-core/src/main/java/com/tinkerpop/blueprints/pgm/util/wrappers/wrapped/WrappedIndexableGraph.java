package com.tinkerpop.blueprints.pgm.util.wrappers.wrapped;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Parameter;
import com.tinkerpop.blueprints.pgm.util.wrappers.WrapperGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.wrapped.util.WrappedIndexSequence;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedIndexableGraph<T extends IndexableGraph> extends WrappedGraph<T> implements IndexableGraph, WrapperGraph<T> {

    public WrappedIndexableGraph(final T baseIndexableGraph) {
        super(baseIndexableGraph);
    }

    public void dropIndex(final String indexName) {
        baseGraph.dropIndex(indexName);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        return new WrappedIndexSequence(baseGraph.getIndices());
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final Index<T> index = baseGraph.getIndex(indexName, indexClass);
        if (null == index)
            return null;
        else {
            return new WrappedIndex<T>(index);
        }
    }

    public <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        return new WrappedIndex<T>(baseGraph.createIndex(indexName, indexClass, indexParameters));
    }

    public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {
        this.baseGraph.dropKeyIndex(key, elementClass);
    }

    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass) {
        this.baseGraph.createKeyIndex(key, elementClass);
    }

    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        return this.baseGraph.getIndexedKeys(elementClass);
    }


}
