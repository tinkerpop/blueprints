package com.tinkerpop.blueprints.pgm.impls.event;


import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.event.util.EventIndexSequence;

import java.util.Set;

/**
 * EventIndexableGraph is merely a proxy to index methods exposing EventGraph methods in the "evented" way.
 */
public class EventIndexableGraph extends EventGraph implements IndexableGraph {
    public EventIndexableGraph(final IndexableGraph graph) {
        super(graph);
    }

    public void dropIndex(final String name) {
        this.getRawGraph().dropIndex(name);
    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        return this.getRawGraph().createManualIndex(indexName, indexClass);
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> autoIndexKeys) {
        return this.getRawGraph().createAutomaticIndex(indexName, indexClass, autoIndexKeys);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final Index<T> index = ((IndexableGraph) this.graph).getIndex(indexName, indexClass);
        if (index.getIndexType().equals(Index.Type.MANUAL))
            return new EventIndex<T>(index, this.graphChangedListeners);
        else
            return new EventAutomaticIndex<T>((AutomaticIndex<T>) index, this.graphChangedListeners);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        return new EventIndexSequence(((IndexableGraph) this.graph).getIndices().iterator(), this.graphChangedListeners);
    }

    public IndexableGraph getRawGraph() {
        return (IndexableGraph) this.graph;
    }
}
