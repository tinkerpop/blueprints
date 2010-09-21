package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.graph.ODatabaseGraphTx;
import com.orientechnologies.orient.core.db.graph.OGraphEdge;
import com.orientechnologies.orient.core.db.graph.OGraphElement;
import com.orientechnologies.orient.core.db.graph.OGraphVertex;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.iterator.OGraphEdgeIterator;
import com.orientechnologies.orient.core.iterator.OGraphVertexIterator;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

import java.util.HashMap;
import java.util.Map;

/**
 * OrientDB implementation of Graph interface. This implementation is transactional.
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph implements Graph, TransactionalGraph {
    private final ODatabaseGraphTx database;
    private OrientIndex index;
    private final Map<ORID, OrientElement> rid2Elements = new HashMap<ORID, OrientElement>();
    private Mode mode = Mode.MANUAL;
    private boolean useCache = true;
    private final static String ADMIN = "admin";

    public OrientGraph(final String url) {
        this(url, ADMIN, ADMIN);
    }

    public OrientGraph(final String url, final String username, final String password) {
        this.database = new ODatabaseGraphTx(url);
        if (this.database.exists())
            this.database.open(username, password);
        else
            this.database.create();

        this.index = new OrientIndex(this);
        rid2Elements.clear();
    }

    public Vertex addVertex(final Object id) {
        try {
            beginTransaction();

            final OrientVertex vertex = new OrientVertex(this, this.database.createVertex());
            vertex.save();
            putElementInCache(vertex.getRaw().getDocument().getIdentity(), vertex);

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

            final OrientEdge edge = new OrientEdge(this, ((OrientVertex) outVertex).getRawVertex().link(
                    ((OrientVertex) inVertex).getRawVertex()));
            edge.setLabel(label);

            ((OrientVertex) outVertex).getRawVertex().getDocument().setDirty();
            ((OrientVertex) inVertex).getRawVertex().getDocument().setDirty();

            edge.save();
            putElementInCache(edge.getRaw().getDocument().getIdentity(), edge);

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

        // TRY IN CACHE
        final OrientVertex v = (OrientVertex) getCachedElement(rid);
        if (v != null)
            return v;

        final ODocument doc = this.database.getRecordById(rid);
        if (doc != null) {
            return new OrientVertex(this, (OGraphVertex) this.database.getUserObjectByRecord(doc, null));
        } else
            return new OrientVertex(this, (OGraphVertex) this.database.load(rid));
    }

    public void removeVertex(final Vertex vertex) {
        try {
            beginTransaction();

            final OGraphElement e = ((OrientVertex) vertex).getRaw();
            removeElementFromCache(e.getId());
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

        // TRY IN CACHE
        final OrientEdge e = (OrientEdge) getCachedElement(rid);
        if (e != null)
            return e;

        final ODocument doc = this.database.getRecordById(rid);
        if (doc != null) {
            return new OrientEdge(this, (OGraphEdge) this.database.getUserObjectByRecord(doc, null));
        } else
            return new OrientEdge(this, (OGraphEdge) this.database.load(rid));
    }

    public void removeEdge(final Edge edge) {
        try {
            beginTransaction();

            final OGraphEdge e = (OGraphEdge) ((OrientEdge) edge).getRaw();
            removeElementFromCache(e.getId());

            // REMOVE FROM CACHE ALSO THE CONNECTED VERTICES
            removeElementFromCache(e.getIn().getId());
            removeElementFromCache(e.getOut().getId());

            ((OrientEdge) edge).delete();

            commitTransaction();

        } catch (RuntimeException e) {
            rollbackTransaction();
            throw e;
        }
    }

    public void clear() {
        for (ODocument v : ((ODatabaseDocumentTx) this.database.getUnderlying()).browseClass(OGraphVertex.class.getSimpleName())) {
            if (v != null)
                v.delete();
        }

        for (ODocument e : ((ODatabaseDocumentTx) this.database.getUnderlying()).browseClass(OGraphEdge.class.getSimpleName())) {
            if (e != null)
                e.delete();
        }

        this.index.clear();
        this.rid2Elements.clear();
    }

    public Index getIndex() {
        return this.index;
    }

    public void shutdown() {
        this.database.close();
        this.index = null;
        rid2Elements.clear();
    }

    public String toString() {
        return "orientgraph[" + this.database.getURL() + "]";
    }

    public OrientElement getCachedElement(final ORID iRID) {
        if (this.useCache && iRID.isValid())
            return this.rid2Elements.get(iRID);
        return null;
    }

    protected void putElementInCache(final ORID iRID, final OrientElement iElement) {
        if (this.useCache && iRID.isValid())
            this.rid2Elements.put(iRID, iElement);
    }

    protected void removeElementFromCache(final ORID iRID) {
        if (this.useCache && iRID.isValid())
            this.rid2Elements.remove(iRID);
    }

    public ODatabaseGraphTx getRawGraph() {
        return database;
    }

    /*public void startStopTransaction() {
        if (Mode.AUTOMATIC == this.mode) {
            database.commit();
            database.begin();
        }
    }*/

    public void startTransaction() {
        if (Mode.AUTOMATIC == this.mode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);

        database.begin();
    }

    public void stopTransaction(final Conclusion conclusion) {
        if (Mode.AUTOMATIC == this.mode)
            throw new RuntimeException(TransactionalGraph.TURN_OFF_MESSAGE);

        if (conclusion == Conclusion.FAILURE)
            database.rollback();
        else
            database.commit();
    }

    public void setTransactionMode(final Mode mode) {
        this.mode = mode;
    }

    public Mode getTransactionMode() {
        return this.mode;
    }

    protected void beginTransaction() {
        if (getTransactionMode() == Mode.AUTOMATIC)
            database.begin();
    }

    protected void commitTransaction() {
        if (getTransactionMode() == Mode.AUTOMATIC)
            database.commit();
    }

    protected void rollbackTransaction() {
        if (getTransactionMode() == Mode.AUTOMATIC)
            database.rollback();
    }

    /*public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }*/
}
