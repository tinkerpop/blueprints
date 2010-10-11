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
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * OrientDB implementation of Graph interface. This implementation is transactional.
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraph implements Graph, TransactionalGraph {
    private final ODatabaseGraphTx database;
    private OrientIndex index;
    private Set<String> cachedClasses = new HashSet<String>();

    private final Map<ORID, OrientElement> rid2Elements;

    private Mode mode = Mode.MANUAL;
    private boolean useCache = true;
    private final static String ADMIN = "admin";

    public OrientGraph(final String url) {
        this(url, ADMIN, ADMIN);
    }

    @SuppressWarnings("serial")
    public OrientGraph(final String url, final String username, final String password) {
        this.database = new ODatabaseGraphTx(url);
        if (this.database.exists())
            this.database.open(username, password);
        else
            this.database.create();

        this.index = new OrientIndex(this);

        rid2Elements = new LinkedHashMap<ORID, OrientElement>(10000, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<ORID, OrientElement> iEldest) {
                return size() > 10000;
            }
        };

        rid2Elements.clear();
    }

    public Vertex addVertex(final Object id) {
        try {
            beginTransaction();

            final OrientVertex vertex = new OrientVertex(this, this.database.createVertex(null));
            vertex.save();
            putElementInCache(vertex);

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

            // SAVE THE VERTEX TO ASSURE THEY ARE IN TX
            ((OrientVertex) outVertex).save();
            ((OrientVertex) inVertex).save();
            edge.save();

            putElementInCache(edge);

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

    public OrientIndex getIndex() {
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

    public OrientElement getCachedElement(final ORID rid) {
        if (this.useCache && rid.isValid())
            return this.rid2Elements.get(rid);
        return null;
    }

    protected void putElementInCache(final OrientElement element) {
        final ODocument d = element.getRaw().getDocument();

        if (this.useCache && d.getIdentity().isValid()) {
            if (cachedClasses.contains(d.getClassName()))
                this.rid2Elements.put(d.getIdentity(), element);
        }
    }

    protected void removeElementFromCache(final ORID rid) {
        if (this.useCache && rid.isValid())
            this.rid2Elements.remove(rid);
    }

    public ODatabaseGraphTx getRawGraph() {
        return database;
    }

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
     }

     public void addCachedClass(final String iKey) {
         cachedClasses.add(iKey);
     }

     public void removeCachedClass(final String iKey) {
         cachedClasses.remove(iKey);
     }*/
}
