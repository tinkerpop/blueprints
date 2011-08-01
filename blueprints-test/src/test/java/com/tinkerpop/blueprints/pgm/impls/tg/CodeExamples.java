package com.tinkerpop.blueprints.pgm.impls.tg;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import junit.framework.TestCase;

/**
 * This class maintains a collection of standard Blueprints patterns.
 * This is used as a tutorial for how to use Blueprints.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class CodeExamples extends TestCase {

    public void testSimpleGraphCreation() {
        Graph graph = new TinkerGraph();
        Vertex a = graph.addVertex(null);
        Vertex b = graph.addVertex(null);
        a.setProperty("name", "marko");
        b.setProperty("name", "peter");
        Edge e = graph.addEdge(null, a, b, "knows");
        System.out.println(e.getOutVertex().getProperty("name") + "--" + e.getLabel() + "-->" + e.getInVertex().getProperty("name"));
    }

    public void testIteratingGraph() {
        // creates the graph diagrammed here
        //  https://github.com/tinkerpop/blueprints/wiki/Property-Graph-Model
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        System.out.println("Vertices of " + graph);
        for (Vertex vertex : graph.getVertices()) {
            System.out.println(vertex);
        }
        System.out.println("Edges of " + graph);
        for (Edge edge : graph.getEdges()) {
            System.out.println(edge);
        }
    }

    public void testIterateThroughEdges() {
        // creates the graph diagrammed here
        //  https://github.com/tinkerpop/blueprints/wiki/Property-Graph-Model
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex a = graph.getVertex("1");
        System.out.println("vertex " + a.getId() + " has name " + a.getProperty("name"));
        for (Edge e : a.getOutEdges()) {
            System.out.println(e);
        }
    }

    public void testGetAVertexByProperty() {
        // creates the graph diagrammed here
        //  https://github.com/tinkerpop/blueprints/wiki/Property-Graph-Model
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex a = ((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "marko").iterator().next();
        System.out.println("vertex " + a.getId() + " has age " + a.getProperty("age"));
    }

    /*public void testPlay() {
        TinkerGraph graph = new TinkerGraph();
        graph.addVertex("0");
        graph.addVertex(null);
    }*/
}
