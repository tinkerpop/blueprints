package com.tinkerpop.blueprints.impls.datomic;

import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import datomic.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * A Blueprints implementation of a graph on top of Datomic
 *
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicGraph implements MetaGraph<Database>, KeyIndexableGraph, TimeAwareGraph {

    private final String graphURI;
    private final Connection connection;
    public static final String DATOMIC_ERROR_EXCEPTION_MESSAGE = "An error occured within the Datomic datastore";

    public final Object GRAPH_ELEMENT_TYPE;
    public final Object GRAPH_ELEMENT_TYPE_VERTEX;
    public final Object GRAPH_ELEMENT_TYPE_EDGE;
    public final Object GRAPH_EDGE_IN_VERTEX;
    public final Object GRAPH_EDGE_OUT_VERTEX;
    public final Object GRAPH_EDGE_LABEL;

    private final DatomicIndex vertexIndex;
    private final DatomicIndex edgeIndex;

    private final ThreadLocal<List> tx = new ThreadLocal<List>() {
        protected List initialValue() {
            return new ArrayList();
        }
    };
    private final ThreadLocal<Long> checkpointTime = new ThreadLocal<Long>() {
        protected Long initialValue() {
            return null;
        }
    };
    private final ThreadLocal<Date> transactionTime = new ThreadLocal<Date>() {
        protected Date initialValue() {
            return null;
        }
    };

    private static final Features FEATURES = new Features();

    static {
        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.isPersistent = false;
        FEATURES.isTimeAware = true;
        FEATURES.isRDFModel = false;
        FEATURES.supportsVertexIteration = true;
        FEATURES.supportsEdgeIteration = true;
        FEATURES.supportsVertexIndex = false;
        FEATURES.supportsEdgeIndex = false;
        FEATURES.ignoresSuppliedIds = true;
        FEATURES.supportsEdgeRetrieval = true;
        FEATURES.supportsVertexProperties = true;
        FEATURES.supportsEdgeProperties = true;
        FEATURES.supportsTransactions = false;
        FEATURES.supportsIndices = false;

        FEATURES.supportsSerializableObjectProperty = false;
        FEATURES.supportsBooleanProperty = true;
        FEATURES.supportsDoubleProperty = true;
        FEATURES.supportsFloatProperty = true;
        FEATURES.supportsIntegerProperty = true;
        FEATURES.supportsPrimitiveArrayProperty = false;
        FEATURES.supportsUniformListProperty = false;
        FEATURES.supportsMixedListProperty = false;
        FEATURES.supportsLongProperty = true;
        FEATURES.supportsMapProperty = false;
        FEATURES.supportsStringProperty = true;

        FEATURES.isWrapper = true;
        FEATURES.supportsKeyIndices = true;
        FEATURES.supportsVertexKeyIndex = true;
        FEATURES.supportsEdgeKeyIndex = true;
        FEATURES.supportsThreadedTransactions = false;
    }

    public DatomicGraph(final String graphURI) {
        this.graphURI = graphURI;
        Peer.createDatabase(graphURI);
        // Retrieve the connection
        this.connection = Peer.connect(graphURI);

        try {
            // Setup the meta model for the graph
            setupMetaModel();
            // Retrieve the relevant ids for the properties (for raw index access later on)
            GRAPH_ELEMENT_TYPE = DatomicUtil.getIdForAttribute(this,"graph.element/type");
            GRAPH_ELEMENT_TYPE_VERTEX = DatomicUtil.getIdForAttribute(this,"graph.element.type/vertex");
            GRAPH_ELEMENT_TYPE_EDGE = DatomicUtil.getIdForAttribute(this,"graph.element.type/edge");
            GRAPH_EDGE_IN_VERTEX = DatomicUtil.getIdForAttribute(this,"graph.edge/inVertex");
            GRAPH_EDGE_OUT_VERTEX = DatomicUtil.getIdForAttribute(this,"graph.edge/outVertex");
            GRAPH_EDGE_LABEL = DatomicUtil.getIdForAttribute(this,"graph.edge/label");
        } catch (ExecutionException e) {
            throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
        } catch (InterruptedException e) {
            throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
        }
        // Create the required indexes
        this.vertexIndex = new DatomicIndex("vertexIndex", this, null, Vertex.class);
        this.edgeIndex = new DatomicIndex("edgeIndex", this, null, Edge.class);
    }

    @Override
    public Features getFeatures() {
        return FEATURES;
    }

    @Override
    public void shutdown() {
        // No actions required
    }

    @Override
    public TimeAwareEdge getEdge(final Object id) {
        if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();
        try {
            return new DatomicEdge(this, null, Long.valueOf(id.toString()).longValue());
        } catch (NumberFormatException e) {
            return null;
        } catch (RuntimeException re) {
            return null;
        }
    }

    @Override
    public Iterable<Edge> getEdges() {
        Iterable<Datom> edges = connection.db().datoms(Database.AVET, GRAPH_ELEMENT_TYPE, GRAPH_ELEMENT_TYPE_EDGE);
        return new DatomicIterable<Edge>(edges, this, null, Edge.class);
    }

    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        return edgeIndex.get(key, value);
    }

    @Override
    public TimeAwareEdge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        // Create the new edge
        final DatomicEdge edge = new DatomicEdge(this, null);
        tx.get().add(Util.map(":db/id", edge.id,
                              ":graph.edge/label", label,
                              ":graph.edge/inVertex", inVertex.getId(),
                              ":graph.edge/outVertex", outVertex.getId()));

        // Update the transaction info of both vertices (moving up their current transaction)
        addTransactionInfo((TimeAwareVertex)inVertex, (TimeAwareVertex)outVertex);

        // Transact
        transact();

        // Set the real id on the entity
        edge.id = getRawGraph().entid(edge.uuid);
        return edge;
    }

    @Override
    public void removeEdge(final Edge edge) {
        removeEdge(edge, true);
    }

    @Override
    public TimeAwareVertex addVertex(final Object id) {
        // Create the new vertex
        DatomicVertex vertex = new DatomicVertex(this, null);

        // Transact
        transact();

        // Set the real id on the entity
        vertex.id = getRawGraph().entid(vertex.uuid);

        return vertex;
    }

    @Override
    public TimeAwareVertex getVertex(final Object id) {
        if (null == id)
            throw ExceptionFactory.vertexIdCanNotBeNull();
        try {
            final Long longId = Long.valueOf(id.toString()).longValue();
            return new DatomicVertex(this, null, longId);
        } catch (NumberFormatException e) {
            return null;
        } catch (RuntimeException re) {
            return null;
        }
    }

    @Override
    public Iterable<Vertex> getVertices() {
        Iterable<Datom> vertices = getRawGraph(new Date(Long.MAX_VALUE)).datoms(Database.AVET, this.GRAPH_ELEMENT_TYPE, this.GRAPH_ELEMENT_TYPE_VERTEX);
        return new DatomicIterable<Vertex>(vertices, this, null, Vertex.class);
    }

    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
        return vertexIndex.get(key, value);
    }

    @Override
    public void removeVertex(final Vertex vertex) {
        removeVertex(vertex, true);
    }

    @Override
    public Database getRawGraph() {
        if (checkpointTime.get() != null) {
            return getRawGraph(checkpointTime.get());
        }
        return connection.db();
    }

    @Override
    public void setCheckpointTime(Date checkpoint) {
        Long transaction = null;
        // Retrieve the transactions
        Iterator<List<Object>> tx = (Peer.q("[:find ?tx ?when " +
                                           ":where [?tx :db/txInstant ?when]]", connection.db().asOf(checkpoint))).iterator();
        while (tx.hasNext()) {
            List<Object> txobject = tx.next();
            Long transactionid = (Long)txobject.get(0);
            if (transaction == null) {
                transaction = transactionid;
            }
            else {
                if (transactionid > transaction) {
                    transaction = transactionid;
                }
            }
        }
        this.checkpointTime.set(transaction);
    }

    @Override
    public void setTransactionTime(Date transactionTime) {
        this.transactionTime.set(transactionTime);
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this, graphURI);
    }

    @Override
    public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {
        DatomicUtil.removeAttributeIndex(key, elementClass, this);
    }

    @Override
    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass) {
        DatomicUtil.createAttributeIndex(key, elementClass, this);
    }

    @Override
    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        return DatomicUtil.getIndexedAttributes(elementClass, this);
    }

    public Date getTransactionTime() {
        return transactionTime.get();
    }

    public void clear() {
        Iterator<Vertex> verticesit = getVertices().iterator();
        while (verticesit.hasNext()) {
            removeVertex(verticesit.next(), false);
        }
        transact();
    }

    public Database getRawGraph(Object transaction) {
        if (transaction == null) {
            return connection.db();
        }
        return connection.db().asOf(transaction);
    }

    public void addToTransaction(Object o) {
        tx.get().add(o);
    }

    public void transact() {
        try {
            // We are adding a fact which dates back to the past. Add the required meta data on the transaction
            if (transactionTime.get() != null) {
                tx.get().add(datomic.Util.map(":db/id", datomic.Peer.tempid(":db.part/tx"), ":db/txInstant", transactionTime.get()));
            }
            connection.transact(tx.get()).get();
            tx.get().clear();
        } catch (InterruptedException e) {
            tx.get().clear();
            throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
        } catch (ExecutionException e) {
            tx.get().clear();
            throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    // Ensures that add-transaction-info database function is called during the transaction execution. This will setup the linked list of transactions
    public void addTransactionInfo(TimeAwareElement... elements) {
        for (TimeAwareElement element : elements) {
            tx.get().add(Util.list(":add-transaction-info", element.getId(), element.getTimeId()));
        }
    }

    private void removeEdge(final Edge edge, boolean transact) {
        // Retract the edge element in its totality
        DatomicEdge theEdge =  (DatomicEdge)edge;
        tx.get().add(Util.list(":db.fn/retractEntity", theEdge.getId()));

        // Get the in and out vertex (as their version also needs to be updated)
        DatomicVertex inVertex = (DatomicVertex)theEdge.getVertex(Direction.IN);
        DatomicVertex outVertex = (DatomicVertex)theEdge.getVertex(Direction.OUT);

        // Update the transaction info of the edge and both vertices (moving up their current transaction)
        addTransactionInfo(theEdge, inVertex, outVertex);

        // We need to commit
        if (transact) {
            transact();
        }
    }

    private void removeVertex(Vertex vertex, boolean transact) {
        // Retrieve all edges associated with this vertex and remove them one bye one
        Iterator<Edge> edgesIt = vertex.getEdges(Direction.BOTH).iterator();
        while (edgesIt.hasNext()) {
            removeEdge(edgesIt.next(), false);
        }
        // Retract the vertex element in its totality
        tx.get().add(Util.list(":db.fn/retractEntity", vertex.getId()));

        // Update the transaction info of the vertex
        addTransactionInfo((DatomicVertex)vertex);

        // We need to commit
        if (transact) {
            transact();
        }
    }

    // Setup of the various attribute types required for DatomicGraph
    private void setupMetaModel() throws ExecutionException, InterruptedException {

        // The graph element type attribute
        tx.get().add(Util.map(":db/id", Peer.tempid(":db.part/db"),
                              ":db/ident", ":graph.element/type",
                              ":db/valueType", ":db.type/ref",
                              ":db/cardinality", ":db.cardinality/one",
                              ":db/doc", "A graph element type",
                              ":db/index", true,
                              ":db.install/_attribute", ":db.part/db"));

        // The graph vertex element type
        tx.get().add(Util.map(":db/id", Peer.tempid(":db.part/user"),
                              ":db/ident", ":graph.element.type/vertex"));

        // The graph edge element type
        tx.get().add(Util.map(":db/id", Peer.tempid(":db.part/user"),
                              ":db/ident", ":graph.element.type/edge"));

        // The incoming vertex of an edge attribute
        tx.get().add(Util.map(":db/id", Peer.tempid(":db.part/db"),
                              ":db/ident", ":graph.edge/inVertex",
                              ":db/valueType", ":db.type/ref",
                              ":db/cardinality", ":db.cardinality/one",
                              ":db/doc", "The incoming vertex of an edge",
                              ":db/index", true,
                              ":db.install/_attribute", ":db.part/db"));

        // The outgoing vertex of an edge attribute
        tx.get().add(Util.map(":db/id", Peer.tempid(":db.part/db"),
                              ":db/ident", ":graph.edge/outVertex",
                              ":db/valueType", ":db.type/ref",
                              ":db/cardinality", ":db.cardinality/one",
                              ":db/doc", "The outgoing vertex of an edge",
                              ":db/index", true,
                              ":db.install/_attribute", ":db.part/db"));

        // The outgoing vertex of an edge attribute
        tx.get().add(Util.map(":db/id", Peer.tempid(":db.part/db"),
                              ":db/ident", ":graph.edge/label",
                              ":db/valueType", ":db.type/string",
                              ":db/cardinality", ":db.cardinality/one",
                              ":db/doc", "The label of a vertex",
                              ":db/index", true,
                              ":db.install/_attribute", ":db.part/db"));

        // The previous transaction through which the entity (vertex or edge) was changed
        tx.get().add(Util.map(":db/id", Peer.tempid(":db.part/db"),
                              ":db/ident", ":graph.element/previousTransaction",
                              ":db/valueType", ":db.type/ref",
                              ":db/cardinality", ":db.cardinality/many",
                              ":db/doc", "The previous transactions of the elements that wer changed",
                              ":db/index", true,
                              ":db.install/_attribute", ":db.part/db"));

        tx.get().add(Util.map(":db/id", Peer.tempid(":db.part/db"),
                              ":db/ident", ":graph.element/previousTransaction/elementId",
                              ":db/valueType", ":db.type/ref",
                              ":db/cardinality", ":db.cardinality/one",
                              ":db/doc", "The element id of the entity that was part of the previous transaction",
                              ":db/index", true,
                              ":db.install/_attribute", ":db.part/db"));

        tx.get().add(Util.map(":db/id", Peer.tempid(":db.part/db"),
                              ":db/ident", ":graph.element/previousTransaction/transactionId",
                              ":db/valueType", ":db.type/ref",
                              ":db/cardinality", ":db.cardinality/one",
                              ":db/doc", "The transaction id for the entity that was part of the previous transaction",
                              ":db/index", true,
                              ":db.install/_attribute", ":db.part/db"));

        String addTransactionInfoCode = "Object transactInfoId = datomic.Peer.tempid(\":db.part/user\");\n" +
                                        "return datomic.Util.list(datomic.Util.list(\":db/add\", transactInfoId, \":graph.element/previousTransaction/transactionId\", lastTransaction), datomic.Util.list(\":db/add\", transactInfoId, \":graph.element/previousTransaction/elementId\", id), datomic.Util.list(\":db/add\", datomic.Peer.tempid(\":db.part/tx\"), \":graph.element/previousTransaction\", transactInfoId));\n";

        // Database function that retrieves the previous transaction and sets the new one
        tx.get().add(Util.map(":db/id", Peer.tempid(":db.part/user"),
                              ":db/ident", ":add-transaction-info",
                              ":db/fn", Peer.function(Util.map("lang", "java",
                                                      "params", Util.list("db", "id", "lastTransaction"),
                                                      "code", addTransactionInfoCode))));

        // Add new graph partition
        tx.get().add(Util.map(":db/id", Peer.tempid(":db.part/db"),
                              ":db/ident", ":graph",
                              ":db.install/_partition", ":db.part/db"));

        tx.get().add(datomic.Util.map(":db/id", datomic.Peer.tempid(":db.part/tx"), ":db/txInstant", new Date(0)));
        connection.transact(tx.get()).get();
        tx.get().clear();
    }

}