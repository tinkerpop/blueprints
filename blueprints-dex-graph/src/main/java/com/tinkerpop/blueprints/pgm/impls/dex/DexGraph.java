package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.Parameter;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.dex.util.DexAttributes;
import com.tinkerpop.blueprints.pgm.impls.dex.util.DexTypes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Dex is a graph database developed by Sparsity Technologies.
 * Dex natively supports the property graph data model defined by Blueprints. However, there are a few peculiarities.
 * No user defined element identifiers: Dex is the gatekeeper and creator of vertex and edge identifiers.
 * Thus, when creating a new vertex or edge instance, the provided object identifier is ignored.
 * Vertices are labeled too: When adding vertices, the user can set DexGraph#LABEL to be used as the label of the vertex to be created.
 * Also, the label of a vertex (or even an element) can be retrieved through the DEXElement#LABEL_PROPERTY property.
 * DexGraph implements IndexableGraph. However, the use of indices is limited when working with Dex and is explained as follows:
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

    private com.sparsity.dex.gdb.Dex dex = null;
    private com.sparsity.dex.gdb.Database gpool = null;
    private com.sparsity.dex.gdb.Session session = null;
    private com.sparsity.dex.gdb.Graph rawGraph = null;

    /**
     * Gets the Dex raw graph.
     *
     * @return Dex raw graph.
     */
    com.sparsity.dex.gdb.Graph getRawGraph() {
        return rawGraph;
    }

    /**
     * Gets the Dex
     *
     * @return The Dex
     */
    com.sparsity.dex.gdb.Session getRawSession() {
        return session;
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
            final File dbPath = db.getParentFile();

            if (!dbPath.exists()) {
                if (!dbPath.mkdirs()) {
                    throw new RuntimeException("Could not create directory.");
                }
            }

            final boolean create = !db.exists();

            this.db = db;
            dex = new com.sparsity.dex.gdb.Dex(new com.sparsity.dex.gdb.DexConfig());
            gpool = (create ? dex.create(db.getPath(), db.getName()) : dex.open(db.getPath(), false));
            session = gpool.newSession();
            rawGraph = session.getGraph();
        } catch (Exception e) {
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

        int type = DexTypes.getTypeId(rawGraph, label);
        if (type == com.sparsity.dex.gdb.Type.InvalidType) {
            // First instance of this type, let's create it
            type = rawGraph.newNodeType(label);
        }
        assert type != com.sparsity.dex.gdb.Type.InvalidType;
        // create object instance
        long oid = rawGraph.newNode(type);
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
            throw new IllegalArgumentException("Element identifier cannot be null");
        try {
            final Long longId = Double.valueOf(id.toString()).longValue();
            final int type = rawGraph.getObjectType(longId);
            if (type != com.sparsity.dex.gdb.Type.InvalidType)
                return new DexVertex(this, longId);
            else
                return null;
        } catch (NumberFormatException e) {
            return null;
        } catch (RuntimeException re) {
            // dex throws a runtime exception => [DEX: 12] Invalid object identifier.
            return null;
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
        rawGraph.drop((Long) vertex.getId());
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Graph#getVertices()
      */
    @Override
    public Iterable<Vertex> getVertices() {
        com.sparsity.dex.gdb.Objects result = session.newObjects();
        com.sparsity.dex.gdb.TypeList tlist = rawGraph.findNodeTypes();
        for (Integer type : tlist) {
            com.sparsity.dex.gdb.Objects objs = rawGraph.select(type);
            result.union(objs);
            objs.close();
        }
        tlist = null;
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
        int type = DexTypes.getTypeId(rawGraph, label);
        if (type == com.sparsity.dex.gdb.Type.InvalidType) {
            // First instance of this type, let's create it
            type = rawGraph.newEdgeType(label, true, true);
        }
        assert type != com.sparsity.dex.gdb.Type.InvalidType;
        // create object instance
        assert outVertex instanceof DexVertex && inVertex instanceof DexVertex;
        long oid = rawGraph.newEdge(type, (Long) outVertex.getId(), (Long) inVertex.getId());
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
            throw new IllegalArgumentException("Element identifier cannot be null");
        try {
            Long longId = Double.valueOf(id.toString()).longValue();
            int type = rawGraph.getObjectType(longId);
            if (type != com.sparsity.dex.gdb.Type.InvalidType)
                return new DexEdge(this, longId);
            else
                return null;
        } catch (NumberFormatException e) {
            return null;
        } catch (RuntimeException re) {
            // dex throws an runtime exception => [DEX: 12] Invalid object identifier.
            return null;
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
        rawGraph.drop((Long) edge.getId());
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Graph#getEdges()
      */
    @Override
    public Iterable<Edge> getEdges() {
        com.sparsity.dex.gdb.Objects result = session.newObjects();
        com.sparsity.dex.gdb.TypeList tlist = rawGraph.findEdgeTypes();
        for (Integer type : tlist) {
            com.sparsity.dex.gdb.Objects objs = rawGraph.select(type);
            result.union(objs);
            objs.close();
        }
        tlist = null;
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

        com.sparsity.dex.gdb.TypeList tlist = rawGraph.findEdgeTypes();
        for (Integer etype : tlist) {
            com.sparsity.dex.gdb.AttributeList alist = rawGraph.findAttributes(etype);
            for (Integer attr : alist) {
                rawGraph.removeAttribute(attr);
            }
            alist = null;
            rawGraph.removeType(etype);
        }
        tlist = null;
        tlist = rawGraph.findNodeTypes();
        for (Integer ntype : tlist) {
            com.sparsity.dex.gdb.AttributeList alist = rawGraph.findAttributes(ntype);
            for (Integer attr : alist) {
                rawGraph.removeAttribute(attr);
            }
            alist = null;
            rawGraph.removeType(ntype);
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

        rawGraph = null;
        session.close();
        gpool.close();
        dex.close();

        DexAttributes.clear();
        DexTypes.clear();
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this, db.getPath());
    }

    /*
    * (non-Javadoc)
    *
    * @see
    * com.tinkerpop.blueprints.pgm.IndexableGraph#createManualIndex(java.lang
    * .String, java.lang.Class, Parameter...)
    */
    @Override
    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        throw new UnsupportedOperationException();
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * com.tinkerpop.blueprints.pgm.IndexableGraph#createAutomaticIndex(java
      * .lang.String, java.lang.Class, java.util.Set, Parameter...)
      */
    @Override
    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> indexKeys, final Parameter... indexParameters) {
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
        if (type == com.sparsity.dex.gdb.Type.InvalidType) {
            throw new IllegalArgumentException();
        }
        com.sparsity.dex.gdb.Type tdata = DexTypes.getTypeData(getRawGraph(), indexName);
        Index<T> index = null;
        if (tdata.getObjectType() == com.sparsity.dex.gdb.ObjectType.Node) {
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
        com.sparsity.dex.gdb.TypeList tlist = getRawGraph().findNodeTypes();
        for (Integer ntype : tlist) {
            ret.add(new DexAutomaticIndex<Vertex>(this, Vertex.class, ntype));
        }
        tlist = null;
        tlist = getRawGraph().findEdgeTypes();
        for (Integer etype : tlist) {
            ret.add(new DexAutomaticIndex<Edge>(this, Edge.class, etype));
        }
        tlist = null;
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
