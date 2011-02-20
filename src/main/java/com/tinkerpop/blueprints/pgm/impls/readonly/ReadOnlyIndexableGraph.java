package com.tinkerpop.blueprints.pgm.impls.readonly;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.readonly.util.ReadOnlyIndexSequence;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyIndexableGraph extends ReadOnlyGraph implements IndexableGraph {

    public ReadOnlyIndexableGraph(final IndexableGraph graph) {
        super(graph);
    }

    public void dropIndex(final String name) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> autoIndexKeys) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final Index<T> index = ((IndexableGraph) this.graph).getIndex(indexName, indexClass);
        if (index.getIndexType().equals(Index.Type.MANUAL))
            return new ReadOnlyIndex<T>(index);
        else
            return new ReadOnlyAutomaticIndex<T>((AutomaticIndex<T>) index);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        return new ReadOnlyIndexSequence(((IndexableGraph) this.graph).getIndices().iterator());
    }

    public IndexableGraph getRawGraph() {
        return (IndexableGraph) this.graph;
    }
}
