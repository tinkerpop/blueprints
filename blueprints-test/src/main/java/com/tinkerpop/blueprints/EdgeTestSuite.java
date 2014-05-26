package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.StringFactory;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeTestSuite extends TestSuite {

    public EdgeTestSuite() {
    }

    public EdgeTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testEdgeEquality() {
        Graph graph = graphTest.generateGraph();

        Vertex v = graph.addVertex(graphTest.convertId("1"));
        Vertex u = graph.addVertex(graphTest.convertId("2"));
        Edge e = graph.addEdge(null, v, u, graphTest.convertLabel("knows"));
        assertEquals(e.getLabel(), graphTest.convertLabel("knows"));
        assertEquals(e.getVertex(Direction.IN), u);
        assertEquals(e.getVertex(Direction.OUT), v);
        assertEquals(e, v.getEdges(Direction.OUT).iterator().next());
        assertEquals(e, u.getEdges(Direction.IN).iterator().next());
        assertEquals(v.getEdges(Direction.OUT).iterator().next(), u.getEdges(Direction.IN).iterator().next());
        Set<Edge> set = new HashSet<Edge>();
        set.add(e);
        set.add(e);
        set.add(v.getEdges(Direction.OUT).iterator().next());
        set.add(v.getEdges(Direction.OUT).iterator().next());
        set.add(u.getEdges(Direction.IN).iterator().next());
        set.add(u.getEdges(Direction.IN).iterator().next());
        if (graph.getFeatures().supportsEdgeIteration)
            set.add(graph.getEdges().iterator().next());
        assertEquals(set.size(), 1);
        graph.shutdown();
    }


    public void testAddEdges() {
        Graph graph = graphTest.generateGraph();

        this.stopWatch();
        Vertex v1 = graph.addVertex(graphTest.convertId("1"));
        Vertex v2 = graph.addVertex(graphTest.convertId("2"));
        Vertex v3 = graph.addVertex(graphTest.convertId("3"));
        graph.addEdge(null, v1, v2, graphTest.convertLabel("knows"));
        graph.addEdge(null, v2, v3, graphTest.convertLabel("pets"));
        graph.addEdge(null, v2, v3, graphTest.convertLabel("caresFor"));
        assertEquals(1, count(v1.getEdges(Direction.OUT)));
        assertEquals(2, count(v2.getEdges(Direction.OUT)));
        assertEquals(0, count(v3.getEdges(Direction.OUT)));
        assertEquals(0, count(v1.getEdges(Direction.IN)));
        assertEquals(1, count(v2.getEdges(Direction.IN)));
        assertEquals(2, count(v3.getEdges(Direction.IN)));
        printPerformance(graph.toString(), 6, "elements added and checked", this.stopWatch());
        graph.shutdown();
    }

    public void testAddManyEdges() {
        Graph graph = graphTest.generateGraph();
        int edgeCount = 100;
        int vertexCount = 200;
        long counter = 0l;
        this.stopWatch();
        for (int i = 0; i < edgeCount; i++) {
            Vertex out = graph.addVertex(graphTest.convertId("" + counter++));
            Vertex in = graph.addVertex(graphTest.convertId("" + counter++));
            graph.addEdge(null, out, in, graphTest.convertLabel(UUID.randomUUID().toString()));
        }
        printPerformance(graph.toString(), vertexCount + edgeCount, "elements added", this.stopWatch());
        if (graph.getFeatures().supportsEdgeIteration) {
            this.stopWatch();
            assertEquals(edgeCount, count(graph.getEdges()));
            printPerformance(graph.toString(), edgeCount, "edges counted", this.stopWatch());
        }
        if (graph.getFeatures().supportsVertexIteration) {
            this.stopWatch();
            assertEquals(vertexCount, count(graph.getVertices()));
            printPerformance(graph.toString(), vertexCount, "vertices counted", this.stopWatch());
            this.stopWatch();
            for (Vertex vertex : graph.getVertices()) {
                if (count(vertex.getEdges(Direction.OUT)) > 0) {
                    assertEquals(1, count(vertex.getEdges(Direction.OUT)));
                    assertFalse(count(vertex.getEdges(Direction.IN)) > 0);

                } else {
                    assertEquals(1, count(vertex.getEdges(Direction.IN)));
                    assertFalse(count(vertex.getEdges(Direction.OUT)) > 0);
                }
            }
            printPerformance(graph.toString(), vertexCount, "vertices checked", this.stopWatch());
        }
        graph.shutdown();
    }

    public void testGetEdges() {
        Graph graph = graphTest.generateGraph();
        Vertex v1 = graph.addVertex(null);
        Vertex v2 = graph.addVertex(null);
        Vertex v3 = graph.addVertex(null);

        Edge e1 = graph.addEdge(null, v1, v2, graphTest.convertLabel("test1"));
        Edge e2 = graph.addEdge(null, v2, v3, graphTest.convertLabel("test2"));
        Edge e3 = graph.addEdge(null, v3, v1, graphTest.convertLabel("test3"));

        if (graph.getFeatures().supportsEdgeRetrieval) {
            this.stopWatch();
            assertEquals(graph.getEdge(e1.getId()), e1);
            assertEquals(graph.getEdge(e1.getId()).getVertex(Direction.IN), v2);
            assertEquals(graph.getEdge(e1.getId()).getVertex(Direction.OUT), v1);

            assertEquals(graph.getEdge(e2.getId()), e2);
            assertEquals(graph.getEdge(e2.getId()).getVertex(Direction.IN), v3);
            assertEquals(graph.getEdge(e2.getId()).getVertex(Direction.OUT), v2);

            assertEquals(graph.getEdge(e3.getId()), e3);
            assertEquals(graph.getEdge(e3.getId()).getVertex(Direction.IN), v1);
            assertEquals(graph.getEdge(e3.getId()).getVertex(Direction.OUT), v3);

            printPerformance(graph.toString(), 3, "edges retrieved", this.stopWatch());
        }

        assertEquals(getOnlyElement(v1.getEdges(Direction.OUT)), e1);
        assertEquals(getOnlyElement(v1.getEdges(Direction.OUT)).getVertex(Direction.IN), v2);
        assertEquals(getOnlyElement(v1.getEdges(Direction.OUT)).getVertex(Direction.OUT), v1);

        assertEquals(getOnlyElement(v2.getEdges(Direction.OUT)), e2);
        assertEquals(getOnlyElement(v2.getEdges(Direction.OUT)).getVertex(Direction.IN), v3);
        assertEquals(getOnlyElement(v2.getEdges(Direction.OUT)).getVertex(Direction.OUT), v2);

        assertEquals(getOnlyElement(v3.getEdges(Direction.OUT)), e3);
        assertEquals(getOnlyElement(v3.getEdges(Direction.OUT)).getVertex(Direction.IN), v1);
        assertEquals(getOnlyElement(v3.getEdges(Direction.OUT)).getVertex(Direction.OUT), v3);

        graph.shutdown();
    }

    public void testGetEdgesByLabel() {
        Graph graph = graphTest.generateGraph();

        if (graph.getFeatures().supportsEdgeIteration) {
            Vertex v1 = graph.addVertex(null);
            Vertex v2 = graph.addVertex(null);
            Vertex v3 = graph.addVertex(null);

            Edge e1 = graph.addEdge(null, v1, v2, graphTest.convertLabel("test1"));
            Edge e2 = graph.addEdge(null, v2, v3, graphTest.convertLabel("test2"));
            Edge e3 = graph.addEdge(null, v3, v1, graphTest.convertLabel("test3"));

            assertEquals(e1, getOnlyElement(graph.query().has("label", graphTest.convertLabel("test1")).edges()));
            assertEquals(e2, getOnlyElement(graph.query().has("label", graphTest.convertLabel("test2")).edges()));
            assertEquals(e3, getOnlyElement(graph.query().has("label", graphTest.convertLabel("test3")).edges()));

            assertEquals(e1, getOnlyElement(graph.getEdges("label", graphTest.convertLabel("test1"))));
            assertEquals(e2, getOnlyElement(graph.getEdges("label", graphTest.convertLabel("test2"))));
            assertEquals(e3, getOnlyElement(graph.getEdges("label", graphTest.convertLabel("test3"))));
        }

        graph.shutdown();
    }

    public void testGetNonExistantEdges() {
        Graph graph = graphTest.generateGraph();

        if (graph.getFeatures().supportsEdgeRetrieval) {
            try {
                graph.getEdge(null);
                fail("Getting an element with a null identifier must throw IllegalArgumentException");
            } catch (IllegalArgumentException iae) {
                assertTrue(true);
            }

            assertNull(graph.getEdge("asbv"));
            assertNull(graph.getEdge(12.0d));
        }

        graph.shutdown();
    }

    public void testRemoveManyEdges() {
        Graph graph = graphTest.generateGraph();
        long counter = 200000l;
        int edgeCount = 10;
        Set<Edge> edges = new HashSet<Edge>();
        for (int i = 0; i < edgeCount; i++) {
            Vertex out = graph.addVertex(graphTest.convertId("" + counter++));
            Vertex in = graph.addVertex(graphTest.convertId("" + counter++));
            edges.add(graph.addEdge(null, out, in, graphTest.convertLabel("a" + UUID.randomUUID().toString())));
        }
        assertEquals(edgeCount, edges.size());

        if (graph.getFeatures().supportsVertexIteration) {
            this.stopWatch();
            assertEquals(edgeCount * 2, count(graph.getVertices()));
            printPerformance(graph.toString(), edgeCount * 2, "vertices counted", this.stopWatch());
        }

        if (graph.getFeatures().supportsEdgeIteration) {
            this.stopWatch();
            assertEquals(edgeCount, count(graph.getEdges()));
            printPerformance(graph.toString(), edgeCount, "edges counted", this.stopWatch());

            Random random = new Random();
            int i = edgeCount;
            this.stopWatch();
            for (Edge edge : edges) {
                if (random.nextBoolean())
                    graph.removeEdge(edge);
                else
                    edge.remove();
                i--;
                assertEquals(i, count(graph.getEdges()));
                if (graph.getFeatures().supportsVertexIteration) {
                    int x = 0;
                    for (Vertex vertex : graph.getVertices()) {
                        if (count(vertex.getEdges(Direction.OUT)) > 0) {
                            assertEquals(1, count(vertex.getEdges(Direction.OUT)));
                            assertFalse(count(vertex.getEdges(Direction.IN)) > 0);
                        } else if (count(vertex.getEdges(Direction.IN)) > 0) {
                            assertEquals(1, count(vertex.getEdges(Direction.IN)));
                            assertFalse(count(vertex.getEdges(Direction.OUT)) > 0);
                        } else {
                            x++;
                        }
                    }
                    assertEquals((edgeCount - i) * 2, x);
                }
            }
            printPerformance(graph.toString(), edgeCount, "edges removed and graph checked", this.stopWatch());
        }
        graph.shutdown();
    }

    public void testAddingDuplicateEdges() {
        Graph graph = graphTest.generateGraph();

        Vertex v1 = graph.addVertex(graphTest.convertId("1"));
        Vertex v2 = graph.addVertex(graphTest.convertId("2"));
        Vertex v3 = graph.addVertex(graphTest.convertId("3"));
        graph.addEdge(null, v1, v2, graphTest.convertLabel("knows"));
        graph.addEdge(null, v2, v3, graphTest.convertLabel("pets"));
        graph.addEdge(null, v2, v3, graphTest.convertLabel("pets"));
        graph.addEdge(null, v2, v3, graphTest.convertLabel("pets"));
        graph.addEdge(null, v2, v3, graphTest.convertLabel("pets"));

        if (graph.getFeatures().supportsDuplicateEdges) {
            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(3, count(graph.getVertices()));
            if (graph.getFeatures().supportsEdgeIteration)
                assertEquals(5, count(graph.getEdges()));

            assertEquals(0, count(v1.getEdges(Direction.IN)));
            assertEquals(1, count(v1.getEdges(Direction.OUT)));
            assertEquals(1, count(v2.getEdges(Direction.IN)));
            assertEquals(4, count(v2.getEdges(Direction.OUT)));
            assertEquals(4, count(v3.getEdges(Direction.IN)));
            assertEquals(0, count(v3.getEdges(Direction.OUT)));
        } else {
            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 3);
            if (graph.getFeatures().supportsEdgeIteration)
                assertEquals(count(graph.getEdges()), 2);

            assertEquals(0, count(v1.getEdges(Direction.IN)));
            assertEquals(1, count(v1.getEdges(Direction.OUT)));
            assertEquals(1, count(v2.getEdges(Direction.IN)));
            assertEquals(1, count(v2.getEdges(Direction.OUT)));
            assertEquals(1, count(v3.getEdges(Direction.IN)));
            assertEquals(0, count(v3.getEdges(Direction.OUT)));
        }
        graph.shutdown();
    }

    public void testRemoveEdgesByRemovingVertex() {
        Graph graph = graphTest.generateGraph();

        Vertex v1 = graph.addVertex(graphTest.convertId("1"));
        Vertex v2 = graph.addVertex(graphTest.convertId("2"));
        Vertex v3 = graph.addVertex(graphTest.convertId("3"));
        graph.addEdge(null, v1, v2, graphTest.convertLabel("knows"));
        graph.addEdge(null, v2, v3, graphTest.convertLabel("pets"));
        graph.addEdge(null, v2, v3, graphTest.convertLabel("pets"));

        assertEquals(0, count(v1.getEdges(Direction.IN)));
        assertEquals(1, count(v1.getEdges(Direction.OUT)));
        assertEquals(1, count(v2.getEdges(Direction.IN)));
        assertEquals(0, count(v3.getEdges(Direction.OUT)));

        if (!graph.getFeatures().ignoresSuppliedIds) {
            v1 = graph.getVertex(graphTest.convertId("1"));
            v2 = graph.getVertex(graphTest.convertId("2"));
            v3 = graph.getVertex(graphTest.convertId("3"));

            assertEquals(0, count(v1.getEdges(Direction.IN)));
            assertEquals(1, count(v1.getEdges(Direction.OUT)));
            assertEquals(1, count(v2.getEdges(Direction.IN)));
            assertEquals(0, count(v3.getEdges(Direction.OUT)));
        }

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(3, count(graph.getVertices()));

        graph.removeVertex(v1);

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(2, count(graph.getVertices()));

        if (graph.getFeatures().supportsDuplicateEdges)
            assertEquals(2, count(v2.getEdges(Direction.OUT)));
        else
            assertEquals(1, count(v2.getEdges(Direction.OUT)));

        assertEquals(0, count(v3.getEdges(Direction.OUT)));
        assertEquals(0, count(v2.getEdges(Direction.IN)));

        if (graph.getFeatures().supportsDuplicateEdges)
            assertEquals(2, count(v3.getEdges(Direction.IN)));
        else
            assertEquals(1, count(v3.getEdges(Direction.IN)));

        graph.shutdown();
    }

    public void testRemoveEdges() {
        Graph graph = graphTest.generateGraph();
        Vertex v1 = graph.addVertex(graphTest.convertId("1"));
        Vertex v2 = graph.addVertex(graphTest.convertId("2"));
        Vertex v3 = graph.addVertex(graphTest.convertId("3"));
        Edge e1 = graph.addEdge(null, v1, v2, graphTest.convertLabel("knows"));
        Edge e2 = graph.addEdge(null, v2, v3, graphTest.convertLabel("pets"));
        Edge e3 = graph.addEdge(null, v2, v3, graphTest.convertLabel("cares_for"));

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(3, count(graph.getVertices()));

        graph.removeEdge(e1);
        assertEquals(0, count(v1.getEdges(Direction.OUT)));
        assertEquals(2, count(v2.getEdges(Direction.OUT)));
        assertEquals(0, count(v3.getEdges(Direction.OUT)));
        assertEquals(0, count(v1.getEdges(Direction.IN)));
        assertEquals(0, count(v2.getEdges(Direction.IN)));
        assertEquals(2, count(v3.getEdges(Direction.IN)));
        if (!graph.getFeatures().ignoresSuppliedIds) {
            v1 = graph.getVertex(graphTest.convertId("1"));
            v2 = graph.getVertex(graphTest.convertId("2"));
            v3 = graph.getVertex(graphTest.convertId("3"));
        }
        assertEquals(0, count(v1.getEdges(Direction.OUT)));
        assertEquals(2, count(v2.getEdges(Direction.OUT)));
        assertEquals(0, count(v3.getEdges(Direction.OUT)));
        assertEquals(0, count(v1.getEdges(Direction.IN)));
        assertEquals(0, count(v2.getEdges(Direction.IN)));
        assertEquals(2, count(v3.getEdges(Direction.IN)));

        e2.remove();
        assertEquals(0, count(v1.getEdges(Direction.OUT)));
        assertEquals(1, count(v2.getEdges(Direction.OUT)));
        assertEquals(0, count(v3.getEdges(Direction.OUT)));
        assertEquals(0, count(v1.getEdges(Direction.IN)));
        assertEquals(0, count(v2.getEdges(Direction.IN)));
        assertEquals(1, count(v3.getEdges(Direction.IN)));
        if (!graph.getFeatures().ignoresSuppliedIds) {
            v1 = graph.getVertex(graphTest.convertId("1"));
            v2 = graph.getVertex(graphTest.convertId("2"));
            v3 = graph.getVertex(graphTest.convertId("3"));
        }
        assertEquals(0, count(v1.getEdges(Direction.OUT)));
        assertEquals(1, count(v2.getEdges(Direction.OUT)));
        assertEquals(0, count(v3.getEdges(Direction.OUT)));
        assertEquals(0, count(v1.getEdges(Direction.IN)));
        assertEquals(0, count(v2.getEdges(Direction.IN)));
        assertEquals(1, count(v3.getEdges(Direction.IN)));

        graph.removeEdge(e3);
        assertEquals(0, count(v1.getEdges(Direction.OUT)));
        assertEquals(0, count(v2.getEdges(Direction.OUT)));
        assertEquals(0, count(v3.getEdges(Direction.OUT)));
        assertEquals(0, count(v1.getEdges(Direction.IN)));
        assertEquals(0, count(v2.getEdges(Direction.IN)));
        assertEquals(0, count(v3.getEdges(Direction.IN)));
        if (!graph.getFeatures().ignoresSuppliedIds) {
            v1 = graph.getVertex(graphTest.convertId("1"));
            v2 = graph.getVertex(graphTest.convertId("2"));
            v3 = graph.getVertex(graphTest.convertId("3"));
        }
        assertEquals(0, count(v1.getEdges(Direction.OUT)));
        assertEquals(0, count(v2.getEdges(Direction.OUT)));
        assertEquals(0, count(v3.getEdges(Direction.OUT)));
        assertEquals(0, count(v1.getEdges(Direction.IN)));
        assertEquals(0, count(v2.getEdges(Direction.IN)));
        assertEquals(0, count(v3.getEdges(Direction.IN)));
        graph.shutdown();

    }

    public void testAddingSelfLoops() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsSelfLoops) {
            Vertex v1 = graph.addVertex(graphTest.convertId("1"));
            Vertex v2 = graph.addVertex(graphTest.convertId("2"));
            Vertex v3 = graph.addVertex(graphTest.convertId("3"));
            graph.addEdge(null, v1, v1, graphTest.convertLabel("is_self"));
            graph.addEdge(null, v2, v2, graphTest.convertLabel("is_self"));
            graph.addEdge(null, v3, v3, graphTest.convertLabel("is_self"));

            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(3, count(graph.getVertices()));
            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(3, count(graph.getEdges()));
                int counter = 0;
                for (Edge edge : graph.getEdges()) {
                    counter++;
                    assertEquals(edge.getVertex(Direction.IN), edge.getVertex(Direction.OUT));
                    assertEquals(edge.getVertex(Direction.IN).getId(), edge.getVertex(Direction.OUT).getId());
                }
                assertEquals(counter, 3);
            }
        }
        graph.shutdown();
    }

    public void testRemoveSelfLoops() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsSelfLoops) {
            Vertex v1 = graph.addVertex(graphTest.convertId("1"));
            Vertex v2 = graph.addVertex(graphTest.convertId("2"));
            Vertex v3 = graph.addVertex(graphTest.convertId("3"));
            Edge e1 = graph.addEdge(null, v1, v1, graphTest.convertLabel("is_self"));
            Edge e2 = graph.addEdge(null, v2, v2, graphTest.convertLabel("is_self"));
            Edge e3 = graph.addEdge(null, v3, v3, graphTest.convertLabel("is_self"));

            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(3, count(graph.getVertices()));
            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(3, count(graph.getEdges()));
                for (Edge edge : graph.getEdges()) {
                    assertEquals(edge.getVertex(Direction.IN), edge.getVertex(Direction.OUT));
                    assertEquals(edge.getVertex(Direction.IN).getId(), edge.getVertex(Direction.OUT).getId());
                }
            }

            graph.removeVertex(v1);
            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(2, count(graph.getEdges()));
                for (Edge edge : graph.getEdges()) {
                    assertEquals(edge.getVertex(Direction.IN), edge.getVertex(Direction.OUT));
                    assertEquals(edge.getVertex(Direction.IN).getId(), edge.getVertex(Direction.OUT).getId());
                }
            }

            assertEquals(1, count(v2.getEdges(Direction.OUT)));
            assertEquals(1, count(v2.getEdges(Direction.IN)));
            graph.removeEdge(e2);
            assertEquals(0, count(v2.getEdges(Direction.OUT)));
            assertEquals(0, count(v2.getEdges(Direction.IN)));

            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(count(graph.getEdges()), 1);
                for (Edge edge : graph.getEdges()) {
                    assertEquals(edge.getVertex(Direction.IN), edge.getVertex(Direction.OUT));
                    assertEquals(edge.getVertex(Direction.IN).getId(), edge.getVertex(Direction.OUT).getId());
                }
            }
        }
        graph.shutdown();
    }

    public void testEdgeIterator() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeIteration) {
            Vertex v1 = graph.addVertex(graphTest.convertId("1"));
            Vertex v2 = graph.addVertex(graphTest.convertId("2"));
            Vertex v3 = graph.addVertex(graphTest.convertId("3"));
            Edge e1 = graph.addEdge(null, v1, v2, graphTest.convertLabel("test"));
            Edge e2 = graph.addEdge(null, v2, v3, graphTest.convertLabel("test"));
            Edge e3 = graph.addEdge(null, v3, v1, graphTest.convertLabel("test"));

            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(3, count(graph.getVertices()));
            if (graph.getFeatures().supportsEdgeIteration)
                assertEquals(3, count(graph.getEdges()));

            Set<String> edgeIds = new HashSet<String>();
            int count = 0;
            for (Edge e : graph.getEdges()) {
                count++;
                edgeIds.add(e.getId().toString());
                assertEquals(graphTest.convertLabel("test"), e.getLabel());
                if (e.getId().toString().equals(e1.getId().toString())) {
                    assertEquals(v1, e.getVertex(Direction.OUT));
                    assertEquals(v2, e.getVertex(Direction.IN));
                } else if (e.getId().toString().equals(e2.getId().toString())) {
                    assertEquals(v2, e.getVertex(Direction.OUT));
                    assertEquals(v3, e.getVertex(Direction.IN));
                } else if (e.getId().toString().equals(e3.getId().toString())) {
                    assertEquals(v3, e.getVertex(Direction.OUT));
                    assertEquals(v1, e.getVertex(Direction.IN));
                } else {
                    assertTrue(false);
                }
                //System.out.println(e);
            }
            assertEquals(3, count);
            assertEquals(3, edgeIds.size());
            assertTrue(edgeIds.contains(e1.getId().toString()));
            assertTrue(edgeIds.contains(e2.getId().toString()));
            assertTrue(edgeIds.contains(e3.getId().toString()));
        }
        graph.shutdown();
    }

    public void testAddingRemovingEdgeProperties() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeProperties) {
            Vertex a = graph.addVertex(graphTest.convertId("1"));
            Vertex b = graph.addVertex(graphTest.convertId("2"));
            Edge edge = graph.addEdge(graphTest.convertId("3"), a, b, graphTest.convertLabel("knows"));
            assertEquals(edge.getPropertyKeys().size(), 0);
            assertNull(edge.getProperty("weight"));

            if (graph.getFeatures().supportsDoubleProperty) {
                edge.setProperty("weight", 0.5);
                assertEquals(edge.getPropertyKeys().size(), 1);
                assertEquals(edge.getProperty("weight"), 0.5);

                edge.setProperty("weight", 0.6);
                assertEquals(edge.getPropertyKeys().size(), 1);
                assertEquals(edge.getProperty("weight"), 0.6);
                assertEquals(edge.removeProperty("weight"), 0.6);
                assertNull(edge.getProperty("weight"));
                assertEquals(edge.getPropertyKeys().size(), 0);
            }

            if (graph.getFeatures().supportsStringProperty) {
                edge.setProperty("blah", "marko");
                edge.setProperty("blah2", "josh");
                assertEquals(edge.getPropertyKeys().size(), 2);
            }
        }
        graph.shutdown();
    }

    public void testAddingLabelAndIdProperty() {
        Graph graph = graphTest.generateGraph();

        // no point in testing graph features for setting string properties because the intent is for it to
        // fail based on the id or label properties.
        if (graph.getFeatures().supportsEdgeProperties) {

            Edge edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), graphTest.convertLabel("knows"));
            try {
                edge.setProperty("id", "123");
                fail("Setting edge property with reserved key 'id' should fail");
            } catch (RuntimeException e) {
            }
            try {
                edge.setProperty("label", "hates");
                fail("Setting edge property with reserved key 'label' should fail");
            } catch (RuntimeException e) {
            }

        }
        graph.shutdown();
    }

    public void testNoConcurrentModificationException() {
        Graph graph = graphTest.generateGraph();
        for (int i = 0; i < 25; i++) {
            graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), graphTest.convertLabel("test"));
        }
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(BaseTest.count(graph.getVertices()), 50);
        if (graph.getFeatures().supportsEdgeIteration) {
            assertEquals(BaseTest.count(graph.getEdges()), 25);
            for (final Edge edge : graph.getEdges()) {
                graph.removeEdge(edge);
            }
            assertEquals(BaseTest.count(graph.getEdges()), 0);
        }
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(BaseTest.count(graph.getVertices()), 50);
        graph.shutdown();
    }

    public void testEmptyKeyProperty() {
        final Graph graph = graphTest.generateGraph();

        // no point in testing graph features for setting string properties because the intent is for it to
        // fail based on the empty key.
        if (graph.getFeatures().supportsEdgeProperties) {
            final Edge e = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), graphTest.convertLabel("friend"));
            try {
                e.setProperty("", "value");
                fail("Setting an edge property with an empty string key should fail");
            } catch (IllegalArgumentException e1) {
            }
        }
        graph.shutdown();
    }

    public void testEdgeCentricRemoving() {
        final Graph graph = graphTest.generateGraph();

        final Edge a = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), graphTest.convertLabel("knows"));
        final Edge b = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), graphTest.convertLabel("knows"));
        final Edge c = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), graphTest.convertLabel("knows"));

        Object cId = c.getId();

        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 3);

        a.remove();
        b.remove();

        if (graph.getFeatures().supportsEdgeRetrieval)
            assertNotNull(graph.getEdge(cId));

        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 1);

        graph.shutdown();

    }

    public void testSettingBadVertexProperties() {
        final Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties) {
            Edge edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), graphTest.convertLabel("knows"));
            try {
                edge.setProperty(null, -1);
                fail("Setting property with a null key should throw an error");
            } catch (RuntimeException e) {
            }
            try {
                edge.setProperty("", -1);
                fail("Setting edge property with a empty key should throw an error");
            } catch (RuntimeException e) {
            }
            try {
                edge.setProperty(StringFactory.ID, -1);
                fail("Setting edge property with a key of '" + StringFactory.ID +
                        "' should throw an error");
            } catch (RuntimeException e) {
            }
            try {
                edge.setProperty(StringFactory.LABEL, "friend");
                fail("Setting edge property with a key of '" + StringFactory.LABEL +
                        "' should throw an error");
            } catch (RuntimeException e) {
            }
            try {
                edge.setProperty("good", null);
                fail("Setting edge property with a null value should throw an error");
            } catch (RuntimeException e) {
            }
        }
        graph.shutdown();
    }

    public void testSetEdgeLabelNullShouldThrowIllegalArgumentException() {
        Graph graph = graphTest.generateGraph();
        Vertex v1 = graph.addVertex(null);
        Vertex v2 = graph.addVertex(null);

        try {
            v2.addEdge(null, v1);
            fail("Adding edge to vertex with a null label should throw an error");
        } catch (IllegalArgumentException iae) {
        }

        try {
            graph.addEdge(null, v1, v2, null);
            fail("Adding edge to graph with a null label should throw an error");
        } catch (IllegalArgumentException iae) {
        }
        graph.shutdown();
    }
}
