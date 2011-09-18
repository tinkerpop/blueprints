package com.tinkerpop.blueprints.pgm.impls.neo4jbatch;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import org.neo4j.graphdb.index.BatchInserterIndex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jBatchAutomaticIndex<T extends Element> extends Neo4jBatchIndex<T> implements AutomaticIndex<T> {

    protected final Set<String> autoIndexKeys;

    public Neo4jBatchAutomaticIndex(final Neo4jBatchGraph graph, final BatchInserterIndex index, final String indexName, final Class<T> indexClass, final Set<String> indexKeys) {
        super(graph, index, indexName, indexClass);
        this.autoIndexKeys = indexKeys;
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }

    protected void autoUpdate(final T element, final Map<String, Object> properties) {
        final Map<String, Object> keyedProperties = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (null == this.autoIndexKeys || this.autoIndexKeys.contains(entry.getKey())) {
                keyedProperties.put(entry.getKey(), entry.getValue());
            }
        }
        if (keyedProperties.size() > 0) {
            this.rawIndex.add((Long) element.getId(), keyedProperties);
        }

    }

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }

}
