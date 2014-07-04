package com.tinkerpop.blueprints.util.wrappers.partition;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

import java.util.Set;

public class PartitionKeyIndexableGraph<T extends KeyIndexableGraph> extends PartitionGraph<T> implements KeyIndexableGraph, WrapperGraph<T> {

    public PartitionKeyIndexableGraph(final T baseGraph, final String partitionKey, final String writePartition, final Set<String> readPartitions) {
        super(baseGraph, partitionKey, writePartition, readPartitions);
    }

    public PartitionKeyIndexableGraph(final T baseGraph, final String partitionKey, final String readWritePartition) {
        super(baseGraph, partitionKey, readWritePartition);
    }

    @Override
    public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {
        baseGraph.dropKeyIndex(key, elementClass);
    }

    @Override
    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, Parameter... indexParameters) {
        // this index needs to be confined to the partition somehow
        baseGraph.createKeyIndex(key, elementClass, indexParameters);
    }

    @Override
    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        return baseGraph.getIndexedKeys(elementClass);
    }
}
