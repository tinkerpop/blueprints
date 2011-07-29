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
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;
import com.tinkerpop.blueprints.pgm.util.AutomaticIndexHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph implements TransactionalGraph, IndexableGraph {
    private final static String ADMIN = "admin";

    private final String url;
    private final String username;
    private final String password;

    private ThreadLocal<OrientGraphContext> threadContext = new ThreadLocal<OrientGraphContext>();

    public OrientGraph(final String url) {
        this(url, ADMIN, ADMIN);
    }

    public OrientGraph(final String url, final String username, final String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.openOrCreate(true);
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, final Set<String> indexKeys) {
        final OrientGraphContext context = getContext();
        if (context.autoIndices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        final OrientAutomaticIndex index = new OrientAutomaticIndex<OrientElement>(this, indexName, (Class<OrientElement>) indexClass, indexKeys);
        context.autoIndices.put(index.getIndexName(), index);

        // SAVE THE CONFIGURATION INTO THE GLOBAL CONFIG
        saveIndexConfiguration();

        return index;
    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        final OrientGraphContext context = getContext();
        if (context.manualIndices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        final OrientIndex index = new OrientIndex(this, indexName, indexClass, Index.Type.MANUAL, null);
        context.manualIndices.put(index.getIndexName(), index);

        // SAVE THE CONFIGURATION INTO THE GLOBAL CONFIG
        saveIndexConfiguration();

        return index;
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        final OrientGraphContext context = getContext();
        Index<?> index = context.manualIndices.get(indexName);
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
        final OrientGraphContext context = getContext();
        final List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
        for (Index<?> index : context.manualIndices.values()) {
            list.add(index);
        }
        for (Index<?> index : context.autoIndices.values()) {
            list.add(index);
        }
        return list;
    }

    protected Iterable<OrientIndex> getManualIndices() {
        return getContext().manualIndices.values();
    }

    protected Iterable<OrientAutomaticIndex> getAutoIndices() {
        return getContext().autoIndices.values();
    }

    public void dropIndex(final String iIndexName) {
        final OrientGraphContext context = getContext();
        if (context.manualIndices.remove(iIndexName) == null)
            context.autoIndices.remove(iIndexName);

        getRawGraph().getMetadata().getIndexManager().dropIndex(iIndexName);
        saveIndexConfiguration();
    }

    public Vertex addVertex(final Object id) {
        final OGraphDatabase db = getRawGraph();
        final boolean txBegun = autoStartTransaction();
        try {
            final OrientVertex vertex = new OrientVertex(this, db.createVertex(null));
            vertex.save();

            if (txBegun)
                autoStopTransaction(Conclusion.SUCCESS);

            return vertex;
        } catch (RuntimeException e) {
            if (txBegun)
                autoStopTransaction(Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            if (txBegun)
                autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        final OGraphDatabase db = getRawGraph();
        final boolean txBegun = autoStartTransaction();
        try {
            final ODocument edgeDoc = db.createEdge(((OrientVertex) outVertex).getRawElement(), ((OrientVertex) inVertex).getRawElement());
            final OrientEdge edge = new OrientEdge(this, edgeDoc);
            edge.setLabel(label);

            // SAVE THE VERTICES TO ASSURE THEY ARE IN TX
            db.save(((OrientVertex) outVertex).getRawElement());
            db.save(((OrientVertex) inVertex).getRawElement());
            edge.save();

            if (txBegun)
                autoStopTransaction(Conclusion.SUCCESS);

            return edge;

        } catch (RuntimeException e) {
            if (txBegun)
                autoStopTransaction(Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            if (txBegun)
                autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            return null;

        ORID rid;
        if (id instanceof ORID)
            rid = (ORID) id;
        else
            rid = new ORecordId(id.toString());

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

        final boolean txBegun = autoStartTransaction();
        try {
            AutomaticIndexHelper.removeElement(this, vertex);

            for (Index index : this.getManualIndices()) {
                if (Vertex.class.isAssignableFrom(index.getIndexClass())) {
                    OrientIndex<OrientVertex> idx = (OrientIndex<OrientVertex>) index;
                    idx.removeElement(oVertex);
                }
            }

            getRawGraph().removeVertex(oVertex.rawElement);

            if (txBegun)
                autoStopTransaction(Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            if (txBegun)
                autoStopTransaction(Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            if (txBegun)
                autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Iterable<Vertex> getVertices() {
        return getVertices(true);
    }

    private Iterable<Vertex> getVertices(final boolean iPolymorphic) {
        final OGraphDatabase db = getRawGraph();
        return new OrientElementSequence<Vertex>(this, new ORecordIteratorClass<ORecordInternal<?>>(db, (ODatabaseRecordAbstract) db.getUnderlying(), OGraphDatabase.VERTEX_CLASS_NAME, iPolymorphic));
    }

    public Iterable<Edge> getEdges() {
        return getEdges(true);
    }

    private Iterable<Edge> getEdges(final boolean iPolymorphic) {
        final OGraphDatabase db = getRawGraph();
        return new OrientElementSequence<Edge>(this, new ORecordIteratorClass<ORecordInternal<?>>(db, (ODatabaseRecordAbstract) db.getUnderlying(), OGraphDatabase.EDGE_CLASS_NAME, iPolymorphic));
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            return null;

        final ORID rid;
        if (id instanceof ORID)
            rid = (ORID) id;
        else
            rid = new ORecordId(id.toString());

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

        final boolean txBegun = autoStartTransaction();
        try {
            AutomaticIndexHelper.removeElement(this, edge);

            for (Index index : this.getManualIndices()) {
                if (Edge.class.isAssignableFrom(index.getIndexClass())) {
                    OrientIndex<OrientEdge> idx = (OrientIndex<OrientEdge>) index;
                    idx.removeElement(oEdge);
                }
            }

            getRawGraph().removeEdge(oEdge.rawElement);

            if (txBegun)
                autoStopTransaction(Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            if (txBegun)
                autoStopTransaction(Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            if (txBegun)
                autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void clear() {
        final OrientGraphContext context = getContext();

        for (Index<? extends Element> idx : getIndices()) {
            ((OrientIndex<?>) idx).close();
        }
        context.manualIndices.clear();
        context.autoIndices.clear();

        this.getRawGraph().delete();
        this.threadContext.set(null);
        openOrCreate(false);
        this.createAutomaticIndex(Index.VERTICES, OrientVertex.class, null);
        this.createAutomaticIndex(Index.EDGES, OrientEdge.class, null);
    }

    public void shutdown() {
        final OrientGraphContext context = getContext();

        if (context != null) {
            context.rawGraph.rollback();
            context.rawGraph.close();

            for (Index<? extends Element> idx : getIndices()) {
                ((OrientIndex<?>) idx).close();
            }
            context.manualIndices.clear();
            context.autoIndices.clear();

            this.threadContext.set(null);
        }
    }

    public String toString() {
        return "orientgraph[" + getRawGraph().getURL() + "]";
    }

    public OGraphDatabase getRawGraph() {
        return getContext().rawGraph;
    }

    public void startTransaction() {
        final OrientGraphContext context = getContext();

        if (Mode.AUTOMATIC == context.txMode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
        if (context.rawGraph.getTransaction() instanceof OTransactionNoTx || context.rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN) {
            context.rawGraph.begin();
        } else
            throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (Mode.AUTOMATIC == getContext().txMode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);

        if (conclusion == Conclusion.FAILURE) {
            this.getRawGraph().rollback();
        } else
            this.getRawGraph().commit();
    }

    public void setTransactionMode(final Mode mode) {
        getRawGraph().commit();
        getContext().txMode = mode;
    }

    public Mode getTransactionMode() {
        return getContext().txMode;
    }

    protected void saveIndexConfiguration() {
    }

    protected boolean autoStartTransaction() {
        final OGraphDatabase db = getRawGraph();
        if (getTransactionMode() == Mode.AUTOMATIC && (db.getTransaction() instanceof OTransactionNoTx || db.getTransaction().getStatus() != TXSTATUS.BEGUN)) {
            db.begin();
            return true;
        }
        return false;
    }

    protected void autoStopTransaction(final Conclusion conclusion) {
        final OGraphDatabase db = getRawGraph();
        if (getTransactionMode() == Mode.AUTOMATIC) {
            if (conclusion == Conclusion.SUCCESS)
                db.commit();
            else
                db.rollback();
        }
    }

    protected OrientGraphContext getContext() {
        OrientGraphContext context = threadContext.get();

        if (context == null)
            context = openOrCreate(false);

        return context;
    }

    private OrientGraphContext openOrCreate(final boolean createDefaultIndices) {
        synchronized (this) {
            OrientGraphContext context = threadContext.get();
            if (context != null)
                shutdown();

            context = new OrientGraphContext();
            this.threadContext.set(context);

            context.rawGraph = new OGraphDatabase(url);

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
                getContext().manualIndices.put(index.getIndexName(), index);
                break;

            case AUTOMATIC:
                index = new OrientAutomaticIndex(this, rawIndex);

                // REGISTER THE INDEX INTO THE AUTOMATIC INDEXES
                getContext().autoIndices.put(index.getIndexName(), (OrientAutomaticIndex<?>) index);
                break;

            default:
                throw new IllegalArgumentException("Index type '" + indexType + "' is not supported. Supported indicies: MANUAL, AUTOMATIC");
        }

        return index;
    }
}
