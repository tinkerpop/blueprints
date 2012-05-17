package com.tinkerpop.blueprints.util.io.gml;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMigrator;

import java.util.HashSet;
import java.util.Set;

public class GMLReaderTestSuite extends TestSuite {

    public GMLReaderTestSuite() {
    }

    public GMLReaderTestSuite(GraphTest graphTest) {
        super(graphTest);
    }

    public void testReadingTinkerGraph() throws Exception {
        Graph graph = graphTest.generateGraph();

        // note that GML does not have the notion of Edge Identifiers built into the specification
        // so that values are not tested here even for graphs that allow edge identifier assignment
        // like tinkergraph.
        if (!graph.getFeatures().ignoresSuppliedIds) {
            this.stopWatch();
            GMLReader gmlReader = new GMLReader(graph);
            gmlReader.inputGraph(GMLReader.class.getResourceAsStream("graph-example-1.gml"));
            printPerformance(graph.toString(), null, "graph-example-1 loaded", this.stopWatch());

            assertEquals(count(graph.getVertex("1").getEdges(Direction.OUT)), 3);
            assertEquals(count(graph.getVertex("1").getEdges(Direction.IN)), 0);
            Vertex marko = graph.getVertex("1");
            assertEquals(marko.getProperty("name"), "marko");
            assertEquals(marko.getProperty("age"), 29);
            int counter = 0;
            for (Edge e : graph.getVertex("1").getEdges(Direction.OUT)) {
                if (e.getVertex(Direction.IN).getId().equals("2")) {
                    // assertEquals(e.getProperty("weight"), 0.5);
                    assertEquals(e.getLabel(), "knows");
                    counter++;
                } else if (e.getVertex(Direction.IN).getId().equals("3")) {
                    assertEquals(0, Math.round(((Number) e.getProperty("weight")).floatValue()));
                    assertEquals(e.getLabel(), "created");
                    counter++;
                } else if (e.getVertex(Direction.IN).getId().equals("4")) {
                    assertEquals(1, Math.round(((Number) e.getProperty("weight")).floatValue()));
                    assertEquals(e.getLabel(), "knows");
                    counter++;
                }
            }

            assertEquals(count(graph.getVertex("4").getEdges(Direction.OUT)), 2);
            assertEquals(count(graph.getVertex("4").getEdges(Direction.IN)), 1);
            Vertex josh = graph.getVertex("4");
            assertEquals(josh.getProperty("name"), "josh");
            assertEquals(josh.getProperty("age"), 32);
            for (Edge e : graph.getVertex("4").getEdges(Direction.OUT)) {
                if (e.getVertex(Direction.IN).getId().equals("3")) {
                    assertEquals(0, Math.round(((Number) e.getProperty("weight")).floatValue()));
                    assertEquals(e.getLabel(), "created");
                    counter++;
                } else if (e.getVertex(Direction.IN).getId().equals("5")) {
                    assertEquals(1, Math.round(((Number) e.getProperty("weight")).floatValue()));
                    assertEquals(e.getLabel(), "created");
                    counter++;
                }
            }

            assertEquals(counter, 5);
        }
        graph.shutdown();
    }

    public void testTinkerGraphEdges() throws Exception {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeIteration) {
            this.stopWatch();
            GMLReader gmlReader = new GMLReader(graph);
            gmlReader.inputGraph(GMLReader.class.getResourceAsStream("graph-example-1.gml"));
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
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIteration) {
            this.stopWatch();
            new GMLReader(graph).inputGraph(GMLReader.class.getResourceAsStream("graph-example-1.gml"));
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
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIteration) {
            this.stopWatch();
            new GMLReader(graph).inputGraph(GMLReader.class.getResourceAsStream("graph-example-1.gml"));
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
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIteration) {
            this.stopWatch();
            GMLReader gmlReader = new GMLReader(graph);
            gmlReader.inputGraph(GMLReader.class.getResourceAsStream("graph-example-1.gml"));
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

    public void testReadingTinkerGraphExample3() throws Exception {
        Graph graph = graphTest.generateGraph();
        if (!graph.getFeatures().ignoresSuppliedIds && graph.getFeatures().supportsEdgeIteration && graph.getFeatures().supportsVertexIteration) {

            this.stopWatch();
            GMLReader gmlReader = new GMLReader(graph);
            gmlReader.setEdgeIdKey(GMLTokens.ID);
            gmlReader.inputGraph(GMLReader.class.getResourceAsStream("graph-example-3.gml"), 1000);
            printPerformance(graph.toString(), null, "graph-example-3 loaded", this.stopWatch());

            // Specific Graph Characteristics

            assertEquals(count(graph.getVertex(1).getEdges(Direction.OUT)), 3);
            assertEquals(count(graph.getVertex(1).getEdges(Direction.IN)), 0);
            Vertex marko = graph.getVertex(1);
            assertEquals(marko.getProperty("name"), "marko");
            assertEquals(marko.getProperty("age"), 29);
            assertEquals(marko.getProperty("id2"), 2);
            int counter = 0;
            for (Edge e : graph.getVertex(1).getEdges(Direction.OUT)) {
                if (e.getVertex(Direction.IN).getId().equals("2")) {
                    // assertEquals(e.getProperty("weight"), 0.5);
                    assertEquals(e.getProperty("id2"), 8);
                    assertEquals(e.getProperty("label2"), "has high fived");
                    assertEquals(e.getLabel(), "knows");
                    assertEquals(e.getId(), "7");
                    counter++;
                } else if (e.getVertex(Direction.IN).getId().equals("3")) {
                    assertEquals(0, Math.round(((Number) e.getProperty("weight")).floatValue()));
                    assertEquals(e.getProperty("id2"), 10);
                    assertEquals(e.getProperty("label2"), "has high fived");
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "9");
                    counter++;
                } else if (e.getVertex(Direction.IN).getId().equals("4")) {
                    assertEquals(1, Math.round(((Number) e.getProperty("weight")).floatValue()));
                    assertEquals(e.getProperty("id2"), 9);
                    assertEquals(e.getProperty("label2"), "has high fived");
                    assertEquals(e.getLabel(), "knows");
                    assertEquals(e.getId(), "8");
                    counter++;
                }
            }

            assertEquals(count(graph.getVertex(2).getEdges(Direction.OUT)), 0);
            assertEquals(count(graph.getVertex(2).getEdges(Direction.IN)), 1);
            Vertex vadas = graph.getVertex(2);
            assertEquals(vadas.getProperty("name"), "vadas");
            assertEquals(vadas.getProperty("age"), 27);
            assertEquals(vadas.getProperty("id2"), 3);

            assertEquals(count(graph.getVertex(3).getEdges(Direction.OUT)), 0);
            assertEquals(count(graph.getVertex(3).getEdges(Direction.IN)), 3);
            Vertex lop = graph.getVertex(3);
            assertEquals(lop.getProperty("name"), "lop");
            assertEquals(lop.getProperty("lang"), "java");
            assertEquals(lop.getProperty("id2"), 4);

            assertEquals(count(graph.getVertex(4).getEdges(Direction.OUT)), 2);
            assertEquals(count(graph.getVertex(4).getEdges(Direction.IN)), 1);
            Vertex josh = graph.getVertex(4);
            assertEquals(josh.getProperty("name"), "josh");
            assertEquals(josh.getProperty("age"), 32);
            for (Edge e : graph.getVertex(4).getEdges(Direction.OUT)) {
                if (e.getVertex(Direction.IN).getId().equals("3")) {
                    assertEquals(Math.round(((Number) e.getProperty("weight")).floatValue()), 0);
                    assertEquals(e.getProperty("id2"), 13);
                    assertEquals(e.getProperty("label2"), null);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "11");
                    counter++;
                } else if (e.getVertex(Direction.IN).getId().equals("5")) {
                    assertEquals(Math.round(((Number) e.getProperty("weight")).floatValue()), 1);
                    assertEquals(e.getProperty("id2"), 11);
                    assertEquals(e.getProperty("label2"), "has high fived");
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "10");
                    counter++;
                }
            }

            assertEquals(count(graph.getVertex(5).getEdges(Direction.OUT)), 0);
            assertEquals(count(graph.getVertex(5).getEdges(Direction.IN)), 1);
            Vertex ripple = graph.getVertex(5);
            assertEquals(ripple.getProperty("name"), "ripple");
            assertEquals(ripple.getProperty("lang"), "java");
            assertEquals(ripple.getProperty("id2"), 7);

            assertEquals(count(graph.getVertex(6).getEdges(Direction.OUT)), 1);
            assertEquals(count(graph.getVertex(6).getEdges(Direction.IN)), 0);
            Vertex peter = graph.getVertex(6);
            assertEquals(peter.getProperty("name"), "peter");
            assertEquals(peter.getProperty("age"), 35);

            for (Edge e : graph.getVertex(6).getEdges(Direction.OUT)) {
                if (e.getVertex(Direction.IN).getId().equals("3")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                    assertEquals(e.getProperty("id2"), null);
                    assertEquals(e.getProperty("label2"), null);
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
                for (String key : v.getPropertyKeys()) {
                    vertexKeys.add(key);
                }
            }

            Set<String> edgeIds = new HashSet<String>();
            Set<String> edgeKeys = new HashSet<String>();
            int edgeCount = 0;
            for (Edge e : graph.getEdges()) {
                edgeCount++;
                edgeIds.add(e.getId().toString());
                for (String key : e.getPropertyKeys()) {
                    edgeKeys.add(key);
                }
            }

            assertEquals(vertexCount, 6);
            assertEquals(vertexIds.size(), 6);
            assertEquals(vertexKeys.contains("name"), true);
            assertEquals(vertexKeys.contains("age"), true);
            assertEquals(vertexKeys.contains("lang"), true);
            assertEquals(vertexKeys.contains("id2"), true);
            assertEquals(vertexKeys.size(), 4);

            assertEquals(edgeCount, 6);
            assertEquals(edgeIds.size(), 6);
            assertEquals(edgeKeys.contains("weight"), true);
            assertEquals(edgeKeys.contains("id2"), true);
            assertEquals(edgeKeys.contains("label2"), true);
            assertEquals(edgeKeys.size(), 3);
        }
        graph.shutdown();
    }

    public void testReadingTinkerGraphExample3MappingLabels() throws Exception {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeIteration && graph.getFeatures().supportsVertexIteration) {
            this.stopWatch();
            GMLReader r = new GMLReader(graph);
            r.setEdgeLabelKey("label2");
            r.setEdgeIdKey("id");
            r.inputGraph(GMLReader.class.getResourceAsStream("graph-example-3.gml"), 1000);
            printPerformance(graph.toString(), null, "graph-example-3 loaded", this.stopWatch());

            Set<String> vertexIds = new HashSet<String>();
            Set<String> vertexKeys = new HashSet<String>();
            Set<String> vertexNames = new HashSet<String>();
            int vertexCount = 0;
            for (Vertex v : graph.getVertices()) {
                vertexCount++;
                vertexIds.add(v.getId().toString());
                vertexNames.add(v.getProperty("name").toString());
                for (String key : v.getPropertyKeys()) {
                    vertexKeys.add(key);
                }
            }

            Set<String> edgeIds = new HashSet<String>();
            Set<String> edgeKeys = new HashSet<String>();
            Set<String> edgeLabels = new HashSet<String>();
            int edgeCount = 0;
            for (Edge e : graph.getEdges()) {
                edgeCount++;
                edgeIds.add(e.getId().toString());
                edgeLabels.add(e.getLabel());
                for (String key : e.getPropertyKeys()) {
                    edgeKeys.add(key);
                }
            }

            assertEquals(vertexCount, 6);
            assertEquals(vertexIds.size(), 6);
            assertEquals(vertexKeys.contains("name"), true);
            assertEquals(vertexKeys.contains("age"), true);
            assertEquals(vertexKeys.contains("lang"), true);
            assertEquals(vertexKeys.contains("id2"), true);
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
            assertEquals(edgeKeys.contains("id2"), true);
            assertEquals(edgeKeys.contains("label2"), false);
            assertEquals(edgeKeys.size(), 2);
            assertEquals(edgeLabels.size(), 2);
            assertEquals(edgeLabels.contains("has high fived"), true);
            assertEquals(edgeLabels.contains("knows"), false);
            assertEquals(edgeLabels.contains("created"), true);
        }
        graph.shutdown();
    }

    public void testReadingTinkerGraphExample3MappingIDs() throws Exception {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeIteration && graph.getFeatures().supportsVertexIteration) {
            this.stopWatch();
            GMLReader r = new GMLReader(graph);
            r.setVertexIdKey("id2");
            r.setEdgeIdKey("id2");
            r.inputGraph(GMLReader.class.getResourceAsStream("graph-example-3.gml"), 1000);
            printPerformance(graph.toString(), null, "graph-example-3 loaded", this.stopWatch());

            Set<String> vertexIds = new HashSet<String>();
            Set<String> vertexKeys = new HashSet<String>();
            Set<String> vertexNames = new HashSet<String>();
            int vertexCount = 0;
            for (Vertex v : graph.getVertices()) {
                vertexCount++;
                vertexIds.add(v.getId().toString());
                vertexNames.add(v.getProperty("name").toString());
                for (String key : v.getPropertyKeys()) {
                    vertexKeys.add(key);
                }
            }

            Set<String> edgeIds = new HashSet<String>();
            Set<String> edgeKeys = new HashSet<String>();
            Set<String> edgeLabels = new HashSet<String>();
            int edgeCount = 0;
            for (Edge e : graph.getEdges()) {
                edgeCount++;
                edgeIds.add(e.getId().toString());
                edgeLabels.add(e.getLabel());
                for (String key : e.getPropertyKeys()) {
                    edgeKeys.add(key);
                }
            }

            assertEquals(vertexCount, 6);
            assertEquals(vertexIds.size(), 6);
            assertEquals(vertexKeys.contains("name"), true);
            assertEquals(vertexKeys.contains("age"), true);
            assertEquals(vertexKeys.contains("lang"), true);
            assertEquals(vertexKeys.contains("id2"), false);
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
            assertEquals(edgeKeys.contains("id2"), false);
            assertEquals(edgeKeys.contains("label2"), true);
            assertEquals(edgeKeys.size(), 2);
            assertEquals(edgeLabels.size(), 2);
            assertEquals(edgeLabels.contains("has high fived"), false);
            assertEquals(edgeLabels.contains("knows"), true);
            assertEquals(edgeLabels.contains("created"), true);
        }
        graph.shutdown();
    }

    public void testReadingTinkerGraphExample3MappingAll() throws Exception {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeIteration && graph.getFeatures().supportsVertexIteration) {
            this.stopWatch();
            GMLReader r = new GMLReader(graph);
            r.setVertexIdKey("id2");
            r.setEdgeIdKey("id2");
            r.setEdgeLabelKey("label2");
            r.inputGraph(GMLReader.class.getResourceAsStream("graph-example-3.gml"), 1000);
            printPerformance(graph.toString(), null, "graph-example-3 loaded", this.stopWatch());

            Set<String> vertexIds = new HashSet<String>();
            Set<String> vertexKeys = new HashSet<String>();
            Set<String> vertexNames = new HashSet<String>();
            int vertexCount = 0;
            for (Vertex v : graph.getVertices()) {
                vertexCount++;
                vertexIds.add(v.getId().toString());
                vertexNames.add(v.getProperty("name").toString());
                for (String key : v.getPropertyKeys()) {
                    vertexKeys.add(key);
                }
            }

            Set<String> edgeIds = new HashSet<String>();
            Set<String> edgeKeys = new HashSet<String>();
            Set<String> edgeLabels = new HashSet<String>();
            int edgeCount = 0;
            for (Edge e : graph.getEdges()) {
                edgeCount++;
                edgeIds.add(e.getId().toString());
                edgeLabels.add(e.getLabel());
                for (String key : e.getPropertyKeys()) {
                    edgeKeys.add(key);
                }
            }

            assertEquals(vertexCount, 6);
            assertEquals(vertexIds.size(), 6);
            assertEquals(vertexKeys.contains("name"), true);
            assertEquals(vertexKeys.contains("age"), true);
            assertEquals(vertexKeys.contains("lang"), true);
            assertEquals(vertexKeys.contains("id2"), false);
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
            assertEquals(edgeKeys.contains("id2"), false);
            assertEquals(edgeKeys.contains("label2"), false);
            assertEquals(edgeKeys.size(), 1);
            assertEquals(edgeLabels.size(), 2);
            assertEquals(edgeLabels.contains("has high fived"), true);
            assertEquals(edgeLabels.contains("knows"), false);
            assertEquals(edgeLabels.contains("created"), true);
        }
        graph.shutdown();
    }

    public void testMigratingTinkerGraphExample3() throws Exception {
        Graph graph = graphTest.generateGraph();
        if (!graph.getFeatures().ignoresSuppliedIds && graph.getFeatures().supportsEdgeIteration && graph.getFeatures().supportsVertexIteration) {

            this.stopWatch();
            GMLReader gmlReader = new GMLReader(graph);
            gmlReader.setEdgeIdKey("id");
            gmlReader.inputGraph(GMLReader.class.getResourceAsStream("graph-example-3.gml"), 1000);
            printPerformance(graph.toString(), null, "graph-example-3 loaded", this.stopWatch());

            this.stopWatch();
            // FIXME Should not explicitly define the Graph type (TinkerGraph)
            // here. Need to accept 2 graphs as input params?
            Graph toGraph = new TinkerGraph();
            GraphMigrator.migrateGraph(graph, toGraph);
            printPerformance(toGraph.toString(), null, "graph-example-3 migrated", this.stopWatch());

            // Specific Graph Characteristics

            assertEquals(count(toGraph.getVertex(1).getEdges(Direction.OUT)), 3);
            assertEquals(count(toGraph.getVertex(1).getEdges(Direction.IN)), 0);
            Vertex marko = toGraph.getVertex(1);
            assertEquals(marko.getProperty("name"), "marko");
            assertEquals(marko.getProperty("age"), 29);
            assertEquals(marko.getProperty("id2"), 2);
            int counter = 0;
            for (Edge e : toGraph.getVertex(1).getEdges(Direction.OUT)) {
                if (e.getVertex(Direction.IN).getId().equals("2")) {
                    // assertEquals(e.getProperty("weight"), 0.5);
                    assertEquals(e.getProperty("id2"), 8);
                    assertEquals(e.getProperty("label2"), "has high fived");
                    assertEquals(e.getLabel(), "knows");
                    assertEquals(e.getId(), "7");
                    counter++;
                } else if (e.getVertex(Direction.IN).getId().equals("3")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                    assertEquals(e.getProperty("id2"), 10);
                    assertEquals(e.getProperty("label2"), "has high fived");
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "9");
                    counter++;
                } else if (e.getVertex(Direction.IN).getId().equals("4")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 1);
                    assertEquals(e.getProperty("id2"), 9);
                    assertEquals(e.getProperty("label2"), "has high fived");
                    assertEquals(e.getLabel(), "knows");
                    assertEquals(e.getId(), "8");
                    counter++;
                }
            }

            assertEquals(count(toGraph.getVertex(2).getEdges(Direction.OUT)), 0);
            assertEquals(count(toGraph.getVertex(2).getEdges(Direction.IN)), 1);
            Vertex vadas = toGraph.getVertex(2);
            assertEquals(vadas.getProperty("name"), "vadas");
            assertEquals(vadas.getProperty("age"), 27);
            assertEquals(vadas.getProperty("id2"), 3);

            assertEquals(count(toGraph.getVertex(3).getEdges(Direction.OUT)), 0);
            assertEquals(count(toGraph.getVertex(3).getEdges(Direction.IN)), 3);
            Vertex lop = toGraph.getVertex(3);
            assertEquals(lop.getProperty("name"), "lop");
            assertEquals(lop.getProperty("lang"), "java");
            assertEquals(lop.getProperty("id2"), 4);

            assertEquals(count(toGraph.getVertex(4).getEdges(Direction.OUT)), 2);
            assertEquals(count(toGraph.getVertex(4).getEdges(Direction.IN)), 1);
            Vertex josh = toGraph.getVertex(4);
            assertEquals(josh.getProperty("name"), "josh");
            assertEquals(josh.getProperty("age"), 32);
            for (Edge e : toGraph.getVertex(4).getEdges(Direction.OUT)) {
                if (e.getVertex(Direction.IN).getId().equals("3")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                    assertEquals(e.getProperty("id2"), 13);
                    assertEquals(e.getProperty("label2"), null);
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "11");
                    counter++;
                } else if (e.getVertex(Direction.IN).getId().equals("5")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 1);
                    assertEquals(e.getProperty("id2"), 11);
                    assertEquals(e.getProperty("label2"), "has high fived");
                    assertEquals(e.getLabel(), "created");
                    assertEquals(e.getId(), "10");
                    counter++;
                }
            }

            assertEquals(count(toGraph.getVertex(5).getEdges(Direction.OUT)), 0);
            assertEquals(count(toGraph.getVertex(5).getEdges(Direction.IN)), 1);
            Vertex ripple = toGraph.getVertex(5);
            assertEquals(ripple.getProperty("name"), "ripple");
            assertEquals(ripple.getProperty("lang"), "java");
            assertEquals(ripple.getProperty("id2"), 7);

            assertEquals(count(toGraph.getVertex(6).getEdges(Direction.OUT)), 1);
            assertEquals(count(toGraph.getVertex(6).getEdges(Direction.IN)), 0);
            Vertex peter = toGraph.getVertex(6);
            assertEquals(peter.getProperty("name"), "peter");
            assertEquals(peter.getProperty("age"), 35);

            for (Edge e : toGraph.getVertex(6).getEdges(Direction.OUT)) {
                if (e.getVertex(Direction.IN).getId().equals("3")) {
                    assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                    assertEquals(e.getProperty("id2"), null);
                    assertEquals(e.getProperty("label2"), null);
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
                for (String key : v.getPropertyKeys()) {
                    vertexKeys.add(key);
                }
            }

            Set<String> edgeIds = new HashSet<String>();
            Set<String> edgeKeys = new HashSet<String>();
            int edgeCount = 0;
            for (Edge e : toGraph.getEdges()) {
                edgeCount++;
                edgeIds.add(e.getId().toString());
                for (String key : e.getPropertyKeys()) {
                    edgeKeys.add(key);
                }
            }

            assertEquals(vertexCount, 6);
            assertEquals(vertexIds.size(), 6);
            assertEquals(vertexKeys.contains("name"), true);
            assertEquals(vertexKeys.contains("age"), true);
            assertEquals(vertexKeys.contains("lang"), true);
            assertEquals(vertexKeys.contains("id2"), true);
            assertEquals(vertexKeys.size(), 4);

            assertEquals(edgeCount, 6);
            assertEquals(edgeIds.size(), 6);
            assertEquals(edgeKeys.contains("weight"), true);
            assertEquals(edgeKeys.contains("id2"), true);
            assertEquals(edgeKeys.contains("label2"), true);
            assertEquals(edgeKeys.size(), 3);
        }
        graph.shutdown();
    }

}
