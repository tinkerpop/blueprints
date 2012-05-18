package com.tinkerpop.blueprints.impls.dex;

import com.sparsity.dex.gdb.AttributeKind;
import com.sparsity.dex.gdb.ObjectType;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.MetaGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.StringFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
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
public class DexGraph implements MetaGraph<com.sparsity.dex.gdb.Graph>, KeyIndexableGraph {

    /**
     * Default Vertex label. Just used when invoked addVertex with a null parameter.
     */
    public static final String DEFAULT_DEX_VERTEX_LABEL = "NO_LABEL";

    /**
     * Database persistent file.
     */
    private File db = null;

    private com.sparsity.dex.gdb.Dex dex = null;
    private com.sparsity.dex.gdb.Database gpool = null;
    private com.sparsity.dex.gdb.Session session = null;
    private com.sparsity.dex.gdb.Graph rawGraph = null;

    private static final Features FEATURES = new Features();

    static {
        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.isPersistent = true;
        FEATURES.isRDFModel = false;
        FEATURES.supportsVertexIteration = true;
        FEATURES.supportsEdgeIteration = true;
        FEATURES.supportsVertexIndex = false;
        FEATURES.supportsEdgeIndex = false;
        FEATURES.ignoresSuppliedIds = true;
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
        FEATURES.supportsLongProperty = false;
        FEATURES.supportsMapProperty = false;
        FEATURES.supportsStringProperty = true;

        FEATURES.isWrapper = false;
        FEATURES.supportsKeyIndices = true;
        FEATURES.supportsVertexKeyIndex = true;
        FEATURES.supportsEdgeKeyIndex = true;
        FEATURES.supportsThreadedTransactions = false;
    }

    /**
     * Gets the Dex raw graph.
     *
     * @return Dex raw graph.
     */
    public com.sparsity.dex.gdb.Graph getRawGraph() {
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
     * All iterables are registered here to be automatically closed when the
     * database is stopped (at {@link #shutdown()}).
     */
    private List<DexIterable<? extends Element>> iterables = new ArrayList<DexIterable<? extends Element>>();


    /**
     * Registers a collection.
     *
     * @param col Collection to be registered.
     */
    protected void register(final DexIterable<? extends Element> col) {
        iterables.add(col);
    }

    /**
     * Unregisters a collection.
     *
     * @param col Collection to be unregistered
     */
    protected void unregister(final DexIterable<? extends Element> col) {
        iterables.remove(col);
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
                    throw new RuntimeException("Could not create directory");
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
     *
     * @param id In case this is <code>null</code> it adds a new vertex with
     *           the default label ({@link #DEFAULT_DEX_VERTEX_LABEL}). In case
     *           this is a string, it will be the label for the new vertex.
     *           Otherwise, it will try to retrieve an already existing vertex
     *           using the given id somehow.
     * @return Added or retrieved Vertex.
     * @see com.tinkerpop.blueprints.Graph#addVertex(java.lang.Object)
     */
    @Override
    public Vertex addVertex(final Object id) {
        if (id == null || id instanceof String) {
            String label = (id == null ? DEFAULT_DEX_VERTEX_LABEL : (String) id);
            int type = this.getRawGraph().findType(label);
            if (type == com.sparsity.dex.gdb.Type.InvalidType) {
                // First instance of this type, let's create it
                type = rawGraph.newNodeType(label);
            }
            assert type != com.sparsity.dex.gdb.Type.InvalidType;
            // create object instance
            long oid = rawGraph.newNode(type);
            return new DexVertex(this, oid);
        } else {
            return getVertex(id);
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Graph#getVertex(java.lang.Object)
      */
    @Override
    public Vertex getVertex(final Object id) {
        if (null == id)
            throw ExceptionFactory.vertexIdCanNotBeNull();
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
      * com.tinkerpop.blueprints.Graph#removeVertex(com.tinkerpop.blueprints
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
      * @see com.tinkerpop.blueprints.Graph#getVertices()
      */
    @Override
    public Iterable<Vertex> getVertices() {
        com.sparsity.dex.gdb.TypeList tlist = rawGraph.findNodeTypes();
        List<Iterable<Vertex>> vertices = new ArrayList<Iterable<Vertex>>();
        for (Integer type : tlist) {
            com.sparsity.dex.gdb.Objects objs = rawGraph.select(type);
            vertices.add(new DexIterable<Vertex>(this, objs, Vertex.class));
        }
        tlist = null;
        return new MultiIterable<Vertex>(vertices);
    }

    public Iterable<Vertex> getVertices(final String key, final Object value) {
        if (key.compareTo(StringFactory.LABEL) == 0) {
            int type = this.getRawGraph().findType(value.toString());
            if (type != com.sparsity.dex.gdb.Type.InvalidType) {
                com.sparsity.dex.gdb.Type tdata = this.getRawGraph().getType(type);
                if (tdata.getObjectType() == ObjectType.Edge) {
                    com.sparsity.dex.gdb.Objects objs = this.getRawGraph().select(type);
                    return new DexIterable<Vertex>(this, objs, Vertex.class);
                }
            }
            return null;
        }

        com.sparsity.dex.gdb.TypeList tlist = this.getRawGraph().findNodeTypes();
        List<Iterable<Vertex>> vertices = new ArrayList<Iterable<Vertex>>();
        for (Integer type : tlist) {
            int attr = this.getRawGraph().findAttribute(type, key);
            if (com.sparsity.dex.gdb.Attribute.InvalidAttribute != attr) {
                com.sparsity.dex.gdb.Attribute adata = this.getRawGraph().getAttribute(attr);
                if (adata.getKind() == AttributeKind.Basic) {
                    // It is better to index the attribute than performing a "table" scan
                    this.getRawGraph().indexAttribute(attr, com.sparsity.dex.gdb.AttributeKind.Indexed);
                }
                vertices.add(new DexIterable<Vertex>(this, this.rawGet(adata, value), Vertex.class));
            }
        }
        tlist = null;

        if (vertices.size() > 0) return new MultiIterable<Vertex>(vertices);
        else throw new IllegalArgumentException("The given attribute '" + key + "' does not exist");
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Graph#addEdge(java.lang.Object,
      * com.tinkerpop.blueprints.Vertex, com.tinkerpop.blueprints.Vertex,
      * java.lang.String)
      */
    @Override
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        int type = this.getRawGraph().findType(label);
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
      * @see com.tinkerpop.blueprints.Graph#getEdge(java.lang.Object)
      */
    @Override
    public Edge getEdge(final Object id) {
        if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();
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
      * com.tinkerpop.blueprints.Graph#removeEdge(com.tinkerpop.blueprints
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
      * @see com.tinkerpop.blueprints.Graph#getEdges()
      */
    @Override
    public Iterable<Edge> getEdges() {
        com.sparsity.dex.gdb.TypeList tlist = rawGraph.findEdgeTypes();
        List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
        for (Integer type : tlist) {
            com.sparsity.dex.gdb.Objects objs = rawGraph.select(type);
            edges.add(new DexIterable<Edge>(this, objs, Edge.class));
        }
        tlist = null;
        return new MultiIterable<Edge>(edges);
    }

    public Iterable<Edge> getEdges(final String key, final Object value) {
        if (key.compareTo(StringFactory.LABEL) == 0) {
            int type = this.getRawGraph().findType(value.toString());
            if (type != com.sparsity.dex.gdb.Type.InvalidType) {
                com.sparsity.dex.gdb.Type tdata = this.getRawGraph().getType(type);
                if (tdata.getObjectType() == ObjectType.Edge) {
                    com.sparsity.dex.gdb.Objects objs = this.getRawGraph().select(type);
                    return new DexIterable<Edge>(this, objs, Edge.class);
                }
            }
            return null;
        }

        com.sparsity.dex.gdb.TypeList tlist = this.getRawGraph().findEdgeTypes();
        List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
        for (Integer type : tlist) {
            int attr = this.getRawGraph().findAttribute(type, key);
            if (com.sparsity.dex.gdb.Attribute.InvalidAttribute != attr) {
                com.sparsity.dex.gdb.Attribute adata = this.getRawGraph().getAttribute(attr);
                if (adata.getKind() == AttributeKind.Basic) {
                    // It is better to index the attribute than performing a "table" scan
                    this.getRawGraph().indexAttribute(attr, com.sparsity.dex.gdb.AttributeKind.Indexed);
                }
                edges.add(new DexIterable<Edge>(this, this.rawGet(adata, value), Edge.class));
            }
        }
        tlist = null;

        if (edges.size() > 0) return new MultiIterable<Edge>(edges);
        else throw new IllegalArgumentException("The given attribute '" + key + "' does not exist");
    }

    /**
     * Closes all non-closed iterables.
     */
    protected void closeAllCollections() {
        while (!iterables.isEmpty()) {
            iterables.remove(iterables.size() - 1).close();
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Graph#shutdown()
      */
    @Override
    public void shutdown() {
        closeAllCollections();

        rawGraph = null;
        session.close();
        gpool.close();
        dex.close();
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this, db.getPath());
    }

    public Features getFeatures() {
        return FEATURES;
    }

    private com.sparsity.dex.gdb.Objects rawGet(final com.sparsity.dex.gdb.Attribute adata, final Object value) {
        com.sparsity.dex.gdb.Value v = new com.sparsity.dex.gdb.Value();
        switch (adata.getDataType()) {
            case Boolean:
                v.setBooleanVoid((Boolean) value);
                break;
            case Integer:
                v.setIntegerVoid((Integer) value);
                break;
            case Long:
                v.setLongVoid((Long) value);
                break;
            case String:
                v.setStringVoid((String) value);
                break;
            case Double:
                if (value instanceof Double) {
                    v.setDoubleVoid((Double) value);
                } else if (value instanceof Float) {
                    v.setDoubleVoid(((Float) value).doubleValue());
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return this.getRawGraph().select(adata.getId(), com.sparsity.dex.gdb.Condition.Equal, v);
    }

    @Override
    public <T extends Element> void dropKeyIndex(String key,
                                                 Class<T> elementClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass) {

        com.sparsity.dex.gdb.TypeList tlist = null;
        if (Vertex.class.isAssignableFrom(elementClass)) {
            tlist = this.getRawGraph().findNodeTypes();
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            tlist = this.getRawGraph().findEdgeTypes();
        } else {
            throw ExceptionFactory.classIsNotIndexable(elementClass);
        }
        boolean found = false;
        for (Integer type : tlist) {
            int attr = this.getRawGraph().findAttribute(type, key);
            if (com.sparsity.dex.gdb.Attribute.InvalidAttribute != attr) {
                found = true;
                this.getRawGraph().indexAttribute(attr, com.sparsity.dex.gdb.AttributeKind.Indexed);
            }
        }
        tlist = null;
        if (!found) {
            throw new IllegalArgumentException("There is no '" + key + "' attribute");
        }
    }

    @Override
    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        com.sparsity.dex.gdb.TypeList tlist = null;
        if (Vertex.class.isAssignableFrom(elementClass)) {
            tlist = this.getRawGraph().findNodeTypes();
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            tlist = this.getRawGraph().findEdgeTypes();
        } else {
            throw ExceptionFactory.classIsNotIndexable(elementClass);
        }
        boolean found = false;
        Set<String> ret = new HashSet<String>();
        for (Integer type : tlist) {
            com.sparsity.dex.gdb.AttributeList alist = this.getRawGraph().findAttributes(type);
            for (Integer attr : alist) {
                com.sparsity.dex.gdb.Attribute adata = this.getRawGraph().getAttribute(attr);
                if (adata.getKind() == AttributeKind.Indexed || adata.getKind() == AttributeKind.Unique) {
                    ret.add(adata.getName());
                }
            }
        }
        tlist = null;
        return ret;
    }

}
