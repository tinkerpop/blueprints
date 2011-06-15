package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.ODatabaseRecordAbstract;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexManagerImpl;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.tx.OTransaction.TXSTATUS;
import com.orientechnologies.orient.core.tx.OTransactionNoTx;
import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;
import com.tinkerpop.blueprints.pgm.util.AutomaticIndexHelper;

import java.util.*;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph implements TransactionalGraph, IndexableGraph {
    private final static String ADMIN = "admin";

    private final ThreadLocal<OGraphDatabase> rawGraph = new ThreadLocal<OGraphDatabase>();

    private final String url;
    private final String username;
    private final String password;

    private Mode txMode = Mode.AUTOMATIC;

    protected Map<String, OrientIndex> manualIndices = new HashMap<String, OrientIndex>();
    protected Map<String, OrientAutomaticIndex> autoIndices = new HashMap<String, OrientAutomaticIndex>();

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
        if (this.autoIndices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        final OrientAutomaticIndex index = new OrientAutomaticIndex<OrientElement>(this, indexName, (Class<OrientElement>) indexClass, indexKeys);
        this.autoIndices.put(index.getIndexName(), index);

        // SAVE THE CONFIGURATION INTO THE GLOBAL CONFIG
        saveIndexConfiguration();

        return index;
    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        if (this.manualIndices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        final OrientIndex index = new OrientIndex(this, indexName, indexClass, Index.Type.MANUAL);
        this.manualIndices.put(index.getIndexName(), index);

        // SAVE THE CONFIGURATION INTO THE GLOBAL CONFIG
        saveIndexConfiguration();

        return index;
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        Index<?> index = this.manualIndices.get(indexName);
        if (null == index) {
            index = this.autoIndices.get(indexName);
            if (null == index)
                throw new RuntimeException("No such index exists: " + indexName);
        }

        if (indexClass.isAssignableFrom(index.getIndexClass()))
            return (Index<T>) index;
        else
            throw new RuntimeException("Can not convert " + index.getIndexClass() + " to " + indexClass);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        final List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
        for (Index<?> index : this.manualIndices.values()) {
            list.add(index);
        }
        for (Index<?> index : this.autoIndices.values()) {
            list.add(index);
        }
        return list;
    }

    protected Iterable<OrientIndex> getManualIndices() {
        return this.manualIndices.values();
    }

    protected Iterable<OrientAutomaticIndex> getAutoIndices() {
        return this.autoIndices.values();
    }

    public void dropIndex(final String iIndexName) {
        if (this.manualIndices.remove(iIndexName) == null)
            this.autoIndices.remove(iIndexName);

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
        final OGraphDatabase db = rawGraph.get();
        return new OrientElementSequence<Vertex>(this, new ORecordIteratorClass<ORecordInternal<?>>(db, (ODatabaseRecordAbstract) db.getUnderlying(), OGraphDatabase.VERTEX_CLASS_NAME, iPolymorphic));
    }

    public Iterable<Edge> getEdges() {
        return getEdges(true);
    }

    private Iterable<Edge> getEdges(final boolean iPolymorphic) {
        final OGraphDatabase db = rawGraph.get();
        return new OrientElementSequence<Edge>(this, new ORecordIteratorClass<ORecordInternal<?>>(db, (ODatabaseRecordAbstract) db.getUnderlying(), OGraphDatabase.EDGE_CLASS_NAME, iPolymorphic));
    }

    public Edge getEdge(final Object id) {
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
        this.manualIndices.clear();
        this.autoIndices.clear();
        this.getRawGraph().delete();
        this.rawGraph.set(null);
        openOrCreate(false);
    }

    public void shutdown() {
        final OGraphDatabase db = getRawGraph();

        if (db != null) {
            db.rollback();
            db.close();
            this.rawGraph.set(null);
        }

        this.manualIndices.clear();
        this.autoIndices.clear();
    }

    public String toString() {
        return "orientgraph[" + getRawGraph().getURL() + "]";
    }

    public OGraphDatabase getRawGraph() {
        OGraphDatabase db = this.rawGraph.get();

        if (db == null)
            db = openOrCreate(false);

        return db;
    }

    public void startTransaction() {
        final OGraphDatabase db = getRawGraph();

        if (Mode.AUTOMATIC == txMode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
        if (db.getTransaction() instanceof OTransactionNoTx || db.getTransaction().getStatus() != TXSTATUS.BEGUN) {
            db.begin();
        } else
            throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (Mode.AUTOMATIC == txMode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);

        if (conclusion == Conclusion.FAILURE) {
            this.getRawGraph().rollback();
            for (Index<?> index : this.manualIndices.values())
                ((OrientIndex<?>) index).getRawIndex().unload();
            for (Index<?> index : this.autoIndices.values())
                ((OrientIndex<?>) index).getRawIndex().unload();
        } else
            this.getRawGraph().commit();
    }

    public void setTransactionMode(final Mode mode) {
        getRawGraph().commit();
        txMode = mode;
    }

    public Mode getTransactionMode() {
        return txMode;
    }

    protected void saveIndexConfiguration() {
        OIndexManagerImpl idxMgr = (OIndexManagerImpl) getRawGraph().getMetadata().getIndexManager();
        idxMgr.setDirty();
        idxMgr.save();
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
            else {
                db.rollback();
                for (Index<?> index : this.manualIndices.values())
                    ((OrientIndex<?>) index).getRawIndex().unload();
                for (Index<?> index : this.autoIndices.values())
                    ((OrientIndex<?>) index).getRawIndex().unload();
            }

        }
    }

    private OGraphDatabase openOrCreate(final boolean createDefaultIndices) {
        final OGraphDatabase db = new OGraphDatabase(url);
        this.rawGraph.set(db);

        if (url.startsWith("remote:") || db.exists()) {
            db.open(username, password);

            // LOAD THE INDEX CONFIGURATION FROM INTO THE DICTIONARY
            final ODocument indexConfiguration = ((OIndexManagerImpl) db.getMetadata().getIndexManager()).getDocument();
            if (indexConfiguration == null)
                createIndexConfiguration(createDefaultIndices);

            for (OIndex idx : db.getMetadata().getIndexManager().getIndexes()) {
                if (idx.getConfiguration().field(OrientIndex.CONFIG_TYPE) != null)
                    // LOAD THE INDEXES
                    loadIndex(idx);
            }

        } else {
            db.create();

            // CREATE THE INDEX CONFIGURATION FOR IT AND SAVE IT INTO THE DICTIONARY
            createIndexConfiguration(createDefaultIndices);
        }
        return db;
    }

    private void createIndexConfiguration(final boolean createDefaultIndices) {
        if (createDefaultIndices) {
            this.createAutomaticIndex(Index.VERTICES, OrientVertex.class, null);
            this.createAutomaticIndex(Index.EDGES, OrientEdge.class, null);
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
                this.manualIndices.put(index.getIndexName(), index);
                break;

            case AUTOMATIC:
                index = new OrientAutomaticIndex(this, rawIndex);

                // REGISTER THE INDEX INTO THE AUTOMATIC INDEXES
                this.autoIndices.put(index.getIndexName(), (OrientAutomaticIndex<?>) index);
                break;
            default:
                throw new IllegalArgumentException("Index type '" + indexType + "' is not supported. Supported indicies: MANUAL, AUTOMATIC");
        }

        return index;
    }
}
