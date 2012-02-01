package com.tinkerpop.blueprints.pgm.impls.named;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.impls.named.util.PartitionIndexSequence;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionIndexableGraph extends PartitionGraph implements IndexableGraph {

    public PartitionIndexableGraph(final IndexableGraph rawIndexableGraph, final String writeGraphKey, final String writeGraph, final Set<String> readGraphs) {
        super(rawIndexableGraph, writeGraphKey, writeGraph, readGraphs);
    }

    public PartitionIndexableGraph(final IndexableGraph rawIndexableGraph, final String writeGraphKey, final String readWriteGraph) {
        super(rawIndexableGraph, writeGraphKey, readWriteGraph);
    }

    public void dropIndex(final String indexName) {
        ((IndexableGraph) rawGraph).dropIndex(indexName);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        return new PartitionIndexSequence(((IndexableGraph) rawGraph).getIndices().iterator(), this);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final Index<T> index = ((IndexableGraph) rawGraph).getIndex(indexName, indexClass);
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

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        return new PartitionIndex<T>(((IndexableGraph) rawGraph).createManualIndex(indexName, indexClass), this);
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> autoIndexKeys) {
        return new PartitionAutomaticIndex<T>(((IndexableGraph) rawGraph).createAutomaticIndex(indexName, indexClass, autoIndexKeys), this);
    }


}
