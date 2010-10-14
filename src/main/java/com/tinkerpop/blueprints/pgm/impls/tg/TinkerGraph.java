package com.tinkerpop.blueprints.pgm.impls.tg;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerGraph implements Graph {

    private Long currentId = 0l;
    protected Map<String, Vertex> vertices = new HashMap<String, Vertex>();
    protected Map<String, Edge> edges = new HashMap<String, Edge>();
    private TinkerIndex index = new TinkerIndex();

    public Vertex addVertex(final Object id) {
        String idString;
        if (null != id) {
            idString = id.toString();
        } else {
            idString = this.getNextId();
        }

        Vertex vertex = this.vertices.get(idString);

        if (null != vertex) {
            throw new RuntimeException("Vertex with id " + id + " already exists");
        } else {
            vertex = new TinkerVertex(idString, this.index);
            this.vertices.put(vertex.getId().toString(), vertex);
            return vertex;
        }
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

        for (String key : vertex.getPropertyKeys()) {
            this.index.remove(key, vertex.getProperty(key), vertex);
        }
        this.vertices.remove(vertex.getId().toString());
    }

    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        final String idString;
        Edge edge;
        if (null != id) {
            idString = id.toString();
            edge = this.edges.get(idString);
        } else {
            idString = this.getNextId();
            edge = null;
        }

        if (null != edge) {
            throw new RuntimeException("Vertex with id " + id + " already exists");
        } else {
            edge = new TinkerEdge(idString, outVertex, inVertex, label, this.index);
            this.edges.put(edge.getId().toString(), edge);
            final TinkerVertex out = (TinkerVertex) outVertex;
            final TinkerVertex in = (TinkerVertex) inVertex;
            out.outEdges.add(edge);
            in.inEdges.add(edge);
            return edge;
        }
    }

    public void removeEdge(final Edge edge) {
        TinkerVertex outVertex = (TinkerVertex) edge.getOutVertex();
        TinkerVertex inVertex = (TinkerVertex) edge.getInVertex();
        if (null != outVertex && null != outVertex.outEdges)
            outVertex.outEdges.remove(edge);
        if (null != inVertex && null != inVertex.inEdges)
            inVertex.inEdges.remove(edge);
        this.edges.remove(edge.getId());
    }

    public Index getIndex() {
        return this.index;
    }

    public String toString() {
        return "tinkergraph[vertices:" + this.vertices.size() + " edges:" + this.edges.size() + "]";
    }

    public void clear() {
        this.vertices.clear();
        this.edges.clear();
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
