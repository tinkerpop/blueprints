package com.tinkerpop.blueprints.impls.neo4j2.batch;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.MetaGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.AutoIndexer;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.impl.core.NodeManager;
import org.neo4j.tooling.GlobalGraphOperations;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Blueprints implementation of the Neo4j batch inserter for bulk loading data into a Neo4j graph.
 * This is a single threaded, non-transactional bulk loader and should not be used for any other reason than for massive initial data loads.
 * <p/>
 * Neo4j2BatchGraph is <b>not</b> a completely faithful Blueprints implementation.
 * Many methods throw UnsupportedOperationExceptions and take unique arguments. Be sure to review each method's JavaDoc.
 * The Neo4j "reference node" (vertex 0) is automatically created and is not removed until the database is shutdown() (do not add edges to the reference node).
 * Key indices are not available until after the graph has been shutdown.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4j2BatchGraph implements KeyIndexableGraph, IndexableGraph, MetaGraph<BatchInserter> {

    private final BatchInserter rawGraph;
    private final BatchInserterIndexProvider indexProvider;

    private final Map<String, Neo4j2BatchIndex<? extends Element>> indices = new HashMap<String, Neo4j2BatchIndex<? extends Element>>();

    private Long idCounter = 0l;

    protected final Set<String> vertexIndexKeys = new HashSet<String>();
    protected final Set<String> edgeIndexKeys = new HashSet<String>();

    private static final Features FEATURES = new Features();

    private static final String INDEXED_KEYS_POSTFIX = ":indexed_keys";

    static {

        FEATURES.supportsSerializableObjectProperty = false;
        FEATURES.supportsBooleanProperty = true;
        FEATURES.supportsDoubleProperty = true;
        FEATURES.supportsFloatProperty = true;
        FEATURES.supportsIntegerProperty = true;
        FEATURES.supportsPrimitiveArrayProperty = true;
        FEATURES.supportsUniformListProperty = true;
        FEATURES.supportsMixedListProperty = false;
        FEATURES.supportsLongProperty = true;
        FEATURES.supportsMapProperty = false;
        FEATURES.supportsStringProperty = true;

        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.isPersistent = true;
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
        FEATURES.supportsEdgeRetrieval = true;
        FEATURES.supportsVertexProperties = true;
        FEATURES.supportsEdgeProperties = true;
        FEATURES.supportsThreadedTransactions = false;
    }


    public Neo4j2BatchGraph(final String directory) {
        this.rawGraph = BatchInserters.inserter(directory);
        this.indexProvider = new LuceneBatchInserterIndexProvider(this.rawGraph);
    }

    public Neo4j2BatchGraph(final String directory, final Map<String, String> parameters) {
        if (null == parameters)
            this.rawGraph = BatchInserters.inserter(directory);
        else
            this.rawGraph = BatchInserters.inserter(directory, parameters);
        this.indexProvider = new LuceneBatchInserterIndexProvider(this.rawGraph);
    }

    public Neo4j2BatchGraph(final BatchInserter rawGraph, final BatchInserterIndexProvider indexProvider) {
        this.rawGraph = rawGraph;
        this.indexProvider = indexProvider;
    }

    public void shutdown() {
        this.flushIndices();
        this.indexProvider.shutdown();
        this.rawGraph.shutdown();
        removeReferenceNodeAndFinalizeKeyIndices();
    }

    /**
     * This is necessary prior to using indices to ensure that indexed data is available to index queries.
     * This method is not part of the Blueprints Graph or IndexableGraph API.
     * Therefore, be sure to typecast your graph to a Neo4j2BatchGraph to use this necessary index-based method.
     * Note that key indices are not usable until the Neo4j2BatchGraph has been shutdown.
     */
    public void flushIndices() {
        for (final Neo4j2BatchIndex index : this.indices.values()) {
            index.flush();
        }
    }

    private void removeReferenceNodeAndFinalizeKeyIndices() {
        GraphDatabaseService rawGraphDB = null;
        try {
            GraphDatabaseBuilder builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(this.rawGraph.getStoreDir());
            if (this.vertexIndexKeys.size() > 0)
                builder.setConfig(GraphDatabaseSettings.node_keys_indexable, vertexIndexKeys.toString().replace("[", "").replace("]", "")).setConfig(GraphDatabaseSettings.node_auto_indexing, "true");
            if (this.edgeIndexKeys.size() > 0)
                builder.setConfig(GraphDatabaseSettings.relationship_keys_indexable, edgeIndexKeys.toString().replace("[", "").replace("]", "")).setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true");

            rawGraphDB = builder.newGraphDatabase();

            Transaction tx = rawGraphDB.beginTx();
            try {
                GlobalGraphOperations graphOperations = GlobalGraphOperations.at(rawGraphDB);
                if (this.vertexIndexKeys.size() > 0)
                    populateKeyIndices(rawGraphDB, rawGraphDB.index().getNodeAutoIndexer(), graphOperations.getAllNodes(), Vertex.class);
                if (this.edgeIndexKeys.size() > 0)
                    populateKeyIndices(rawGraphDB, rawGraphDB.index().getRelationshipAutoIndexer(), graphOperations.getAllRelationships(), Edge.class);
                tx.success();
            } finally {
                tx.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (rawGraphDB != null) rawGraphDB.shutdown();
        }
    }

    private static <T extends PropertyContainer> void populateKeyIndices(final GraphDatabaseService rawGraphDB, final AutoIndexer<T> rawAutoIndexer, final Iterable<T> rawElements, final Class elementClass) {
        if (!rawAutoIndexer.isEnabled())
            return;


        final Set<String> properties = rawAutoIndexer.getAutoIndexedProperties();
        Transaction tx = rawGraphDB.beginTx();

        final PropertyContainer kernel = ((GraphDatabaseAPI) rawGraphDB).getDependencyResolver().resolveDependency(NodeManager.class).getGraphProperties();
        kernel.setProperty(elementClass.getSimpleName() + INDEXED_KEYS_POSTFIX, properties.toArray(new String[properties.size()]));

        int count = 0;
        for (final PropertyContainer pc : rawElements) {
            for (final String property : properties) {
                if (!pc.hasProperty(property)) continue;
                pc.setProperty(property, pc.getProperty(property));
                count++;
                if (count >= 10000) {
                    count = 0;
                    tx.success();
                    tx.finish();
                    tx = rawGraphDB.beginTx();
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
     * {@inheritDoc}
     * <p/>
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
            final Long providedId = (Long) ((Map<String, Object>) id).get(Neo4j2BatchTokens.ID);
            finalProperties.remove(Neo4j2BatchTokens.ID);
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

        return new Neo4j2BatchVertex(this, finalId);
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            throw ExceptionFactory.vertexIdCanNotBeNull();

        try {
            final Long longId;
            if (id instanceof Long)
                longId = (Long) id;
            else
                longId = Double.valueOf(id.toString()).longValue();

            if (rawGraph.nodeExists(longId)) {
                return new Neo4j2BatchVertex(this, longId);
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
        throw new UnsupportedOperationException(Neo4j2BatchTokens.DELETE_OPERATION_MESSAGE);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The object id must be a Map&lt;String,Object&gt; or null.
     * The id is the properties written when the vertex is created.
     * While it is possible to Edge.setProperty(), this method is faster.
     *
     * @param id a id of properties which can be null
     * @return the newly created vertex
     */
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        if (label == null)
            throw ExceptionFactory.edgeLabelCanNotBeNull();

        final Map<String, Object> finalProperties;
        if (id == null || !(id instanceof Map))
            finalProperties = new HashMap<String, Object>();
        else
            finalProperties = makePropertyMap((Map<String, Object>) id);
        final Long finalId = this.rawGraph.createRelationship((Long) outVertex.getId(), (Long) inVertex.getId(), DynamicRelationshipType.withName(label), finalProperties);

        return new Neo4j2BatchEdge(this, finalId, label);
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
        throw new UnsupportedOperationException(Neo4j2BatchTokens.DELETE_OPERATION_MESSAGE);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        return (Index<T>) this.indices.get(indexName);
    }

    public <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        final Neo4j2BatchIndex<T> index;

        final Map<String, String> map = generateParameterMap(indexParameters);
        if (indexParameters.length == 0) {
            map.put(Neo4j2BatchTokens.TYPE, Neo4j2BatchTokens.EXACT);
        }

        if (Vertex.class.isAssignableFrom(indexClass)) {
            index = new Neo4j2BatchIndex<T>(this, indexProvider.nodeIndex(indexName, map), indexName, indexClass);
        } else {
            index = new Neo4j2BatchIndex<T>(this, indexProvider.relationshipIndex(indexName, map), indexName, indexClass);
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
        throw new UnsupportedOperationException(Neo4j2BatchTokens.DELETE_OPERATION_MESSAGE);
    }

    private Map<String, Object> makePropertyMap(final Map<String, Object> map) {
        final Map<String, Object> properties = new HashMap<String, Object>();
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            if (!entry.getKey().equals(Neo4j2BatchTokens.ID)) {
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

    public <T extends Element> void createKeyIndex(final String key, final Class<T> elementClass, final Parameter... indexParameters) {
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

    public GraphQuery query() {
        throw new UnsupportedOperationException();
    }
}
