package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Features;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.KeyIndexableGraph;
import com.tinkerpop.blueprints.pgm.MetaGraph;
import com.tinkerpop.blueprints.pgm.Parameter;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.PropertyFilteredIterable;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jEdgeIterable;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jVertexIterable;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.HighlyAvailableGraphDatabase;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Blueprints implementation of the graph database Neo4j (http://neo4j.o
 * rg)
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jGraph implements TransactionalGraph, IndexableGraph, KeyIndexableGraph, MetaGraph<GraphDatabaseService> {

    private GraphDatabaseService rawGraph;

    private final ThreadLocal<Transaction> tx = new ThreadLocal<Transaction>() {
        protected Transaction initialValue() {
            return null;
        }
    };

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

        FEATURES.allowDuplicateEdges = true;
        FEATURES.allowSelfLoops = true;
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
    }

    public Neo4jGraph(final String directory) {
        this(directory, null);
    }

    public Neo4jGraph(final String directory, final Map<String, String> configuration) {
        this(directory, configuration, false);
    }

    public Neo4jGraph(final GraphDatabaseService rawGraph) {
        this.rawGraph = rawGraph;
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

        } catch (RuntimeException e) {
            if (this.rawGraph != null)
                this.rawGraph.shutdown();
            throw e;
        } catch (Exception e) {
            if (this.rawGraph != null)
                this.rawGraph.shutdown();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void freshLoad() {
        // remove reference node and create default indices
        try {
            this.startTransaction();
            this.removeVertex(this.getVertex(0));
            this.stopTransaction(Conclusion.SUCCESS);
        } catch (Exception e) {
            this.stopTransaction(Conclusion.FAILURE);
        }
    }

    public synchronized <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        if (this.rawGraph.index().existsForNodes(indexName) || this.rawGraph.index().existsForRelationships(indexName)) {
            throw new RuntimeException("Index already exists: " + indexName);
        }
        this.autoStartTransaction();
        return new Neo4jIndex(indexName, indexClass, this, indexParameters);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        if (Vertex.class.isAssignableFrom(indexClass)) {
            if (this.rawGraph.index().existsForNodes(indexName)) {
                return new Neo4jIndex(indexName, indexClass, this);
            } else if (this.rawGraph.index().existsForRelationships(indexName)) {
                throw new RuntimeException("Can not convert existing " + indexName + " index to a " + indexClass + " index");
            } else {
                return null;
            }
        } else if (Edge.class.isAssignableFrom(indexClass)) {
            if (this.rawGraph.index().existsForRelationships(indexName)) {
                return new Neo4jIndex(indexName, indexClass, this);
            } else if (this.rawGraph.index().existsForNodes(indexName)) {
                throw new RuntimeException("Can not convert existing " + indexName + " index to a " + indexClass + " index");
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
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
            throw new IllegalArgumentException("Vertex identifier cannot be null");

        try {
            final Long longId;
            if (id instanceof Long)
                longId = (Long) id;
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
     * The underlying Neo4j graph does not support this method within a transaction.
     * Calling this method will commit the current transaction successfully and then return the vertex iterable.
     *
     * @return all the vertices in the graph
     */
    public Iterable<Vertex> getVertices() {
        this.stopTransaction(Conclusion.SUCCESS);
        return new Neo4jVertexIterable(GlobalGraphOperations.at(rawGraph).getAllNodes(), this);
    }

    public Iterable<Vertex> getVertices(final String key, final Object value) {
        if (this.rawGraph.index().getNodeAutoIndexer().isEnabled() && this.rawGraph.index().getNodeAutoIndexer().getAutoIndexedProperties().contains(key))
            return new Neo4jVertexIterable(this.rawGraph.index().getNodeAutoIndexer().getAutoIndex().get(key, value), this);
        else
            return new PropertyFilteredIterable<Vertex>(key, value, this.getVertices());
    }

    /**
     * The underlying Neo4j graph does not support this method within a transaction.
     * Calling this method will commit the current transaction successfully and then return the edge iterable.
     *
     * @return all the edges in the graph
     */
    public Iterable<Edge> getEdges() {
        this.stopTransaction(Conclusion.SUCCESS);
        return new Neo4jEdgeIterable(GlobalGraphOperations.at(rawGraph).getAllRelationships(), this);
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
            throw new IllegalArgumentException("The class " + elementClass + " is not indexable");
        }
    }

    public <T extends Element> void createKeyIndex(final String key, final Class<T> elementClass) {
        this.autoStartTransaction();
        if (Vertex.class.isAssignableFrom(elementClass)) {
            if (!this.rawGraph.index().getNodeAutoIndexer().isEnabled())
                this.rawGraph.index().getNodeAutoIndexer().setEnabled(true);
            this.rawGraph.index().getNodeAutoIndexer().startAutoIndexingProperty(key);
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            if (!this.rawGraph.index().getRelationshipAutoIndexer().isEnabled())
                this.rawGraph.index().getRelationshipAutoIndexer().setEnabled(true);
            this.rawGraph.index().getRelationshipAutoIndexer().startAutoIndexingProperty(key);
        } else {
            throw new IllegalArgumentException("The class " + elementClass + " is not indexable");
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
            throw new IllegalArgumentException("The class " + elementClass + " is not indexable");
        }
    }

    public void removeVertex(final Vertex vertex) {
        this.autoStartTransaction();
        final Long id = (Long) vertex.getId();
        final Node node = this.rawGraph.getNodeById(id);
        if (null != node) {
            try {
                for (final Edge edge : vertex.getInEdges()) {
                    ((Relationship) ((Neo4jEdge) edge).getRawElement()).delete();
                }
                for (final Edge edge : vertex.getOutEdges()) {
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
            return new Neo4jEdge(relationship, this, true);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            throw new IllegalArgumentException("Edge identifier cannot be null");

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

    public void startTransaction() {
        if (tx.get() == null) {
            tx.set(this.rawGraph.beginTx());
        } else
            throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (null == tx.get()) {
            return;
        }

        if (conclusion == Conclusion.SUCCESS)
            tx.get().success();
        else
            tx.get().failure();

        tx.get().finish();
        tx.remove();
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