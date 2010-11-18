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
        // test to make sure its a valid, accessible url
        RestHelper.get(graphURI);
    }

    public String getGraphURI() {
        return this.graphURI;
    }

    public void shutdown() {

    }

    public void clear() {
        // todo: this is very bad..very very bad :)
        List<Object> ids = new ArrayList<Object>();
        for (Vertex vertex : this.getVertices()) {
            ids.add(vertex.getId());
        }
        for (Object id : ids) {
            this.removeVertex(this.getVertex(id));
        }
        /*
        COMING SOON TO REXSTER
        DELETE http://localhost/graph/vertices
           - deletes all vertices
        DELETE http://localhost/graph/edgs
           - delete all edges
        DELETE http://localhost/graph/indices
           - delete all indices
        */
    }

    public Iterable<Vertex> getVertices() {
        return new RexsterVertexSequence(this.graphURI + RexsterTokens.SLASH_VERTICES, this);
    }

    public Vertex addVertex(Object id) {
        // todo: this needs to use http://localhost/graph/edges/null (coming soon to rexster)
        if (null == id)
            id = UUID.randomUUID().toString();
        return new RexsterVertex(RestHelper.postResultObject(this.graphURI + RexsterTokens.SLASH_VERTICES_SLASH + id), this);
    }

    public Vertex getVertex(Object id) {
        return new RexsterVertex(RestHelper.getResultObject(this.graphURI + RexsterTokens.SLASH_VERTICES_SLASH + id), this);
    }

    public Edge getEdge(Object id) {
        return new RexsterEdge(RestHelper.getResultObject(this.graphURI + RexsterTokens.SLASH_EDGES_SLASH + id), this);
    }

    public Iterable<Edge> getEdges() {
        return new RexsterEdgeSequence(this.graphURI + RexsterTokens.SLASH_EDGES, this);
    }

    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        // todo: this needs to use http://localhost/graph/edges/null (coming soon to rexster)
        if (null == id)
            id = UUID.randomUUID().toString();
        return new RexsterEdge(RestHelper.postResultObjectForm(this.graphURI + RexsterTokens.SLASH_EDGES_SLASH + id, RexsterTokens._OUTV + RexsterTokens.EQUALS + outVertex.getId() + RexsterTokens.AND + RexsterTokens._INV + RexsterTokens.EQUALS + inVertex.getId() + RexsterTokens.AND + RexsterTokens._LABEL + RexsterTokens.EQUALS + label), this);
    }

    public void removeEdge(Edge edge) {
        RestHelper.delete(this.graphURI + RexsterTokens.SLASH_EDGES_SLASH + edge.getId());
    }

    public void removeVertex(Vertex vertex) {
        RestHelper.delete(this.graphURI + RexsterTokens.SLASH_VERTICES_SLASH + vertex.getId());
    }

    public String toString() {
        return "rexstergraph[" + this.graphURI + "]";
    }
}
