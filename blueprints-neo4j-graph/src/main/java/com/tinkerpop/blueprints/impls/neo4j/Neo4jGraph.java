package com.tinkerpop.blueprints.impls.neo4j;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.MetaGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.KeyIndexableGraphHelper;
import com.tinkerpop.blueprints.util.PropertyFilteredIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.HighlyAvailableGraphDatabase;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Blueprints implementation of the graph database Neo4j (http://neo4j.org)
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jGraph implements TransactionalGraph, IndexableGraph, KeyIndexableGraph, MetaGraph<GraphDatabaseService> {

    private GraphDatabaseService rawGraph;
    private static final String INDEXED_KEYS_POSTFIX = ":indexed_keys";

    protected final ThreadLocal<Transaction> tx = new ThreadLocal<Transaction>() {
        protected Transaction initialValue() {
            return null;
        }
    };

    protected final ThreadLocal<Boolean> checkElementsInTransaction = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        }
    };

    private static final Features FEATURES = new Features();

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
        FEATURES.isRDFModel = false;
        FEATURES.isWrapper = false;
        FEATURES.supportsVertexIteration = true;
        FEATURES.supportsEdgeIteration = true;
        FEATURES.supportsVertexIndex = true;
        FEATURES.supportsEdgeIndex = true;
        FEATURES.ignoresSuppliedIds = true;
        FEATURES.supportsTransactions = true;
        FEATURES.supportsIndices = true;
        FEATURES.supportsKeyIndices = true;
        FEATURES.supportsVertexKeyIndex = true;
        FEATURES.supportsEdgeKeyIndex = true;
        FEATURES.supportsEdgeRetrieval = true;
        FEATURES.supportsVertexProperties = true;
        FEATURES.supportsEdgeProperties = true;
        FEATURES.supportsThreadedTransactions = false;
    }

    protected boolean checkElementsInTransaction() {
        if (this.tx.get() == null) {
            return false;
        } else {
            return this.checkElementsInTransaction.get();
        }
    }

    /**
     * Neo4j's transactions are not consistent between the graph and the graph indices.
     * Moreover, global graph operations are not consistent.
     * For example, if a vertex is removed and then an index is queried in the same transaction, the removed vertex can be returned.
     * This method allows the developer to turn on/off a Neo4jGraph 'hack' that ensures transactional consistency.
     * The default behavior for Neo4jGraph is to use Neo4j's native behavior which ensures speed at the expensive of consistency.
     * Note that this boolean switch is local to the current thread (i.e. a ThreadLocal variable).
     *
     * @param checkElementsInTransaction check whether an element is in the transaction between returning it
     */
    public void setCheckElementsInTransaction(final boolean checkElementsInTransaction) {
        this.checkElementsInTransaction.set(checkElementsInTransaction);
    }

    public Neo4jGraph(final String directory) {
        this(directory, null);
    }

    public Neo4jGraph(final String directory, final Map<String, String> configuration) {
        this(directory, configuration, false);
    }

    public Neo4jGraph(final GraphDatabaseService rawGraph) {
        this.rawGraph = rawGraph;
        this.loadKeyIndices();
    }

    public Neo4jGraph(final GraphDatabaseService rawGraph, boolean fresh) {
        this(rawGraph);
        if (fresh)
            this.freshLoad();
    }

    protected Neo4jGraph(final String directory, final Map<String, String> configuration, boolean highAvailabilityMode) {
        if (highAvailabilityMode && configuration == null) {
            throw new IllegalArgumentException("Configuration parameters must be supplied when using HA mode.");
        }
        boolean fresh = !new File(directory).exists();
        try {
            if (null != configuration)
                if (highAvailabilityMode)
                    this.rawGraph = new HighlyAvailableGraphDatabase(directory, configuration);
                else
                    this.rawGraph = new EmbeddedGraphDatabase(directory, configuration);
            else
                this.rawGraph = new EmbeddedGraphDatabase(directory);

            if (fresh)
                this.freshLoad();

            this.loadKeyIndices();

        } catch (Exception e) {
            if (this.rawGraph != null)
                this.rawGraph.shutdown();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void loadKeyIndices() {
        for (final String key : this.getInternalIndexKeys(Vertex.class)) {
            this.createKeyIndex(key, Vertex.class);
        }
        for (final String key : this.getInternalIndexKeys(Edge.class)) {
            this.createKeyIndex(key, Edge.class);
        }
    }

    private void freshLoad() {
        // remove reference node in a single transaction
        try {
            this.autoStartTransaction();
            this.removeVertex(this.getVertex(0));
            this.stopTransaction(Conclusion.SUCCESS);
        } catch (Exception e) {
            this.stopTransaction(Conclusion.FAILURE);
        }
    }

    private <T extends Element> void createInternalIndexKey(final String key, final Class<T> elementClass) {
        final String propertyName = elementClass.getSimpleName() + INDEXED_KEYS_POSTFIX;
        final PropertyContainer pc = ((GraphDatabaseAPI) this.rawGraph).getKernelData().properties();
        try {
            final String[] keys = (String[]) pc.getProperty(propertyName);
            final Set<String> temp = new HashSet<String>(Arrays.asList(keys));
            temp.add(key);
            pc.setProperty(propertyName, temp.toArray(new String[temp.size()]));
        } catch (Exception e) {
            // no indexed_keys kernel data property
            pc.setProperty(propertyName, new String[]{key});

        }
    }

    private <T extends Element> void dropInternalIndexKey(final String key, final Class<T> elementClass) {
        final String propertyName = elementClass.getSimpleName() + INDEXED_KEYS_POSTFIX;
        final PropertyContainer pc = ((GraphDatabaseAPI) this.rawGraph).getKernelData().properties();
        try {
            final String[] keys = (String[]) pc.getProperty(propertyName);
            final Set<String> temp = new HashSet<String>(Arrays.asList(keys));
            temp.remove(key);
            pc.setProperty(propertyName, temp.toArray(new String[temp.size()]));
        } catch (Exception e) {
            // no indexed_keys kernel data property
        }
    }

    public <T extends Element> Set<String> getInternalIndexKeys(final Class<T> elementClass) {
        final String propertyName = elementClass.getSimpleName() + INDEXED_KEYS_POSTFIX;
        final PropertyContainer pc = ((GraphDatabaseAPI) this.rawGraph).getKernelData().properties();
        try {
            final String[] keys = (String[]) pc.getProperty(propertyName);
            return new HashSet<String>(Arrays.asList(keys));
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    public synchronized <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        if (this.rawGraph.index().existsForNodes(indexName) || this.rawGraph.index().existsForRelationships(indexName)) {
            throw ExceptionFactory.indexAlreadyExists(indexName);
        }
        this.autoStartTransaction();
        return new Neo4jIndex(indexName, indexClass, this, indexParameters);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        if (Vertex.class.isAssignableFrom(indexClass)) {
            if (this.rawGraph.index().existsForNodes(indexName)) {
                return new Neo4jIndex(indexName, indexClass, this);
            } else if (this.rawGraph.index().existsForRelationships(indexName)) {
                throw ExceptionFactory.indexDoesNotSupportClass(indexName, indexClass);
            } else {
                return null;
            }
        } else if (Edge.class.isAssignableFrom(indexClass)) {
            if (this.rawGraph.index().existsForRelationships(indexName)) {
                return new Neo4jIndex(indexName, indexClass, this);
            } else if (this.rawGraph.index().existsForNodes(indexName)) {
                throw ExceptionFactory.indexDoesNotSupportClass(indexName, indexClass);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Note that this method will force a successful closing of the current thread's transaction.
     * As such, once the index is dropped, the operation is committed.
     *
     * @param indexName the name of the index to drop
     */
    public synchronized void dropIndex(final String indexName) {
        this.autoStartTransaction();
        try {
            if (this.rawGraph.index().existsForNodes(indexName)) {
                org.neo4j.graphdb.index.Index<Node> nodeIndex = this.rawGraph.index().forNodes(indexName);
                if (nodeIndex.isWriteable()) {
                    nodeIndex.delete();
                }
            } else if (this.rawGraph.index().existsForRelationships(indexName)) {
                RelationshipIndex relationshipIndex = this.rawGraph.index().forRelationships(indexName);
                if (relationshipIndex.isWriteable()) {
                    relationshipIndex.delete();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        this.stopTransaction(Conclusion.SUCCESS);
    }


    public Iterable<Index<? extends Element>> getIndices() {
        final List<Index<? extends Element>> indices = new ArrayList<Index<? extends Element>>();
        for (final String name : this.rawGraph.index().nodeIndexNames()) {
            if (!name.equals(Neo4jTokens.NODE_AUTO_INDEX))
                indices.add(new Neo4jIndex(name, Vertex.class, this));
        }
        for (final String name : this.rawGraph.index().relationshipIndexNames()) {
            if (!name.equals(Neo4jTokens.RELATIONSHIP_AUTO_INDEX))
                indices.add(new Neo4jIndex(name, Edge.class, this));
        }
        return indices;
    }


    public Vertex addVertex(final Object id) {
        try {
            this.autoStartTransaction();
            return new Neo4jVertex(this.rawGraph.createNode(), this);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            throw ExceptionFactory.vertexIdCanNotBeNull();

        try {
            final Long longId;
            if (id instanceof Long)
                longId = (Long) id;
            else if (id instanceof Number)
                longId = ((Number) id).longValue();
            else
                longId = Double.valueOf(id.toString()).longValue();
            return new Neo4jVertex(this.rawGraph.getNodeById(longId), this);
        } catch (NotFoundException e) {
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The underlying Neo4j graph does not natively support this method within a transaction.
     * If the graph is not currently in a transaction, then the operation runs efficiently.
     * If the graph is in a transaction, then, for every vertex, a try/catch is used to determine if its in the current transaction.
     *
     * @return all the vertices in the graph
     */
    public Iterable<Vertex> getVertices() {
        return new Neo4jVertexIterable(GlobalGraphOperations.at(rawGraph).getAllNodes(), this, this.checkElementsInTransaction());
    }

    public Iterable<Vertex> getVertices(final String key, final Object value) {
        if (this.rawGraph.index().getNodeAutoIndexer().isEnabled() && this.rawGraph.index().getNodeAutoIndexer().getAutoIndexedProperties().contains(key))
            return new Neo4jVertexIterable(this.rawGraph.index().getNodeAutoIndexer().getAutoIndex().get(key, value), this);
        else
            return new PropertyFilteredIterable<Vertex>(key, value, this.getVertices());
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The underlying Neo4j graph does not natively support this method within a transaction.
     * If the graph is not currently in a transaction, then the operation runs efficiently.
     * If the graph is in a transaction, then, for every edge, a try/catch is used to determine if its in the current transaction.
     *
     * @return all the edges in the graph
     */
    public Iterable<Edge> getEdges() {
        return new Neo4jEdgeIterable(GlobalGraphOperations.at(rawGraph).getAllRelationships(), this, this.checkElementsInTransaction());
    }

    public Iterable<Edge> getEdges(final String key, final Object value) {
        if (this.rawGraph.index().getRelationshipAutoIndexer().isEnabled() && this.rawGraph.index().getRelationshipAutoIndexer().getAutoIndexedProperties().contains(key))
            return new Neo4jEdgeIterable(this.rawGraph.index().getRelationshipAutoIndexer().getAutoIndex().get(key, value), this);
        else
            return new PropertyFilteredIterable<Edge>(key, value, this.getEdges());
    }

    public <T extends Element> void dropKeyIndex(final String key, final Class<T> elementClass) {
        this.autoStartTransaction();
        if (Vertex.class.isAssignableFrom(elementClass)) {
            if (!this.rawGraph.index().getNodeAutoIndexer().isEnabled())
                return;
            this.rawGraph.index().getNodeAutoIndexer().stopAutoIndexingProperty(key);
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            if (!this.rawGraph.index().getRelationshipAutoIndexer().isEnabled())
                return;
            this.rawGraph.index().getRelationshipAutoIndexer().stopAutoIndexingProperty(key);
        } else {
            throw ExceptionFactory.classIsNotIndexable(elementClass);
        }
        this.dropInternalIndexKey(key, elementClass);
    }

    public <T extends Element> void createKeyIndex(final String key, final Class<T> elementClass) {
        this.autoStartTransaction();
        if (Vertex.class.isAssignableFrom(elementClass)) {
            if (!this.rawGraph.index().getNodeAutoIndexer().isEnabled())
                this.rawGraph.index().getNodeAutoIndexer().setEnabled(true);

            this.rawGraph.index().getNodeAutoIndexer().startAutoIndexingProperty(key);
            if (!this.getInternalIndexKeys(Vertex.class).contains(key)) {
                KeyIndexableGraphHelper.reIndexElements(this, this.getVertices(), new HashSet<String>(Arrays.asList(key)));
                this.createInternalIndexKey(key, elementClass);
            }
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            if (!this.rawGraph.index().getRelationshipAutoIndexer().isEnabled())
                this.rawGraph.index().getRelationshipAutoIndexer().setEnabled(true);

            this.rawGraph.index().getRelationshipAutoIndexer().startAutoIndexingProperty(key);
            if (!this.getInternalIndexKeys(Edge.class).contains(key)) {
                KeyIndexableGraphHelper.reIndexElements(this, this.getEdges(), new HashSet<String>(Arrays.asList(key)));
                this.createInternalIndexKey(key, elementClass);
            }
        } else {
            throw ExceptionFactory.classIsNotIndexable(elementClass);
        }
    }

    public <T extends Element> Set<String> getIndexedKeys(final Class<T> elementClass) {
        if (Vertex.class.isAssignableFrom(elementClass)) {
            if (!this.rawGraph.index().getNodeAutoIndexer().isEnabled())
                return Collections.emptySet();
            return this.rawGraph.index().getNodeAutoIndexer().getAutoIndexedProperties();
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            if (!this.rawGraph.index().getRelationshipAutoIndexer().isEnabled())
                return Collections.emptySet();
            return this.rawGraph.index().getRelationshipAutoIndexer().getAutoIndexedProperties();
        } else {
            throw ExceptionFactory.classIsNotIndexable(elementClass);
        }
    }

    public void removeVertex(final Vertex vertex) {
        this.autoStartTransaction();
        final Long id = (Long) vertex.getId();
        final Node node = this.rawGraph.getNodeById(id);
        if (null != node) {
            try {
                for (final Edge edge : vertex.getEdges(Direction.BOTH)) {
                    ((Relationship) ((Neo4jEdge) edge).getRawElement()).delete();
                }
                node.delete();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        try {
            this.autoStartTransaction();
            final Node outNode = ((Neo4jVertex) outVertex).getRawVertex();
            final Node inNode = ((Neo4jVertex) inVertex).getRawVertex();
            final Relationship relationship = outNode.createRelationshipTo(inNode, DynamicRelationshipType.withName(label));
            return new Neo4jEdge(relationship, this);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();

        try {
            final Long longId;
            if (id instanceof Long)
                longId = (Long) id;
            else
                longId = Double.valueOf(id.toString()).longValue();
            return new Neo4jEdge(this.rawGraph.getRelationshipById(longId), this);
        } catch (NotFoundException e) {
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }


    public void removeEdge(final Edge edge) {
        try {
            this.autoStartTransaction();
            ((Relationship) ((Neo4jEdge) edge).getRawElement()).delete();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (null == tx.get()) {
            return;
        }

        try {
            if (conclusion.equals(Conclusion.SUCCESS))
                tx.get().success();
            else
                tx.get().failure();
        } finally {
            tx.get().finish();
            tx.remove();
        }
    }

    public void shutdown() {
        //todo inspect why certain transactions fail
        try {
            if (null != tx.get()) {
                tx.get().success();
                tx.get().finish();
                tx.remove();
            }
        } catch (TransactionFailureException e) {
        }
        this.rawGraph.shutdown();
    }

    protected void autoStartTransaction() {
        if (tx.get() == null) {
            tx.set(this.rawGraph.beginTx());
        }
    }

    public GraphDatabaseService getRawGraph() {
        return this.rawGraph;
    }

    public Features getFeatures() {
        return FEATURES;
    }

    public String toString() {
        return StringFactory.graphString(this, this.rawGraph.toString());
    }
}