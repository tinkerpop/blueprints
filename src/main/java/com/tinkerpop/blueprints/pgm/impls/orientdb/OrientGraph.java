package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.ODatabaseGraphTx;
import com.orientechnologies.orient.core.db.graph.OGraphEdge;
import com.orientechnologies.orient.core.db.graph.OGraphVertex;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.iterator.OGraphEdgeIterator;
import com.orientechnologies.orient.core.iterator.OGraphVertexIterator;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OrientDB implementation of Graph interface. This implementation is transactional.
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph implements TransactionalGraph, IndexableGraph {

    private ODatabaseGraphTx database;

    private final String url;
    private final String username;
    private final String password;

    private Mode mode = Mode.AUTOMATIC;
    private final static String ADMIN = "admin";

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

    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Index.Type type) {
        OrientIndex index;
        if (type == Index.Type.MANUAL) {
            index = new OrientIndex(indexName, indexClass, this);
        } else {
            index = new OrientAutomaticIndex(indexName, indexClass, null, this);
            this.autoIndices.put(index.getIndexName(), (OrientAutomaticIndex) index);
        }
        this.indices.put(index.getIndexName(), index);
        return index;

    }

    public <T extends Element> Index<T> getIndex(String indexName, Class<T> indexClass) {
        Index index = this.indices.get(indexName);
        if (indexClass.isAssignableFrom(index.getIndexClass()))
            return (Index<T>) index;
        else
            throw new RuntimeException("Can not convert " + index.getIndexClass() + " to " + indexClass);
    }

    public Iterable<Index> getIndices() {
        List<Index> list = new ArrayList<Index>();
        for (Index index : this.indices.values()) {
            list.add(index);
        }
        return list;
    }

    protected Iterable<OrientAutomaticIndex> getAutoIndices() {
        return this.autoIndices.values();
    }

    public void dropIndex(String indexName) {
        OrientIndex index = this.indices.get(indexName);
        index.clear();
        this.indices.remove(indexName);
        this.autoIndices.remove(indexName);
    }

    public Vertex addVertex(final Object id) {
        try {
            beginTransaction();
            final OrientVertex vertex = new OrientVertex(this, this.database.createVertex(null));
            vertex.save();
            commitTransaction();
            return vertex;
        } catch (RuntimeException e) {
            rollbackTransaction();
            throw e;
        }
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        try {
            beginTransaction();

            final OrientEdge edge = new OrientEdge(this, ((OrientVertex) outVertex).getRawVertex().link(((OrientVertex) inVertex).getRawVertex()));
            edge.setLabel(label);

            ((OrientVertex) outVertex).getRawVertex().getDocument().setDirty();
            ((OrientVertex) inVertex).getRawVertex().getDocument().setDirty();

            // SAVE THE VERTICES TO ASSURE THEY ARE IN TX
            ((OrientVertex) outVertex).save();
            ((OrientVertex) inVertex).save();
            edge.save();

            commitTransaction();

            return edge;

        } catch (RuntimeException e) {
            rollbackTransaction();
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
            beginTransaction();

            // removal requires removal from all indices
            for (String key : vertex.getPropertyKeys()) {
                for (Index index : this.indices.values()) {
                    if (vertex.getClass().isAssignableFrom(index.getIndexClass()))
                        index.remove(key, vertex.getProperty(key), vertex);
                }
            }

            ((OrientVertex) vertex).delete();
            commitTransaction();
        } catch (RuntimeException e) {
            rollbackTransaction();
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
            beginTransaction();

            // removal requires removal from all indices
            for (String key : edge.getPropertyKeys()) {
                for (Index index : this.indices.values()) {
                    if (edge.getClass().isAssignableFrom(index.getIndexClass()))
                        index.remove(key, edge.getProperty(key), edge);
                }
            }

            ((OrientEdge) edge).delete();
            commitTransaction();
        } catch (RuntimeException e) {
            rollbackTransaction();
            throw e;
        }
    }

    public void clear() {
        for (OrientIndex index : indices.values()) {
            index.clear();
        }
        this.database.delete();
        //this.database.commit();
        this.database = null;
        this.indices.clear();
        this.autoIndices.clear();
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
            for (Index index : this.indices.values())
                ((OrientIndex) index).getRawIndex().unload();
        } else
            this.database.commit();
    }

    public void setTransactionMode(final Mode mode) {
        this.mode = mode;
    }

    public Mode getTransactionMode() {
        return this.mode;
    }

    protected void beginTransaction() {
        if (getTransactionMode() == Mode.AUTOMATIC)
            this.database.begin();
    }

    protected void commitTransaction() {
        if (getTransactionMode() == Mode.AUTOMATIC)
            this.database.commit();
    }

    protected void rollbackTransaction() {
        if (getTransactionMode() == Mode.AUTOMATIC) {
            this.database.rollback();
            for (Index index : this.indices.values())
                ((OrientIndex) index).getRawIndex().unload();
        }
    }

    private void openOrCreate() {
        this.database = new ODatabaseGraphTx(url);
        if (this.database.exists())
            this.database.open(username, password);
        else {
            this.database.create();
            this.createIndex(Index.VERTICES, OrientVertex.class, Index.Type.AUTOMATIC);
            this.createIndex(Index.EDGES, OrientEdge.class, Index.Type.AUTOMATIC);
        }
    }
}
