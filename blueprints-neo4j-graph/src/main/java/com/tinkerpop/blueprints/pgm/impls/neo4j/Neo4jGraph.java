package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jGraphEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jVertexSequence;
import com.tinkerpop.blueprints.pgm.util.AutomaticIndexHelper;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final ThreadLocal<Mode> txMode = new ThreadLocal<Mode>() {
        protected Mode initialValue() {
            return Mode.AUTOMATIC;
        }
    };
    protected Map<String, Neo4jIndex> indices = new HashMap<String, Neo4jIndex>();
    protected Map<String, Neo4jAutomaticIndex> autoIndices = new HashMap<String, Neo4jAutomaticIndex>();

    public Neo4jGraph(final String directory) {
        this(directory, null);
    }

    public Neo4jGraph(final String directory, final Map<String, String> configuration) {
        boolean fresh = !new File(directory).exists();
        try {
            if (null != configuration)
                this.rawGraph = new EmbeddedGraphDatabase(directory, configuration);
            else
                this.rawGraph = new EmbeddedGraphDatabase(directory);

            if (fresh) {
                // remove reference node
                try {
                    this.removeVertex(this.getVertex(0));
                } catch (Exception e) {
                }
                this.createAutomaticIndex(Index.VERTICES, Neo4jVertex.class, null);
                this.createAutomaticIndex(Index.EDGES, Neo4jEdge.class, null);
            } else {
                this.loadIndices();
            }

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

    public Neo4jGraph(final GraphDatabaseService rawGraph) {
        this.rawGraph = rawGraph;
        this.loadIndices();
    }

    private void loadIndices() {
        final IndexManager manager = this.rawGraph.index();
        for (final String indexName : manager.nodeIndexNames()) {
            final org.neo4j.graphdb.index.Index<Node> neo4jIndex = manager.forNodes(indexName);
            final String type = manager.getConfiguration(neo4jIndex).get(Neo4jTokens.BLUEPRINTS_TYPE);
            if (null != type && type.equals(Index.Type.AUTOMATIC.toString()))
                this.createAutomaticIndex(indexName, Neo4jVertex.class, null);
            else
                this.createManualIndex(indexName, Neo4jVertex.class);
        }

        for (final String indexName : manager.relationshipIndexNames()) {
            final org.neo4j.graphdb.index.Index<Relationship> neo4jIndex = manager.forRelationships(indexName);
            final String type = manager.getConfiguration(neo4jIndex).get(Neo4jTokens.BLUEPRINTS_TYPE);
            if (null != type && type.equals(Index.Type.AUTOMATIC.toString()))
                this.createAutomaticIndex(indexName, Neo4jEdge.class, null);
            else
                this.createManualIndex(indexName, Neo4jEdge.class);
        }
    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        if (this.indices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        Neo4jIndex index = new Neo4jIndex(indexName, indexClass, this);
        this.indices.put(index.getIndexName(), index);
        return index;
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, Set<String> keys) {
        if (this.indices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        Neo4jAutomaticIndex index = new Neo4jAutomaticIndex(indexName, indexClass, this, keys);
        this.autoIndices.put(index.getIndexName(), index);
        this.indices.put(index.getIndexName(), index);
        return index;
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        Index index = this.indices.get(indexName);
        // todo: be sure to do code for multiple connections interacting with graph
        if (null == index)
            return null;
        else if (indexClass.isAssignableFrom(index.getIndexClass()))
            return (Index<T>) index;
        else
            throw new RuntimeException("Can not convert " + index.getIndexClass() + " to " + indexClass);
    }

    public void dropIndex(final String indexName) {
        try {
            this.autoStartTransaction();
            this.rawGraph.index().forNodes(indexName).delete();
            this.rawGraph.index().forRelationships(indexName).delete();
            this.autoStopTransaction(Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            this.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            this.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }

        this.indices.remove(indexName);
        this.autoIndices.remove(indexName);
    }

    protected Iterable<Neo4jAutomaticIndex> getAutoIndices() {
        return autoIndices.values();
    }

    public Iterable<Index<? extends Element>> getIndices() {
        List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
        for (Index index : this.indices.values()) {
            list.add(index);
        }
        return list;
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
            return null;

        try {
            Long longId = Double.valueOf(id.toString()).longValue();
            Node node = this.rawGraph.getNodeById(longId);
            return new Neo4jVertex(node, this);
        } catch (NotFoundException e) {
            return null;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Neo4j vertex ids must be convertible to a long value", e);
        }
    }

    public Iterable<Vertex> getVertices() {
        return new Neo4jVertexSequence(this.rawGraph.getAllNodes(), this);
    }

    public Iterable<Edge> getEdges() {
        return new Neo4jGraphEdgeSequence(this.rawGraph.getAllNodes(), this);
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
        final Node outNode = ((Neo4jVertex) outVertex).getRawVertex();
        final Node inNode = ((Neo4jVertex) inVertex).getRawVertex();
        try {
            this.autoStartTransaction();
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
            return null;

        try {
            final Long longId = Double.valueOf(id.toString()).longValue();
            final Relationship relationship = this.rawGraph.getRelationshipById(longId);
            return new Neo4jEdge(relationship, this);
        } catch (NotFoundException e) {
            return null;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Neo4j edge ids must be convertible to a long value", e);
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
        if (Mode.AUTOMATIC == txMode.get())
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
        if (tx.get() == null) {
            tx.set(this.rawGraph.beginTx());
        } else
            throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (Mode.AUTOMATIC == txMode.get())
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
        if (null == tx.get())
            throw new RuntimeException("There is no active transaction to stop");

        if (conclusion == Conclusion.SUCCESS) {
            tx.get().success();
            tx.get().finish();
        } else {
            tx.get().failure();
            tx.get().finish();
        }
        tx.remove();
    }

    public void setTransactionMode(final Mode mode) {
        if (null != tx.get()) {
            tx.get().success();
            tx.get().finish();
            tx.remove();
        }
        txMode.set(mode);
    }

    public Mode getTransactionMode() {
        return txMode.get();
    }

    public void shutdown() {
        if (null != tx.get()) {
            try {
                tx.get().failure();
                tx.get().finish();
                tx.remove();
            } catch (TransactionFailureException e) {
            }
        }
        this.rawGraph.shutdown();
    }

    public void clear() {
        for (Vertex vertex : this.getVertices()) {
            this.removeVertex(vertex);
        }
        for (Index index : this.getIndices()) {
            this.dropIndex(index.getIndexName());
        }
        this.createAutomaticIndex(Index.VERTICES, Neo4jVertex.class, null);
        this.createAutomaticIndex(Index.EDGES, Neo4jEdge.class, null);
    }

    protected void autoStartTransaction() {
        if (getTransactionMode() == Mode.AUTOMATIC) {
            if (tx.get() == null) {
                tx.set(this.rawGraph.beginTx());
            } else
                throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
        }
    }

    protected void autoStopTransaction(final Conclusion conclusion) {
        if (getTransactionMode() == Mode.AUTOMATIC) {
            if (conclusion == Conclusion.SUCCESS) {
                tx.get().success();
            } else {
                tx.get().failure();
            }
            tx.get().finish();
            tx.remove();
        }
    }

    public GraphDatabaseService getRawGraph() {
        return this.rawGraph;
    }

    public String toString() {
        return "neo4jgraph[" + this.rawGraph + "]";
    }
}
