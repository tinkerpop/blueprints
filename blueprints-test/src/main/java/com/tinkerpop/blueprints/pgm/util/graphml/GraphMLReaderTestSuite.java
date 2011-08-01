package com.tinkerpop.blueprints.pgm.util.graphml;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TestSuite;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GraphMLReaderTestSuite extends TestSuite {

    public GraphMLReaderTestSuite() {
    }

    public GraphMLReaderTestSuite(GraphTest graphTest) {
        super(graphTest);
    }

    public void testReadingTinkerGraph() throws Exception {
        Graph graph = graphTest.getGraphInstance();
        if (!graphTest.ignoresSuppliedIds) {
            this.stopWatch();
            new GraphMLReader(graph).inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-1.xml"));
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
                    assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "9");
                    counter++;
                } else if (e.getInVertex().getId().equals("4")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 1);
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
                    assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "11");
                    counter++;
                } else if (e.getInVertex().getId().equals("5")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 1);
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
            new GraphMLReader(graph).inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-1.xml"));
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
            new GraphMLReader(graph).inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-1.xml"));
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
            new GraphMLReader(graph).inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-1.xml"));
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
            new GraphMLReader(graph).inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-1.xml"));
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
                new GraphMLReader(graph).inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-2.xml"), i);
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

    public void testReadingTinkerGraphExample3() throws Exception {
        Graph graph = this.graphTest.getGraphInstance();
        if (!graphTest.ignoresSuppliedIds && graphTest.supportsEdgeIteration && graphTest.supportsVertexIteration) {

            this.stopWatch();
            new GraphMLReader(graph).inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-3.xml"), 1000);
            printPerformance(graph.toString(), null, "graph-example-3 loaded", this.stopWatch());

            // Specific Graph Characteristics

            assertEquals(count(graph.getVertex("1").getOutEdges()), 3);
            assertEquals(count(graph.getVertex("1").getInEdges()), 0);
            Vertex marko = graph.getVertex("1");
            assertEquals(marko.getProperty("name"), "marko");
            assertEquals(marko.getProperty("age"), 29);
            assertEquals(marko.getProperty("_id"), 2);
            int counter = 0;
            for (Edge e : graph.getVertex("1").getOutEdges()) {
                if (e.getInVertex().getId().equals("2")) {
                    // assertEquals(e.getProperty("weight"), 0.5);
                    assertEquals(e.getProperty("_id"), 8);
                    assertEquals(e.getProperty("_label"), "has high fived");
                    assertEquals(e.getLabel(), "knows");
                    assertEquals(e.getId(), "7");
                    counter++;
                } else if (e.getInVertex().getId().equals("3")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                    assertEquals(e.getProperty("_id"), 10);
                    assertEquals(e.getProperty("_label"), "has high fived");
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "9");
                    counter++;
                } else if (e.getInVertex().getId().equals("4")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 1);
                    assertEquals(e.getProperty("_id"), 9);
                    assertEquals(e.getProperty("_label"), "has high fived");
                    assertEquals(e.getLabel(), "knows");
                    assertEquals(e.getId(), "8");
                    counter++;
                }
            }

            assertEquals(count(graph.getVertex("2").getOutEdges()), 0);
            assertEquals(count(graph.getVertex("2").getInEdges()), 1);
            Vertex vadas = graph.getVertex("2");
            assertEquals(vadas.getProperty("name"), "vadas");
            assertEquals(vadas.getProperty("age"), 27);
            assertEquals(vadas.getProperty("_id"), 3);

            assertEquals(count(graph.getVertex("3").getOutEdges()), 0);
            assertEquals(count(graph.getVertex("3").getInEdges()), 3);
            Vertex lop = graph.getVertex("3");
            assertEquals(lop.getProperty("name"), "lop");
            assertEquals(lop.getProperty("lang"), "java");
            assertEquals(lop.getProperty("_id"), 4);

            assertEquals(count(graph.getVertex("4").getOutEdges()), 2);
            assertEquals(count(graph.getVertex("4").getInEdges()), 1);
            Vertex josh = graph.getVertex("4");
            assertEquals(josh.getProperty("name"), "josh");
            assertEquals(josh.getProperty("age"), 32);
            for (Edge e : graph.getVertex("4").getOutEdges()) {
                if (e.getInVertex().getId().equals("3")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                    assertEquals(e.getProperty("_id"), 13);
                    assertEquals(e.getProperty("_label"), null);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "11");
                    counter++;
                } else if (e.getInVertex().getId().equals("5")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 1);
                    assertEquals(e.getProperty("_id"), 11);
                    assertEquals(e.getProperty("_label"), "has high fived");
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "10");
                    counter++;
                }
            }

            assertEquals(count(graph.getVertex("5").getOutEdges()), 0);
            assertEquals(count(graph.getVertex("5").getInEdges()), 1);
            Vertex ripple = graph.getVertex("5");
            assertEquals(ripple.getProperty("name"), "ripple");
            assertEquals(ripple.getProperty("lang"), "java");
            assertEquals(ripple.getProperty("_id"), 7);

            assertEquals(count(graph.getVertex("6").getOutEdges()), 1);
            assertEquals(count(graph.getVertex("6").getInEdges()), 0);
            Vertex peter = graph.getVertex("6");
            assertEquals(peter.getProperty("name"), "peter");
            assertEquals(peter.getProperty("age"), 35);

            for (Edge e : graph.getVertex("6").getOutEdges()) {
                if (e.getInVertex().getId().equals("3")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                    assertEquals(e.getProperty("_id"), null);
                    assertEquals(e.getProperty("_label"), null);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "12");
                    counter++;
                }
            }

            assertEquals(counter, 6);

            // General Graph Characteristics

            Set<String> vertexIds = new HashSet<String>();
            Set<String> vertexKeys = new HashSet<String>();
            Set<String> vertexNames = new HashSet<String>();
            int vertexCount = 0;
            for (Vertex v : graph.getVertices()) {
                vertexCount++;
                vertexIds.add(v.getId().toString());
                vertexNames.add(v.getProperty("name").toString());
                for (String key : v.getPropertyKeys())
                    vertexKeys.add(key);
            }

            Set<String> edgeIds = new HashSet<String>();
            Set<String> edgeKeys = new HashSet<String>();
            int edgeCount = 0;
            for (Edge e : graph.getEdges()) {
                edgeCount++;
                edgeIds.add(e.getId().toString());
                for (String key : e.getPropertyKeys())
                    edgeKeys.add(key);
            }

            assertEquals(vertexCount, 6);
            assertEquals(vertexIds.size(), 6);
            assertEquals(vertexKeys.contains("name"), true);
            assertEquals(vertexKeys.contains("age"), true);
            assertEquals(vertexKeys.contains("lang"), true);
            assertEquals(vertexKeys.contains("_id"), true);
            assertEquals(vertexKeys.size(), 4);

            assertEquals(edgeCount, 6);
            assertEquals(edgeIds.size(), 6);
            assertEquals(edgeKeys.contains("weight"), true);
            assertEquals(edgeKeys.contains("_id"), true);
            assertEquals(edgeKeys.contains("_label"), true);
            assertEquals(edgeKeys.size(), 3);
        }
        graph.shutdown();
    }

    public void testReadingTinkerGraphExample3MappingLabels() throws Exception {
        Graph graph = this.graphTest.getGraphInstance();
        if (graphTest.supportsEdgeIteration && graphTest.supportsVertexIteration) {
            this.stopWatch();
            GraphMLReader r = new GraphMLReader(graph);
            r.setEdgeLabelKey("_label");
            r.inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-3.xml"), 1000);
            printPerformance(graph.toString(), null, "graph-example-3 loaded", this.stopWatch());

            Set<String> vertexIds = new HashSet<String>();
            Set<String> vertexKeys = new HashSet<String>();
            Set<String> vertexNames = new HashSet<String>();
            int vertexCount = 0;
            for (Vertex v : graph.getVertices()) {
                vertexCount++;
                vertexIds.add(v.getId().toString());
                vertexNames.add(v.getProperty("name").toString());
                for (String key : v.getPropertyKeys())
                    vertexKeys.add(key);
            }

            Set<String> edgeIds = new HashSet<String>();
            Set<String> edgeKeys = new HashSet<String>();
            Set<String> edgeLabels = new HashSet<String>();
            int edgeCount = 0;
            for (Edge e : graph.getEdges()) {
                edgeCount++;
                edgeIds.add(e.getId().toString());
                edgeLabels.add(e.getLabel());
                for (String key : e.getPropertyKeys())
                    edgeKeys.add(key);
            }

            assertEquals(vertexCount, 6);
            assertEquals(vertexIds.size(), 6);
            assertEquals(vertexKeys.contains("name"), true);
            assertEquals(vertexKeys.contains("age"), true);
            assertEquals(vertexKeys.contains("lang"), true);
            assertEquals(vertexKeys.contains("_id"), true);
            assertEquals(vertexKeys.size(), 4);
            assertTrue(vertexNames.contains("marko"));
            assertTrue(vertexNames.contains("josh"));
            assertTrue(vertexNames.contains("peter"));
            assertTrue(vertexNames.contains("vadas"));
            assertTrue(vertexNames.contains("ripple"));
            assertTrue(vertexNames.contains("lop"));

            assertEquals(edgeCount, 6);
            assertEquals(edgeIds.size(), 6);
            assertEquals(edgeKeys.contains("weight"), true);
            assertEquals(edgeKeys.contains("_id"), true);
            assertEquals(edgeKeys.contains("_label"), false);
            assertEquals(edgeKeys.size(), 2);
            assertEquals(edgeLabels.size(), 2);
            assertEquals(edgeLabels.contains("has high fived"), true);
            assertEquals(edgeLabels.contains("knows"), false);
            assertEquals(edgeLabels.contains("created"), true);
        }
        graph.shutdown();
    }

    public void testReadingTinkerGraphExample3MappingIDs() throws Exception {
        Graph graph = this.graphTest.getGraphInstance();
        if (graphTest.supportsEdgeIteration && graphTest.supportsVertexIteration) {
            this.stopWatch();
            GraphMLReader r = new GraphMLReader(graph);
            r.setVertexIdKey("_id");
            r.setEdgeIdKey("_id");
            r.inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-3.xml"), 1000);
            printPerformance(graph.toString(), null, "graph-example-3 loaded", this.stopWatch());

            Set<String> vertexIds = new HashSet<String>();
            Set<String> vertexKeys = new HashSet<String>();
            Set<String> vertexNames = new HashSet<String>();
            int vertexCount = 0;
            for (Vertex v : graph.getVertices()) {
                vertexCount++;
                vertexIds.add(v.getId().toString());
                vertexNames.add(v.getProperty("name").toString());
                for (String key : v.getPropertyKeys())
                    vertexKeys.add(key);
            }

            Set<String> edgeIds = new HashSet<String>();
            Set<String> edgeKeys = new HashSet<String>();
            Set<String> edgeLabels = new HashSet<String>();
            int edgeCount = 0;
            for (Edge e : graph.getEdges()) {
                edgeCount++;
                edgeIds.add(e.getId().toString());
                edgeLabels.add(e.getLabel());
                for (String key : e.getPropertyKeys())
                    edgeKeys.add(key);
            }

            assertEquals(vertexCount, 6);
            assertEquals(vertexIds.size(), 6);
            assertEquals(vertexKeys.contains("name"), true);
            assertEquals(vertexKeys.contains("age"), true);
            assertEquals(vertexKeys.contains("lang"), true);
            assertEquals(vertexKeys.contains("_id"), false);
            assertEquals(vertexKeys.size(), 3);
            assertTrue(vertexNames.contains("marko"));
            assertTrue(vertexNames.contains("josh"));
            assertTrue(vertexNames.contains("peter"));
            assertTrue(vertexNames.contains("vadas"));
            assertTrue(vertexNames.contains("ripple"));
            assertTrue(vertexNames.contains("lop"));

            assertEquals(edgeCount, 6);
            assertEquals(edgeIds.size(), 6);
            assertEquals(edgeKeys.contains("weight"), true);
            assertEquals(edgeKeys.contains("_id"), false);
            assertEquals(edgeKeys.contains("_label"), true);
            assertEquals(edgeKeys.size(), 2);
            assertEquals(edgeLabels.size(), 2);
            assertEquals(edgeLabels.contains("has high fived"), false);
            assertEquals(edgeLabels.contains("knows"), true);
            assertEquals(edgeLabels.contains("created"), true);
        }
        graph.shutdown();
    }

    public void testReadingTinkerGraphExample3MappingAll() throws Exception {
        Graph graph = this.graphTest.getGraphInstance();
        if (graphTest.supportsEdgeIteration && graphTest.supportsVertexIteration) {
            this.stopWatch();
            GraphMLReader r = new GraphMLReader(graph);
            r.setVertexIdKey("_id");
            r.setEdgeIdKey("_id");
            r.setEdgeLabelKey("_label");
            r.inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-3.xml"), 1000);
            printPerformance(graph.toString(), null, "graph-example-3 loaded", this.stopWatch());

            Set<String> vertexIds = new HashSet<String>();
            Set<String> vertexKeys = new HashSet<String>();
            Set<String> vertexNames = new HashSet<String>();
            int vertexCount = 0;
            for (Vertex v : graph.getVertices()) {
                vertexCount++;
                vertexIds.add(v.getId().toString());
                vertexNames.add(v.getProperty("name").toString());
                for (String key : v.getPropertyKeys())
                    vertexKeys.add(key);
            }

            Set<String> edgeIds = new HashSet<String>();
            Set<String> edgeKeys = new HashSet<String>();
            Set<String> edgeLabels = new HashSet<String>();
            int edgeCount = 0;
            for (Edge e : graph.getEdges()) {
                edgeCount++;
                edgeIds.add(e.getId().toString());
                edgeLabels.add(e.getLabel());
                for (String key : e.getPropertyKeys())
                    edgeKeys.add(key);
            }

            assertEquals(vertexCount, 6);
            assertEquals(vertexIds.size(), 6);
            assertEquals(vertexKeys.contains("name"), true);
            assertEquals(vertexKeys.contains("age"), true);
            assertEquals(vertexKeys.contains("lang"), true);
            assertEquals(vertexKeys.contains("_id"), false);
            assertEquals(vertexKeys.size(), 3);
            assertTrue(vertexNames.contains("marko"));
            assertTrue(vertexNames.contains("josh"));
            assertTrue(vertexNames.contains("peter"));
            assertTrue(vertexNames.contains("vadas"));
            assertTrue(vertexNames.contains("ripple"));
            assertTrue(vertexNames.contains("lop"));

            assertEquals(edgeCount, 6);
            assertEquals(edgeIds.size(), 6);
            assertEquals(edgeKeys.contains("weight"), true);
            assertEquals(edgeKeys.contains("_id"), false);
            assertEquals(edgeKeys.contains("_label"), false);
            assertEquals(edgeKeys.size(), 1);
            assertEquals(edgeLabels.size(), 2);
            assertEquals(edgeLabels.contains("has high fived"), true);
            assertEquals(edgeLabels.contains("knows"), false);
            assertEquals(edgeLabels.contains("created"), true);
        }
        graph.shutdown();
    }

    public void testMigratingTinkerGraphExample3() throws Exception {
        Graph graph = this.graphTest.getGraphInstance();
        if (!graphTest.ignoresSuppliedIds && graphTest.supportsEdgeIteration && graphTest.supportsVertexIteration) {

            this.stopWatch();
            new GraphMLReader(graph).inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-3.xml"), 1000);
            printPerformance(graph.toString(), null, "graph-example-3 loaded", this.stopWatch());

            this.stopWatch();
            // FIXME Should not explicitly define the Graph type (TinkerGraph)
            // here. Need to accept 2 graphs as input params?
            Graph toGraph = new TinkerGraph();
            GraphMigrator.migrateGraph(graph, toGraph);
            printPerformance(toGraph.toString(), null, "graph-example-3 migrated", this.stopWatch());

            // Specific Graph Characteristics

            assertEquals(count(toGraph.getVertex("1").getOutEdges()), 3);
            assertEquals(count(toGraph.getVertex("1").getInEdges()), 0);
            Vertex marko = toGraph.getVertex("1");
            assertEquals(marko.getProperty("name"), "marko");
            assertEquals(marko.getProperty("age"), 29);
            assertEquals(marko.getProperty("_id"), 2);
            int counter = 0;
            for (Edge e : toGraph.getVertex("1").getOutEdges()) {
                if (e.getInVertex().getId().equals("2")) {
                    // assertEquals(e.getProperty("weight"), 0.5);
                    assertEquals(e.getProperty("_id"), 8);
                    assertEquals(e.getProperty("_label"), "has high fived");
                    assertEquals(e.getLabel(), "knows");
                    assertEquals(e.getId(), "7");
                    counter++;
                } else if (e.getInVertex().getId().equals("3")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                    assertEquals(e.getProperty("_id"), 10);
                    assertEquals(e.getProperty("_label"), "has high fived");
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "9");
                    counter++;
                } else if (e.getInVertex().getId().equals("4")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 1);
                    assertEquals(e.getProperty("_id"), 9);
                    assertEquals(e.getProperty("_label"), "has high fived");
                    assertEquals(e.getLabel(), "knows");
                    assertEquals(e.getId(), "8");
                    counter++;
                }
            }

            assertEquals(count(toGraph.getVertex("2").getOutEdges()), 0);
            assertEquals(count(toGraph.getVertex("2").getInEdges()), 1);
            Vertex vadas = toGraph.getVertex("2");
            assertEquals(vadas.getProperty("name"), "vadas");
            assertEquals(vadas.getProperty("age"), 27);
            assertEquals(vadas.getProperty("_id"), 3);

            assertEquals(count(toGraph.getVertex("3").getOutEdges()), 0);
            assertEquals(count(toGraph.getVertex("3").getInEdges()), 3);
            Vertex lop = toGraph.getVertex("3");
            assertEquals(lop.getProperty("name"), "lop");
            assertEquals(lop.getProperty("lang"), "java");
            assertEquals(lop.getProperty("_id"), 4);

            assertEquals(count(toGraph.getVertex("4").getOutEdges()), 2);
            assertEquals(count(toGraph.getVertex("4").getInEdges()), 1);
            Vertex josh = toGraph.getVertex("4");
            assertEquals(josh.getProperty("name"), "josh");
            assertEquals(josh.getProperty("age"), 32);
            for (Edge e : toGraph.getVertex("4").getOutEdges()) {
                if (e.getInVertex().getId().equals("3")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                    assertEquals(e.getProperty("_id"), 13);
                    assertEquals(e.getProperty("_label"), null);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "11");
                    counter++;
                } else if (e.getInVertex().getId().equals("5")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 1);
                    assertEquals(e.getProperty("_id"), 11);
                    assertEquals(e.getProperty("_label"), "has high fived");
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "10");
                    counter++;
                }
            }

            assertEquals(count(toGraph.getVertex("5").getOutEdges()), 0);
            assertEquals(count(toGraph.getVertex("5").getInEdges()), 1);
            Vertex ripple = toGraph.getVertex("5");
            assertEquals(ripple.getProperty("name"), "ripple");
            assertEquals(ripple.getProperty("lang"), "java");
            assertEquals(ripple.getProperty("_id"), 7);

            assertEquals(count(toGraph.getVertex("6").getOutEdges()), 1);
            assertEquals(count(toGraph.getVertex("6").getInEdges()), 0);
            Vertex peter = toGraph.getVertex("6");
            assertEquals(peter.getProperty("name"), "peter");
            assertEquals(peter.getProperty("age"), 35);

            for (Edge e : toGraph.getVertex("6").getOutEdges()) {
                if (e.getInVertex().getId().equals("3")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                    assertEquals(e.getProperty("_id"), null);
                    assertEquals(e.getProperty("_label"), null);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "12");
                    counter++;
                }
            }

            assertEquals(counter, 6);

            // General Graph Characteristics

            Set<String> vertexIds = new HashSet<String>();
            Set<String> vertexKeys = new HashSet<String>();
            int vertexCount = 0;
            for (Vertex v : toGraph.getVertices()) {
                vertexCount++;
                vertexIds.add(v.getId().toString());
                for (String key : v.getPropertyKeys())
                    vertexKeys.add(key);
            }

            Set<String> edgeIds = new HashSet<String>();
            Set<String> edgeKeys = new HashSet<String>();
            int edgeCount = 0;
            for (Edge e : toGraph.getEdges()) {
                edgeCount++;
                edgeIds.add(e.getId().toString());
                for (String key : e.getPropertyKeys())
                    edgeKeys.add(key);
            }

            assertEquals(vertexCount, 6);
            assertEquals(vertexIds.size(), 6);
            assertEquals(vertexKeys.contains("name"), true);
            assertEquals(vertexKeys.contains("age"), true);
            assertEquals(vertexKeys.contains("lang"), true);
            assertEquals(vertexKeys.contains("_id"), true);
            assertEquals(vertexKeys.size(), 4);

            assertEquals(edgeCount, 6);
            assertEquals(edgeIds.size(), 6);
            assertEquals(edgeKeys.contains("weight"), true);
            assertEquals(edgeKeys.contains("_id"), true);
            assertEquals(edgeKeys.contains("_label"), true);
            assertEquals(edgeKeys.size(), 3);
        }
        graph.shutdown();
    }

}
