package com.tinkerpop.blueprints.pgm.util.wrappers.partition;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionAutomaticIndex<T extends Element> extends PartitionIndex<T> implements AutomaticIndex<T> {

    public PartitionAutomaticIndex(final AutomaticIndex<T> rawIndex, final PartitionGraph graph) {
        super(rawIndex, graph);
    }

    public Set<String> getAutoIndexKeys() {
        return ((AutomaticIndex<T>) this.rawIndex).getAutoIndexKeys();
    }

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }
}

