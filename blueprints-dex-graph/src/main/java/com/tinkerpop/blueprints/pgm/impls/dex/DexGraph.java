package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.dex.util.DexAttributes;
import com.tinkerpop.blueprints.pgm.impls.dex.util.DexTypes;
import edu.upc.dama.dex.core.DEX;
import edu.upc.dama.dex.core.Objects;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * DEX is a graph database developed by Sparsity Technologies.
 * DEX natively supports the property graph data model defined by Blueprints. However, there are a few peculiarities.
 * No user defined element identifiers: DEX is the gatekeeper and creator of vertex and edge identifiers.
 * Thus, when creating a new vertex or edge instance, the provided object identifier is ignored.
 * Vertices are labeled too: When adding vertices, the user can set DEXGraph#LABEL to be used as the label of the vertex to be created.
 * Also, the label of a vertex (or even an element) can be retrieved through the DEXElement#LABEL_PROPERTY property.
 * DexGraph implements IndexableGraph. However, the use of indices is limited when working with DEX and is explained as follows:
 * There is no support to create indices. By default, there is an AutomaticIndex for each existing label which corresponds to the name of the index.
 * Also, each index contains a key for each existing property.
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity Technologies</a>
 */
public class DexGraph implements IndexableGraph {

    /**
     * Default Vertex label. Just used when invoked addVertex with a null parameter.
     */
    public static final String DEFAULT_DEX_VERTEX_LABEL = "?DEFAULT_DEX_VERTEX_LABEL?";

    /**
     * Vertex label used at {@link #addVertex(Object)} method.
     */
    public static String LABEL = DEFAULT_DEX_VERTEX_LABEL;

    /**
     * Database persistent file.
     */
    private File db = null;

    private edu.upc.dama.dex.core.DEX dex = null;
    private edu.upc.dama.dex.core.GraphPool gpool = null;
    private edu.upc.dama.dex.core.Session session = null;
    private edu.upc.dama.dex.core.DbGraph graph = null;

    /**
     * Gets the DEX raw graph.
     *
     * @return DEX raw graph.
     */
    edu.upc.dama.dex.core.Graph getRawGraph() {
        return graph;
    }

    /**
     * All collections are registered here to be automatically closed when the
     * database is stopped (at {@link #shutdown()}).
     */
    private List<DexIterable<? extends Element>> collections = new ArrayList<DexIterable<? extends Element>>();

    /**
     * Registers a collection.
     *
     * @param col Collection to be registered.
     */
    protected void register(final DexIterable<? extends Element> col) {
        collections.add(col);
    }

    /**
     * Unregisters a collection.
     *
     * @param col Collection to be unregistered
     */
    protected void unregister(final DexIterable<? extends Element> col) {
        collections.remove(col);
    }

    /**
     * Creates a new instance.
     *
     * @param fileName Database persistent file.
     */
    public DexGraph(final String fileName) {
        try {
            final File db = new File(fileName);
            boolean create = !db.exists();
            this.db = db;
            //DEX.Config cfg = new DEX.Config();
            //cfg.setCacheMaxSize(0); // use as much memory as possible
            //dex = new DEX(cfg);
            dex = new DEX();
            gpool = (create ? dex.create(db) : dex.open(db));
            session = gpool.newSession();
            graph = session.getDbGraph();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Adds a Vertex or retrieves an existing one.
     * <p/>
     * Since all {@link DexVertex} instances are labeled, the static field
     * {@link #LABEL} sets the label of the vertex to be created. If this is
     * null the {@link #DEFAULT_DEX_VERTEX_LABEL} is used.
     *
     * @param id In case this is an instance of Long, then it corresponds to
     *           the identifier of the instance to be retrieved. Otherwise, it
     *           is ignored.
     * @return Added or retrieved Vertex.
     * @see com.tinkerpop.blueprints.pgm.Graph#addVertex(java.lang.Object)
     */
    @Override
    public Vertex addVertex(final Object id) {
        String label = LABEL;
        if (label == null) {
            label = DEFAULT_DEX_VERTEX_LABEL;
        }

        int type = DexTypes.getTypeId(graph, label);
        if (type == edu.upc.dama.dex.core.Graph.INVALID_TYPE) {
            // First instance of this type, let's create it
            type = graph.newNodeType(label);
        }
        assert type != edu.upc.dama.dex.core.Graph.INVALID_TYPE;
        // create object instance
        long oid = graph.newNode(type);
        return new DexVertex(this, oid);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Graph#getVertex(java.lang.Object)
      */
    @Override
    public Vertex getVertex(final Object id) {
        if (null == id)
            return null;
        try {
            Long longId = Double.valueOf(id.toString()).longValue();
            int type = graph.getType(longId);
            if (type != edu.upc.dama.dex.core.Graph.INVALID_TYPE)
                return new DexVertex(this, longId);
            else
                return null;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Dex vertex ids must be convertible to a long value", e);
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * com.tinkerpop.blueprints.pgm.Graph#removeVertex(com.tinkerpop.blueprints
      * .pgm.Vertex)
      */
    @Override
    public void removeVertex(final Vertex vertex) {
        assert vertex instanceof DexVertex;
        graph.drop((Long) vertex.getId());
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Graph#getVertices()
      */
    @Override
    public Iterable<Vertex> getVertices() {
        Objects result = new Objects(session);
        for (Integer type : graph.nodeTypes()) {
            Objects objs = graph.select(type);
            result.union(objs);
            objs.close();
        }
        Iterable<Vertex> ret = new DexIterable<Vertex>(this, result,
                Vertex.class);
        return ret;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Graph#addEdge(java.lang.Object,
      * com.tinkerpop.blueprints.pgm.Vertex, com.tinkerpop.blueprints.pgm.Vertex,
      * java.lang.String)
      */
    @Override
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        int type = DexTypes.getTypeId(graph, label);
        if (type == edu.upc.dama.dex.core.Graph.INVALID_TYPE) {
            // First instance of this type, let's create it
            type = graph.newEdgeType(label, true, true);
        }
        assert type != edu.upc.dama.dex.core.Graph.INVALID_TYPE;
        // create object instance
        assert outVertex instanceof DexVertex && inVertex instanceof DexVertex;
        long oid = graph.newEdge((Long) outVertex.getId(), (Long) inVertex.getId(), type);
        return new DexEdge(this, oid);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Graph#getEdge(java.lang.Object)
      */
    @Override
    public Edge getEdge(final Object id) {
        if (null == id)
            return null;
        try {
            Long longId = Double.valueOf(id.toString()).longValue();
            int type = graph.getType(longId);
            if (type != edu.upc.dama.dex.core.Graph.INVALID_TYPE)
                return new DexEdge(this, longId);
            else
                return null;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Dex vertex ids must be convertible to a long value", e);
        }

    }

    /*
      * (non-Javadoc)
      *
      * @see
      * com.tinkerpop.blueprints.pgm.Graph#removeEdge(com.tinkerpop.blueprints
      * .pgm.Edge)
      */
    @Override
    public void removeEdge(final Edge edge) {
        assert edge instanceof DexEdge;
        graph.drop((Long) edge.getId());
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Graph#getEdges()
      */
    @Override
    public Iterable<Edge> getEdges() {
        Objects result = new Objects(session);
        for (Integer type : graph.edgeTypes()) {
            Objects objs = graph.select(type);
            result.union(objs);
            objs.close();
        }
        return new DexIterable<Edge>(this, result, Edge.class);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Graph#clear()
      */
    @Override
    public void clear() {
        closeAllCollections();

        for (Integer etype : graph.edgeTypes()) {
            for (Long attr : graph.getAttributesFromType(etype)) {
                graph.removeAttribute(attr);
            }
            graph.removeType(etype);
        }
        for (Integer ntype : graph.nodeTypes()) {
            for (Long attr : graph.getAttributesFromType(ntype)) {
                graph.removeAttribute(attr);
            }
            graph.removeType(ntype);
        }

        DexAttributes.clear();
        DexTypes.clear();
    }

    /**
     * Closes all non-closed collections.
     */
    protected void closeAllCollections() {
        while (!collections.isEmpty()) {
            collections.remove(collections.size() - 1).close();
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Graph#shutdown()
      */
    @Override
    public void shutdown() {
        closeAllCollections();

        graph = null;
        session.close();
        gpool.close();
        dex.close();

        DexAttributes.clear();
        DexTypes.clear();
    }

    @Override
    public String toString() {
        return "dexgraph[" + db.getPath() + "]";
    }

    /*
    * (non-Javadoc)
    *
    * @see
    * com.tinkerpop.blueprints.pgm.IndexableGraph#createManualIndex(java.lang
    * .String, java.lang.Class)
    */
    @Override
    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        throw new UnsupportedOperationException();
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * com.tinkerpop.blueprints.pgm.IndexableGraph#createAutomaticIndex(java
      * .lang.String, java.lang.Class, java.util.Set)
      */
    @Override
    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> indexKeys) {
        throw new UnsupportedOperationException();
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * com.tinkerpop.blueprints.pgm.IndexableGraph#getIndex(java.lang.String,
      * java.lang.Class)
      */
    @Override
    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        if (indexName.compareTo(Index.VERTICES) == 0 || indexName.compareTo(Index.EDGES) == 0)
            return null;

        int type = DexTypes.getTypeId(getRawGraph(), indexName);
        if (type == edu.upc.dama.dex.core.Graph.INVALID_TYPE) {
            throw new IllegalArgumentException();
        }
        edu.upc.dama.dex.core.Graph.TypeData tdata = DexTypes.getTypeData(getRawGraph(), indexName);
        Index<T> index = null;
        if (tdata.isNodeType()) {
            index = (Index<T>) new DexAutomaticIndex<Vertex>(this, Vertex.class, type);
        } else {
            index = (Index<T>) new DexAutomaticIndex<Edge>(this, Edge.class, type);
        }
        return index;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.IndexableGraph#getIndices()
      */
    @Override
    public Iterable<Index<? extends Element>> getIndices() {
        List<Index<? extends Element>> ret = new ArrayList<Index<? extends Element>>();
        for (Integer ntype : getRawGraph().nodeTypes()) {
            ret.add(new DexAutomaticIndex<Vertex>(this, Vertex.class, ntype));
        }
        for (Integer etype : getRawGraph().edgeTypes()) {
            ret.add(new DexAutomaticIndex<Edge>(this, Edge.class, etype));
        }
        return ret;
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * com.tinkerpop.blueprints.pgm.IndexableGraph#dropIndex(java.lang.String)
      */
    @Override
    public void dropIndex(final String indexName) {
        throw new UnsupportedOperationException();
    }
}
