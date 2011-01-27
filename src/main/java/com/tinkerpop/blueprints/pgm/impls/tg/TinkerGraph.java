package com.tinkerpop.blueprints.pgm.impls.tg;


import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.util.AutomaticIndexHelper;

import java.util.*;

/**
 * A in-memory, reference implementation of the property graph interfaces provided by Blueprints.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerGraph implements IndexableGraph {

    private Long currentId = 0l;
    protected Map<String, Vertex> vertices = new HashMap<String, Vertex>();
    protected Map<String, Edge> edges = new HashMap<String, Edge>();
    protected Map<String, TinkerIndex> indices = new HashMap<String, TinkerIndex>();
    protected Map<String, TinkerAutomaticIndex> autoIndices = new HashMap<String, TinkerAutomaticIndex>();

    public TinkerGraph() {
        this.createAutomaticIndex(Index.VERTICES, TinkerVertex.class, null);
        this.createAutomaticIndex(Index.EDGES, TinkerEdge.class, null);
    }

    protected Iterable<TinkerAutomaticIndex> getAutoIndices() {
        return this.autoIndices.values();
    }

    protected Iterable<TinkerIndex> getManualIndices() {
        HashSet<TinkerIndex> indices = new HashSet<TinkerIndex>(this.indices.values());
        indices.removeAll(this.autoIndices.values());
        return indices;
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, Set<String> keys) {
        if (this.indices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        TinkerAutomaticIndex index = new TinkerAutomaticIndex(indexName, indexClass, keys);
        this.autoIndices.put(index.getIndexName(), index);
        this.indices.put(index.getIndexName(), index);
        return index;
    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        if (this.indices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        TinkerIndex index = new TinkerIndex(indexName, indexClass);
        this.indices.put(index.getIndexName(), index);
        return index;
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        Index index = this.indices.get(indexName);
        if (null == index)
            throw new RuntimeException("No such index exists: " + indexName);
        if (!indexClass.isAssignableFrom(index.getIndexClass()))
            throw new RuntimeException(indexClass + " is not assignable from " + index.getIndexClass());
        else
            return (Index<T>) index;
    }

    public Iterable<Index<? extends Element>> getIndices() {
        List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
        for (Index index : indices.values()) {
            list.add(index);
        }
        return list;
    }

    public void dropIndex(final String indexName) {
        this.indices.remove(indexName);
        this.autoIndices.remove(indexName);
    }


    public Vertex addVertex(final Object id) {
        String idString = null;
        Vertex vertex;
        if (null != id) {
            idString = id.toString();
            vertex = this.vertices.get(idString);
            if (null != vertex) {
                throw new RuntimeException("Vertex with id " + idString + " already exists");
            }
        } else {
            boolean done = false;
            while (!done) {
                idString = this.getNextId();
                vertex = this.vertices.get(idString);
                if (null == vertex)
                    done = true;
            }
        }

        vertex = new TinkerVertex(idString, this);
        this.vertices.put(vertex.getId().toString(), vertex);
        return vertex;

    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            return null;
        else {
            String idString = id.toString();
            return this.vertices.get(idString);
        }
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            return null;
        else {
            String idString = id.toString();
            return this.edges.get(idString);
        }
    }


    public Iterable<Vertex> getVertices() {
        return vertices.values();
    }

    public Iterable<Edge> getEdges() {
        return edges.values();
    }

    public void removeVertex(final Vertex vertex) {
        Set<Edge> toRemove = new HashSet<Edge>();
        for (Edge edge : vertex.getInEdges()) {
            toRemove.add(edge);
        }
        for (Edge edge : vertex.getOutEdges()) {
            toRemove.add(edge);
        }
        for (Edge edge : toRemove) {
            this.removeEdge(edge);
        }

        AutomaticIndexHelper.unIndexElement(this, vertex);
        for (Index index : this.getManualIndices()) {
            if (Vertex.class.isAssignableFrom(index.getIndexClass())) {
                TinkerIndex<TinkerVertex> idx = (TinkerIndex<TinkerVertex>) index;
                idx.removeElement((TinkerVertex) vertex);
            }
        }

        this.vertices.remove(vertex.getId().toString());
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        String idString = null;
        Edge edge;
        if (null != id) {
            idString = id.toString();
            edge = this.edges.get(idString);
            if (null != edge) {
                throw new RuntimeException("Edge with id " + id + " already exists");
            }
        } else {
            boolean done = false;
            while (!done) {
                idString = this.getNextId();
                edge = this.edges.get(idString);
                if (null == edge)
                    done = true;
            }
        }

        edge = new TinkerEdge(idString, outVertex, inVertex, label, this);
        this.edges.put(edge.getId().toString(), edge);
        final TinkerVertex out = (TinkerVertex) outVertex;
        final TinkerVertex in = (TinkerVertex) inVertex;
        out.outEdges.add(edge);
        in.inEdges.add(edge);
        return edge;

    }

    public void removeEdge(final Edge edge) {
        TinkerVertex outVertex = (TinkerVertex) edge.getOutVertex();
        TinkerVertex inVertex = (TinkerVertex) edge.getInVertex();
        if (null != outVertex && null != outVertex.outEdges)
            outVertex.outEdges.remove(edge);
        if (null != inVertex && null != inVertex.inEdges)
            inVertex.inEdges.remove(edge);

        AutomaticIndexHelper.unIndexElement(this, edge);
        for (Index index : this.getManualIndices()) {
            if (Edge.class.isAssignableFrom(index.getIndexClass())) {
                TinkerIndex<TinkerEdge> idx = (TinkerIndex<TinkerEdge>) index;
                idx.removeElement((TinkerEdge) edge);
            }
        }

        this.edges.remove(edge.getId().toString());
    }


    public String toString() {
        return "tinkergraph[vertices:" + this.vertices.size() + " edges:" + this.edges.size() + "]";
    }

    public void clear() {
        this.vertices.clear();
        this.edges.clear();
        this.indices.clear();
        this.autoIndices.clear();
        this.currentId = 0l;
    }

    public void shutdown() {

    }

    private String getNextId() {
        String idString;
        while (true) {
            idString = this.currentId.toString();
            this.currentId++;
            if (null == this.vertices.get(idString) || null == this.edges.get(idString) || this.currentId == Long.MAX_VALUE)
                break;
        }
        return idString;
    }

    public TinkerGraph getRawGraph() {
        return this;
    }
}
