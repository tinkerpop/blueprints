package com.tinkerpop.blueprints.impls.datomic;

import clojure.lang.Keyword;
import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.impls.datomic.util.DatomicUtil;
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
public class DatomicGraph implements MetaGraph<Database>, KeyIndexableGraph {

    private final String graphURI;
    private final Connection connection;
    public static final String DATOMIC_ERROR_EXCEPTION_MESSAGE = "An error occured within the Datomic datastore";

    public final Object GRAPH_ELEMENT_TYPE;
    public final Object GRAPH_ELEMENT_TYPE_VERTEX;
    public final Object GRAPH_ELEMENT_TYPE_EDGE;
    public final Object GRAPH_EDGE_IN_VERTEX;
    public final Object GRAPH_EDGE_OUT_VERTEX;
    public final Object GRAPH_EDGE_LABEL;

    private final DatomicIndex vertexIndex = new DatomicIndex("vertexIndex", this, Vertex.class);
    private final DatomicIndex edgeIndex = new DatomicIndex("edgeIndex", this, Edge.class);

    private final ThreadLocal<List> tx = new ThreadLocal<List>() {
        protected List initialValue() {
            return new ArrayList();
        }
    };
    private final ThreadLocal<Long> checkpoint = new ThreadLocal<Long>() {
        protected Long initialValue() {
            return null;
        }
    };

    private static final Features FEATURES = new Features();

    static {
        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.isPersistent = false;
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

    /**
     * Construct a graph on top of com.tinkerpop.blueprints.pgm.impl.com.tinkerpop.blueprints.pgm.impls.datomic
     */
    public DatomicGraph(final String graphURI) {
        this.graphURI = graphURI;
        Peer.createDatabase(graphURI);

        this.connection = Peer.connect(graphURI);
        try {
            setupMetaModel();
            // Retrieve the relevant id for the properties (for raw index access later on)
            GRAPH_ELEMENT_TYPE = getIdForAttribute("graph.element/type");
            GRAPH_ELEMENT_TYPE_VERTEX = getIdForAttribute("graph.element.type/vertex");
            GRAPH_ELEMENT_TYPE_EDGE = getIdForAttribute("graph.element.type/edge");
            GRAPH_EDGE_IN_VERTEX = getIdForAttribute("graph.edge/inVertex");
            GRAPH_EDGE_OUT_VERTEX = getIdForAttribute("graph.edge/outVertex");
            GRAPH_EDGE_LABEL = getIdForAttribute("graph.edge/label");
        } catch (ExecutionException e) {
            throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
        } catch (InterruptedException e) {
            throw new RuntimeException(DatomicGraph.DATOMIC_ERROR_EXCEPTION_MESSAGE);
        }
    }

    public void shutdown() {
        // No actions required
    }

    public void clear() {
        Iterator<Vertex> verticesit = getVertices().iterator();
        while (verticesit.hasNext()) {
            removeVertex(verticesit.next(), false);
        }
        transact();
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();
        try {
            return new DatomicEdge(this, Long.valueOf(id.toString()).longValue());
        } catch (NumberFormatException e) {
            return null;
        } catch (RuntimeException re) {
            return null;
        }
    }

    public Iterable<Edge> getEdges() {
        Iterable<Datom> edges = getRawGraph().datoms(Database.AVET, GRAPH_ELEMENT_TYPE, GRAPH_ELEMENT_TYPE_EDGE);
        return new DatomicIterable<Edge>(edges, this, Edge.class);
    }

    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        return edgeIndex.get(key, value);
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        final DatomicEdge edge = new DatomicEdge(this);
        tx.get().add(Util.map(":db/id", edge.id,
                              ":graph.edge/label", label,
                              ":graph.edge/inVertex", inVertex.getId(),
                              ":graph.edge/outVertex", outVertex.getId()));
        transact();
        edge.setId(getRawGraph().entid(edge.uuid));
        return edge;
    }

    public void removeEdge(final Edge edge) {
        removeEdge(edge, true);
    }

    public void removeEdge(final Edge edge, boolean transact) {
        DatomicEdge theedge =  (DatomicEdge)edge;
        tx.get().add(Util.list(":db.fn/retractEntity", theedge.getId()));
        if (transact) transact();
    }

    @Override
    public Features getFeatures() {
        return FEATURES;
    }

    public Vertex addVertex(final Object id) {
        DatomicVertex vertex = new DatomicVertex(this);
        transact();
        vertex.setId(getRawGraph().entid(vertex.uuid));
        return vertex;
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            throw ExceptionFactory.vertexIdCanNotBeNull();
        try {
            final Long longId = Long.valueOf(id.toString()).longValue();
            return new DatomicVertex(this, longId);
        } catch (NumberFormatException e) {
            return null;
        } catch (RuntimeException re) {
            return null;
        }
    }

    public Iterable<Vertex> getVertices() {
        Iterable<Datom> vertices = getRawGraph().datoms(Database.AVET, this.GRAPH_ELEMENT_TYPE, this.GRAPH_ELEMENT_TYPE_VERTEX);
        return new DatomicIterable(vertices, this, Vertex.class);
    }

    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
        return vertexIndex.get(key, value);
    }

    public void removeVertex(final Vertex vertex) {
        removeVertex(vertex, true);
    }

    public void removeVertex(final Vertex vertex, boolean transact) {
        DatomicVertex thevertex =  (DatomicVertex)vertex;
        Iterator<Edge> inedgesit = thevertex.getInEdges().iterator();
        while (inedgesit.hasNext()) {
            removeEdge(inedgesit.next(), false);
        }
        Iterator<Edge> outedgesit = thevertex.getOutEdges().iterator();
        while (outedgesit.hasNext()) {
            removeEdge(outedgesit.next(), false);
        }
        tx.get().add(Util.list(":db.fn/retractEntity", thevertex.id));
        if (transact) transact();
    }

    public Database getRawGraph() {
        if (checkpoint.get() != null) {
            return connection.db().asOf(checkpoint.get());
        }
        return connection.db();
    }

    public void addToTransaction(Object o) {
        tx.get().add(o);
    }

    public void transact() {
        try {
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

    public void setCheckPoint(Date checkpoint) {
        Long transaction = 0L;
        // Retrieve the transactions
        Iterator<List<Object>> tx = (Peer.q("[:find ?tx ?when :where [?tx :db/txInstant ?when]]", connection.db())).iterator();
        while (tx.hasNext()) {
            List<Object> txobject = tx.next();
            Long transactionid = (Long)txobject.get(0);
            Date time = (Date)txobject.get(1);
            if (time.getTime() <= checkpoint.getTime()) {
                if (transaction == null) {
                    transaction = transactionid;
                }
                else {
                    if (transactionid > transaction) {
                        transaction = transactionid;
                    }
                }
            }
        }
        this.checkpoint.set(transaction);
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

        transact();
    }

    private Object getIdForAttribute(String attribute) {
        return Peer.q("[:find ?entity " +
                ":in $ ?attribute " +
                ":where [?entity :db/ident ?attribute] ] ", getRawGraph(), Keyword.intern(attribute)).iterator().next().get(0);
    }

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

}