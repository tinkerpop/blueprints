package com.tinkerpop.blueprints.impls.sparksee;

import com.sparsity.sparksee.gdb.AttributeKind;
import com.sparsity.sparksee.gdb.ObjectType;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.MetaGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultGraphQuery;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.PropertyFilteredIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import org.apache.commons.configuration.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sparksee is a graph database developed by Sparsity Technologies.
 * <p/>
 * Sparksee natively supports the property graph data model defined by Blueprints.
 * However, there are a few peculiarities. No user defined element identifiers:
 * Sparksee is the gatekeeper and creator of vertex and edge identifiers. Thus, when
 * creating a new vertex or edge instance, the provided object identifier is
 * ignored.
 * <p/>
 * Vertices are labeled too: When adding vertices, the user can set
 * {@link SparkseeGraph#label} to be used as the label of the vertex to be created.
 * Also, the label of a vertex (or even an element) can be retrieved through the
 * {@link StringFactory#LABEL} property.
 * <p/>
 * SparkseeGraph implements {@link KeyIndexableGraph} with some particularities on
 * the way it can be used. As both vertices and edges are labeled when working
 * with Sparksee, the use of some APIs may require previously setting the label (by
 * means of {@link SparkseeGraph#label}). Those APIs are:
 * {@link #getVertices(String, Object)}, {@link #getEdges(String, Object)}, and
 * {@link #createKeyIndex(String, Class)}.
 * <p/>
 * When working with SparkseeGraph, all methods having as a result a collection
 * actually return a {@link CloseableIterable} collection. Thus users can
 * {@link CloseableIterable#close()} the collection to free resources.
 * Otherwise, all those collections will automatically be closed when the
 * transaction is stopped ({@link #stopTransaction(com.tinkerpop.blueprints.TransactionalGraph.Conclusion)}
 * or if the database is stopped ( {@link #shutdown()}).
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class SparkseeGraph implements MetaGraph<com.sparsity.sparksee.gdb.Graph>, KeyIndexableGraph, TransactionalGraph {

    /**
     * Default Vertex label.
     */
    public static final String DEFAULT_SPARKSEE_VERTEX_LABEL = "VERTEX_LABEL";

    /**
     * This is a "bypass" to set the Sparksee vertex label (node type).
     * <p/>
     * Sparksee vertices belong to a vertex/node type (thus all of them have a label).
     * By default, all vertices will have the {@link #DEFAULT_SPARKSEE_VERTEX_LABEL} label.
     * The user may set a different vertex label by setting this property when calling
     * {@link #addVertex(Object)}.
     * <p/>
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
    private File dbFile = null;

    private com.sparsity.sparksee.gdb.Sparksee sparksee = null;
    private com.sparsity.sparksee.gdb.Database db = null;

    private class Metadata {
        com.sparsity.sparksee.gdb.Session session = null;
        List<SparkseeIterable<? extends Element>> collection = null;
    }

    ;

    private ThreadLocal<Metadata> sessionData = new ThreadLocal<Metadata>() {
        @Override
        protected Metadata initialValue() {
            return new Metadata();
        }
    };

    private static final Features FEATURES = new Features();

    static {
        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.isPersistent = true;
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

        FEATURES.isWrapper = false;
        FEATURES.supportsKeyIndices = true;
        FEATURES.supportsVertexKeyIndex = true;
        FEATURES.supportsEdgeKeyIndex = true;
        FEATURES.supportsThreadedTransactions = false;
    }

    /**
     * Gets the Sparksee raw graph.
     *
     * @return Sparksee raw graph.
     */
    public com.sparsity.sparksee.gdb.Graph getRawGraph() {
        com.sparsity.sparksee.gdb.Session sess = getRawSession(false);
        if (sess == null) {
            throw new IllegalStateException("Transaction has not been started");
        }
        return sess.getGraph();
    }

    /**
     * Gets the Sparksee Session
     *
     * @return The Sparksee Session
     */
    com.sparsity.sparksee.gdb.Session getRawSession() {
        return getRawSession(true);
    }

    /**
     * Gets the Sparksee Session
     *
     * @return The Sparksee Session
     */
    com.sparsity.sparksee.gdb.Session getRawSession(boolean exception) {
        com.sparsity.sparksee.gdb.Session sess = sessionData.get().session;
        if (sess == null && exception) {
            throw new IllegalStateException("Transaction has not been started");
        }
        return sess;
    }

    /**
     * Registers a collection.
     *
     * @param col Collection to be registered.
     */
    protected void register(final SparkseeIterable<? extends Element> col) {
        if (sessionData.get().collection == null) {
            sessionData.get().collection = new ArrayList<SparkseeIterable<? extends Element>>();
        }
        sessionData.get().collection.add(col);
        //System.out.println("> register " + sess + ":" + col);
    }

    /**
     * Unregisters a collection.
     *
     * @param col Collection to be unregistered
     */
    protected void unregister(final SparkseeIterable<? extends Element> col) {
        if (sessionData.get().collection == null) {
            throw new IllegalStateException("Session with no collections");
        }
        sessionData.get().collection.remove(col);
        //System.out.println("< unregister " + sess + ":" + col);
    }

    /**
     * Creates a new instance.
     *
     * @param fileName Database persistent file.
     */
    public SparkseeGraph(final String fileName) {
        this(fileName, null);
    }

    /**
     * Creates a new instance.
     *
     * @param fileName Database persistent file.
     * @param config   Sparksee configuration file.
     */
    public SparkseeGraph(final String fileName, final String config) {
        try {
            this.dbFile = new File(fileName);
            final File dbPath = dbFile.getParentFile();

            if (!dbPath.exists()) {
                if (!dbPath.mkdirs()) {
                    throw new RuntimeException("Could not create directory");
                }
            }

            final boolean create = !dbFile.exists();

            if (config != null) com.sparsity.sparksee.gdb.SparkseeProperties.load(config);
            sparksee = new com.sparsity.sparksee.gdb.Sparksee(new com.sparsity.sparksee.gdb.SparkseeConfig());
            db = (create ? sparksee.create(dbFile.getPath(), dbFile.getName()) : sparksee.open(dbFile.getPath(), false));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public SparkseeGraph(final Configuration configuration) {
        this(configuration.getString("blueprints.sparksee.directory", null), configuration.getString("blueprints.sparksee.config", null));
    }

    /**
     * Creates a new Vertex.
     * <p/>
     * Given identifier is ignored.
     * <p/>
     * Use {@link #label} to specify the label for the new Vertex.
     * If no label is given, {@value #DEFAULT_SPARKSEE_VERTEX_LABEL} will be used.
     *
     * @param id It is ignored.
     * @return Added Vertex.
     * @see com.tinkerpop.blueprints.Graph#addVertex(java.lang.Object)
     * @see #label
     */
    @Override
    public Vertex addVertex(final Object id) {
        autoStartTransaction();

        String label = this.label.get() == null ? DEFAULT_SPARKSEE_VERTEX_LABEL : this.label.get();
        com.sparsity.sparksee.gdb.Graph rawGraph = getRawGraph();
        int type = rawGraph.findType(label);
        if (type == com.sparsity.sparksee.gdb.Type.InvalidType) {
            // First instance of this type, let's create it
            type = rawGraph.newNodeType(label);
        }
        assert type != com.sparsity.sparksee.gdb.Type.InvalidType;
        // create object instance
        long oid = rawGraph.newNode(type);
        return new SparkseeVertex(this, oid);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Graph#getVertex(java.lang.Object)
      */
    @Override
    public Vertex getVertex(final Object id) {
        autoStartTransaction();

        if (null == id)
            throw ExceptionFactory.vertexIdCanNotBeNull();
        try {
            final Long longId = Double.valueOf(id.toString()).longValue();
            com.sparsity.sparksee.gdb.Graph rawGraph = getRawGraph();
            final int type = rawGraph.getObjectType(longId);
            if (type != com.sparsity.sparksee.gdb.Type.InvalidType)
                return new SparkseeVertex(this, longId);
            else
                return null;
        } catch (NumberFormatException e) {
            return null;
        } catch (RuntimeException re) {
            // sparksee throws a runtime exception => [SPARKSEE: 12] Invalid object identifier.
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
        autoStartTransaction();

        if (getVertex(vertex.getId()) == null)
            throw ExceptionFactory.vertexWithIdDoesNotExist(vertex.getId());

        assert vertex instanceof SparkseeVertex;

        try {
            getRawGraph().drop((Long) vertex.getId());
        } catch (RuntimeException re) {
            ExceptionFactory.vertexWithIdDoesNotExist(vertex.getId());
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Graph#getVertices()
      */
    @Override
    public CloseableIterable<Vertex> getVertices() {
        autoStartTransaction();

        com.sparsity.sparksee.gdb.Graph rawGraph = getRawGraph();
        com.sparsity.sparksee.gdb.TypeList tlist = rawGraph.findNodeTypes();
        List<Iterable<Vertex>> vertices = new ArrayList<Iterable<Vertex>>();
        for (Integer type : tlist) {
            com.sparsity.sparksee.gdb.Objects objs = rawGraph.select(type);
            vertices.add(new SparkseeIterable<Vertex>(this, objs, Vertex.class));
        }
        tlist.delete();
        tlist = null;
        return new MultiIterable<Vertex>(vertices);
    }

    /**
     * Returns an iterable to all the vertices in the graph that have a particular key/value property.
     * <p/>
     * In case key is {@link StringFactory#LABEL}, it returns an iterable of all the vertices having
     * the given value as the label (therefore, belonging to the given type).
     * <p/>
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
        autoStartTransaction();

        com.sparsity.sparksee.gdb.Graph rawGraph = getRawGraph();

        if (key.compareTo(StringFactory.LABEL) == 0) { // label is "indexed"

            int type = rawGraph.findType(value.toString());
            if (type != com.sparsity.sparksee.gdb.Type.InvalidType) {
                com.sparsity.sparksee.gdb.Type tdata = rawGraph.getType(type);
                if (tdata.getObjectType() == ObjectType.Node) {
                    com.sparsity.sparksee.gdb.Objects objs = rawGraph.select(type);
                    return new SparkseeIterable<Vertex>(this, objs, Vertex.class);
                }
            }
            return null;
        }

        String label = this.label.get();
        if (label == null) { // all vertex types

            com.sparsity.sparksee.gdb.TypeList tlist = rawGraph.findNodeTypes();
            List<Iterable<Vertex>> vertices = new ArrayList<Iterable<Vertex>>();
            for (Integer type : tlist) {
                int attr = rawGraph.findAttribute(type, key);
                if (com.sparsity.sparksee.gdb.Attribute.InvalidAttribute != attr) {
                    com.sparsity.sparksee.gdb.Attribute adata = rawGraph.getAttribute(attr);
                    if (adata.getKind() == AttributeKind.Basic) { // "table" scan
                        com.sparsity.sparksee.gdb.Objects objs = rawGraph.select(type);
                        vertices.add(new PropertyFilteredIterable<Vertex>(key, value, new SparkseeIterable<Vertex>(this, objs, Vertex.class)));
                    } else { // use the index
                        vertices.add(new SparkseeIterable<Vertex>(this, this.rawGet(adata, value), Vertex.class));
                    }
                }
            }
            tlist.delete();
            tlist = null;

            if (vertices.size() > 0) return new MultiIterable<Vertex>(vertices);
            else throw new IllegalArgumentException("The given attribute '" + key + "' does not exist");

        } else { // restricted to a type

            int type = rawGraph.findType(label);
            if (type == com.sparsity.sparksee.gdb.Type.InvalidType) {
                throw new IllegalArgumentException("Unnexisting vertex label: " + label);
            }
            com.sparsity.sparksee.gdb.Type tdata = rawGraph.getType(type);
            if (tdata.getObjectType() != com.sparsity.sparksee.gdb.ObjectType.Node) {
                throw new IllegalArgumentException("Given label is not a vertex label: " + label);
            }

            int attr = rawGraph.findAttribute(type, key);
            if (com.sparsity.sparksee.gdb.Attribute.InvalidAttribute == attr) {
                throw new IllegalArgumentException("The given attribute '" + key
                        + "' does not exist for the given node label '" + label + "'");
            }

            com.sparsity.sparksee.gdb.Attribute adata = rawGraph.getAttribute(attr);
            if (adata.getKind() == AttributeKind.Basic) { // "table" scan
                com.sparsity.sparksee.gdb.Objects objs = rawGraph.select(type);
                return new PropertyFilteredIterable<Vertex>(key, value, new SparkseeIterable<Vertex>(this, objs, Vertex.class));
            } else { // use the index
                return new SparkseeIterable<Vertex>(this, this.rawGet(adata, value), Vertex.class);
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
        if (label == null)
            throw ExceptionFactory.edgeLabelCanNotBeNull();

        autoStartTransaction();

        com.sparsity.sparksee.gdb.Graph rawGraph = getRawGraph();
        int type = rawGraph.findType(label);
        if (type == com.sparsity.sparksee.gdb.Type.InvalidType) {
            // First instance of this type, let's create it
            type = rawGraph.newEdgeType(label, true, true);
        }
        assert type != com.sparsity.sparksee.gdb.Type.InvalidType;
        // create object instance
        assert outVertex instanceof SparkseeVertex && inVertex instanceof SparkseeVertex;
        long oid = rawGraph.newEdge(type, (Long) outVertex.getId(), (Long) inVertex.getId());
        return new SparkseeEdge(this, oid);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Graph#getEdge(java.lang.Object)
      */
    @Override
    public Edge getEdge(final Object id) {
        autoStartTransaction();

        if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();
        try {
            com.sparsity.sparksee.gdb.Graph rawGraph = getRawGraph();
            Long longId = Double.valueOf(id.toString()).longValue();
            int type = rawGraph.getObjectType(longId);
            if (type != com.sparsity.sparksee.gdb.Type.InvalidType)
                return new SparkseeEdge(this, longId);
            else
                return null;
        } catch (NumberFormatException e) {
            return null;
        } catch (RuntimeException re) {
            // sparksee throws an runtime exception => [SPARKSEE: 12] Invalid object identifier.
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
        autoStartTransaction();

        assert edge instanceof SparkseeEdge;
        getRawGraph().drop((Long) edge.getId());
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Graph#getEdges()
      */
    @Override
    public CloseableIterable<Edge> getEdges() {
        autoStartTransaction();

        com.sparsity.sparksee.gdb.Graph rawGraph = getRawGraph();
        com.sparsity.sparksee.gdb.TypeList tlist = rawGraph.findEdgeTypes();
        List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
        for (Integer type : tlist) {
            com.sparsity.sparksee.gdb.Objects objs = rawGraph.select(type);
            edges.add(new SparkseeIterable<Edge>(this, objs, Edge.class));
        }
        tlist.delete();
        tlist = null;
        return new MultiIterable<Edge>(edges);
    }

    /**
     * Returns an iterable to all the edges in the graph that have a particular key/value property.
     * <p/>
     * In case key is {@link StringFactory#LABEL}, it returns an iterable of all the edges having
     * the given value as the label (therefore, belonging to the given type).
     * <p/>
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
        autoStartTransaction();

        com.sparsity.sparksee.gdb.Graph rawGraph = getRawGraph();

        if (key.compareTo(StringFactory.LABEL) == 0) { // label is "indexed"

            int type = rawGraph.findType(value.toString());
            if (type != com.sparsity.sparksee.gdb.Type.InvalidType) {
                com.sparsity.sparksee.gdb.Type tdata = rawGraph.getType(type);
                if (tdata.getObjectType() == ObjectType.Edge) {
                    com.sparsity.sparksee.gdb.Objects objs = rawGraph.select(type);
                    return new SparkseeIterable<Edge>(this, objs, Edge.class);
                }
            }
            return null;
        }

        String label = this.label.get();
        if (label == null) { // all vertex types

            com.sparsity.sparksee.gdb.TypeList tlist = rawGraph.findEdgeTypes();
            List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            for (Integer type : tlist) {
                int attr = rawGraph.findAttribute(type, key);
                if (com.sparsity.sparksee.gdb.Attribute.InvalidAttribute != attr) {
                    com.sparsity.sparksee.gdb.Attribute adata = rawGraph.getAttribute(attr);
                    if (adata.getKind() == AttributeKind.Basic) { // "table" scan
                        com.sparsity.sparksee.gdb.Objects objs = rawGraph.select(type);
                        edges.add(new PropertyFilteredIterable<Edge>(key, value, new SparkseeIterable<Edge>(this, objs, Edge.class)));
                    } else { // use the index
                        edges.add(new SparkseeIterable<Edge>(this, this.rawGet(adata, value), Edge.class));
                    }
                }
            }
            tlist.delete();
            tlist = null;

            if (edges.size() > 0) return new MultiIterable<Edge>(edges);
            else throw new IllegalArgumentException("The given attribute '" + key + "' does not exist");

        } else { // restricted to a type

            int type = rawGraph.findType(label);
            if (type == com.sparsity.sparksee.gdb.Type.InvalidType) {
                throw new IllegalArgumentException("Unnexisting edge label: " + label);
            }
            com.sparsity.sparksee.gdb.Type tdata = rawGraph.getType(type);
            if (tdata.getObjectType() != com.sparsity.sparksee.gdb.ObjectType.Edge) {
                throw new IllegalArgumentException("Given label is not a edge label: " + label);
            }

            int attr = rawGraph.findAttribute(type, key);
            if (com.sparsity.sparksee.gdb.Attribute.InvalidAttribute == attr) {
                throw new IllegalArgumentException("The given attribute '" + key
                        + "' does not exist for the given edge label '" + label + "'");
            }

            com.sparsity.sparksee.gdb.Attribute adata = rawGraph.getAttribute(attr);
            if (adata.getKind() == AttributeKind.Basic) { // "table" scan
                com.sparsity.sparksee.gdb.Objects objs = rawGraph.select(type);
                return new PropertyFilteredIterable<Edge>(key, value, new SparkseeIterable<Edge>(this, objs, Edge.class));
            } else { // use the index
                return new SparkseeIterable<Edge>(this, this.rawGet(adata, value), Edge.class);
            }
        }
    }

    /**
     * Closes all non-closed iterables.
     */
    protected void closeAllSessionCollections() {
        if (sessionData.get().collection != null) {
            for (SparkseeIterable<? extends Element> elem : sessionData.get().collection) {
                elem.close(false);
            }
            sessionData.get().collection.clear();
        }
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Graph#shutdown()
      */
    @Override
    public void shutdown() {
        commit();

        db.close();
        sparksee.close();
    }

    @Override
    public String toString() {
        return StringFactory.graphString(this, dbFile.getPath());
    }

    public Features getFeatures() {
        return FEATURES;
    }

    private com.sparsity.sparksee.gdb.Objects rawGet(final com.sparsity.sparksee.gdb.Attribute adata, final Object value) {
        com.sparsity.sparksee.gdb.Value v = new com.sparsity.sparksee.gdb.Value();
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
        return this.getRawGraph().select(adata.getId(), com.sparsity.sparksee.gdb.Condition.Equal, v);
    }

    @Override
    public <T extends Element> void dropKeyIndex(String key,
                                                 Class<T> elementClass) {
        if (elementClass == null)
            throw ExceptionFactory.classForElementCannotBeNull();

        throw new UnsupportedOperationException();
    }

    /**
     * Create an automatic indexing structure for indexing provided key for element class.
     * <p/>
     * Sparksee attributes are restricted to an specific vertex/edge type. The property
     * {@link #label} must be used to specify the vertex/edge label.
     * <p/>
     * The index could be created even before the vertex/edge label
     * had been created (that is, there are no instances for the given vertex/edge label).
     * If so, this will create the vertex/edge type automatically.
     * The same way, if necessary the attribute will be created automatically.
     * <p/>
     * FIXME: In case the attribute is created, this always creates an String
     * attribute, could this be set somehow?
     */
    @Override
    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, final Parameter... indexParameters) {
        if (elementClass == null)
            throw ExceptionFactory.classForElementCannotBeNull();

        autoStartTransaction();

        String label = this.label.get();
        if (label == null) {
            throw new IllegalArgumentException("Label must be given");
        }

        com.sparsity.sparksee.gdb.Graph rawGraph = getRawGraph();

        int type = rawGraph.findType(label);
        if (type == com.sparsity.sparksee.gdb.Type.InvalidType) {
            // create the node/edge type
            if (Vertex.class.isAssignableFrom(elementClass)) {
                type = rawGraph.newNodeType(label);
            } else if (Edge.class.isAssignableFrom(elementClass)) {
                type = rawGraph.newEdgeType(label, true, true);
            } else {
                throw ExceptionFactory.classIsNotIndexable(elementClass);
            }
        } else {
            // validate the node/edge type
            com.sparsity.sparksee.gdb.Type tdata = rawGraph.getType(type);
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

        int attr = rawGraph.findAttribute(type, key);
        if (com.sparsity.sparksee.gdb.Attribute.InvalidAttribute == attr) {
            // create the attribute (indexed)
            attr = rawGraph.newAttribute(type, key,
                    com.sparsity.sparksee.gdb.DataType.String,
                    com.sparsity.sparksee.gdb.AttributeKind.Indexed);
        } else {
            // it already exists, let's indexe it if necessary
            com.sparsity.sparksee.gdb.Attribute adata = rawGraph.getAttribute(attr);
            if (adata.getKind() == AttributeKind.Indexed || adata.getKind() == AttributeKind.Unique) {
                throw ExceptionFactory.indexAlreadyExists(label + " " + key);
            }
            rawGraph.indexAttribute(attr,
                    com.sparsity.sparksee.gdb.AttributeKind.Indexed);
        }
    }

    @Override
    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        if (elementClass == null)
            throw ExceptionFactory.classForElementCannotBeNull();

        autoStartTransaction();

        com.sparsity.sparksee.gdb.TypeList tlist = null;
        com.sparsity.sparksee.gdb.Graph rawGraph = getRawGraph();
        if (Vertex.class.isAssignableFrom(elementClass)) {
            tlist = rawGraph.findNodeTypes();
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            tlist = rawGraph.findEdgeTypes();
        } else {
            throw ExceptionFactory.classIsNotIndexable(elementClass);
        }
        Set<String> ret = new HashSet<String>();
        for (Integer type : tlist) {
            com.sparsity.sparksee.gdb.AttributeList alist = rawGraph.findAttributes(type);
            for (Integer attr : alist) {
                com.sparsity.sparksee.gdb.Attribute adata = rawGraph.getAttribute(attr);
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

    void autoStartTransaction() {
        if (sessionData.get().session == null) {
            sessionData.get().session = db.newSession();
            //System.out.println("> th=" + Thread.currentThread().getId() + " starts tx with sess=" + sess);
        } else {
            assert !sessionData.get().session.isClosed();
        }
    }

    public void commit() {
        if (sessionData.get().session == null) {
            // already closed session
            return;
        }
        closeAllSessionCollections();
        if (sessionData.get().session != null && !sessionData.get().session.isClosed()) {
            sessionData.get().session.close();
        }
        sessionData.get().session = null;
    }

    public void rollback() {
        throw new UnsupportedOperationException("Rollback is not supported");
    }

    @Override
    public void stopTransaction(Conclusion conclusion) {
        if (Conclusion.SUCCESS == conclusion)
            commit();
        else
            rollback();
    }

    public GraphQuery query() {
        return new DefaultGraphQuery(this);
    }
}