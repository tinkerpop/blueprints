package com.tinkerpop.blueprints.pgm.util.wrappers.wrapped;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.Parameter;
import com.tinkerpop.blueprints.pgm.util.wrappers.WrappingGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.wrapped.util.WrappedIndexSequence;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedIndexableGraph<T extends IndexableGraph> extends WrappedGraph<T> implements IndexableGraph, WrappingGraph<T> {

    public WrappedIndexableGraph(final T rawIndexableGraph) {
        super(rawIndexableGraph);
    }

    public void dropIndex(final String indexName) {
        rawGraph.dropIndex(indexName);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        return new WrappedIndexSequence(rawGraph.getIndices().iterator());
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final Index<T> index = rawGraph.getIndex(indexName, indexClass);
        if (null == index)
            return null;
        else {
            if (index.getIndexType().equals(Index.Type.MANUAL)) {
                return new WrappedIndex<T>(index);
            } else {
                return new WrappedAutomaticIndex<T>((AutomaticIndex<T>) index);
            }
        }
    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        return new WrappedIndex<T>(rawGraph.createManualIndex(indexName, indexClass, indexParameters));
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> autoIndexKeys, final Parameter... indexParameters) {
        return new WrappedAutomaticIndex<T>(rawGraph.createAutomaticIndex(indexName, indexClass, autoIndexKeys, indexParameters));
    }


}
