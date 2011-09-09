package com.tinkerpop.blueprints.pgm.impls.neo4jbatch;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.index.BatchInserterIndexProvider;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.impl.lucene.LuceneBatchInserterIndexProvider;
import org.neo4j.kernel.impl.batchinsert.BatchInserter;
import org.neo4j.kernel.impl.batchinsert.BatchInserterImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jBatchGraph implements IndexableGraph {

    final BatchInserter inserter;
    final BatchInserterIndexProvider indexProvider;

    final Map<String, Neo4jBatchIndex<? extends Element>> indices = new HashMap<String, Neo4jBatchIndex<? extends Element>>();
    final Map<String, Neo4jBatchAutomaticIndex<? extends Element>> autoIndices = new HashMap<String, Neo4jBatchAutomaticIndex<? extends Element>>();

    public Neo4jBatchGraph(final String directory) {
        this.inserter = new BatchInserterImpl(directory);
        this.indexProvider = new LuceneBatchInserterIndexProvider(inserter);
    }

    public Neo4jBatchGraph(final String directory, final Map<String, String> parameters) {
        this.inserter = new BatchInserterImpl(directory, parameters);
        this.indexProvider = new LuceneBatchInserterIndexProvider(inserter);
    }

    public Neo4jBatchGraph(final BatchInserter inserter, final BatchInserterIndexProvider indexProvider) {
        this.inserter = inserter;
        this.indexProvider = indexProvider;
    }

    public void shutdown() {
        this.flushIndices();
        this.inserter.shutdown();
    }

    public void flushIndices() {
        for (final Neo4jBatchIndex index : this.indices.values()) {
            index.flush();
        }
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public BatchInserter getRawGraph() {
        return this.inserter;
    }

    public Vertex addVertex(final Object map) {
        if (!(map instanceof Map)) {
            throw new IllegalArgumentException("Provided object id must be a Map<String,Object>");
        }

        final Map<String, Object> properties = makePropertyMap((Map<String, Object>) map);
        final Long providedId = (Long) ((Map<String, Object>) map).get("_id");
        final Long id;
        if (providedId == null)
            id = inserter.createNode(properties);
        else {
            inserter.createNode(providedId, properties);
            id = providedId;
        }
        return new Neo4jBatchVertex(this, id);
    }

    public Vertex getVertex(final Object id) {
        throw new UnsupportedOperationException();
    }

    public Iterable<Vertex> getVertices() {
        throw new UnsupportedOperationException();
    }

    public void removeVertex(final Vertex vertex) {
        throw new UnsupportedOperationException();
    }

    public Edge addEdge(final Object map, final Vertex outVertex, final Vertex inVertex, final String label) {
        if (!(map instanceof Map)) {
            throw new IllegalArgumentException("Provided object id must be a Map<String,Object>");
        }

        final Map<String, Object> properties = makePropertyMap((Map<String, Object>) map);
        final Long id = inserter.createRelationship((Long) outVertex.getId(), (Long) inVertex.getId(), DynamicRelationshipType.withName(label), properties);

        return new Neo4jBatchEdge(this, id, label);
    }

    public Edge getEdge(final Object id) {
        throw new UnsupportedOperationException();
    }

    public Iterable<Edge> getEdges() {
        throw new UnsupportedOperationException();
    }

    public void removeEdge(final Edge edge) {
        throw new UnsupportedOperationException();
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        return (Index<T>) this.indices.get(indexName);
    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        final Neo4jBatchIndex<T> index;

        if (indexClass.equals(Vertex.class)) {
            index = new Neo4jBatchIndex<T>(this, indexProvider.nodeIndex(indexName, MapUtil.stringMap(Neo4jBatchTokens.TYPE, Neo4jBatchTokens.EXACT)), indexName, indexClass);
        } else {
            index = new Neo4jBatchIndex<T>(this, indexProvider.relationshipIndex(indexName, MapUtil.stringMap(Neo4jBatchTokens.TYPE, Neo4jBatchTokens.EXACT)), indexName, indexClass);
        }
        this.indices.put(indexName, index);
        return index;
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> indexKeys) {
        final Neo4jBatchAutomaticIndex<T> index;

        if (indexClass.equals(Vertex.class)) {
            index = new Neo4jBatchAutomaticIndex<T>(this, indexProvider.nodeIndex(indexName, MapUtil.stringMap(Neo4jBatchTokens.TYPE, Neo4jBatchTokens.EXACT)), indexName, indexClass, indexKeys);
        } else {
            index = new Neo4jBatchAutomaticIndex<T>(this, indexProvider.relationshipIndex(indexName, MapUtil.stringMap(Neo4jBatchTokens.TYPE, Neo4jBatchTokens.EXACT)), indexName, indexClass, indexKeys);
        }
        this.indices.put(indexName, index);
        return index;
    }

    protected Iterable<Neo4jBatchAutomaticIndex<? extends Element>> getAutoIndices() {
        return autoIndices.values();
    }

    public Iterable<Index<? extends Element>> getIndices() {
        return (Iterable) this.indices.values();
    }

    public void dropIndex(final String indexName) {
        throw new UnsupportedOperationException();
    }

    private Map<String, Object> makePropertyMap(final Map<String, Object> map) {
        final Map<String, Object> properties = new HashMap<String, Object>();
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            if (!entry.getKey().equals(Neo4jBatchTokens.ID)) {
                properties.put(entry.getKey(), entry.getValue());
            }
        }
        return properties;
    }
}
