package com.tinkerpop.blueprints.pgm.util.io.graphson;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TestSuite;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;

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
        Graph graph = graphTest.getGraphInstance();
        if (!graphTest.ignoresSuppliedIds) {
            this.stopWatch();
            new GraphSONReader(graph).inputGraph(GraphSONReader.class.getResourceAsStream("graph-example-1.json"));
            printPerformance(graph.toString(), null, "graph-example-1 loaded", this.stopWatch());

            assertEquals(count(graph.getVertex("1").getOutEdges()), 3);
            assertEquals(count(graph.getVertex("1").getInEdges()), 0);
            Vertex marko = graph.getVertex("1");
            assertEquals(marko.getProperty("name"), "marko");
            assertEquals(marko.getProperty("age"), 29);
            int counter = 0;
            for (Edge e : graph.getVertex("1").getOutEdges()) {
                if (e.getInVertex().getId().equals("2")) {
                    // assertEquals(e.getProperty("weight"), 0.5);
                    assertEquals(e.getLabel(), "knows");
                    assertEquals(e.getId(), "7");
                    counter++;
                } else if (e.getInVertex().getId().equals("3")) {
                    assertEquals(Math.round((Double) e.getProperty("weight")), 0);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "9");
                    counter++;
                } else if (e.getInVertex().getId().equals("4")) {
                    assertEquals(Math.round((Double) e.getProperty("weight")), 1);
                    assertEquals(e.getLabel(), "knows");
                    assertEquals(e.getId(), "8");
                    counter++;
                }
            }

            assertEquals(count(graph.getVertex("4").getOutEdges()), 2);
            assertEquals(count(graph.getVertex("4").getInEdges()), 1);
            Vertex josh = graph.getVertex("4");
            assertEquals(josh.getProperty("name"), "josh");
            assertEquals(josh.getProperty("age"), 32);
            for (Edge e : graph.getVertex("4").getOutEdges()) {
                if (e.getInVertex().getId().equals("3")) {
                    assertEquals(Math.round((Double) e.getProperty("weight")), 0);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "11");
                    counter++;
                } else if (e.getInVertex().getId().equals("5")) {
                    assertEquals(Math.round((Double) e.getProperty("weight")), 1);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "10");
                    counter++;
                }
            }

            assertEquals(counter, 5);
        }
        graph.shutdown();
    }

    public void testTinkerGraphEdges() throws Exception {
        Graph graph = this.graphTest.getGraphInstance();
        if (graphTest.supportsEdgeIteration) {
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
        Graph graph = this.graphTest.getGraphInstance();
        if (graphTest.supportsVertexIteration) {
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
        Graph graph = this.graphTest.getGraphInstance();
        if (graphTest.supportsVertexIteration) {
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
        Graph graph = this.graphTest.getGraphInstance();
        if (graphTest.supportsVertexIteration) {
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

            if (graphTest.supportsEdgeIteration) {
                assertEquals(count(graph.getEdges()), 6);
            }

            // test marko
            Set<Vertex> vertices = new HashSet<Vertex>();
            assertEquals(marko.getProperty("name"), "marko");
            assertEquals(((Number) marko.getProperty("age")).intValue(), 29);
            assertEquals(marko.getPropertyKeys().size(), 2);
            assertEquals(count(marko.getOutEdges()), 3);
            assertEquals(count(marko.getInEdges()), 0);
            for (Edge e : marko.getOutEdges()) {
                vertices.add(e.getInVertex());
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
            assertEquals(count(peter.getOutEdges()), 1);
            assertEquals(count(peter.getInEdges()), 0);
            for (Edge e : peter.getOutEdges()) {
                vertices.add(e.getInVertex());
            }
            assertEquals(vertices.size(), 1);
            assertTrue(vertices.contains(lop));
            // test josh
            vertices = new HashSet<Vertex>();
            assertEquals(josh.getProperty("name"), "josh");
            assertEquals(((Number) josh.getProperty("age")).intValue(), 32);
            assertEquals(josh.getPropertyKeys().size(), 2);
            assertEquals(count(josh.getOutEdges()), 2);
            assertEquals(count(josh.getInEdges()), 1);
            for (Edge e : josh.getOutEdges()) {
                vertices.add(e.getInVertex());
            }
            assertEquals(vertices.size(), 2);
            assertTrue(vertices.contains(lop));
            assertTrue(vertices.contains(ripple));
            vertices = new HashSet<Vertex>();
            for (Edge e : josh.getInEdges()) {
                vertices.add(e.getOutVertex());
            }
            assertEquals(vertices.size(), 1);
            assertTrue(vertices.contains(marko));
            // test vadas
            vertices = new HashSet<Vertex>();
            assertEquals(vadas.getProperty("name"), "vadas");
            assertEquals(((Number) vadas.getProperty("age")).intValue(), 27);
            assertEquals(vadas.getPropertyKeys().size(), 2);
            assertEquals(count(vadas.getOutEdges()), 0);
            assertEquals(count(vadas.getInEdges()), 1);
            for (Edge e : vadas.getInEdges()) {
                vertices.add(e.getOutVertex());
            }
            assertEquals(vertices.size(), 1);
            assertTrue(vertices.contains(marko));
            // test lop
            vertices = new HashSet<Vertex>();
            assertEquals(lop.getProperty("name"), "lop");
            assertEquals(lop.getProperty("lang"), "java");
            assertEquals(lop.getPropertyKeys().size(), 2);
            assertEquals(count(lop.getOutEdges()), 0);
            assertEquals(count(lop.getInEdges()), 3);
            for (Edge e : lop.getInEdges()) {
                vertices.add(e.getOutVertex());
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
            assertEquals(count(ripple.getOutEdges()), 0);
            assertEquals(count(ripple.getInEdges()), 1);
            for (Edge e : ripple.getInEdges()) {
                vertices.add(e.getOutVertex());
            }
            assertEquals(vertices.size(), 1);
            assertTrue(vertices.contains(josh));
        }
        graph.shutdown();
    }


    public void donttestGratefulGraph() throws Exception {
        Graph graph = this.graphTest.getGraphInstance();
        if (graphTest.supportsVertexIndex) {
            for (int i = 200; i < 1002; i = i + 200) {
                graph.clear();
                ((IndexableGraph) graph).createAutomaticIndex(Index.VERTICES, Vertex.class, null);
                this.stopWatch();
                new GraphSONReader(graph).inputGraph(GraphSONReader.class.getResourceAsStream("graph-example-2.json"), i);
                if (graph instanceof TransactionalGraph)
                    printPerformance(graph.toString(), null, "graph-example-2 loaded with buffer size equal to " + i + " for transactional graphs", this.stopWatch());
                else
                    printPerformance(graph.toString(), null, "graph-example-2 loaded", this.stopWatch());


                if (graphTest.supportsVertexIteration) {
                    assertEquals(count(graph.getVertices()), 809);
                }
                if (graphTest.supportsEdgeIteration) {
                    assertEquals(count(graph.getEdges()), 8049);
                }
                assertEquals(count(((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "Garcia")), 1);
                assertEquals(count(((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "Weir")), 1);
                assertEquals(count(((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "Lesh")), 1);
                assertEquals(count(((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "DARK STAR")), 1);
                assertEquals(count(((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "TERRAPIN STATION")), 1);
                assertEquals(count(((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "TERRAPIN STATION BAD SPELLING")), 0);

                Vertex garcia = ((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "Garcia").iterator().next();
                boolean found = false;
                for (Edge edge : garcia.getInEdges()) {
                    if (edge.getLabel().equals("sung_by")) {
                        if (edge.getOutVertex().getProperty("name").equals("TERRAPIN STATION"))
                            found = true;
                    }
                }
                assertTrue(found);

                if (!(graph instanceof TransactionalGraph)) {
                    i = 2000;
                }
            }
        }
        graph.shutdown();
    }

}
