package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordAbstract;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.tx.OTransaction.TXSTATUS;
import com.orientechnologies.orient.core.tx.OTransactionNoTx;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;
import com.tinkerpop.blueprints.pgm.util.AutomaticIndexHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph implements TransactionalGraph, IndexableGraph {
    private final static String ADMIN = "admin";

    private String url;
    private String username;
    private String password;

    private final ThreadLocal<OrientGraphContext> threadContext = new ThreadLocal<OrientGraphContext>();

    static {
        //OGlobalConfiguration.STORAGE_KEEP_OPEN.setValue(false);
    }

    public OrientGraph(final String url) {
        this(url, ADMIN, ADMIN);
    }

    public OrientGraph(final String url, final String username, final String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.openOrCreate(true);
    }

    @SuppressWarnings("unchecked")
    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> indexKeys) {
        final OrientGraphContext context = getContext(true);
        if (context.autoIndices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        final OrientAutomaticIndex<? extends Element> index = new OrientAutomaticIndex<OrientElement>(this, indexName, (Class<OrientElement>) indexClass, indexKeys);
        context.autoIndices.put(index.getIndexName(), index);

        // SAVE THE CONFIGURATION INTO THE GLOBAL CONFIG
        saveIndexConfiguration();

        return (AutomaticIndex<T>) index;
    }

    @SuppressWarnings("unchecked")
    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        final OrientGraphContext context = getContext(true);
        if (context.manualIndices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        final OrientIndex<? extends OrientElement> index = new OrientIndex<OrientElement>(this, indexName, indexClass, Index.Type.MANUAL, null);
        context.manualIndices.put(index.getIndexName(), index);

        // SAVE THE CONFIGURATION INTO THE GLOBAL CONFIG
        saveIndexConfiguration();

        return (Index<T>) index;
    }

    @SuppressWarnings("unchecked")
    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final OrientGraphContext context = getContext(true);
        Index<? extends Element> index = context.manualIndices.get(indexName);
        if (null == index) {
            index = context.autoIndices.get(indexName);
            if (null == index)
                return null;
        }

        if (indexClass.isAssignableFrom(index.getIndexClass()))
            return (Index<T>) index;
        else
            throw new RuntimeException("Can not convert " + index.getIndexClass() + " to " + indexClass);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        final OrientGraphContext context = getContext(true);
        final List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
        for (Index<?> index : context.manualIndices.values()) {
            list.add(index);
        }
        for (Index<?> index : context.autoIndices.values()) {
            list.add(index);
        }
        return list;
    }

    protected Iterable<OrientIndex<? extends OrientElement>> getManualIndices() {
        return getContext(true).manualIndices.values();
    }

    protected Iterable<OrientAutomaticIndex<? extends OrientElement>> getAutoIndices() {
        return getContext(true).autoIndices.values();
    }

    public void dropIndex(final String indexName) {

        this.autoStartTransaction();
        try {
            final OrientGraphContext context = getContext(true);
            context.manualIndices.remove(indexName);
            context.autoIndices.remove(indexName);

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
            autoStopTransaction(Conclusion.SUCCESS);
            return vertex;
        } catch (RuntimeException e) {
            autoStopTransaction(Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
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

            this.autoStopTransaction(Conclusion.SUCCESS);
            return edge;
        } catch (RuntimeException e) {
            this.autoStopTransaction(Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            throw new IllegalArgumentException("Element identifier cannot be null");

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
            AutomaticIndexHelper.removeElement(this, vertex);

            final Set<Edge> allEdges = new HashSet<Edge>();
            for (Edge e : oVertex.getInEdges())
                allEdges.add(e);
            for (Edge e : oVertex.getOutEdges())
                allEdges.add(e);

            for (Edge e : allEdges)
                AutomaticIndexHelper.removeElement(this, e);

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
            autoStopTransaction(Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            autoStopTransaction(Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Iterable<Vertex> getVertices() {
        return getVertices(true);
    }

    private Iterable<Vertex> getVertices(final boolean polymorphic) {
        final OGraphDatabase db = getRawGraph();
        return new OrientElementSequence<Vertex>(this, new ORecordIteratorClass<ORecordInternal<?>>(db, (ODatabaseRecordAbstract) db.getUnderlying(), OGraphDatabase.VERTEX_CLASS_NAME, polymorphic));
    }

    public Iterable<Edge> getEdges() {
        return getEdges(true);
    }

    private Iterable<Edge> getEdges(final boolean polymorphic) {
        final OGraphDatabase db = getRawGraph();
        return new OrientElementSequence<Edge>(this, new ORecordIteratorClass<ORecordInternal<?>>(db, (ODatabaseRecordAbstract) db.getUnderlying(), OGraphDatabase.EDGE_CLASS_NAME, polymorphic));
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            throw new IllegalArgumentException("Element identifier cannot be null");

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
            AutomaticIndexHelper.removeElement(this, edge);

            for (final Index<? extends Element> index : this.getManualIndices()) {
                if (Edge.class.isAssignableFrom(index.getIndexClass())) {
                    @SuppressWarnings("unchecked")
                    OrientIndex<OrientEdge> idx = (OrientIndex<OrientEdge>) index;
                    idx.removeElement(oEdge);
                }
            }

            getRawGraph().removeEdge(oEdge.rawElement);
            autoStopTransaction(Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            autoStopTransaction(Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * This operation does not respect the transaction buffer. A clear will eradicate the graph and commit the results immediately.
     */
    public void clear() {
        final OrientGraphContext context = getContext(true);
        final int previousMaxBufferSize = context.txBuffer;
        for (Index<? extends Element> index : this.getIndices()) {
            this.dropIndex(index.getIndexName());
        }
        this.getRawGraph().delete();
        this.threadContext.set(null);
        openOrCreate(false);
        this.setMaxBufferSize(previousMaxBufferSize);
        this.createAutomaticIndex(Index.VERTICES, OrientVertex.class, null);
        this.createAutomaticIndex(Index.EDGES, OrientEdge.class, null);
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

        if (context.rawGraph.getTransaction() instanceof OTransactionNoTx || context.rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN) {
            context.rawGraph.begin();
        } else
            throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (conclusion == Conclusion.FAILURE) {
            this.getRawGraph().rollback();
        } else
            this.getRawGraph().commit();
        this.getContext(true).txCounter = 0;
    }

    public void setMaxBufferSize(final int bufferSize) {
        getRawGraph().commit();
        getContext(true).txCounter = 0;
        getContext(true).txBuffer = bufferSize;
    }

    public int getMaxBufferSize() {
        return getContext(true).txBuffer;
    }

    public int getCurrentBufferSize() {
        return getContext(true).txCounter;
    }

    protected void saveIndexConfiguration() {
        getRawGraph().getMetadata().getIndexManager().getConfiguration().save();
    }

    protected void autoStartTransaction() {
        final OrientGraphContext context = getContext(true);
        if (context.txBuffer > 0) {
            if (context.rawGraph.getTransaction() instanceof OTransactionNoTx || context.rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN) {
                context.rawGraph.begin();
            }
        }
    }

    protected void autoStopTransaction(final Conclusion conclusion) {
        final OrientGraphContext context = getContext(true);
        if (context.txBuffer > 0) {
            context.txCounter = getContext(true).txCounter + 1;
            if (conclusion == Conclusion.FAILURE) {
                final OGraphDatabase db = getRawGraph();
                db.rollback();
                context.txCounter = 0;
            } else if (context.txCounter % context.txBuffer == 0) {
                final OGraphDatabase db = getRawGraph();
                db.commit();
                context.txCounter = 0;
            }
        }
    }

    protected OrientGraphContext getContext(final boolean create) {
        OrientGraphContext context = threadContext.get();
        if (context == null && create)
            context = openOrCreate(false);
        return context;
    }

    private OrientGraphContext openOrCreate(final boolean createDefaultIndices) {
        if (url == null)
            throw new IllegalStateException("Database is closed");

        synchronized (this) {
            OrientGraphContext context = threadContext.get();
            if (context != null)
                removeContext();

            context = new OrientGraphContext();
            this.threadContext.set(context);

            context.rawGraph = new OGraphDatabase(url);
            context.rawGraph.setUseCustomTypes(false);

            if (url.startsWith("remote:") || context.rawGraph.exists()) {
                context.rawGraph.open(username, password);

                // LOAD THE INDEX CONFIGURATION FROM INTO THE DICTIONARY
                final ODocument indexConfiguration = context.rawGraph.getMetadata().getIndexManager().getConfiguration();
                if (indexConfiguration == null)
                    createIndexConfiguration(context, createDefaultIndices);

                for (OIndex idx : context.rawGraph.getMetadata().getIndexManager().getIndexes()) {
                    if (idx.getConfiguration().field(OrientIndex.CONFIG_TYPE) != null)
                        // LOAD THE INDEXES
                        loadIndex(idx);
                }

            } else {
                context.rawGraph.create();

                // CREATE THE INDEX CONFIGURATION FOR IT AND SAVE IT INTO THE DICTIONARY
                createIndexConfiguration(context, createDefaultIndices);
            }

            return context;
        }
    }

    private void createIndexConfiguration(final OrientGraphContext context, final boolean createDefaultIndices) {
        if (createDefaultIndices) {
            createAutomaticIndex(Index.VERTICES, OrientVertex.class, null);
            createAutomaticIndex(Index.EDGES, OrientEdge.class, null);
        }
    }

    @SuppressWarnings("rawtypes")
    private OrientIndex<?> loadIndex(final OIndex rawIndex) {
        String indexType = rawIndex.getConfiguration().field(OrientIndex.CONFIG_TYPE);
        final OrientIndex<?> index;

        switch (Index.Type.valueOf(indexType.toUpperCase())) {
            case MANUAL:
                index = new OrientIndex(this, rawIndex);

                // REGISTER THE INDEX
                getContext(true).manualIndices.put(index.getIndexName(), index);
                break;

            case AUTOMATIC:
                index = new OrientAutomaticIndex(this, rawIndex);

                // REGISTER THE INDEX INTO THE AUTOMATIC INDEXES
                getContext(true).autoIndices.put(index.getIndexName(), (OrientAutomaticIndex<?>) index);
                break;

            default:
                throw new IllegalArgumentException("Index type '" + indexType + "' is not supported. Supported indicies: MANUAL, AUTOMATIC");
        }

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
            context.autoIndices.clear();

            this.threadContext.set(null);
        }
    }
}
