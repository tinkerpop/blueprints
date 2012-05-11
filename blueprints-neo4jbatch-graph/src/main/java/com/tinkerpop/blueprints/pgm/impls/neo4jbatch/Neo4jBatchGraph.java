package com.tinkerpop.blueprints.pgm.impls.neo4jbatch;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Features;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.KeyIndexableGraph;
import com.tinkerpop.blueprints.pgm.MetaGraph;
import com.tinkerpop.blueprints.pgm.Parameter;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSetting;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.AutoIndexer;
import org.neo4j.tooling.GlobalGraphOperations;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.neo4j.unsafe.batchinsert.LuceneBatchInserterIndexProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An Blueprints implementation of the Neo4j batch inserter for bulk loading data into a Neo4j graph.
 * This is a single threaded, non-transactional bulk loader and should not be used for any other reason than for massive initial data loads.
 * <p/>
 * Neo4jBatchGraph is <b>not</b> a completely faithful Blueprints implementation.
 * Many methods throw UnsupportedOperationExceptions and take unique arguments. Be sure to review each method's JavaDoc.
 * The Neo4j "reference node" (vertex 0) is automatically created and, if so desired, must be manually deleted post data insertion.
 * Key indices are not available until after the graph has been shutdown and loaded up using Neo4jGraph.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jBatchGraph implements KeyIndexableGraph, IndexableGraph, MetaGraph<BatchInserter> {

    private final BatchInserter rawGraph;
    private final BatchInserterIndexProvider indexProvider;

    private final Map<String, Neo4jBatchIndex<? extends Element>> indices = new HashMap<String, Neo4jBatchIndex<? extends Element>>();

    private Long idCounter = 0l;

    protected final Set<String> vertexIndexKeys = new HashSet<String>();
    protected final Set<String> edgeIndexKeys = new HashSet<String>();

    private static final Features FEATURES = new Features();

    static {

        FEATURES.allowSerializableObjectProperty = false;
        FEATURES.allowBooleanProperty = true;
        FEATURES.allowDoubleProperty = true;
        FEATURES.allowFloatProperty = true;
        FEATURES.allowIntegerProperty = true;
        FEATURES.allowPrimitiveArrayProperty = true;
        FEATURES.allowUniformListProperty = true;
        FEATURES.allowMixedListProperty = false;
        FEATURES.allowLongProperty = true;
        FEATURES.allowMapProperty = false;
        FEATURES.allowStringProperty = true;

        FEATURES.allowsDuplicateEdges = true;
        FEATURES.allowsSelfLoops = true;
        FEATURES.isPersistent = true;
        FEATURES.isRDFModel = false;
        FEATURES.isWrapper = false;
        FEATURES.supportsVertexIteration = false;
        FEATURES.supportsEdgeIteration = false;
        FEATURES.supportsVertexIndex = true;
        FEATURES.supportsEdgeIndex = true;
        FEATURES.ignoresSuppliedIds = false;
        FEATURES.supportsTransactions = false;
        FEATURES.supportsIndices = true;
        FEATURES.supportsKeyIndices = true;
        FEATURES.supportsVertexKeyIndex = true;
        FEATURES.supportsEdgeKeyIndex = true;
    }


    public Neo4jBatchGraph(final String directory) {
        this.rawGraph = BatchInserters.inserter(directory);
        this.indexProvider = new LuceneBatchInserterIndexProvider(rawGraph);
    }

    public Neo4jBatchGraph(final String directory, final Map<String, String> parameters) {
        this.rawGraph = BatchInserters.inserter(directory, parameters);
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
        finalizeKeyIndices();
    }

    /**
     * This is necessary prior to using indices to ensure that indexed data is available to index queries.
     * This method is not part of the Blueprints Graph or IndexableGraph API.
     * Therefore, be sure to typecast your graph to a Neo4jBatchGraph to use this necessary index-based method.
     * Note that key indices are not usable until the Neo4jBatchGraph has been shutdown.
     */
    public void flushIndices() {
        for (final Neo4jBatchIndex index : this.indices.values()) {
            index.flush();
        }
    }

    private void finalizeKeyIndices() {
        GraphDatabaseService db = null;
        try {
            GraphDatabaseBuilder builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(this.rawGraph.getStoreDir());
            if (this.vertexIndexKeys.size() > 0)
                builder.setConfig(GraphDatabaseSettings.node_keys_indexable, vertexIndexKeys.toString().replace("[", "").replace("]", "")).setConfig(GraphDatabaseSettings.node_auto_indexing, GraphDatabaseSetting.TRUE);
            if (this.edgeIndexKeys.size() > 0)
                builder.setConfig(GraphDatabaseSettings.relationship_keys_indexable, edgeIndexKeys.toString().replace("[", "").replace("]", "")).setConfig(GraphDatabaseSettings.relationship_auto_indexing, GraphDatabaseSetting.TRUE);

            db = builder.newGraphDatabase();
            GlobalGraphOperations graphOperations = GlobalGraphOperations.at(db);
            if (this.vertexIndexKeys.size() > 0)
                updateAutoIndex(db, db.index().getNodeAutoIndexer(), graphOperations.getAllNodes());
            if (this.edgeIndexKeys.size() > 0)
                updateAutoIndex(db, db.index().getRelationshipAutoIndexer(), graphOperations.getAllRelationships());
        } finally {
            if (db != null) db.shutdown();
        }
    }

    private static <T extends PropertyContainer> void updateAutoIndex(final GraphDatabaseService db, final AutoIndexer<T> autoIndexer, final Iterable<T> elements) {
        if (!autoIndexer.isEnabled()) return;
        final Set<String> properties = autoIndexer.getAutoIndexedProperties();
        Transaction tx = db.beginTx();
        int count = 0;
        for (final PropertyContainer pc : elements) {
            for (final String property : properties) {
                if (!pc.hasProperty(property)) continue;
                pc.setProperty(property, pc.getProperty(property));
                count++;
                if (count % 10000 == 0) {
                    tx.success();
                    tx.finish();
                    tx = db.beginTx();
                }
            }
        }
        tx.success();
        tx.finish();
    }

    public String toString() {
        return StringFactory.graphString(this, this.rawGraph.toString());
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

        return new Neo4jBatchVertex(this, finalId);
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
    public Iterable<Vertex> getVertices(final String key, final Object value) throws UnsupportedOperationException {
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

        return new Neo4jBatchEdge(this, finalId, label);
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
    public Iterable<Edge> getEdges(final String key, final Object value) throws UnsupportedOperationException {
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

    public <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        final Neo4jBatchIndex<T> index;

        final Map<String, String> map = generateParameterMap(indexParameters);
        if (indexParameters.length == 0) {
            map.put(Neo4jBatchTokens.TYPE, Neo4jBatchTokens.EXACT);
        }

        if (Vertex.class.isAssignableFrom(indexClass)) {
            index = new Neo4jBatchIndex<T>(this, indexProvider.nodeIndex(indexName, map), indexName, indexClass);
        } else {
            index = new Neo4jBatchIndex<T>(this, indexProvider.relationshipIndex(indexName, map), indexName, indexClass);
        }
        this.indices.put(indexName, index);
        return index;
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

    public <T extends Element> void dropKeyIndex(final String key, final Class<T> elementClass) {
        if (Vertex.class.isAssignableFrom(elementClass)) {
            this.vertexIndexKeys.remove(key);
        } else {
            this.edgeIndexKeys.remove(key);
        }
    }

    public <T extends Element> void createKeyIndex(final String key, final Class<T> elementClass) {
        if (Vertex.class.isAssignableFrom(elementClass)) {
            this.vertexIndexKeys.add(key);
        } else {
            this.edgeIndexKeys.add(key);
        }
    }

    public <T extends Element> Set<String> getIndexedKeys(final Class<T> elementClass) {
        if (Vertex.class.isAssignableFrom(elementClass)) {
            return this.vertexIndexKeys;
        } else {
            return this.edgeIndexKeys;
        }
    }

    private static Map<String, String> generateParameterMap(final Parameter<Object, Object>... indexParameters) {
        final Map<String, String> map = new HashMap<String, String>();
        for (final Parameter<Object, Object> parameter : indexParameters) {
            map.put(parameter.getKey().toString(), parameter.getValue().toString());
        }
        return map;
    }

    public Features getFeatures() {
        return FEATURES;
    }
}
