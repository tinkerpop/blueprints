package com.tinkerpop.blueprints.pgm.util.wrappers.partition;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Parameter;
import com.tinkerpop.blueprints.pgm.util.wrappers.WrapperGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.util.PartitionIndexIterable;

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
