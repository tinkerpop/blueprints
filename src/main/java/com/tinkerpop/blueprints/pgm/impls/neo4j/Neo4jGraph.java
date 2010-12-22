package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jGraphEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jVertexSequence;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Blueprints implementation of the graph database Neo4j (http://neo4j.org)
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jGraph implements TransactionalGraph, IndexableGraph {

    private GraphDatabaseService rawGraph;
    private String directory;
    private Transaction tx;
    private Mode mode = Mode.AUTOMATIC;
    protected Map<String, Neo4jIndex> indices = new HashMap<String, Neo4jIndex>();
    protected Map<String, Neo4jAutomaticIndex> autoIndices = new HashMap<String, Neo4jAutomaticIndex>();

    public Neo4jGraph(final String directory) {
        this(directory, null);
    }

    public Neo4jGraph(final String directory, final Map<String, String> configuration) {
        this.directory = directory;
        boolean fresh = !new File(this.directory).exists();
        try {
            if (null != configuration)
                this.rawGraph = new EmbeddedGraphDatabase(this.directory, configuration);
            else
                this.rawGraph = new EmbeddedGraphDatabase(this.directory);

            if (fresh) {
                // remove reference node
                try {
                    this.removeVertex(this.getVertex(0));
                } catch (Exception e) {
                }
                this.createIndex(Index.VERTICES, Neo4jVertex.class, Index.Type.AUTOMATIC);
                this.createIndex(Index.EDGES, Neo4jEdge.class, Index.Type.AUTOMATIC);
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
        IndexManager manager = this.rawGraph.index();
        for (String indexName : manager.nodeIndexNames()) {
            org.neo4j.graphdb.index.Index<Node> neo4jIndex = manager.forNodes(indexName);
            if (manager.getConfiguration(neo4jIndex).get(Neo4jTokens.BLUEPRINTS_TYPE).equals(Index.Type.AUTOMATIC.toString()))
                this.createIndex(indexName, Neo4jVertex.class, Index.Type.AUTOMATIC);
            else
                this.createIndex(indexName, Neo4jVertex.class, Index.Type.MANUAL);
        }

        for (String indexName : manager.relationshipIndexNames()) {
            org.neo4j.graphdb.index.Index<Relationship> neo4jIndex = manager.forRelationships(indexName);
            if (manager.getConfiguration(neo4jIndex).get(Neo4jTokens.BLUEPRINTS_TYPE).equals(Index.Type.AUTOMATIC.toString()))
                this.createIndex(indexName, Neo4jEdge.class, Index.Type.AUTOMATIC);
            else
                this.createIndex(indexName, Neo4jEdge.class, Index.Type.MANUAL);
        }
    }

    public <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass, final Index.Type type) {
        if (this.indices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        Neo4jIndex index;
        if (type == Index.Type.MANUAL) {
            index = new Neo4jIndex(indexName, indexClass, this);
        } else {
            index = new Neo4jAutomaticIndex(indexName, indexClass, this);
            this.autoIndices.put(index.getIndexName(), (Neo4jAutomaticIndex) index);
        }
        this.indices.put(index.getIndexName(), index);
        return index;
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        Index index = this.indices.get(indexName);
        // todo: be sure to do code for multiple connections interacting with db
        if (null == index)
            throw new RuntimeException("No such index exists: " + indexName);
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
                this.autoStartTransaction();
                for (final Edge edge : vertex.getInEdges()) {
                    ((Relationship) ((Neo4jEdge) edge).getRawElement()).delete();
                }
                for (final Edge edge : vertex.getOutEdges()) {
                    ((Relationship) ((Neo4jEdge) edge).getRawElement()).delete();
                }
                for (Neo4jAutomaticIndex idx : this.getAutoIndices()) {
                    idx.removeElement((Neo4jVertex) vertex);
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
        if (Mode.AUTOMATIC == this.mode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
        if (this.tx == null)
            this.tx = this.rawGraph.beginTx();
        else
            throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (Mode.AUTOMATIC == this.mode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);

        if (null == this.tx)
            throw new RuntimeException("There is no active transaction to stop");

        if (conclusion == Conclusion.SUCCESS) {
            this.tx.success();
        } else {
            this.tx.failure();
        }
        this.tx.finish();
        this.tx = null;
    }

    public void setTransactionMode(final Mode mode) {
        if (null != this.tx) {
            this.tx.success();
            this.tx.finish();
            this.tx = null;
        }
        this.mode = mode;
    }

    public Mode getTransactionMode() {
        return this.mode;
    }

    public void shutdown() {
        if (null != this.tx) {
            try {
                this.tx.failure();
                this.tx.finish();
                this.tx = null;
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
    }

    protected void autoStartTransaction() {
        if (getTransactionMode() == Mode.AUTOMATIC) {
            if (this.tx == null)
                this.tx = this.rawGraph.beginTx();
            else
                throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
        }
    }

    protected void autoStopTransaction(final Conclusion conclusion) {
        if (getTransactionMode() == Mode.AUTOMATIC) {
            if (conclusion == Conclusion.SUCCESS)
                this.tx.success();
            else
                this.tx.failure();
            this.tx.finish();
            this.tx = null;
        }
    }

    public GraphDatabaseService getRawGraph() {
        return this.rawGraph;
    }

    public String toString() {
        return "neo4jgraph[" + this.rawGraph + "]";
    }
}
