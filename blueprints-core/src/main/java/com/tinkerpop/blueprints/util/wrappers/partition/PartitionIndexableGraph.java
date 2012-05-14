package com.tinkerpop.blueprints.util.wrappers.partition;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionIndexableGraph<T extends IndexableGraph> extends PartitionGraph<T> implements IndexableGraph, WrapperGraph<T> {

    public PartitionIndexableGraph(final T baseIndexableGraph, final String writeGraphKey, final String writeGraph, final Set<String> readGraphs) {
        super(baseIndexableGraph, writeGraphKey, writeGraph, readGraphs);
    }

    public PartitionIndexableGraph(final T baseIndexableGraph, final String writeGraphKey, final String readWriteGraph) {
        super(baseIndexableGraph, writeGraphKey, readWriteGraph);
    }

    public void dropIndex(final String indexName) {
        baseGraph.dropIndex(indexName);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        return new PartitionIndexIterable(baseGraph.getIndices(), this);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final Index<T> index = baseGraph.getIndex(indexName, indexClass);
        if (null == index)
            return null;
        else {
            return new PartitionIndex<T>(index, this);
        }
    }

    public <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        return new PartitionIndex<T>(baseGraph.createIndex(indexName, indexClass, indexParameters), this);
    }

}
