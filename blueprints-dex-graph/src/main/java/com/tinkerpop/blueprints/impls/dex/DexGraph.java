package com.tinkerpop.blueprints.impls.dex;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sparsity.dex.gdb.AttributeKind;
import com.sparsity.dex.gdb.ObjectType;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.MetaGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.PropertyFilteredIterable;
import com.tinkerpop.blueprints.util.StringFactory;

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
     * Default Vertex label.
     */
    public static final String DEFAULT_DEX_VERTEX_LABEL = "VERTEX_LABEL";
    
    /**
     * This is a "bypass" to set the Dex vertex label (node type).
     * <p>
     * Dex vertices belong to a vertex/node type (thus all of them have a label). 
     * By default, all vertices will have the {@link #DEFAULT_DEX_VERTEX_LABEL} label.
     * The user may set a different vertex label by setting this property when calling
     * {@link #addVertex(Object)}.
     * <p>
     * Moreover, this value will also be used for the KeyIndex-related methods.
     * 
     * @see #addVertex(Object)
     * @see #createKeyIndex(String, Class)
     * @see #getVertices(String, Object)
     * @see #getEdges(String, Object)
     */
    public ThreadLocal<String> label = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return null;
        }
    };
    
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
     * Creates a new Vertex.
     * <p>
     * Given identifier is ignored.
     * <p>
     * Use {@link #label} to specify the label for the new Vertex. 
     * If no label is given, {@value #DEFAULT_DEX_VERTEX_LABEL} will be used.
     * 
     * @param id It is ignored.
     * @return Added Vertex.
     * 
     * @see com.tinkerpop.blueprints.Graph#addVertex(java.lang.Object)
     * @see #label
     */
    @Override
    public Vertex addVertex(final Object id) {
        String label = this.label.get() == null ? DEFAULT_DEX_VERTEX_LABEL : this.label.get();
        int type = this.getRawGraph().findType(label);
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
    public CloseableIterable<Vertex> getVertices() {
        com.sparsity.dex.gdb.TypeList tlist = rawGraph.findNodeTypes();
        List<Iterable<Vertex>> vertices = new ArrayList<Iterable<Vertex>>();
        for (Integer type : tlist) {
            com.sparsity.dex.gdb.Objects objs = rawGraph.select(type);
            vertices.add(new DexIterable<Vertex>(this, objs, Vertex.class));
        }
        tlist.delete();
        tlist = null;
        return new MultiIterable<Vertex>(vertices);
    }

    /**
     * Returns an iterable to all the vertices in the graph that have a particular key/value property.
     * <p>
     * In case key is {@link StringFactory#LABEL}, it returns an iterable of all the vertices having
     * the given value as the label (therefore, belonging to the given type).
     * <p>
     * In case {@link #label} is null, it will return all vertices having a particular 
     * key/value no matters the type.
     * In case {@link #label} is not null, it will return all vertices having a particular 
     * key/value belonging to the given type.
     * 
     * @see com.tinkerpop.blueprints.Graph#getVertices(String, Object)
     * @see #label
     */
    @Override
    public CloseableIterable<Vertex> getVertices(final String key, final Object value) {
        
        if (key.compareTo(StringFactory.LABEL) == 0) { // label is "indexed"
            
            int type = this.getRawGraph().findType(value.toString());
            if (type != com.sparsity.dex.gdb.Type.InvalidType) {
                com.sparsity.dex.gdb.Type tdata = this.getRawGraph().getType(type);
                if (tdata.getObjectType() == ObjectType.Node) {
                    com.sparsity.dex.gdb.Objects objs = this.getRawGraph().select(type);
                    return new DexIterable<Vertex>(this, objs, Vertex.class);
                }
            }
            return null;
        }

        String label = this.label.get();
        if (label == null) { // all vertex types

            com.sparsity.dex.gdb.TypeList tlist = this.getRawGraph().findNodeTypes();
            List<Iterable<Vertex>> vertices = new ArrayList<Iterable<Vertex>>();
            for (Integer type : tlist) {
                int attr = this.getRawGraph().findAttribute(type, key);
                if (com.sparsity.dex.gdb.Attribute.InvalidAttribute != attr) {
                    com.sparsity.dex.gdb.Attribute adata = this.getRawGraph().getAttribute(attr);
                    if (adata.getKind() == AttributeKind.Basic) { // "table" scan
                        com.sparsity.dex.gdb.Objects objs = this.getRawGraph().select(type);
                        vertices.add(new PropertyFilteredIterable<Vertex>(key, value, new DexIterable<Vertex>(this, objs, Vertex.class)));
                    } else { // use the index
                        vertices.add(new DexIterable<Vertex>(this, this.rawGet(adata, value), Vertex.class));
                    }
                }
            }
            tlist.delete();
            tlist = null;

            if (vertices.size() > 0) return new MultiIterable<Vertex>(vertices);
            else throw new IllegalArgumentException("The given attribute '" + key + "' does not exist");
        
        } else { // restricted to a type
            
            int type = this.getRawGraph().findType(label);
            if (type == com.sparsity.dex.gdb.Type.InvalidType) {
                throw new IllegalArgumentException("Unnexisting vertex label: " + label);
            }
            com.sparsity.dex.gdb.Type tdata = this.getRawGraph().getType(type);
            if (tdata.getObjectType() != com.sparsity.dex.gdb.ObjectType.Node) {
                throw new IllegalArgumentException("Given label is not a vertex label: " + label);
            }

            int attr = this.getRawGraph().findAttribute(type, key);
            if (com.sparsity.dex.gdb.Attribute.InvalidAttribute == attr) {
                throw new IllegalArgumentException("The given attribute '" + key
                        + "' does not exist for the given node label '" + label + "'");
            }

            com.sparsity.dex.gdb.Attribute adata = this.getRawGraph().getAttribute(attr);
            if (adata.getKind() == AttributeKind.Basic) { // "table" scan
                com.sparsity.dex.gdb.Objects objs = this.getRawGraph().select(type);
                return new PropertyFilteredIterable<Vertex>(key, value, new DexIterable<Vertex>(this, objs, Vertex.class));
            } else { // use the index
                return new DexIterable<Vertex>(this, this.rawGet(adata, value), Vertex.class);
            }
        }
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
    public CloseableIterable<Edge> getEdges() {
        com.sparsity.dex.gdb.TypeList tlist = rawGraph.findEdgeTypes();
        List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
        for (Integer type : tlist) {
            com.sparsity.dex.gdb.Objects objs = rawGraph.select(type);
            edges.add(new DexIterable<Edge>(this, objs, Edge.class));
        }
        tlist.delete();
        tlist = null;
        return new MultiIterable<Edge>(edges);
    }

    /**
     * Returns an iterable to all the edges in the graph that have a particular key/value property.
     * <p>
     * In case key is {@link StringFactory#LABEL}, it returns an iterable of all the edges having
     * the given value as the label (therefore, belonging to the given type).
     * <p>
     * In case {@link #label} is null, it will return all edges having a particular 
     * key/value no matters the type.
     * In case {@link #label} is not null, it will return all edges having a particular 
     * key/value belonging to the given type.
     * 
     * @see com.tinkerpop.blueprints.Graph#getEdges(String, Object)
     * @see #label
     */
    @Override
    public CloseableIterable<Edge> getEdges(final String key, final Object value) {
        
        if (key.compareTo(StringFactory.LABEL) == 0) { // label is "indexed"
            
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

        String label = this.label.get();
        if (label == null) { // all vertex types

            com.sparsity.dex.gdb.TypeList tlist = this.getRawGraph().findEdgeTypes();
            List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            for (Integer type : tlist) {
                int attr = this.getRawGraph().findAttribute(type, key);
                if (com.sparsity.dex.gdb.Attribute.InvalidAttribute != attr) {
                    com.sparsity.dex.gdb.Attribute adata = this.getRawGraph().getAttribute(attr);
                    if (adata.getKind() == AttributeKind.Basic) { // "table" scan
                        com.sparsity.dex.gdb.Objects objs = this.getRawGraph().select(type);
                        edges.add(new PropertyFilteredIterable<Edge>(key, value, new DexIterable<Edge>(this, objs, Edge.class)));
                    } else { // use the index
                        edges.add(new DexIterable<Edge>(this, this.rawGet(adata, value), Edge.class));
                    }
                }
            }
            tlist.delete();
            tlist = null;

            if (edges.size() > 0) return new MultiIterable<Edge>(edges);
            else throw new IllegalArgumentException("The given attribute '" + key + "' does not exist");
        
        } else { // restricted to a type
            
            int type = this.getRawGraph().findType(label);
            if (type == com.sparsity.dex.gdb.Type.InvalidType) {
                throw new IllegalArgumentException("Unnexisting edge label: " + label);
            }
            com.sparsity.dex.gdb.Type tdata = this.getRawGraph().getType(type);
            if (tdata.getObjectType() != com.sparsity.dex.gdb.ObjectType.Edge) {
                throw new IllegalArgumentException("Given label is not a edge label: " + label);
            }

            int attr = this.getRawGraph().findAttribute(type, key);
            if (com.sparsity.dex.gdb.Attribute.InvalidAttribute == attr) {
                throw new IllegalArgumentException("The given attribute '" + key
                        + "' does not exist for the given edge label '" + label + "'");
            }

            com.sparsity.dex.gdb.Attribute adata = this.getRawGraph().getAttribute(attr);
            if (adata.getKind() == AttributeKind.Basic) { // "table" scan
                com.sparsity.dex.gdb.Objects objs = this.getRawGraph().select(type);
                return new PropertyFilteredIterable<Edge>(key, value, new DexIterable<Edge>(this, objs, Edge.class));
            } else { // use the index
                return new DexIterable<Edge>(this, this.rawGet(adata, value), Edge.class);
            }
        }
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

    /**
     * Create an automatic indexing structure for indexing provided key for element class.
     * <p>
     * Dex attributes are restricted to an specific vertex/edge type. The property
     * {@link #label} must be used to specify the vertex/edge label.
     * <p>
     * The index could be created even before the vertex/edge label 
     * had been created (that is, there are no instances for the given vertex/edge label).
     * If so, this will create the vertex/edge type automatically.
     * The same way, if necessary the attribute will be created automatically.
     * <p>
     * FIXME: In case the attribute is created, this always creates an String
     * attribute, could this be set somehow?
     * 
     * @see com.tinkerpop.blueprints.Graph#createKeyIndex(String, Class)
     * @see #label
     */
    @Override
    public <T extends Element> void createKeyIndex(String key,
            Class<T> elementClass) {
        String label = this.label.get();
        if (label == null) {
            throw new IllegalArgumentException("Label must be given");
        }

        int type = this.getRawGraph().findType(label);
        if (type == com.sparsity.dex.gdb.Type.InvalidType) {
            // create the node/edge type
            if (Vertex.class.isAssignableFrom(elementClass)) {
                type = this.getRawGraph().newNodeType(label);
            } else if (Edge.class.isAssignableFrom(elementClass)) {
                type = this.getRawGraph().newEdgeType(label, true, true);
            } else {
                throw ExceptionFactory.classIsNotIndexable(elementClass);
            }
        } else {
            // validate the node/edge type
            com.sparsity.dex.gdb.Type tdata = this.getRawGraph().getType(type);
            if (tdata.getObjectType() == ObjectType.Node) {
                if (!Vertex.class.isAssignableFrom(elementClass)) {
                    throw new IllegalArgumentException("Given element class '"
                            + elementClass.getName()
                            + "' is not valid for the given node type '"
                            + label + "'");
                }
            } else if (tdata.getObjectType() == ObjectType.Edge) {
                if (!Edge.class.isAssignableFrom(elementClass)) {
                    throw new IllegalArgumentException("Given element class '"
                            + elementClass.getName()
                            + "' is not valid for the given edge type '"
                            + label + "'");
                }
            }
        }

        int attr = this.getRawGraph().findAttribute(type, key);
        if (com.sparsity.dex.gdb.Attribute.InvalidAttribute == attr) {
            // create the attribute (indexed)
            attr = this.getRawGraph().newAttribute(type, key,
                    com.sparsity.dex.gdb.DataType.String,
                    com.sparsity.dex.gdb.AttributeKind.Indexed);
        } else {
            // it already exists, let's indexe it if necessary
            com.sparsity.dex.gdb.Attribute adata = this.getRawGraph().getAttribute(attr);
            if (adata.getKind() == AttributeKind.Indexed || adata.getKind() == AttributeKind.Unique) {
                throw ExceptionFactory.indexAlreadyExists(label + " " + key);
            }
            this.getRawGraph().indexAttribute(attr,
                    com.sparsity.dex.gdb.AttributeKind.Indexed);
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
            alist.delete();
            alist = null;
        }
        tlist.delete();
        tlist = null;
        return ret;
    }

}
