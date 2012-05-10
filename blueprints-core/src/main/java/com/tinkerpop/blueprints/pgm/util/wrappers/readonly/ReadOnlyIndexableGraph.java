package com.tinkerpop.blueprints.pgm.util.wrappers.readonly;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Parameter;
import com.tinkerpop.blueprints.pgm.util.wrappers.WrapperGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.readonly.util.ReadOnlyIndexIterable;

import java.util.Set;

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
