package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.graph.ODatabaseGraphTx;
import com.orientechnologies.orient.core.db.graph.OGraphEdge;
import com.orientechnologies.orient.core.db.graph.OGraphVertex;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.iterator.OGraphEdgeIterator;
import com.orientechnologies.orient.core.iterator.OGraphVertexIterator;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.tx.OTransaction.TXSTATUS;
import com.orientechnologies.orient.core.tx.OTransactionNoTx;
import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;
import com.tinkerpop.blueprints.pgm.util.AutomaticIndexHelper;

import java.util.*;
import java.util.Map.Entry;

/**
 * A Blueprints implementation of the graph database OrientDB (http://www.orientechnologies.com)
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph implements TransactionalGraph, IndexableGraph {
    protected static final String DICTIONARY_INDEXES = "graphIndexes";
    protected static final String FIELD_INDEXES = "indexes";
    protected static final String FIELD_TYPE = "indexType";
    protected static final String FIELD_CLASSNAME = "indexClass";
    protected static final String FIELD_TREEMAP_RID = "indexTreeMapRid";
    protected static final String VERTEX = "Vertex";
    private static final String EDGE = "Edge";
    private final static String ADMIN = "admin";

    private ODatabaseGraphTx rawGraph;

    private final String url;
    private final String username;
    private final String password;

    private Mode mode = Mode.AUTOMATIC;

    private ODocument indexConfiguration;
    protected Map<String, OrientIndex> indices = new HashMap<String, OrientIndex>();
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

    public <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass, final Index.Type type) {
        if (this.indices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        final ODocument indexCfg = new ODocument((ODatabaseDocumentTx) rawGraph.getUnderlying());

        OrientIndex<? extends OrientElement> index;
        if (type == Index.Type.MANUAL) {
            index = new OrientIndex(indexName, indexClass, this, indexCfg);
        } else {
            index = new OrientAutomaticIndex(indexName, indexClass, this, indexCfg);
            this.autoIndices.put(index.getIndexName(), (OrientAutomaticIndex<?>) index);
        }
        this.indices.put(index.getIndexName(), index);

        final String className;
        if (Vertex.class.isAssignableFrom(indexClass))
            className = VERTEX;
        else if (Edge.class.isAssignableFrom(indexClass))
            className = EDGE;
        else
            className = indexClass.getName();

        // CREATE THE CONFIGURATION FOR THE NEW INDEX
        indexCfg.field(FIELD_CLASSNAME, className);
        indexCfg.field(FIELD_TYPE, type.toString());
        indexCfg.field(FIELD_TREEMAP_RID, index.getRawIndex().getRecord().getIdentity());

        // SAVE THE CONFIGURATION INTO THE GLOBAL CONFIG
        Map<String, ODocument> indexes = indexConfiguration.field(OrientGraph.FIELD_INDEXES);
        indexes.put(indexName, indexCfg);
        saveIndexConfiguration();

        return (Index<T>) index;
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        Index<?> index = this.indices.get(indexName);
        if (null == index)
            throw new RuntimeException("No such index exists: " + indexName);

        if (indexClass.isAssignableFrom(index.getIndexClass()))
            return (Index<T>) index;
        else
            throw new RuntimeException("Can not convert " + index.getIndexClass() + " to " + indexClass);
    }

    public Iterable<Index<? extends Element>> getIndices() {
        List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
        for (Index<?> index : this.indices.values()) {
            list.add(index);
        }
        return list;
    }

    protected Iterable<OrientIndex> getManualIndices() {
        HashSet<OrientIndex> indices = new HashSet<OrientIndex>(this.indices.values());
        indices.removeAll(this.autoIndices.values());
        return indices;
    }

    protected Iterable<OrientAutomaticIndex> getAutoIndices() {
        return this.autoIndices.values();
    }

    public void dropIndex(String indexName) {
        OrientIndex<?> index = this.indices.get(indexName);
        index.clear();
        this.indices.remove(indexName);
        this.autoIndices.remove(indexName);

        final Map<String, ODocument> indexes = indexConfiguration.field(OrientGraph.FIELD_INDEXES);
        indexes.remove(indexName);
        saveIndexConfiguration();
    }

    public Vertex addVertex(final Object id) {
        try {
            autoStartTransaction();
            final OrientVertex vertex = new OrientVertex(this, this.rawGraph.createVertex(null));
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
        try {
            autoStartTransaction();

            final OrientEdge edge = new OrientEdge(this, ((OrientVertex) outVertex).getRawVertex().link(((OrientVertex) inVertex).getRawVertex()));
            edge.setLabel(label);

            ((OrientVertex) outVertex).getRawVertex().getDocument().setDirty();
            ((OrientVertex) inVertex).getRawVertex().getDocument().setDirty();

            // SAVE THE VERTICES TO ASSURE THEY ARE IN TX
            ((OrientVertex) outVertex).save();
            ((OrientVertex) inVertex).save();
            edge.save();

            autoStopTransaction(Conclusion.SUCCESS);

            return edge;

        } catch (RuntimeException e) {
            autoStopTransaction(Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
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

        final ODocument doc = this.rawGraph.getRecordById(rid);
        if (doc != null)
            return new OrientVertex(this, (OGraphVertex) this.rawGraph.getUserObjectByRecord(doc, null));
        else {
            OGraphVertex v = (OGraphVertex) this.rawGraph.load(rid);
            if (v != null)
                return new OrientVertex(this, (OGraphVertex) this.rawGraph.load(rid));
            else
                return null;
        }
    }

    public void removeVertex(final Vertex vertex) {
        try {
            AutomaticIndexHelper.unIndexElement(this, vertex);

            autoStartTransaction();

            for (Index index : this.getManualIndices()) {
                if (Vertex.class.isAssignableFrom(index.getIndexClass())) {
                    OrientIndex<OrientVertex> idx = (OrientIndex<OrientVertex>) index;
                    idx.removeElement((OrientVertex) vertex);
                }
            }

            ((OrientVertex) vertex).delete();
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
        return new OrientElementSequence<Vertex>(this, new OGraphVertexIterator(this.rawGraph, true));
    }

    public Iterable<Edge> getEdges() {
        return new OrientElementSequence<Edge>(this, new OGraphEdgeIterator(this.rawGraph, true));
    }

    public Edge getEdge(final Object id) {
        final ORID rid;
        if (id instanceof ORID)
            rid = (ORID) id;
        else
            rid = new ORecordId(id.toString());

        final ODocument doc = this.rawGraph.getRecordById(rid);
        if (doc != null)
            return new OrientEdge(this, (OGraphEdge) this.rawGraph.getUserObjectByRecord(doc, null));
        else {
            OGraphEdge edge = (OGraphEdge) this.rawGraph.load(rid);
            if (edge != null)
                return new OrientEdge(this, edge);
            else
                return null;
        }
    }

    public void removeEdge(final Edge edge) {
        try {
            AutomaticIndexHelper.unIndexElement(this, edge);
            autoStartTransaction();

            for (Index index : this.getManualIndices()) {
                if (Edge.class.isAssignableFrom(index.getIndexClass())) {
                    OrientIndex<OrientEdge> idx = (OrientIndex<OrientEdge>) index;
                    idx.removeElement((OrientEdge) edge);
                }
            }

            ((OrientEdge) edge).delete();
            autoStopTransaction(Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            autoStopTransaction(Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void clear() {
        this.indices.clear();
        this.autoIndices.clear();
        this.rawGraph.delete();
        this.rawGraph = null;
        openOrCreate(false);
    }

    public void shutdown() {
        this.rawGraph.rollback();
        this.rawGraph.close();
        this.rawGraph = null;
        this.indices.clear();
        this.autoIndices.clear();
    }

    public String toString() {
        return "orientgraph[" + this.rawGraph.getURL() + "]";
    }

    public ODatabaseGraphTx getRawGraph() {
        return this.rawGraph;
    }

    public void startTransaction() {
        if (Mode.AUTOMATIC == this.mode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
        if (rawGraph.getTransaction() instanceof OTransactionNoTx || rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN) {
            this.rawGraph.begin();
        } else
            throw new RuntimeException(TransactionalGraph.NESTED_MESSAGE);
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (Mode.AUTOMATIC == this.mode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);

        if (conclusion == Conclusion.FAILURE) {
            this.rawGraph.rollback();
            for (Index<?> index : this.indices.values())
                ((OrientIndex<?>) index).getRawIndex().unload();
        } else
            this.rawGraph.commit();
    }

    public void setTransactionMode(final Mode mode) {
        this.mode = mode;
    }

    public Mode getTransactionMode() {
        return this.mode;
    }

    void saveIndexConfiguration() {
        indexConfiguration.setDirty();
        indexConfiguration.save();
    }

    protected boolean autoStartTransaction() {
        if (getTransactionMode() == Mode.AUTOMATIC && (rawGraph.getTransaction() instanceof OTransactionNoTx || rawGraph.getTransaction().getStatus() != TXSTATUS.BEGUN)) {
            this.rawGraph.begin();
            return true;
        }
        return false;
    }

    protected void autoStopTransaction(final Conclusion conclusion) {
        if (getTransactionMode() == Mode.AUTOMATIC) {
            if (conclusion == Conclusion.SUCCESS)
                this.rawGraph.commit();
            else {
                this.rawGraph.rollback();
                for (Index<?> index : this.indices.values())
                    ((OrientIndex<?>) index).getRawIndex().unload();
            }

        }
    }

    private void openOrCreate(final boolean createDefaultIndices) {
        this.rawGraph = new ODatabaseGraphTx(url);
        if (this.rawGraph.exists()) {
            this.rawGraph.open(username, password);

            // LOAD THE INDEX CONFIGURATION FROM INTO THE DICTIONARY
            indexConfiguration = ((ODatabaseDocumentTx) rawGraph.getUnderlying()).getDictionary().get(DICTIONARY_INDEXES);
            if (indexConfiguration == null)
                createIndexConfiguration(createDefaultIndices);
            else {
                final Map<String, ODocument> indexes = indexConfiguration.field(FIELD_INDEXES);
                // LOAD THE INDEXES
                if (null != indexes) {
                    for (Entry<String, ODocument> idx : indexes.entrySet()) {
                        loadIndex(idx.getKey(), idx.getValue());
                    }
                } else {
                    throw new IllegalStateException("no indexes found");
                }
            }

        } else {
            this.rawGraph.create();

            // CREATE THE INDEX CONFIGURATION FOR IT AND SAVE IT INTO THE DICTIONARY
            createIndexConfiguration(createDefaultIndices);
        }
    }

    private void createIndexConfiguration(final boolean createDefaultIndices) {
        indexConfiguration = new ODocument((ODatabaseDocumentTx) rawGraph.getUnderlying());
        indexConfiguration.field(FIELD_INDEXES, new HashMap<String, ORecordId>(), OType.EMBEDDEDMAP);

        if (createDefaultIndices) {
            this.createIndex(Index.VERTICES, OrientVertex.class, Index.Type.AUTOMATIC);
            this.createIndex(Index.EDGES, OrientEdge.class, Index.Type.AUTOMATIC);
        }

        ((ODatabaseDocumentTx) rawGraph.getUnderlying()).getDictionary().put(DICTIONARY_INDEXES, indexConfiguration);
    }

    @SuppressWarnings("unchecked")
    private OrientIndex<?> loadIndex(final String indexName, final ODocument indexCfg) {
        final String indexType = indexCfg.field(FIELD_TYPE);

        final OrientIndex<?> index;

        switch (Index.Type.valueOf(indexType.toUpperCase())) {
            case MANUAL:
                index = new OrientIndex(indexName, null, this, indexCfg);
                break;
            case AUTOMATIC:
                //index = new OrientAutomaticIndex(indexName, null, this, indexCfg);
                index = new OrientAutomaticIndex(indexName, this, indexCfg);
                // REGISTER THE INDEX INTO THE AUTOMATIC INDEXES
                this.autoIndices.put(index.getIndexName(), (OrientAutomaticIndex<?>) index);
                break;
            default:
                throw new IllegalArgumentException("Index type '" + indexCfg.field(FIELD_TYPE).toString() + "' is not supported. Supported indicies: MANUAL, AUTOMATIC");
        }

        // REGISTER THE INDEX
        this.indices.put(index.getIndexName(), index);

        return index;
    }
}
