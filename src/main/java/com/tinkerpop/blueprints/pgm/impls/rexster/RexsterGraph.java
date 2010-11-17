package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterVertexSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterGraph implements Graph {

    private final String graphURI;

    public RexsterGraph(String graphURI) {
        this.graphURI = graphURI;
    }

    public String getGraphURI() {
        return this.graphURI;
    }

    public void shutdown() {

    }

    public void clear() {
        // todo: this is very bad..very very bad :) Add clear() method to ReXster?
        List<Object> ids = new ArrayList<Object>();
        for (Vertex vertex : this.getVertices()) {
            ids.add(vertex.getId());
        }
        for (Object id : ids) {
            this.removeVertex(this.getVertex(id));
        }
    }

    public Iterable<Vertex> getVertices() {
        return new RexsterVertexSequence(graphURI + RexsterTokens.SLASH_VERTICES, this);
    }

    public Vertex addVertex(Object id) {
        if (null == id)
            id = UUID.randomUUID().toString();
        return new RexsterVertex(RestHelper.postResultObject(graphURI + RexsterTokens.SLASH_VERTICES_SLASH + id), this);
    }

    public Vertex getVertex(Object id) {
        return new RexsterVertex(RestHelper.getResultObject(graphURI + RexsterTokens.SLASH_VERTICES_SLASH + id), this);

    }

    public Edge getEdge(Object id) {
        return new RexsterEdge(RestHelper.getResultObject(graphURI + RexsterTokens.SLASH_EDGES_SLASH + id), this);
    }

    public Iterable<Edge> getEdges() {
        return new RexsterEdgeSequence(graphURI + RexsterTokens.SLASH_EDGES, this);
    }

    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        if (null == id)
            id = UUID.randomUUID().toString();
        return new RexsterEdge(RestHelper.postResultObjectForm(graphURI + RexsterTokens.SLASH_EDGES_SLASH + id, RexsterTokens._OUTV + RexsterTokens.EQUALS + outVertex.getId() + RexsterTokens.AND + RexsterTokens._INV + RexsterTokens.EQUALS + inVertex.getId() + RexsterTokens.AND + RexsterTokens._LABEL + RexsterTokens.EQUALS + label), this);
    }

    public void removeEdge(Edge edge) {
        RestHelper.delete(graphURI + RexsterTokens.SLASH_EDGES_SLASH + edge.getId());
    }

    public void removeVertex(Vertex vertex) {
        RestHelper.delete(graphURI + RexsterTokens.SLASH_VERTICES_SLASH + vertex.getId());
    }

    public String toString() {
        return "rexstergraph[" + this.graphURI + "]";
    }
}
