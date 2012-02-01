package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jVertexSequence;
import com.tinkerpop.blueprints.pgm.util.AutomaticIndexHelper;
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
import org.neo4j.kernel.HighlyAvailableGraphDatabase;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Blueprints implementation of the graph database Neo4j (http://neo4j.org)
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jGraph implements TransactionalGraph, IndexableGraph {

    private GraphDatabaseService rawGraph;
    private final ThreadLocal<Transaction> tx = new ThreadLocal<Transaction>() {
        protected Transaction initialValue() {
            return null;
        }
    };
    private final ThreadLocal<Integer> txBuffer = new ThreadLocal<Integer>() {
        protected Integer initialValue() {
            return 1;
        }
    };
    private final ThreadLocal<Integer> txCounter = new ThreadLocal<Integer>() {
        protected Integer initialValue() {
            return 0;
        }
    };

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
        this.rawGraph = rawGraph;
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
            this.removeVertex(this.getVertex(0));
        } catch (Exception e) {
        }
        this.createAutomaticIndex(Index.VERTICES, Neo4jVertex.class, null);
        this.createAutomaticIndex(Index.EDGES, Neo4jEdge.class, null);
    }

    private boolean isAutomaticIndex(final org.neo4j.graphdb.index.Index rawIndex) {
        final String type = this.rawGraph.index().getConfiguration(rawIndex).get(Neo4jTokens.BLUEPRINTS_TYPE);
        return (null != type && type.equals(Index.Type.AUTOMATIC.toString()));
    }

    public synchronized <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        if (this.rawGraph.index().existsForNodes(indexName) || this.rawGraph.index().existsForRelationships(indexName)) {
            throw new RuntimeException("Index already exists: " + indexName);
        }

        return new Neo4jIndex(indexName, indexClass, this);
    }

    public synchronized <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, Set<String> keys) {
        if (this.rawGraph.index().existsForNodes(indexName) || this.rawGraph.index().existsForRelationships(indexName)) {
            throw new RuntimeException("Index already exists: " + indexName);
        }

        return new Neo4jAutomaticIndex(indexName, indexClass, this, keys);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        if (Vertex.class.isAssignableFrom(indexClass)) {
            if (this.rawGraph.index().existsForNodes(indexName)) {
                if (isAutomaticIndex(this.rawGraph.index().forNodes(indexName)))
                    return new Neo4jAutomaticIndex(indexName, indexClass, this, null);
                else
                    return new Neo4jIndex(indexName, indexClass, this);
            } else if (this.rawGraph.index().existsForRelationships(indexName)) {
                throw new RuntimeException("Can not convert existing " + indexName + " index to a " + indexClass + " index");
            } else {
                return null;
            }
        } else if (Edge.class.isAssignableFrom(indexClass)) {
            if (this.rawGraph.index().existsForRelationships(indexName)) {
                if (isAutomaticIndex(this.rawGraph.index().forRelationships(indexName)))
                    return new Neo4jAutomaticIndex(indexName, indexClass, this, null);
                else
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

    public synchronized void dropIndex(final String indexName) {
        try {
            this.autoStartTransaction();
            org.neo4j.graphdb.index.Index<Node> nodeIndex = this.rawGraph.index().forNodes(indexName);
            if(nodeIndex.isWriteable()){
                nodeIndex.delete();
            }
            RelationshipIndex relationshipIndex = this.rawGraph.index().forRelationships(indexName);
            if(relationshipIndex.isWriteable()){
                relationshipIndex.delete();
            }
            this.stopTransaction(Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            this.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            this.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected <T extends Neo4jElement> Iterable<Neo4jAutomaticIndex<T, PropertyContainer>> getAutoIndices(final Class<T> indexClass) {
        final String[] names;
        final boolean forVertices = Vertex.class.isAssignableFrom(indexClass);
        if (forVertices) {
            names = this.rawGraph.index().nodeIndexNames();
        } else {
            names = this.rawGraph.index().relationshipIndexNames();
        }
        final List<Neo4jAutomaticIndex<T, PropertyContainer>> indices = new ArrayList<Neo4jAutomaticIndex<T, PropertyContainer>>();
        for (final String name : names) {
            if (forVertices) {
                if (isAutomaticIndex(this.rawGraph.index().forNodes(name))) {
                    indices.add(new Neo4jAutomaticIndex(name, Vertex.class, this, null));
                }
            } else {
                if (isAutomaticIndex(this.rawGraph.index().forRelationships(name))) {
                    indices.add(new Neo4jAutomaticIndex(name, Edge.class, this, null));
                }
            }
        }
        return indices;

    }

    public Iterable<Index<? extends Element>> getIndices() {
        final List<Index<? extends Element>> indices = new ArrayList<Index<? extends Element>>();
        for (final String name : this.rawGraph.index().nodeIndexNames()) {
            if (isAutomaticIndex(this.rawGraph.index().forNodes(name))) {
                indices.add(new Neo4jAutomaticIndex(name, Vertex.class, this, null));
            } else {
                indices.add(new Neo4jIndex(name, Vertex.class, this));
            }
        }
        for (final String name : this.rawGraph.index().relationshipIndexNames()) {
            if (isAutomaticIndex(this.rawGraph.index().forRelationships(name))) {
                indices.add(new Neo4jAutomaticIndex(name, Edge.class, this, null));
            } else {
                indices.add(new Neo4jIndex(name, Edge.class, this));
            }
        }
        return indices;
    }


    public Vertex addVertex(final Object id) {
        try {
            this.autoStartTransaction();
            final Vertex vertex = new Neo4jVertex(this.rawGraph.createNode(), this);
            this.autoStopTransaction(Conclusion.SUCCESS);
            return vertex;
        } catch (RuntimeException e) {
            this.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            this.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
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
            return new Neo4jVertex(this.rawGraph.getNodeById(longId), this);
        } catch (NotFoundException e) {
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Iterable<Vertex> getVertices() {
        return new Neo4jVertexSequence(GlobalGraphOperations.at(rawGraph).getAllNodes(), this);
    }

    public Iterable<Edge> getEdges() {
        return new Neo4jEdgeSequence(GlobalGraphOperations.at(rawGraph).getAllRelationships(), this);
    }

    public void removeVertex(final Vertex vertex) {
        final Long id = (Long) vertex.getId();
        final Node node = this.rawGraph.getNodeById(id);
        if (null != node) {
            try {
                AutomaticIndexHelper.removeElement(this, vertex);
                this.autoStartTransaction();
                for (final Edge edge : vertex.getInEdges()) {
                    ((Relationship) ((Neo4jEdge) edge).getRawElement()).delete();
                }
                for (final Edge edge : vertex.getOutEdges()) {
                    ((Relationship) ((Neo4jEdge) edge).getRawElement()).delete();
                }
                node.delete();
                this.autoStopTransaction(Conclusion.SUCCESS);
            } catch (RuntimeException e) {
                this.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
                throw e;
            } catch (Exception e) {
                this.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
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
            final Edge edge = new Neo4jEdge(relationship, this, true);
            this.autoStopTransaction(Conclusion.SUCCESS);
            return edge;
        } catch (RuntimeException e) {
            this.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            this.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            throw new IllegalArgumentException("Element identifier cannot be null");

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
            AutomaticIndexHelper.removeElement(this, edge);
            this.autoStartTransaction();
            ((Relationship) ((Neo4jEdge) edge).getRawElement()).delete();
            this.autoStopTransaction(Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            this.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            this.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void startTransaction() {
        if (tx.get() == null) {
            txCounter.set(0);
            tx.set(this.rawGraph.beginTx());
        } else
            throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (null == tx.get()) {
            txCounter.set(0);
            return;
        }

        if (conclusion == Conclusion.SUCCESS)
            tx.get().success();
        else
            tx.get().failure();

        tx.get().finish();
        tx.remove();
        txCounter.set(0);
    }

    public int getMaxBufferSize() {
        return txBuffer.get();
    }

    public int getCurrentBufferSize() {
        return txCounter.get();
    }

    public void setMaxBufferSize(final int size) {
        if (null != tx.get()) {
            tx.get().success();
            tx.get().finish();
            tx.remove();
        }
        this.txBuffer.set(size);
        this.txCounter.set(0);
    }

    public void shutdown() {
        if (null != tx.get()) {
            try {
                tx.get().success();
                tx.get().finish();
                tx.remove();
            } catch (TransactionFailureException e) {
            }
        }
        this.rawGraph.shutdown();
    }

    /**
     * This operation does not respect the transaction buffer. A clear will eradicate the graph and commit the results immediately.
     */
    public void clear() {
        try {
            this.autoStartTransaction();
            for (final Index index : this.getIndices()) {
                this.dropIndex(index.getIndexName());
            }
            this.stopTransaction(Conclusion.SUCCESS);
            this.startTransaction();
            for (final Node node : GlobalGraphOperations.at(this.rawGraph).getAllNodes()) {
                for (final Relationship relationship : node.getRelationships()) {
                    try {
                        relationship.delete();
                    } catch (IllegalStateException e) {
                    }
                }
                try {
                    node.delete();
                } catch (IllegalStateException e) {
                }
            }
            this.stopTransaction(Conclusion.SUCCESS);
            this.autoStartTransaction();
            this.createAutomaticIndex(Index.VERTICES, Neo4jVertex.class, null);
            this.createAutomaticIndex(Index.EDGES, Neo4jEdge.class, null);
            this.stopTransaction(Conclusion.SUCCESS);
        } catch (Exception e) {
            this.stopTransaction(Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void autoStartTransaction() {
        if (this.txBuffer.get() > 0) {
            if (tx.get() == null) {
                tx.set(this.rawGraph.beginTx());
                txCounter.set(0);
            }
        }
    }

    protected void autoStopTransaction(final Conclusion conclusion) {
        if (this.txBuffer.get() > 0) {
            txCounter.set(txCounter.get() + 1);
            if (conclusion == Conclusion.FAILURE) {
                tx.get().failure();
                tx.get().finish();
                tx.remove();
                txCounter.set(0);
            } else if (this.txCounter.get() % this.txBuffer.get() == 0) {
                tx.get().success();
                tx.get().finish();
                tx.remove();
                txCounter.set(0);
            }
        }
    }

    public GraphDatabaseService getRawGraph() {
        return this.rawGraph;
    }

    public String toString() {
        return StringFactory.graphString(this, this.rawGraph.toString());
    }
}