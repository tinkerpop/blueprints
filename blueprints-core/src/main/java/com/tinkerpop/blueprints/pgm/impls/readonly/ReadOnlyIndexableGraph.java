package com.tinkerpop.blueprints.pgm.impls.readonly;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.readonly.util.ReadOnlyIndexSequence;

import java.util.Set;

/**
 * A ReadOnlyIndexableGraph wraps an IndexableGraph and overrides the underlying graph's mutating methods.
 * In this way, a ReadOnlyIndexableGraph can only be read from, not written to.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyIndexableGraph extends ReadOnlyGraph implements IndexableGraph {

    public ReadOnlyIndexableGraph(final IndexableGraph graph) {
        super(graph);
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
    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> autoIndexKeys) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final Index<T> index = ((IndexableGraph) this.rawGraph).getIndex(indexName, indexClass);
        if (index.getIndexType().equals(Index.Type.MANUAL))
            return new ReadOnlyIndex<T>(index);
        else
            return new ReadOnlyAutomaticIndex<T>((AutomaticIndex<T>) index);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        return new ReadOnlyIndexSequence(((IndexableGraph) this.rawGraph).getIndices().iterator());
    }

    public IndexableGraph getRawGraph() {
        return (IndexableGraph) this.rawGraph;
    }
}
