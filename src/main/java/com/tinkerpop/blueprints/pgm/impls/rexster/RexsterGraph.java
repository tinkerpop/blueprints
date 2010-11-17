package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterVertexSequence;

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
        throw new UnsupportedOperationException("Unable to clear a RexsterGraph");
    }

    public Iterable<Vertex> getVertices() {
        return new RexsterVertexSequence(graphURI + RexsterTokens.SLASH_VERTICES, this);
    }

    public Vertex addVertex(Object id) {
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


    public static void main(String[] args) {
        Graph graph = new RexsterGraph("http://localhost:8182/gratefulgraph");
        System.out.println(graph);
        int counter = 0;
        for (Vertex vertex : graph.getVertices()) {
            counter++;
            System.out.println(vertex);
        }
        System.out.println("TOTAL SIZE: " + counter);

        /*for (Edge edge : graph.getEdges()) {
            counter++;
            System.out.println(edge);
        }
        System.out.println("TOTAL SIZE: " + counter);*/
        counter = 0;
        for (Edge e : graph.getVertex(89).getOutEdges()) {
            System.out.println(e);
            counter++;
        }
        System.out.println("TOTAL SIZE: " + counter);
        /*Vertex a = graph.addVertex(100001);
        System.out.println(a.getPropertyKeys());
        Vertex b = graph.addVertex(100002);
        System.out.println(a + "---" + b);
        Edge e = graph.addEdge(101012, a, b, "hello_there");
        System.out.println(e);
        a.setProperty("blah", "marko");
        System.out.println(a.getProperty("blah"));
        System.out.println(a.getOutEdges());
        graph.removeEdge(e);
        System.out.println(a.getOutEdges());*/


    }

}
