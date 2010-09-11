package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.graph.ODatabaseGraphTx;
import com.orientechnologies.orient.core.db.graph.OGraphEdge;
import com.orientechnologies.orient.core.db.graph.OGraphVertex;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.iterator.OGraphEdgeIterator;
import com.orientechnologies.orient.core.iterator.OGraphVertexIterator;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph implements Graph {
    private final ODatabaseGraphTx database;
    private OrientIndex index;

    public OrientGraph(final String url) {
        this.database = new ODatabaseGraphTx(url);
    }

    public OrientGraph open(final String username, final String password) {
        this.database.open(username, password);
        this.index = new OrientIndex(this);
        return this;
    }

    public OrientGraph create() {
        this.database.create();
        this.index = new OrientIndex(this);
        return this;
    }

    public Vertex addVertex(final Object id) {
        final OrientVertex vertex = new OrientVertex(this, this.database.createVertex());
        vertex.save();
        return vertex;
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        final OrientEdge edge = new OrientEdge(this, ((OrientVertex) outVertex).getRawVertex().link(
                ((OrientVertex) inVertex).getRawVertex()));
        edge.setLabel(label);

        ((OrientVertex) outVertex).getRawVertex().getDocument().setDirty();
        ((OrientVertex) inVertex).getRawVertex().getDocument().setDirty();

        edge.save();
        return edge;
    }

    public Vertex getVertex(final Object id) {
        ORID rid;
        if (id instanceof ORID)
            rid = (ORID) id;
        else
            rid = new ORecordId(id.toString());

        final ODocument doc = this.database.getRecordById(rid);
        if (doc != null) {
            return new OrientVertex(this, (OGraphVertex) this.database.getUserObjectByRecord(doc, null));
        } else
            return new OrientVertex(this, (OGraphVertex) this.database.load(rid));
    }

    public void removeVertex(final Vertex vertex) {
        ((OrientVertex) vertex).getRawVertex().delete();
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
        if (doc != null) {
            return new OrientEdge(this, (OGraphEdge) this.database.getUserObjectByRecord(doc, null));
        } else
            return new OrientEdge(this, (OGraphEdge) this.database.load(rid));
    }

    public void removeEdge(final Edge edge) {
        ((OrientEdge) edge).delete();
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

        index.clear();
    }

    public Index getIndex() {
        return index;
    }

    public void shutdown() {
        this.database.close();
        this.index = null;
    }

    public boolean exists() {
        return this.database.exists();
    }

    public String toString() {
        return "orientgraph[" + this.database.getURL() + "]";
    }

    public ODatabaseGraphTx getRawGraph() {
        return database;
    }

    public void startTransaction() {
    }

    public void stopTransaction(boolean success) {
    }

    public void setAutoTransactions(boolean autoTransactions) {
    }

    public boolean isAutoTransactions() {
        return true;
    }
}
