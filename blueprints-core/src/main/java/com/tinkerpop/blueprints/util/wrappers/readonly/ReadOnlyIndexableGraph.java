package com.tinkerpop.blueprints.util.wrappers.readonly;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

/**
 * A ReadOnlyIndexableGraph wraps an IndexableGraph and overrides the underlying graph's mutating methods.
 * In this way, a ReadOnlyIndexableGraph can only be read from, not written to.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyIndexableGraph<T extends IndexableGraph> extends ReadOnlyGraph<T> implements IndexableGraph, WrapperGraph<T> {

    public ReadOnlyIndexableGraph(final T baseIndexableGraph) {
        super(baseIndexableGraph);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void dropIndex(final String name) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final Index<T> index = this.baseGraph.getIndex(indexName, indexClass);
        return new ReadOnlyIndex<T>(index);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        return new ReadOnlyIndexIterable(this.baseGraph.getIndices());
    }
}
