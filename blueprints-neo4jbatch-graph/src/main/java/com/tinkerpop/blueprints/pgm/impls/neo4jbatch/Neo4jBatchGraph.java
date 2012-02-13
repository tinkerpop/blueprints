package com.tinkerpop.blueprints.pgm.impls.neo4jbatch;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.Parameter;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.index.BatchInserterIndexProvider;
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
 * Neo4jBatchGraph is <b>not</b> a completely faithful Blueprints implementation.
 * Many methods throw UnsupportedOperationExceptions and take unique arguments. Be sure to review each method's JavaDoc.
 * The Neo4j "reference node" (vertex 0) is automatically created and, if so desired, must be manually deleted post data insertion.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jBatchGraph implements IndexableGraph {

    private final BatchInserter rawGraph;
    private final BatchInserterIndexProvider indexProvider;

    private final Map<String, Neo4jBatchIndex<? extends Element>> indices = new HashMap<String, Neo4jBatchIndex<? extends Element>>();
    private final Map<String, Neo4jBatchAutomaticIndex<Neo4jBatchVertex>> automaticVertexIndices = new HashMap<String, Neo4jBatchAutomaticIndex<Neo4jBatchVertex>>();
    private final Map<String, Neo4jBatchAutomaticIndex<Neo4jBatchEdge>> automaticEdgeIndices = new HashMap<String, Neo4jBatchAutomaticIndex<Neo4jBatchEdge>>();

    private Long idCounter = 0l;

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
     * The object id can either be null, a long id, or a Map&lt;String,Object&gt;.
     * If null, then an internal long is provided on the construction of the vertex.
     * If a long id is provided, then the vertex is constructed with that long id.
     * If a map is provided, then the map serves as the properties of the vertex.
     * Moreover, if the map contains an _id key, then the value is a user provided long vertex id.
     *
     * @param id a id of properties which can be null
     * @return the newly created vertex
     */
    public Vertex addVertex(final Object id) {

        final Long finalId;
        Map<String, Object> finalProperties = new HashMap<String, Object>();
        if (null == id) {
            rawGraph.createNode(++this.idCounter, finalProperties);
            finalId = this.idCounter;
        } else if (id instanceof Long) {
            rawGraph.createNode((Long) id, finalProperties);
            finalId = (Long) id;
        } else if (id instanceof Map) {
            finalProperties = makePropertyMap((Map<String, Object>) id);
            final Long providedId = (Long) ((Map<String, Object>) id).get(Neo4jBatchTokens.ID);
            finalProperties.remove(Neo4jBatchTokens.ID);
            if (providedId == null)
                finalId = rawGraph.createNode(finalProperties);
            else {
                rawGraph.createNode(providedId, finalProperties);
                finalId = providedId;
            }
        } else {
            try {
                finalId = Double.valueOf(id.toString()).longValue();
                rawGraph.createNode(finalId, finalProperties);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The provided object must be null, a long id, an object convertible to long, or a Map<String,Object>");
            }
        }

        final Neo4jBatchVertex vertex = new Neo4jBatchVertex(this, finalId);
        if (finalProperties.size() > 0) {
            for (final Neo4jBatchAutomaticIndex<Neo4jBatchVertex> index : this.automaticVertexIndices.values()) {
                index.autoUpdate(vertex, finalProperties);
            }
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
     * The id is the properties written when the vertex is created.
     * While it is possible to Edge.setProperty(), this method is faster.
     *
     * @param id a id of properties which can be null
     * @return the newly created vertex
     */
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        final Map<String, Object> finalProperties;
        if (id == null || !(id instanceof Map))
            finalProperties = new HashMap<String, Object>();
        else
            finalProperties = makePropertyMap((Map<String, Object>) id);
        final Long finalId = this.rawGraph.createRelationship((Long) outVertex.getId(), (Long) inVertex.getId(), DynamicRelationshipType.withName(label), finalProperties);

        final Neo4jBatchEdge edge = new Neo4jBatchEdge(this, finalId, label);
        if (finalProperties.size() > 0) {
            for (final Neo4jBatchAutomaticIndex<Neo4jBatchEdge> index : this.automaticEdgeIndices.values()) {
                index.autoUpdate(edge, finalProperties);
            }
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

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        final Neo4jBatchIndex<T> index;

        final Map<String, String> map = generateParameterMap(indexParameters);
        if (indexParameters.length == 0) {
            map.put(Neo4jBatchTokens.TYPE, Neo4jBatchTokens.EXACT);
        }
        map.put(Neo4jBatchTokens.BLUEPRINTS_TYPE, Index.Type.MANUAL.toString());

        if (Vertex.class.isAssignableFrom(indexClass)) {
            index = new Neo4jBatchIndex<T>(this, indexProvider.nodeIndex(indexName, map), indexName, indexClass);
        } else {
            index = new Neo4jBatchIndex<T>(this, indexProvider.relationshipIndex(indexName, map), indexName, indexClass);
        }
        this.indices.put(indexName, index);
        return index;
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> indexKeys, final Parameter... indexParameters) {
        final Neo4jBatchAutomaticIndex<T> index;

        final Map<String, String> map = generateParameterMap(indexParameters);
        if (indexParameters.length == 0) {
            map.put(Neo4jBatchTokens.TYPE, Neo4jBatchTokens.EXACT);
        }
        map.put(Neo4jBatchTokens.BLUEPRINTS_TYPE, Index.Type.AUTOMATIC.toString());
        map.put(Neo4jBatchTokens.BLUEPRINTS_AUTOKEYS, makeAutoIndexKeys(indexKeys));

        if (indexClass.equals(Vertex.class)) {
            index = new Neo4jBatchAutomaticIndex<T>(this, indexProvider.nodeIndex(indexName, map), indexName, indexClass, indexKeys);
        } else {
            index = new Neo4jBatchAutomaticIndex<T>(this, indexProvider.relationshipIndex(indexName, map), indexName, indexClass, indexKeys);
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

    private String makeAutoIndexKeys(final Set<String> autoIndexKeys) {
        String field;
        if (null != autoIndexKeys) {
            field = "";
            for (final String key : autoIndexKeys) {
                field = field + Neo4jBatchTokens.KEY_SEPARATOR + key;
            }
        } else {
            field = "null";
        }
        return field;
    }

    private static Map<String, String> generateParameterMap(final Parameter<Object, Object>... indexParameters) {
        final Map<String, String> map = new HashMap<String, String>();
        for (final Parameter<Object, Object> parameter : indexParameters) {
            map.put(parameter.getA().toString(), parameter.getB().toString());
        }
        return map;
    }
}
