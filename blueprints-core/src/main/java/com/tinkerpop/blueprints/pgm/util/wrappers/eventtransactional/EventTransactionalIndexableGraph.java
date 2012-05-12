package com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Parameter;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.WrapperGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.util.EventTransactionalIndexIterable;

public class EventTransactionalIndexableGraph<T extends IndexableGraph & TransactionalGraph> extends EventTransactionalGraph<T>
        implements IndexableGraph, WrapperGraph<T> {

    public EventTransactionalIndexableGraph(final T baseIndexableGraph) {
        super(baseIndexableGraph);
    }

    @Override
    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Parameter... indexParameters) {
        return new EventTransactionalIndex<T>(this.getBaseGraph().createIndex(indexName, indexClass, indexParameters), this.graphChangedListeners, this.eventBuffer);
    }

    @Override
    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass) {
        final Index<T> index = this.baseGraph.getIndex(indexName, indexClass);
        if (null == index)
            return null;
        else
            return new EventTransactionalIndex<T>(index, this.graphChangedListeners, this.eventBuffer);
    }

    @Override
    public Iterable<Index<? extends Element>> getIndices() {
        return new EventTransactionalIndexIterable(this.baseGraph.getIndices(), this.graphChangedListeners, this.eventBuffer);
    }

    @Override
    public void dropIndex(String indexName) {
        this.getBaseGraph().dropIndex(indexName);
    }
}
