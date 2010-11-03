package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jGraphEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jVertexSequence;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jGraph implements TransactionalGraph, IndexableGraph {

    private GraphDatabaseService neo4j;
    private String directory;
    private Transaction tx;
    private Mode mode = Mode.AUTOMATIC;
    protected Map<String, Neo4jIndex> indices = new HashMap<String, Neo4jIndex>();
    protected Map<String, Neo4jAutomaticIndex> autoIndices = new HashMap<String, Neo4jAutomaticIndex>();

    public Neo4jGraph(final String directory) {
        this(directory, null);
    }

    public Neo4jGraph(final String directory, Map<String, String> configuration) {
        this.directory = directory;
        try {
            if (null != configuration)
                this.neo4j = new EmbeddedGraphDatabase(this.directory, configuration);
            else
                this.neo4j = new EmbeddedGraphDatabase(this.directory);

            this.createIndex(Index.VERTICES, Neo4jVertex.class, Index.Type.AUTOMATIC);
            this.createIndex(Index.EDGES, Neo4jEdge.class, Index.Type.AUTOMATIC);

        } catch (Exception e) {
            if (this.neo4j != null)
                this.neo4j.shutdown();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Neo4jGraph(GraphDatabaseService neo4j) {
        this.neo4j = neo4j;
    }

    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Index.Type type) {
        Neo4jIndex index;
        if (type == Index.Type.MANUAL) {
            index = new Neo4jIndex(indexName, indexClass, this);
        } else {
            index = new Neo4jAutomaticIndex(indexName, indexClass, null, this);
            this.autoIndices.put(index.getIndexName(), (Neo4jAutomaticIndex) index);
        }
        this.indices.put(index.getIndexName(), index);
        return index;
    }

    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass) {
        Index index = this.indices.get(indexName);
        if (null == index) {
            if (Vertex.class.isAssignableFrom(indexClass) && this.neo4j.index().existsForNodes(indexName)) {
                if (indexName.equals(Index.VERTICES)) {
                    index = new Neo4jAutomaticIndex(indexName, indexClass, null, this);
                    this.autoIndices.put(indexName, (Neo4jAutomaticIndex) index);
                } else
                    index = new Neo4jIndex(indexName, indexClass, this);
                this.indices.put(indexName, (Neo4jIndex) index);
                return (Index<T>) index;
            } else if (Edge.class.isAssignableFrom(indexClass) && this.neo4j.index().existsForRelationships(indexName)) {
                if (indexName.equals(Index.EDGES)) {
                    index = new Neo4jAutomaticIndex(indexName, indexClass, null, this);
                    this.autoIndices.put(indexName, (Neo4jAutomaticIndex) index);
                } else
                    index = new Neo4jIndex(indexName, indexClass, this);
                this.indices.put(indexName, (Neo4jIndex) index);
                return (Index<T>) index;
            } else {
                throw new RuntimeException("No such index exists: " + indexName);
            }
        } else if (indexClass.isAssignableFrom(index.getIndexClass()))
            return (Index<T>) index;
        else
            throw new RuntimeException("Can not convert " + index.getIndexClass() + " to " + indexClass);
    }

    public void dropIndex(String indexName) {
        this.autoStartTransaction();
        this.neo4j.index().forNodes(indexName).delete();
        this.neo4j.index().forRelationships(indexName).delete();
        this.autoStopTransaction(Conclusion.SUCCESS);

        this.indices.remove(indexName);
        this.autoIndices.remove(indexName);
    }

    protected Iterable<Neo4jAutomaticIndex> getAutoIndices() {
        return autoIndices.values();
    }

    public Iterable<Index<?>> getIndices() {
        List<Index<?>> list = new ArrayList<Index<?>>();
        for (Index index : this.indices.values()) {
            list.add(index);
        }
        return list;
    }


    public Vertex addVertex(final Object id) {
        this.autoStartTransaction();
        final Vertex vertex = new Neo4jVertex(this.neo4j.createNode(), this);
        this.autoStopTransaction(Conclusion.SUCCESS);
        return vertex;
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            return null;

        try {
            Long longId = Double.valueOf(id.toString()).longValue();
            Node node = this.neo4j.getNodeById(longId);
            return new Neo4jVertex(node, this);
        } catch (NotFoundException e) {
            return null;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Neo4j vertex ids must be convertible to a long value", e);
        }
    }

    public Iterable<Vertex> getVertices() {
        return new Neo4jVertexSequence(this.neo4j.getAllNodes(), this);
    }

    public Iterable<Edge> getEdges() {
        return new Neo4jGraphEdgeSequence(this.neo4j.getAllNodes(), this);
    }

    public void removeVertex(final Vertex vertex) {

        final Long id = (Long) vertex.getId();
        final Node node = this.neo4j.getNodeById(id);
        if (null != node) {
            this.autoStartTransaction();
            for (final Edge edge : vertex.getInEdges()) {
                ((Relationship) ((Neo4jEdge) edge).getRawElement()).delete();
            }
            for (final Edge edge : vertex.getOutEdges()) {
                ((Relationship) ((Neo4jEdge) edge).getRawElement()).delete();
            }
            node.delete();
            this.autoStopTransaction(Conclusion.SUCCESS);
        }
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        final Node outNode = ((Neo4jVertex) outVertex).getRawVertex();
        final Node inNode = ((Neo4jVertex) inVertex).getRawVertex();
        this.autoStartTransaction();
        final Relationship relationship = outNode.createRelationshipTo(inNode, DynamicRelationshipType.withName(label));
        this.autoStopTransaction(Conclusion.SUCCESS);
        return new Neo4jEdge(relationship, this);
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            return null;

        try {
            final Long longId = Double.valueOf(id.toString()).longValue();
            final Relationship relationship = this.neo4j.getRelationshipById(longId);
            return new Neo4jEdge(relationship, this);
        } catch (NotFoundException e) {
            return null;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Neo4j edge ids must be convertible to a long value", e);
        }
    }


    public void removeEdge(Edge edge) {
        this.autoStartTransaction();
        ((Relationship) ((Neo4jEdge) edge).getRawElement()).delete();
        this.autoStopTransaction(Conclusion.SUCCESS);
    }

    public void startTransaction() {
        if (Mode.AUTOMATIC == this.mode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);

        this.tx = this.neo4j.beginTx();
    }

    public void stopTransaction(Conclusion conclusion) {
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
    }

    public void setTransactionMode(Mode mode) {
        if (null != this.tx) {
            this.tx.success();
            this.tx.finish();
        }
        this.mode = mode;
    }

    public Mode getTransactionMode() {
        return this.mode;
    }

    public void shutdown() {
        if (Mode.AUTOMATIC == this.mode && null != this.tx) {
            try {
                this.tx.success();
                this.tx.finish();
            } catch (TransactionFailureException e) {
            }
        }
        this.neo4j.shutdown();
    }

    public void clear() {
        this.shutdown();
        deleteGraphDirectory(new File(this.directory));
        this.neo4j = new EmbeddedGraphDatabase(this.directory);
        this.removeVertex(this.getVertex(0));
        this.indices.clear();
        this.autoIndices.clear();
    }

    protected void autoStartTransaction() {
        if (getTransactionMode() == Mode.AUTOMATIC)
            this.tx = this.neo4j.beginTx();
    }

    protected void autoStopTransaction(Conclusion conclusion) {
        if (getTransactionMode() == Mode.AUTOMATIC) {
            if (conclusion == Conclusion.SUCCESS)
                this.tx.success();
            else
                this.tx.failure();
            this.tx.finish();
        }
    }

    private static void deleteGraphDirectory(final File directory) {
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    deleteGraphDirectory(file);
                }
                file.delete();
            }
        }
    }

    public GraphDatabaseService getRawGraph() {
        return this.neo4j;
    }

    public String toString() {
        return "neo4jgraph[" + this.directory + "]";
    }
}
