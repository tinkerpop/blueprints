package com.tinkerpop.blueprints.pgm.impls.neo4jbatch;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
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
 * An Blueprints implementation of the Neo4j batch inserter for bulk loading data into a Neo4j graph.
 * This is a single threaded, non-transactional bulk loader and should not be used for any other reason than for massive initial data loads.
 * <p/>
 * Neo4jBatchGraph is not a completely faithful Blueprints implementation.
 * Many methods throw UnsupportedOperationExceptions and take unique arguments. Be sure to review each method's JavaDoc.
 * When the graph is created no default indices are provided. It is up to the developer to create desired indices.
 * The "reference node" (vertex 0) is automatically created and, if so desired, must be manually deleted post data insertion.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jBatchGraph implements IndexableGraph {

    private final BatchInserter rawGraph;
    private final BatchInserterIndexProvider indexProvider;

    private final Map<String, Neo4jBatchIndex<? extends Element>> indices = new HashMap<String, Neo4jBatchIndex<? extends Element>>();
    private final Map<String, Neo4jBatchAutomaticIndex<Neo4jBatchVertex>> automaticVertexIndices = new HashMap<String, Neo4jBatchAutomaticIndex<Neo4jBatchVertex>>();
    private final Map<String, Neo4jBatchAutomaticIndex<Neo4jBatchEdge>> automaticEdgeIndices = new HashMap<String, Neo4jBatchAutomaticIndex<Neo4jBatchEdge>>();

    public Neo4jBatchGraph(final String directory) {
        this.rawGraph = new BatchInserterImpl(directory);
        this.indexProvider = new LuceneBatchInserterIndexProvider(rawGraph);
    }

    public Neo4jBatchGraph(final String directory, final Map<String, String> parameters) {
        this.rawGraph = new BatchInserterImpl(directory, parameters);
        this.indexProvider = new LuceneBatchInserterIndexProvider(rawGraph);
    }

    public Neo4jBatchGraph(final BatchInserter rawGraph, final BatchInserterIndexProvider indexProvider) {
        this.rawGraph = rawGraph;
        this.indexProvider = indexProvider;
    }

    public void shutdown() {
        this.flushIndices();
        this.indexProvider.shutdown();
        this.rawGraph.shutdown();
    }

    /**
     * This is necessary prior to using indices to ensure that indexed data is available to index queries.
     * This method is not part of the Blueprints Graph or IndexableGraph API.
     * Therefore, be sure to typecast your graph to a Neo4jBatchGraph to use this necessary index-based method.
     */
    public void flushIndices() {
        for (final Neo4jBatchIndex index : this.indices.values()) {
            index.flush();
        }
    }

    public String toString() {
        return StringFactory.graphString(this, this.rawGraph.toString());
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void clear() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(Neo4jBatchTokens.DELETE_OPERATION_MESSAGE);
    }

    public BatchInserter getRawGraph() {
        return this.rawGraph;
    }

    /**
     * The object id must be a Map&lt;String,Object&gt; or null.
     * The map is the properties written when the vertex is created.
     * While it is possible to Vertex.setProperty(), this method is faster.
     * If the map contains an _id key, then the value is a user provided long vertex id.
     * In other words, Neo4jBatchGraph will not ignore the user provided id.
     *
     * @param map a map of properties which can be null
     * @return the newly created vertex
     */
    public Vertex addVertex(final Object map) {
        if (null != map && !(map instanceof Map)) {
            throw new IllegalArgumentException("Provided object id must be a Map<String,Object>");
        }

        final Map<String, Object> properties;
        Long providedId = null;
        if (map == null)
            properties = new HashMap<String, Object>();
        else {
            properties = makePropertyMap((Map<String, Object>) map);
            providedId = (Long) ((Map<String, Object>) map).get(Neo4jBatchTokens.ID);
        }

        final Long id;
        if (providedId == null)
            id = rawGraph.createNode(properties);
        else {
            rawGraph.createNode(providedId, properties);
            id = providedId;
        }

        final Neo4jBatchVertex vertex = new Neo4jBatchVertex(this, id);
        for (final Neo4jBatchAutomaticIndex<Neo4jBatchVertex> index : this.automaticVertexIndices.values()) {
            index.autoUpdate(vertex, properties);
        }
        return vertex;
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            throw new IllegalArgumentException("Element identifier cannot be null");

        try {
            final Long longId;
            if (id instanceof Long)
                longId = (Long) id;
            else
                longId = Double.valueOf(id.toString()).longValue();

            if (rawGraph.nodeExists(longId)) {
                return new Neo4jBatchVertex(this, longId);
            } else {
                return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Iterable<Vertex> getVertices() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void removeVertex(final Vertex vertex) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(Neo4jBatchTokens.DELETE_OPERATION_MESSAGE);
    }

    /**
     * The object id must be a Map&lt;String,Object&gt; or null.
     * The map is the properties written when the vertex is created.
     * While it is possible to Edge.setProperty(), this method is faster.
     *
     * @param map a map of properties which can be null
     * @return the newly created vertex
     */
    public Edge addEdge(final Object map, final Vertex outVertex, final Vertex inVertex, final String label) {
        if (map != null && !(map instanceof Map)) {
            throw new IllegalArgumentException("Provided object id must be a Map<String,Object>");
        }

        final Map<String, Object> properties;
        if (map == null)
            properties = new HashMap<String, Object>();
        else
            properties = makePropertyMap((Map<String, Object>) map);
        final Long id = rawGraph.createRelationship((Long) outVertex.getId(), (Long) inVertex.getId(), DynamicRelationshipType.withName(label), properties);

        final Neo4jBatchEdge edge = new Neo4jBatchEdge(this, id, label);
        for (final Neo4jBatchAutomaticIndex<Neo4jBatchEdge> index : this.automaticEdgeIndices.values()) {
            index.autoUpdate(edge, properties);
        }
        return edge;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Edge getEdge(final Object id) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Iterable<Edge> getEdges() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void removeEdge(final Edge edge) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(Neo4jBatchTokens.DELETE_OPERATION_MESSAGE);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        return (Index<T>) this.indices.get(indexName);
    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        final Neo4jBatchIndex<T> index;

        if (Vertex.class.isAssignableFrom(indexClass)) {
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
        if (Vertex.class.isAssignableFrom(indexClass)) {
            this.automaticVertexIndices.put(indexName, (Neo4jBatchAutomaticIndex<Neo4jBatchVertex>) index);
        } else {
            this.automaticEdgeIndices.put(indexName, (Neo4jBatchAutomaticIndex<Neo4jBatchEdge>) index);
        }
        return index;
    }

    protected Iterable<Neo4jBatchAutomaticIndex<Neo4jBatchVertex>> getAutomaticVertexIndices() {
        return this.automaticVertexIndices.values();
    }

    protected Iterable<Neo4jBatchAutomaticIndex<Neo4jBatchEdge>> getAutomaticEdgeIndices() {
        return this.automaticEdgeIndices.values();
    }

    public Iterable<Index<? extends Element>> getIndices() {
        return (Iterable) this.indices.values();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void dropIndex(final String indexName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(Neo4jBatchTokens.DELETE_OPERATION_MESSAGE);
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
