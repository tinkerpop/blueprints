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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * OrientDB implementation of Graph interface. This implementation is transactional.
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph implements TransactionalGraph, IndexableGraph {
    static final String DICTIONARY_INDEXES = "graphIndexes";
    static final String FIELD_INDEXES = "indexes";
    static final String FIELD_TYPE = "indexType";
    static final String FIELD_CLASSNAME = "indexClass";
    static final String FIELD_TREEMAP_RID = "indexTreeMapRid";

    private ODatabaseGraphTx database;

    private final String url;
    private final String username;
    private final String password;

    private Mode mode = Mode.AUTOMATIC;
    private final static String ADMIN = "admin";

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
        openOrCreate();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Index.Type type) {
        final ODocument indexCfg = new ODocument((ODatabaseDocumentTx) database.getUnderlying());

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
            className = "Vertex";
        else if (Edge.class.isAssignableFrom(indexClass))
            className = "Edge";
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

    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass) {
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

    protected Iterable<OrientAutomaticIndex> getAutoIndices() {
        return this.autoIndices.values();
    }

    public void dropIndex(String indexName) {
        OrientIndex<?> index = this.indices.get(indexName);
        index.clear();
        this.indices.remove(indexName);
        this.autoIndices.remove(indexName);
    }

    public Vertex addVertex(final Object id) {
        try {
            autoStartTransaction();
            final OrientVertex vertex = new OrientVertex(this, this.database.createVertex(null));
            vertex.save();
            autoStopTransaction(Conclusion.SUCCESS);
            return vertex;
        } catch (RuntimeException e) {
            autoStopTransaction(Conclusion.FAILURE);
            throw e;
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

        final ODocument doc = this.database.getRecordById(rid);
        if (doc != null)
            return new OrientVertex(this, (OGraphVertex) this.database.getUserObjectByRecord(doc, null));
        else
            return new OrientVertex(this, (OGraphVertex) this.database.load(rid));
    }

    public void removeVertex(final Vertex vertex) {
        try {
            autoStartTransaction();

            for (OrientIndex<?> index : indices.values())
                index.removeElement(vertex);

            ((OrientVertex) vertex).delete();
            autoStopTransaction(Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            autoStopTransaction(Conclusion.FAILURE);
            throw e;
        }
    }

    public Iterable<Vertex> getVertices() {
        return new OrientElementSequence<Vertex>(this, new OGraphVertexIterator(this.database));
    }

    public Iterable<Edge> getEdges() {
        return new OrientElementSequence<Edge>(this, new OGraphEdgeIterator(this.database));
    }

    public Edge getEdge(final Object id) {
        final ORID rid;
        if (id instanceof ORID)
            rid = (ORID) id;
        else
            rid = new ORecordId(id.toString());

        final ODocument doc = this.database.getRecordById(rid);
        if (doc != null)
            return new OrientEdge(this, (OGraphEdge) this.database.getUserObjectByRecord(doc, null));
        else
            return new OrientEdge(this, (OGraphEdge) this.database.load(rid));
    }

    public void removeEdge(final Edge edge) {
        try {
            autoStartTransaction();

            for (OrientIndex<?> index : indices.values())
                index.removeElement(edge);

            ((OrientEdge) edge).delete();
            autoStopTransaction(Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            autoStopTransaction(Conclusion.FAILURE);
            throw e;
        }
    }

    public void clear() {
        for (Index index : this.getIndices()) {
            this.dropIndex(index.getIndexName());
        }
        this.database.delete();
        this.database = null;
        openOrCreate();
    }

    public void shutdown() {
        this.database.close();
        this.database = null;
        this.indices.clear();
        this.autoIndices.clear();
    }

    public String toString() {
        return "orientgraph[" + this.database.getURL() + "]";
    }

    public ODatabaseGraphTx getRawGraph() {
        return this.database;
    }

    public void startTransaction() {
        if (Mode.AUTOMATIC == this.mode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);
        this.database.begin();
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (Mode.AUTOMATIC == this.mode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);

        if (conclusion == Conclusion.FAILURE) {
            this.database.rollback();
            for (Index<?> index : this.indices.values())
                ((OrientIndex<?>) index).getRawIndex().unload();
        } else
            this.database.commit();
    }

    public void setTransactionMode(final Mode mode) {
        this.mode = mode;
    }

    public Mode getTransactionMode() {
        return this.mode;
    }

    public ODocument getIndexConfiguration() {
        return indexConfiguration;
    }

    void saveIndexConfiguration() {
        indexConfiguration.setDirty();
        indexConfiguration.save();
    }

    protected boolean autoStartTransaction() {
        if (getTransactionMode() == Mode.AUTOMATIC && (database.getTransaction() instanceof OTransactionNoTx || database.getTransaction().getStatus() != TXSTATUS.BEGUN)) {
            this.database.begin();
            return true;
        }
        return false;
    }

    protected void autoStopTransaction(Conclusion conclusion) {
        if (getTransactionMode() == Mode.AUTOMATIC) {
            if (conclusion == Conclusion.SUCCESS)
                this.database.commit();
            else {
                this.database.rollback();
                for (Index<?> index : this.indices.values())
                    ((OrientIndex<?>) index).getRawIndex().unload();
            }

        }
    }

    private void openOrCreate() {
        this.database = new ODatabaseGraphTx(url);
        if (this.database.exists()) {
            this.database.open(username, password);

            // LOAD THE INDEX CONFIGURATION FROM INTO THE DICTIONARY
            indexConfiguration = ((ODatabaseDocumentTx) database.getUnderlying()).getDictionary().get(DICTIONARY_INDEXES);
            if (indexConfiguration == null)
                createIndexConfiguration();
            else {
                final Map<String, ODocument> indexes = indexConfiguration.field(FIELD_INDEXES);
                // LOAD THE INDEXES
                for (Entry<String, ODocument> idx : indexes.entrySet())
                    loadIndex(idx.getKey(), idx.getValue());
            }

        } else {
            this.database.create();

            // CREATE THE INDEX CONFIGURATION FOR IT AND SAVE IT INTO THE DICTIONARY
            createIndexConfiguration();
        }
    }

    private void createIndexConfiguration() {
        indexConfiguration = new ODocument((ODatabaseDocumentTx) database.getUnderlying());
        indexConfiguration.field(FIELD_INDEXES, new HashMap<String, ORecordId>(), OType.EMBEDDEDMAP);

        this.createIndex(Index.VERTICES, OrientVertex.class, Index.Type.AUTOMATIC);
        this.createIndex(Index.EDGES, OrientEdge.class, Index.Type.AUTOMATIC);

        ((ODatabaseDocumentTx) database.getUnderlying()).getDictionary().put(DICTIONARY_INDEXES, indexConfiguration);
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
