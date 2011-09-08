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
public class Neo4jBatchAutomaticIndex<T extends Element> extends Neo4jBatchIndex implements AutomaticIndex<T> {

    final Set<String> autoIndexKeys;

    public Neo4jBatchAutomaticIndex(final Neo4jBatchGraph graph, final BatchInserterIndex index, final String indexName, final Class<T> indexClass, final Set<String> indexKeys) {
        super(graph, index, indexName, indexClass);
        this.autoIndexKeys = indexKeys;
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }

    protected void autoUpdate(final T element, final Map<String, Object> properties) {
        Map<String, Object> keyedProperties = new HashMap<String, Object>();
        for (String key : properties.keySet()) {
            if (this.getIndexClass().isAssignableFrom(element.getClass()) && (null == this.autoIndexKeys || this.autoIndexKeys.contains(key))) {
                keyedProperties.put(key, properties.get(key));
            }
        }
        this.rawIndex.add((Long) element.getId(), keyedProperties);

    }

}
