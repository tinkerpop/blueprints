package com.tinkerpop.blueprints.util.wrappers.wrapped;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

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
        return new WrappedIndexIterable(baseGraph.getIndices());
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
}
