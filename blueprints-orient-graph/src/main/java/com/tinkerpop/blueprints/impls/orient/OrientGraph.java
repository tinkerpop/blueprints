package com.tinkerpop.blueprints.impls.orient;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.tx.OTransaction.TXSTATUS;
import com.orientechnologies.orient.core.tx.OTransactionNoTx;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.MetaGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.PropertyFilteredIterable;
import com.tinkerpop.blueprints.util.StringFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph implements TransactionalGraph, IndexableGraph, MetaGraph<OGraphDatabase> {       // implements KeyIndexableGraph
    private final static String ADMIN = "admin";

    private String url;
    private String username;
    private String password;

    private static final ThreadLocal<OrientGraphContext> threadContext = new ThreadLocal<OrientGraphContext>();
    private static final List<OrientGraphContext> contexts = new ArrayList<OrientGraphContext>();

    private static final Features FEATURES = new Features();

    static {
        FEATURES.allowDuplicateEdges = true;
        FEATURES.allowSelfLoops = true;
        FEATURES.isPersistent = true;
        FEATURES.isRDFModel = false;
        FEATURES.supportsVertexIteration = true;
        FEATURES.supportsEdgeIteration = true;
        FEATURES.supportsVertexIndex = true;
        FEATURES.supportsEdgeIndex = true;
        FEATURES.ignoresSuppliedIds = true;
        FEATURES.supportsTransactions = true;
        FEATURES.supportsEdgeKeyIndex = false;                //todo
        FEATURES.supportsVertexKeyIndex = false;        //todo
        FEATURES.supportsKeyIndices = false;      //todo
        FEATURES.isWrapper = false;
        FEATURES.supportsIndices = false;  // todo

        // For more information on supported types, please see:
        // http://code.google.com/p/orient/wiki/Types
        FEATURES.allowSerializableObjectProperty = true;
        FEATURES.allowBooleanProperty = true;
        FEATURES.allowDoubleProperty = true;
        FEATURES.allowFloatProperty = true;
        FEATURES.allowIntegerProperty = true;
        FEATURES.allowPrimitiveArrayProperty = true;
        FEATURES.allowUniformListProperty = true;
        FEATURES.allowMixedListProperty = true;
        FEATURES.allowLongProperty = true;
        FEATURES.allowMapProperty = true;
        FEATURES.allowStringProperty = true;
    }

    /**
     * Constructs a new object using an existent OGraphDatabase instance.
     *
     * @param iDatabase Underlying OGraphDatabase object to attach
     */
    public OrientGraph(final OGraphDatabase iDatabase) {
        reuse(iDatabase);
    }

    public OrientGraph(final String url) {
        this(url, ADMIN, ADMIN);
    }

    public OrientGraph(final String url, final String username, final String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.openOrCreate();
    }

    @SuppressWarnings("unchecked")
    public <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass, final Parameter... indexParameters) {
        final OrientGraphContext context = getContext(true);

        synchronized (contexts) {
            if (context.manualIndices.containsKey(indexName))
                throw ExceptionFactory.indexAlreadyExists(indexName);

            final OrientIndex<? extends OrientElement> index = new OrientIndex<OrientElement>(this, indexName, indexClass, null);

            // ADD THE INDEX IN ALL CURRENT CONTEXTS
            for (OrientGraphContext ctx : contexts)
                ctx.manualIndices.put(index.getIndexName(), index);

            // SAVE THE CONFIGURATION INTO THE GLOBAL CONFIG
            saveIndexConfiguration();

            return (Index<T>) index;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final OrientGraphContext context = getContext(true);
        Index<? extends Element> index = context.manualIndices.get(indexName);
        if (null == index) {
            return null;
        }

        if (indexClass.isAssignableFrom(index.getIndexClass()))
            return (Index<T>) index;
        else
            throw ExceptionFactory.indexDoesNotSupportClass(indexName, indexClass);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        final OrientGraphContext context = getContext(true);
        final List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
        for (Index<?> index : context.manualIndices.values()) {
            list.add(index);
        }
        return list;
    }

    protected Iterable<OrientIndex<? extends OrientElement>> getManualIndices() {
        return getContext(true).manualIndices.values();
    }

    public void dropIndex(final String indexName) {

        this.autoStartTransaction();
        try {
            synchronized (contexts) {
                for (OrientGraphContext ctx : contexts) {
                    ctx.manualIndices.remove(indexName);
                }
            }

            getRawGraph().getMetadata().getIndexManager().dropIndex(indexName);
            saveIndexConfiguration();
            this.stopTransaction(Conclusion.SUCCESS);
        } catch (Exception e) {
            this.stopTransaction(Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Vertex addVertex(final Object id) {
        final OGraphDatabase db = getRawGraph();
        this.autoStartTransaction();
        try {
            final OrientVertex vertex = new OrientVertex(this, db.createVertex(null));
            vertex.save();
            return vertex;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        final OGraphDatabase db = getRawGraph();
        this.autoStartTransaction();
        try {
            final ODocument edgeDoc = db.createEdge(((OrientVertex) outVertex).getRawElement(), ((OrientVertex) inVertex).getRawElement());
            final OrientEdge edge = new OrientEdge(this, edgeDoc, label);

            // SAVE THE VERTICES TO ASSURE THEY ARE IN TX
            db.save(((OrientVertex) outVertex).getRawElement());
            db.save(((OrientVertex) inVertex).getRawElement());
            edge.save();
            return edge;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            throw ExceptionFactory.vertexIdCanNotBeNull();

        ORID rid;
        if (id instanceof ORID)
            rid = (ORID) id;
        else {
            try {
                rid = new ORecordId(id.toString());
            } catch (IllegalArgumentException iae) {
                // orientdb throws IllegalArgumentException: Argument 'xxxx' is not a RecordId in form of string. Format must be: <cluster-id>:<cluster-position>
                return null;
            }
        }

        if (!rid.isValid())
            return null;


        final ODocument doc = getRawGraph().load(rid);
        if (doc != null) {
            return new OrientVertex(this, doc);
        }

        return null;
    }

    public void removeVertex(final Vertex vertex) {
        final OrientVertex oVertex = (OrientVertex) vertex;
        if (oVertex == null || oVertex.getRawElement() == null)
            return;

        this.autoStartTransaction();
        try {
            final Set<Edge> allEdges = new HashSet<Edge>();
            for (Edge e : oVertex.getInEdges())
                allEdges.add(e);
            for (Edge e : oVertex.getOutEdges())
                allEdges.add(e);

            for (final Index<? extends Element> index : this.getManualIndices()) {
                if (Vertex.class.isAssignableFrom(index.getIndexClass())) {
                    @SuppressWarnings("unchecked")
                    OrientIndex<OrientVertex> idx = (OrientIndex<OrientVertex>) index;
                    idx.removeElement(oVertex);
                }

                if (Edge.class.isAssignableFrom(index.getIndexClass())) {
                    @SuppressWarnings("unchecked")
                    OrientIndex<OrientEdge> idx = (OrientIndex<OrientEdge>) index;
                    for (Edge e : allEdges)
                        idx.removeElement((OrientEdge) e);
                }
            }

            getRawGraph().removeVertex(oVertex.rawElement);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Iterable<Vertex> getVertices() {
        this.stopTransaction(Conclusion.SUCCESS);
        return getVertices(true);
    }

    public Iterable<Vertex> getVertices(final String key, final Object value) {
        // when auto indices connected, be sure to search for respective index first
        return new PropertyFilteredIterable<Vertex>(key, value, this.getVertices());
    }

    private Iterable<Vertex> getVertices(final boolean polymorphic) {
        return new OrientElementScanIterable<Vertex>(this, Vertex.class);
    }

    public Iterable<Edge> getEdges() {
        return getEdges(true);
    }

    public Iterable<Edge> getEdges(final String key, final Object value) {
        // when auto indices connected, be sure to search for respective index first
        return new PropertyFilteredIterable<Edge>(key, value, this.getEdges());
    }

    private Iterable<Edge> getEdges(final boolean polymorphic) {
        return new OrientElementScanIterable<Edge>(this, Edge.class);
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();

        final ORID rid;
        if (id instanceof ORID)
            rid = (ORID) id;
        else {
            try {
                rid = new ORecordId(id.toString());
            } catch (IllegalArgumentException iae) {
                // orientdb throws IllegalArgumentException: Argument 'xxxx' is not a RecordId in form of string. Format must be: <cluster-id>:<cluster-position>
                return null;
            }
        }

        final ODocument doc = getRawGraph().load(rid);
        if (doc != null) {
            return new OrientEdge(this, doc);
        }

        return null;
    }

    public void removeEdge(final Edge edge) {
        final OrientEdge oEdge = (OrientEdge) edge;
        if (oEdge == null || oEdge.getRawElement() == null)
            return;

        this.autoStartTransaction();
        try {
            for (final Index<? extends Element> index : this.getManualIndices()) {
                if (Edge.class.isAssignableFrom(index.getIndexClass())) {
                    @SuppressWarnings("unchecked")
                    OrientIndex<OrientEdge> idx = (OrientIndex<OrientEdge>) index;
                    idx.removeElement(oEdge);
                }
            }

            getRawGraph().removeEdge(oEdge.rawElement);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Reuses the underlying database avoiding to create and open it every time.
     *
     * @param iDatabase Underlying OGraphDatabase object
     */
    public OrientGraph reuse(final OGraphDatabase iDatabase) {
        this.url = iDatabase.getURL();
        this.url = iDatabase.getUser() != null ? iDatabase.getUser().getName() : null;
        synchronized (this) {
            OrientGraphContext context = threadContext.get();
            if (context == null || context.rawGraph != iDatabase) {
                removeContext();
                context = new OrientGraphContext();
                context.rawGraph = iDatabase;
                iDatabase.checkForGraphSchema();
                this.threadContext.set(context);
            }
        }
        return this;
    }


    public void shutdown() {
        removeContext();

        url = null;
        username = null;
        password = null;
    }

    public String toString() {
        return StringFactory.graphString(this, getRawGraph().getURL());
    }

    public OGraphDatabase getRawGraph() {
        return getContext(true).rawGraph;
    }

    public void startTransaction() {
        final OrientGraphContext context = getContext(true);

        if (context.rawGraph.getTransaction() instanceof OTransactionNoTx && context.rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN) {
            context.rawGraph.begin();
        } else
            throw ExceptionFactory.transactionAlreadyStarted();
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (conclusion == Conclusion.FAILURE) {
            this.getRawGraph().rollback();
        } else
            this.getRawGraph().commit();
    }

    protected void saveIndexConfiguration() {
        getRawGraph().getMetadata().getIndexManager().getConfiguration().save();
    }

    protected void autoStartTransaction() {
        final OrientGraphContext context = getContext(true);
        if (context.rawGraph.getTransaction() instanceof OTransactionNoTx && context.rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN) {
            context.rawGraph.begin();
        }
    }

    protected OrientGraphContext getContext(final boolean create) {
        OrientGraphContext context = threadContext.get();
        if (context == null && create)
            context = openOrCreate();
        return context;
    }

    private OrientGraphContext openOrCreate() {
        if (url == null)
            throw new IllegalStateException("Database is closed");

        synchronized (this) {
            OrientGraphContext context = threadContext.get();
            if (context != null)
                removeContext();

            context = new OrientGraphContext();
            threadContext.set(context);

            synchronized (contexts) {
                contexts.add(context);
            }

            context.rawGraph = new OGraphDatabase(url);
            context.rawGraph.setUseCustomTypes(false);

            if (url.startsWith("remote:") || context.rawGraph.exists()) {
                context.rawGraph.open(username, password);

                // LOAD THE INDEX CONFIGURATION FROM INTO THE DICTIONARY
                // final ODocument indexConfiguration = context.rawGraph.getMetadata().getIndexManager().getConfiguration();

                for (OIndex<?> idx : context.rawGraph.getMetadata().getIndexManager().getIndexes()) {
                    if (idx.getConfiguration().field(OrientIndex.CONFIG_TYPE) != null)
                        // LOAD THE INDEXES
                        loadIndex(idx);
                }

            } else {
                context.rawGraph.create();
            }

            return context;
        }
    }

    @SuppressWarnings("rawtypes")
    private OrientIndex<?> loadIndex(final OIndex rawIndex) {
        final OrientIndex<?> index;
        index = new OrientIndex(this, rawIndex);

        // REGISTER THE INDEX
        getContext(true).manualIndices.put(index.getIndexName(), index);
        return index;
    }

    private void removeContext() {
        final OrientGraphContext context = getContext(false);

        if (context != null) {
            context.rawGraph.commit();
            context.rawGraph.close();

            for (Index<? extends Element> idx : getIndices()) {
                ((OrientIndex<?>) idx).close();
            }

            context.manualIndices.clear();

            synchronized (contexts) {
                contexts.remove(context);
            }

            threadContext.set(null);
        }
    }

    public Features getFeatures() {
        return FEATURES;
    }

    /*public <T extends Element> void dropKeyIndex(final String key, Class<T> elementClass) {

    }
    public <T extends Element> void createKeyIndex(final String key, Class<T> elementClass) {

    }
    public <T extends Element> Set<String> getIndexedKeys(final Class<T> elementClass) {
        
    }*/

}
