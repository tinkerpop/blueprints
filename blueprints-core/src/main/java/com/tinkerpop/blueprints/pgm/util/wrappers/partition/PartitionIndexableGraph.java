package com.tinkerpop.blueprints.pgm.util.wrappers.partition;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Parameter;
import com.tinkerpop.blueprints.pgm.util.wrappers.WrapperGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.util.PartitionIndexSequence;

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
        return new PartitionIndexSequence(baseGraph.getIndices().iterator(), this);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final Index<T> index = baseGraph.getIndex(indexName, indexClass);
        if (null == index)
            return null;
        else {
            if (index.getIndexType().equals(Index.Type.MANUAL)) {
                return new PartitionIndex<T>(index, this);
            } else {
                return new PartitionAutomaticIndex<T>((AutomaticIndex<T>) index, this);
            }
        }
    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        return new PartitionIndex<T>(baseGraph.createManualIndex(indexName, indexClass, indexParameters), this);
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> autoIndexKeys, final Parameter... indexParameters) {
        return new PartitionAutomaticIndex<T>(baseGraph.createAutomaticIndex(indexName, indexClass, autoIndexKeys, indexParameters), this);
    }


}
