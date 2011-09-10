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
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        if (this.getIndexNames().contains(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        return new Neo4jIndex(indexName, indexClass, this);
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, Set<String> keys) {
        if (this.getIndexNames().contains(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        return new Neo4jAutomaticIndex(indexName, indexClass, this, keys);
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        if ((null == indexClass || Vertex.class.isAssignableFrom(indexClass)) && this.rawGraph.index().existsForNodes(indexName)) {
            org.neo4j.graphdb.index.Index<Node> index = this.rawGraph.index().forNodes(indexName);
            String storedType = rawGraph.index().getConfiguration(index).get(Neo4jTokens.BLUEPRINTS_TYPE);
            if (null != storedType && storedType.equals(Index.Type.AUTOMATIC.toString())) {
                return new Neo4jAutomaticIndex(indexName, Neo4jVertex.class, this, index);
            } else {
                return new Neo4jIndex(indexName, Neo4jVertex.class, this, index);
            }
        } else if ((null == indexClass || Edge.class.isAssignableFrom(indexClass)) && this.rawGraph.index().existsForRelationships(indexName)) {
            org.neo4j.graphdb.index.Index<Relationship> index = this.rawGraph.index().forRelationships(indexName);
            String storedType = rawGraph.index().getConfiguration(index).get(Neo4jTokens.BLUEPRINTS_TYPE);
            if (null != storedType && storedType.equals(Index.Type.AUTOMATIC.toString())) {
                return new Neo4jAutomaticIndex(indexName, Neo4jEdge.class, this, index);
            } else {
                return new Neo4jIndex(indexName, Neo4jEdge.class, this, index);
            }
        }
        return null;
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
    }


    public Iterable<Index<? extends Element>> getIndices() {
        final List<Index<? extends Element>> indices = new ArrayList<Index<? extends Element>>();
        for (final String indexName : this.getIndexNames()) {
            indices.add(this.getIndex(indexName, null));
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
        if (tx.get() == null) {
            txCounter.set(0);
            tx.set(this.rawGraph.beginTx());
        } else
            throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (null == tx.get())
            throw new RuntimeException("There is no active transaction to stop");

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
        if (this.txBuffer.get() > 0) {
            if (tx.get() == null) {
                tx.set(this.rawGraph.beginTx());
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
            } else if (this.txBuffer.get() == 0 || (this.txCounter.get() % this.txBuffer.get() == 0)) {
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
        return "neo4jgraph[" + this.rawGraph + "]";
    }

    protected Set<String> getIndexNames() {
        final Set<String> indexNames = new HashSet<String>();
        indexNames.addAll(Arrays.asList(rawGraph.index().nodeIndexNames()));
        indexNames.addAll(Arrays.asList(rawGraph.index().relationshipIndexNames()));
        return indexNames;
    }

    protected Iterable<Neo4jAutomaticIndex> getAutomaticIndices(final Class indexClass) {
        final List<Neo4jAutomaticIndex> indices = new ArrayList<Neo4jAutomaticIndex>();
        for (final String indexName : this.getIndexNames()) {
            final Index index = this.getIndex(indexName, indexClass);
            if (null != index && index instanceof Neo4jAutomaticIndex) {
                indices.add((Neo4jAutomaticIndex) index);
            }
        }
        return indices;
    }
}
