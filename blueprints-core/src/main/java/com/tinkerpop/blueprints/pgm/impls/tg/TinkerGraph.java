package com.tinkerpop.blueprints.pgm.impls.tg;


import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.util.AutomaticIndexHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A in-memory, reference implementation of the property graph interfaces provided by Blueprints.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerGraph implements IndexableGraph, Serializable {

    private Long currentId = 0l;
    protected Map<String, Vertex> vertices = new HashMap<String, Vertex>();
    protected Map<String, Edge> edges = new HashMap<String, Edge>();
    protected Map<String, TinkerIndex> indices = new HashMap<String, TinkerIndex>();
    protected Map<String, TinkerAutomaticIndex> autoIndices = new HashMap<String, TinkerAutomaticIndex>();
    private final String directory;
    private static final String GRAPH_FILE = "/tinkergraph.dat";

    public TinkerGraph(final String directory) {
        this.directory = directory;
        try {
            final File file = new File(directory);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw new RuntimeException("Could not create directory.");
                }

                this.createAutomaticIndex(Index.VERTICES, TinkerVertex.class, null);
                this.createAutomaticIndex(Index.EDGES, TinkerEdge.class, null);
            } else {
                ObjectInputStream input = new ObjectInputStream(new FileInputStream(directory + GRAPH_FILE));
                TinkerGraph temp = (TinkerGraph) input.readObject();
                input.close();
                this.currentId = temp.currentId;
                this.vertices = temp.vertices;
                this.edges = temp.edges;
                this.indices = temp.indices;
                this.autoIndices = temp.autoIndices;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public TinkerGraph() {
        this.directory = null;
        this.createAutomaticIndex(Index.VERTICES, TinkerVertex.class, null);
        this.createAutomaticIndex(Index.EDGES, TinkerEdge.class, null);
    }

    protected Iterable<TinkerAutomaticIndex> getAutoIndices() {
        return this.autoIndices.values();
    }

    protected Iterable<TinkerIndex> getManualIndices() {
        final HashSet<TinkerIndex> indices = new HashSet<TinkerIndex>(this.indices.values());
        indices.removeAll(this.autoIndices.values());
        return indices;
    }

    public <T extends Element> AutomaticIndex<T> createAutomaticIndex(final String indexName, final Class<T> indexClass, Set<String> keys) {
        if (this.indices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        final TinkerAutomaticIndex index = new TinkerAutomaticIndex(indexName, indexClass, keys);
        this.autoIndices.put(index.getIndexName(), index);
        this.indices.put(index.getIndexName(), index);
        return index;
    }

    public <T extends Element> Index<T> createManualIndex(final String indexName, final Class<T> indexClass) {
        if (this.indices.containsKey(indexName))
            throw new RuntimeException("Index already exists: " + indexName);

        final TinkerIndex index = new TinkerIndex(indexName, indexClass);
        this.indices.put(index.getIndexName(), index);
        return index;
    }

    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
        Index index = this.indices.get(indexName);
        if (null == index)
            return null;
        if (!indexClass.isAssignableFrom(index.getIndexClass()))
            throw new RuntimeException(indexClass + " is not assignable from " + index.getIndexClass());
        else
            return (Index<T>) index;
    }

    public Iterable<Index<? extends Element>> getIndices() {
        final List<Index<? extends Element>> list = new ArrayList<Index<? extends Element>>();
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
            throw new IllegalArgumentException("Element identifier cannot be null");

        String idString = id.toString();
        return this.vertices.get(idString);
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            throw new IllegalArgumentException("Element identifier cannot be null");

        String idString = id.toString();
        return this.edges.get(idString);
    }


    public Iterable<Vertex> getVertices() {
        return new LinkedList<Vertex>(vertices.values());
    }

    public Iterable<Edge> getEdges() {
        return new LinkedList<Edge>(edges.values());
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

        AutomaticIndexHelper.removeElement(this, vertex);
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

        AutomaticIndexHelper.removeElement(this, edge);
        for (Index index : this.getManualIndices()) {
            if (Edge.class.isAssignableFrom(index.getIndexClass())) {
                TinkerIndex<TinkerEdge> idx = (TinkerIndex<TinkerEdge>) index;
                idx.removeElement((TinkerEdge) edge);
            }
        }

        this.edges.remove(edge.getId().toString());
    }


    public String toString() {
        if (null == this.directory)
            return StringFactory.graphString(this, "vertices:" + this.vertices.size() + " edges:" + this.edges.size());
        else
            return StringFactory.graphString(this, "vertices:" + this.vertices.size() + " edges:" + this.edges.size() + " directory:" + this.directory);
    }

    public void clear() {
        this.vertices.clear();
        this.edges.clear();
        this.indices.clear();
        this.autoIndices.clear();
        this.currentId = 0l;
        this.createAutomaticIndex(Index.VERTICES, TinkerVertex.class, null);
        this.createAutomaticIndex(Index.EDGES, TinkerEdge.class, null);
    }

    public void shutdown() {
        if (null != this.directory) {
            try {
                File file = new File(this.directory + GRAPH_FILE);
                if (file.exists()) {
                    file.delete();
                } else {
                }
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(this.directory + GRAPH_FILE));
                out.writeObject(this);
                out.close();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
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
