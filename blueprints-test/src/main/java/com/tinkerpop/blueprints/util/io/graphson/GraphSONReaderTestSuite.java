package com.tinkerpop.blueprints.util.io.graphson;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.GraphTest;

import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GraphSONReaderTestSuite extends TestSuite {

    public GraphSONReaderTestSuite() {
    }

    public GraphSONReaderTestSuite(GraphTest graphTest) {
        super(graphTest);
    }

    public void testReadingTinkerGraph() throws Exception {
        Graph graph = graphTest.generateGraph();
        if (!graph.getFeatures().ignoresSuppliedIds) {
            this.stopWatch();
            new GraphSONReader(graph).inputGraph(GraphSONReader.class.getResourceAsStream("graph-example-1.json"));
            printPerformance(graph.toString(), null, "graph-example-1 loaded", this.stopWatch());

            // note that TinkerGraph converts ids to string internally, but the various WrapperGraphs
            // might like the original data type of the ID. so...this tests getVertex with the original
            // type (integer) but then compares on getId() are toString() to deal with scenarios
            // where those ids are dealt with differently per graph implementation.

            assertEquals(count(graph.getVertex(1).getEdges(Direction.OUT)), 3);
            assertEquals(count(graph.getVertex(1).getEdges(Direction.IN)), 0);
            Vertex marko = graph.getVertex(1);
            assertEquals(marko.getProperty("name"), "marko");
            assertEquals(marko.getProperty("age"), 29);
            int counter = 0;
            for (Edge e : graph.getVertex(1).getEdges(Direction.OUT)) {
                if (e.getVertex(Direction.IN).getId().toString().equals("2")) {
                    assertEquals(e.getProperty("weight"), 0.5);
                    assertEquals(e.getLabel(), "knows");
                    assertEquals(e.getId().toString(), "7");
                    counter++;
                } else if (e.getVertex(Direction.IN).getId().toString().equals("3")) {
                    assertEquals(Math.round((Double) e.getProperty("weight")), 0);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId().toString(), "9");
                    counter++;
                } else if (e.getVertex(Direction.IN).getId().toString().equals("4")) {
                    assertEquals(Math.round((Double) e.getProperty("weight")), 1);
                    assertEquals(e.getLabel(), "knows");
                    assertEquals(e.getId().toString(), "8");
                    counter++;
                }
            }

            assertEquals(count(graph.getVertex(4).getEdges(Direction.OUT)), 2);
            assertEquals(count(graph.getVertex(4).getEdges(Direction.IN)), 1);
            Vertex josh = graph.getVertex(4);
            assertEquals(josh.getProperty("name"), "josh");
            assertEquals(josh.getProperty("age"), 32);
            for (Edge e : graph.getVertex(4).getEdges(Direction.OUT)) {
                if (e.getVertex(Direction.IN).getId().toString().equals("3")) {
                    assertEquals(Math.round((Double) e.getProperty("weight")), 0);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId().toString(), "11");
                    counter++;
                } else if (e.getVertex(Direction.IN).getId().toString().equals("5")) {
                    assertEquals(Math.round((Double) e.getProperty("weight")), 1);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId().toString(), "10");
                    counter++;
                }
            }

            assertEquals(counter, 5);
        }
        graph.shutdown();
    }

    public void testTinkerGraphEdges() throws Exception {
        Graph graph = this.graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeIteration) {
            this.stopWatch();
            new GraphSONReader(graph).inputGraph(GraphSONReader.class.getResourceAsStream("graph-example-1.json"));
            printPerformance(graph.toString(), null, "graph-example-1 loaded", this.stopWatch());
            Set<String> edgeIds = new HashSet<String>();
            Set<String> edgeKeys = new HashSet<String>();
            Set<String> edgeValues = new HashSet<String>();
            int count = 0;
            for (Edge e : graph.getEdges()) {
                count++;
                edgeIds.add(e.getId().toString());
                for (String key : e.getPropertyKeys()) {
                    edgeKeys.add(key);
                    edgeValues.add(e.getProperty(key).toString());
                }
            }
            assertEquals(count, 6);
            assertEquals(edgeIds.size(), 6);
            assertEquals(edgeKeys.size(), 1);
            assertEquals(edgeValues.size(), 4);
        }
        graph.shutdown();
    }

    public void testTinkerGraphVertices() throws Exception {
        Graph graph = this.graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIteration) {
            this.stopWatch();
            new GraphSONReader(graph).inputGraph(GraphSONReader.class.getResourceAsStream("graph-example-1.json"));
            printPerformance(graph.toString(), null, "graph-example-1 loaded", this.stopWatch());
            Set<String> vertexNames = new HashSet<String>();
            int count = 0;
            for (Vertex v : graph.getVertices()) {
                count++;
                vertexNames.add(v.getProperty("name").toString());
                // System.out.println(v);
            }
            assertEquals(count, 6);
            assertEquals(vertexNames.size(), 6);
            assertTrue(vertexNames.contains("marko"));
            assertTrue(vertexNames.contains("josh"));
            assertTrue(vertexNames.contains("peter"));
            assertTrue(vertexNames.contains("vadas"));
            assertTrue(vertexNames.contains("ripple"));
            assertTrue(vertexNames.contains("lop"));
        }
        graph.shutdown();
    }

    public void testTinkerGraphSoftwareVertices() throws Exception {
        Graph graph = this.graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIteration) {
            this.stopWatch();
            new GraphSONReader(graph).inputGraph(GraphSONReader.class.getResourceAsStream("graph-example-1.json"));
            printPerformance(graph.toString(), null, "graph-example-1 loaded", this.stopWatch());
            Set<Vertex> softwareVertices = new HashSet<Vertex>();
            int count = 0;
            for (Vertex v : graph.getVertices()) {
                count++;
                String name = v.getProperty("name").toString();
                if (name.equals("lop") || name.equals("ripple")) {
                    softwareVertices.add(v);
                }
            }
            assertEquals(count, 6);
            assertEquals(softwareVertices.size(), 2);
            for (Vertex v : softwareVertices) {
                assertEquals(v.getProperty("lang"), "java");
            }
        }
        graph.shutdown();
    }

    public void testTinkerGraphVertexAndEdges() throws Exception {
        Graph graph = this.graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIteration) {
            this.stopWatch();
            new GraphSONReader(graph).inputGraph(GraphSONReader.class.getResourceAsStream("graph-example-1.json"));
            printPerformance(graph.toString(), null, "graph-example-1 loaded", this.stopWatch());
            Vertex marko = null;
            Vertex peter = null;
            Vertex josh = null;
            Vertex vadas = null;
            Vertex lop = null;
            Vertex ripple = null;
            int count = 0;
            for (Vertex v : graph.getVertices()) {
                count++;
                String name = v.getProperty("name").toString();
                if (name.equals("marko")) {
                    marko = v;
                } else if (name.equals("peter")) {
                    peter = v;
                } else if (name.equals("josh")) {
                    josh = v;
                } else if (name.equals("vadas")) {
                    vadas = v;
                } else if (name.equals("lop")) {
                    lop = v;
                } else if (name.equals("ripple")) {
                    ripple = v;
                } else {
                    assertTrue(false);
                }
            }
            assertEquals(count, 6);
            assertTrue(null != marko);
            assertTrue(null != peter);
            assertTrue(null != josh);
            assertTrue(null != vadas);
            assertTrue(null != lop);
            assertTrue(null != ripple);

            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(count(graph.getEdges()), 6);
            }

            // test marko
            Set<Vertex> vertices = new HashSet<Vertex>();
            assertEquals(marko.getProperty("name"), "marko");
            assertEquals(((Number) marko.getProperty("age")).intValue(), 29);
            assertEquals(marko.getPropertyKeys().size(), 2);
            assertEquals(count(marko.getEdges(Direction.OUT)), 3);
            assertEquals(count(marko.getEdges(Direction.IN)), 0);
            for (Edge e : marko.getEdges(Direction.OUT)) {
                vertices.add(e.getVertex(Direction.IN));
            }
            assertEquals(vertices.size(), 3);
            assertTrue(vertices.contains(lop));
            assertTrue(vertices.contains(josh));
            assertTrue(vertices.contains(vadas));
            // test peter
            vertices = new HashSet<Vertex>();
            assertEquals(peter.getProperty("name"), "peter");
            assertEquals(((Number) peter.getProperty("age")).intValue(), 35);
            assertEquals(peter.getPropertyKeys().size(), 2);
            assertEquals(count(peter.getEdges(Direction.OUT)), 1);
            assertEquals(count(peter.getEdges(Direction.IN)), 0);
            for (Edge e : peter.getEdges(Direction.OUT)) {
                vertices.add(e.getVertex(Direction.IN));
            }
            assertEquals(vertices.size(), 1);
            assertTrue(vertices.contains(lop));
            // test josh
            vertices = new HashSet<Vertex>();
            assertEquals(josh.getProperty("name"), "josh");
            assertEquals(((Number) josh.getProperty("age")).intValue(), 32);
            assertEquals(josh.getPropertyKeys().size(), 2);
            assertEquals(count(josh.getEdges(Direction.OUT)), 2);
            assertEquals(count(josh.getEdges(Direction.IN)), 1);
            for (Edge e : josh.getEdges(Direction.OUT)) {
                vertices.add(e.getVertex(Direction.IN));
            }
            assertEquals(vertices.size(), 2);
            assertTrue(vertices.contains(lop));
            assertTrue(vertices.contains(ripple));
            vertices = new HashSet<Vertex>();
            for (Edge e : josh.getEdges(Direction.IN)) {
                vertices.add(e.getVertex(Direction.OUT));
            }
            assertEquals(vertices.size(), 1);
            assertTrue(vertices.contains(marko));
            // test vadas
            vertices = new HashSet<Vertex>();
            assertEquals(vadas.getProperty("name"), "vadas");
            assertEquals(((Number) vadas.getProperty("age")).intValue(), 27);
            assertEquals(vadas.getPropertyKeys().size(), 2);
            assertEquals(count(vadas.getEdges(Direction.OUT)), 0);
            assertEquals(count(vadas.getEdges(Direction.IN)), 1);
            for (Edge e : vadas.getEdges(Direction.IN)) {
                vertices.add(e.getVertex(Direction.OUT));
            }
            assertEquals(vertices.size(), 1);
            assertTrue(vertices.contains(marko));
            // test lop
            vertices = new HashSet<Vertex>();
            assertEquals(lop.getProperty("name"), "lop");
            assertEquals(lop.getProperty("lang"), "java");
            assertEquals(lop.getPropertyKeys().size(), 2);
            assertEquals(count(lop.getEdges(Direction.OUT)), 0);
            assertEquals(count(lop.getEdges(Direction.IN)), 3);
            for (Edge e : lop.getEdges(Direction.IN)) {
                vertices.add(e.getVertex(Direction.OUT));
            }
            assertEquals(vertices.size(), 3);
            assertTrue(vertices.contains(marko));
            assertTrue(vertices.contains(josh));
            assertTrue(vertices.contains(peter));
            // test ripple
            vertices = new HashSet<Vertex>();
            assertEquals(ripple.getProperty("name"), "ripple");
            assertEquals(ripple.getProperty("lang"), "java");
            assertEquals(ripple.getPropertyKeys().size(), 2);
            assertEquals(count(ripple.getEdges(Direction.OUT)), 0);
            assertEquals(count(ripple.getEdges(Direction.IN)), 1);
            for (Edge e : ripple.getEdges(Direction.IN)) {
                vertices.add(e.getVertex(Direction.OUT));
            }
            assertEquals(vertices.size(), 1);
            assertTrue(vertices.contains(josh));
        }
        graph.shutdown();
    }
}
