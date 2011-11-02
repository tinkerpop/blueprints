package com.tinkerpop.blueprints.pgm.impls.wrapped;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.wrapped.util.WrappedIndexSequence;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedIndexableGraph extends WrappedGraph implements IndexableGraph {

    public WrappedIndexableGraph(final IndexableGraph rawIndexableGraph) {
        super(rawIndexableGraph);
    }

    public void dropIndex(final String indexName) {
        ((IndexableGraph) rawGraph).dropIndex(indexName);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        return new WrappedIndexSequence(((IndexableGraph) rawGraph).getIndices().iterator());
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final Index<T> index = ((IndexableGraph) rawGraph).getIndex(indexName, indexClass);
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

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        return new WrappedIndex<T>(((IndexableGraph) rawGraph).createManualIndex(indexName, indexClass));
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> autoIndexKeys) {
        return new WrappedAutomaticIndex<T>(((IndexableGraph) rawGraph).createAutomaticIndex(indexName, indexClass, autoIndexKeys));
    }


}
