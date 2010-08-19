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

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph implements Graph {
    private final ODatabaseGraphTx database;

    public OrientGraph(final String url) {
        this.database = new ODatabaseGraphTx(url);
    }

    public OrientGraph open(final String username, final String password) {
        this.database.open(username, password);
        return this;
    }

    public OrientGraph create() {
        database.create();
        return this;
    }

    public Vertex addVertex(final Object id) {
        final OrientVertex vertex = new OrientVertex(this.database.createVertex());
        vertex.save();
        return vertex;
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        final OrientEdge edge = new OrientEdge(((OrientVertex) outVertex).getRawVertex().link(((OrientVertex) inVertex).getRawVertex()));
        edge.setLabel(label);
        edge.save();
        return edge;
    }

    public Vertex getVertex(final Object id) {
        ORID rid;
        if (id instanceof ORID)
            rid = (ORID) id;
        else
            rid = new ORecordId((String) id);

        final ODocument doc = this.database.getRecordById(rid);
        if (doc != null) {
            return new OrientVertex((OGraphVertex) this.database.getUserObjectByRecord(doc, null));
        } else
            return new OrientVertex((OGraphVertex) this.database.load(rid));
    }

    public void removeVertex(final Vertex vertex) {
        ((OrientVertex) vertex).getRawVertex().delete();
    }

    public Iterable<Vertex> getVertices() {
        return new OrientVertexIterator(new OGraphVertexIterator(this.database));
    }

    public Iterable<Edge> getEdges() {
        return new OrientEdgeIterator(new OGraphEdgeIterator(this.database));
    }

    public Edge getEdge(final Object id) {
        ORID rid;
        if (id instanceof ORID)
            rid = (ORID) id;
        else
            rid = new ORecordId((String) id);

        final ODocument doc = this.database.getRecordById(rid);
        if (doc != null) {
            return new OrientEdge((OGraphEdge) this.database.getUserObjectByRecord(doc, null));
        } else
            return new OrientEdge((OGraphEdge) this.database.load(rid));
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
    }

    public Index getIndex() {
        throw new UnsupportedOperationException();
    }

    public void shutdown() {
        database.close();
    }

    public boolean exists() {
        return this.database.exists();
    }

    public String toString() {
        return "orientgraph[" + this.database.getURL() + "]";
    }
}
