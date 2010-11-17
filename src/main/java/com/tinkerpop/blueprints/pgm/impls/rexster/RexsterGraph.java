package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterGraph implements Graph {

    private final String graphURI;

    public RexsterGraph(String graphURI) {
        this.graphURI = graphURI;
    }

    protected String getGraphURI() {
        return this.graphURI;
    }

    public void shutdown() {

    }

    public void clear() {
        throw new UnsupportedOperationException("Unable to clear a ReXster graph");
    }

    public Iterable<Vertex> getVertices() {
        List<Vertex> vertices = new ArrayList<Vertex>();
        for (Object vertex : RestHelper.getResultArray(graphURI + RexsterTokens.SLASH_VERTICES)) {
            JSONObject raw = (JSONObject) vertex;
            vertices.add(new RexsterVertex(RestHelper.getResultObject(graphURI + RexsterTokens.SLASH_VERTICES_SLASH + raw.get(RexsterTokens._ID)), this));
        }
        return vertices;
    }

    public Vertex addVertex(Object id) {
        return new RexsterVertex(RestHelper.postObject(graphURI + RexsterTokens.SLASH_VERTICES_SLASH + id), this);
    }

    public Vertex getVertex(Object id) {
        return new RexsterVertex(RestHelper.getResultObject(graphURI + RexsterTokens.SLASH_VERTICES_SLASH + id), this);

    }

    public Edge getEdge(Object id) {
        return new RexsterEdge(RestHelper.getResultObject(graphURI + RexsterTokens.SLASH_EDGES_SLASH + id), this);
    }

    public Iterable<Edge> getEdges() {
        List<Edge> edges = new ArrayList<Edge>();
        for (Object edge : RestHelper.getResultArray(graphURI + RexsterTokens.SLASH_EDGES)) {
            JSONObject raw = (JSONObject) edge;
            edges.add(new RexsterEdge(RestHelper.getResultObject(graphURI + RexsterTokens.SLASH_EDGES_SLASH + raw.get(RexsterTokens._ID)), this));
        }
        return edges;
    }

    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        return new RexsterEdge(RestHelper.postObjectForm(graphURI + RexsterTokens.SLASH_EDGES_SLASH + id, RexsterTokens._OUTV + RexsterTokens.EQUALS + outVertex.getId() + RexsterTokens.AND + RexsterTokens._INV + RexsterTokens.EQUALS + inVertex.getId() + RexsterTokens.AND + RexsterTokens._LABEL + RexsterTokens.EQUALS + label), this);
    }

    public void removeEdge(Edge edge) {

    }

    public void removeVertex(Vertex vertex) {

    }

    public String toString() {
        return "rexstergraph[" + this.graphURI + "]";
    }


    public static void main(String[] args) {
        Graph graph = new RexsterGraph("http://localhost:8182/gratefulgraph");
        System.out.println(graph);
        //System.out.println(graph.getVertex(89).getOutEdges());
        Vertex a = graph.addVertex(100001);
        Vertex b = graph.addVertex(100002);
        Edge e = graph.addEdge(101012, a, b, "hello_there");
        System.out.println(e);
        a.setProperty("blah", "tada");
        System.out.println(a.getProperty("blah"));

    }

}
