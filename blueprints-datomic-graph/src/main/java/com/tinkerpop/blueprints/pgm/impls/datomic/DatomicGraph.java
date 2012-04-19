package com.tinkerpop.blueprints.pgm.impls.datomic;

import clojure.lang.Keyword;
import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.Parameter;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.datomic.util.DatomicEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.datomic.util.DatomicVertexSequence;
import datomic.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * A Blueprints implementation of a graph on top of Datomic
 *
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicGraph implements IndexableGraph, WrappableGraph<Database> {

    private final String graphURI;
    private final Connection connection;
    public static final String DATOMIC_ERROR_EXCEPTION_MESSAGE = "An error occured within the Datomic datastore";
    private Map<String, DatomicAutomaticIndex> autoIndices = new HashMap<String, DatomicAutomaticIndex>();

    public final Object GRAPH_ELEMENT_TYPE;
    public final Object GRAPH_ELEMENT_TYPE_VERTEX;
    public final Object GRAPH_ELEMENT_TYPE_EDGE;
    public final Object GRAPH_EDGE_IN_VERTEX;
    public final Object GRAPH_EDGE_OUT_VERTEX;
    public final Object GRAPH_EDGE_LABEL;

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

    /**
     * Construct a graph on top of com.tinkerpop.blueprints.pgm.impl.com.tinkerpop.blueprints.pgm.impls.datomic
     */
    public DatomicGraph(final String graphURI) {
        this.graphURI = graphURI;
        Peer.createDatabase(graphURI);
        this.connection = Peer.connect(graphURI);
        this.autoIndices.put(Index.VERTICES, new DatomicAutomaticIndex(Index.VERTICES, this, DatomicVertex.class));
        this.autoIndices.put(Index.EDGES, new DatomicAutomaticIndex(Index.EDGES, this, DatomicEdge.class));
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
            throw new IllegalArgumentException("Element identifier cannot be null");
        try {
            return new DatomicEdge(this, id);
        } catch (Exception e) {
            return null;
        }
    }

    public Iterable<Edge> getEdges() {
        Iterator<Datom> edgesit = getRawGraph().datoms(Database.AVET, GRAPH_ELEMENT_TYPE, GRAPH_ELEMENT_TYPE_EDGE).iterator();
        List<Object> edges = new ArrayList<Object>();
        while (edgesit.hasNext()) {
            edges.add(edgesit.next().e());
        }
        return new DatomicEdgeSequence(edges, this);
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
        tx.get().add(Util.list(":db.fn/retractEntity", theedge.id));
        if (transact) transact();
    }

    public Vertex addVertex(final Object id) {
        DatomicVertex vertex = new DatomicVertex(this);
        transact();
        vertex.setId(getRawGraph().entid(vertex.uuid));
        return vertex;
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            throw new IllegalArgumentException("Element identifier cannot be null");
        try {
            return new DatomicVertex(this, id);
        } catch (Exception e) {
            return null;
        }
    }

    public Iterable<Vertex> getVertices() {
        Iterator<Datom> verticesit = getRawGraph().datoms(Database.AVET, this.GRAPH_ELEMENT_TYPE, this.GRAPH_ELEMENT_TYPE_VERTEX).iterator();
        List<Object> vertices = new ArrayList<Object>();
        while (verticesit.hasNext()) {
            vertices.add(verticesit.next().e());
        }
        return new DatomicVertexSequence(vertices, this);
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

    public <T extends Element> Index<T> createManualIndex(String indexName, Class<T> indexClass, Parameter... indexParameters) {
        throw new UnsupportedOperationException();
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(String indexName, Class<T> indexClass, Set<String> indexKeys, Parameter... indexParameters) {
        if (this.autoIndices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);
        final DatomicAutomaticIndex<T> newindex = new DatomicAutomaticIndex(indexName, this, indexClass, indexKeys);
        this.autoIndices.put(indexName, newindex);
        return newindex;
    }

    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass) {
        Index index = this.autoIndices.get(indexName);
        if (null == index)
            return null;
        if (!indexClass.isAssignableFrom(index.getIndexClass()))
            throw new RuntimeException(indexClass + " is not assignable from " + index.getIndexClass());
        else
            return (Index<T>) index;
    }

    public Iterable<Index<? extends Element>> getIndices() {
        final List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
        for (Index index : autoIndices.values()) {
            list.add(index);
        }
        return list;
    }

    public void dropIndex(String indexName) {
         this.autoIndices.remove(indexName);
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

}
