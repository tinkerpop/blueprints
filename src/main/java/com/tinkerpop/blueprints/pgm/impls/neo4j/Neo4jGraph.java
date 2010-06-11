package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jGraphEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jVertexSequence;
import org.neo4j.graphdb.*;
//import org.neo4j.index.Isolation;
import org.neo4j.index.IndexService;
import org.neo4j.index.lucene.LuceneIndexService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jGraph implements Graph {

    private GraphDatabaseService neo4j;
    private String directory;
    private Neo4jIndex index;
    private Transaction tx;
    private boolean automaticTransactions = true;
    private IndexService indexService;

    public Neo4jGraph(final String directory) {
        this(directory, null);
    }

    public GraphDatabaseService getGraphDatabaseService() {
        return this.neo4j;
    }
    
    public IndexService getIndexService() {
        return indexService;
    }

    public Neo4jGraph(final String directory, Map<String, String> configuration) {
        this.directory = directory;
        if (null != configuration)
            this.neo4j = new EmbeddedGraphDatabase(this.directory, configuration);
        else
            this.neo4j = new EmbeddedGraphDatabase(this.directory);
        indexService = new LuceneIndexService(neo4j);
        //indexService.setIsolation(Isolation.SAME_TX);
        this.index = new Neo4jIndex(indexService, this);
        if (this.automaticTransactions) {
            this.tx = neo4j.beginTx();
        }
    }

    public Index getIndex() {
        return this.index;
    }

    public Vertex addVertex(final Object id) {
        Vertex vertex = new Neo4jVertex(neo4j.createNode(), this);
        this.stopStartTransaction();
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
            throw new RuntimeException("Neo vertex ids must be convertible to a long value");
        }
    }

    public Iterable<Vertex> getVertices() {
        return new Neo4jVertexSequence(this.neo4j.getAllNodes(), this);
    }

    public Iterable<Edge> getEdges() {
        return new Neo4jGraphEdgeSequence(this.neo4j.getAllNodes(), this);
    }

    public void removeVertex(final Vertex vertex) {

        Long id = (Long) vertex.getId();
        Node node = neo4j.getNodeById(id);
        if (null != node) {
            for (String key : vertex.getPropertyKeys()) {
                this.index.remove(key, vertex.getProperty(key), vertex);
            }
            for (Edge edge : vertex.getInEdges()) {
                this.removeEdge(edge);
            }
            for (Edge edge : vertex.getOutEdges()) {
                this.removeEdge(edge);
            }
            node.delete();
            this.stopStartTransaction();
        }
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        Node outNode = (Node) ((Neo4jVertex) outVertex).getRawElement();
        Node inNode = (Node) ((Neo4jVertex) inVertex).getRawElement();
        Relationship relationship = outNode.createRelationshipTo(inNode, DynamicRelationshipType.withName(label));
        this.stopStartTransaction();
        return new Neo4jEdge(relationship, this);
    }

    public void removeEdge(Edge edge) {
        ((Relationship) ((Neo4jEdge) edge).getRawElement()).delete();
        this.stopStartTransaction();
    }

    protected void stopStartTransaction() {
        if (this.automaticTransactions) {
            if (null != tx) {
                this.tx.success();
                this.tx.finish();
                this.tx = neo4j.beginTx();
            } else {
                throw new RuntimeException("There is no active transaction to stop");
            }
        }
    }

    public void startTransaction() {
        if (this.automaticTransactions)
            throw new RuntimeException("Turn off automatic transactions to use manual transaction handling");

        this.tx = neo4j.beginTx();
    }

    public void stopTransaction(boolean success) {
        if (this.automaticTransactions)
            throw new RuntimeException("Turn off automatic transactions to use manual transaction handling");

        if (success) {
            this.tx.success();
        } else {
            this.tx.failure();
        }
        this.tx.finish();
    }

    public void setAutoTransactions(boolean automatic) {
        this.automaticTransactions = automatic;
        if (null != this.tx) {
            this.tx.success();
            this.tx.finish();
        }
    }

    public void shutdown() {
        if (this.automaticTransactions) {
            try {
                this.tx.success();
                this.tx.finish();
            } catch (TransactionFailureException e) {
            }
        }
        this.neo4j.shutdown();
        this.index.shutdown();

    }

    public void clear() {
        this.shutdown();
        deleteGraphDirectory(new File(this.directory));
        this.neo4j = new EmbeddedGraphDatabase(this.directory);
        LuceneIndexService indexService = new LuceneIndexService(neo4j);
        //indexService.setIsolation(Isolation.SAME_TX);
        this.index = new Neo4jIndex(indexService, this);
        this.tx = neo4j.beginTx();
        this.removeVertex(this.getVertex(0));
        this.stopStartTransaction();
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


    public String toString() {
        return "neo4jgraph[" + this.directory + "]";
    }
}
