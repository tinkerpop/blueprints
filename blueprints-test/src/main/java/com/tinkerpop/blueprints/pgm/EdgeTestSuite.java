package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;

import java.util.HashSet;
import java.util.List;
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
        List<String> ids = generateIds(2);

        Vertex v = graph.addVertex(convertId(graph, ids.get(0)));
        Vertex u = graph.addVertex(convertId(graph, ids.get(1)));
        Edge e = graph.addEdge(null, v, u, convertId(graph, "test_label"));
        assertFalse(e.equals(null));
        assertEquals(e, v.getOutEdges().iterator().next());
        assertEquals(e, u.getInEdges().iterator().next());
        assertEquals(v.getOutEdges().iterator().next(), u.getInEdges().iterator().next());
        Set<Edge> set = new HashSet<Edge>();
        set.add(e);
        set.add(e);
        set.add(v.getOutEdges().iterator().next());
        set.add(v.getOutEdges().iterator().next());
        set.add(u.getInEdges().iterator().next());
        set.add(u.getInEdges().iterator().next());
        if (graph.getFeatures().supportsEdgeIteration)
            set.add(graph.getEdges().iterator().next());
        assertEquals(set.size(), 1);
        graph.shutdown();
    }

    public void testGetEdgeWithNull() {
        Graph graph = graphTest.generateGraph();
        if (!graph.getFeatures().isRDFModel) {
            try {
                graph.getEdge(null);
                assertFalse(true);
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        }
        graph.shutdown();
    }

    public void testAddEdges() {
        Graph graph = graphTest.generateGraph();
        List<String> ids = generateIds(3);

        this.stopWatch();
        Vertex v1 = graph.addVertex(convertId(graph, ids.get(0)));
        Vertex v2 = graph.addVertex(convertId(graph, ids.get(1)));
        Vertex v3 = graph.addVertex(convertId(graph, ids.get(2)));
        graph.addEdge(null, v1, v2, convertId(graph, "knows"));
        graph.addEdge(null, v2, v3, convertId(graph, "pets"));
        graph.addEdge(null, v2, v3, convertId(graph, "cares_for"));
        assertEquals(1, count(v1.getOutEdges()));
        assertEquals(2, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(1, count(v2.getInEdges()));
        assertEquals(2, count(v3.getInEdges()));
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
            Vertex out = graph.addVertex(convertId(graph, "" + counter++));
            Vertex in = graph.addVertex(convertId(graph, "" + counter++));
            graph.addEdge(null, out, in, convertId(graph, UUID.randomUUID().toString()));
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
                if (count(vertex.getOutEdges()) > 0) {
                    assertEquals(1, count(vertex.getOutEdges()));
                    assertFalse(count(vertex.getInEdges()) > 0);

                } else {
                    assertEquals(1, count(vertex.getInEdges()));
                    assertFalse(count(vertex.getOutEdges()) > 0);
                }
            }
            printPerformance(graph.toString(), vertexCount, "vertices checked", this.stopWatch());
        }
        graph.shutdown();
    }

    public void testGetEdges() {
        Graph graph = graphTest.generateGraph();
        if (!graph.getFeatures().isRDFModel) {
            Vertex v1 = graph.addVertex(null);
            Vertex v2 = graph.addVertex(null);
            Vertex v3 = graph.addVertex(null);

            Edge e1 = graph.addEdge(null, v1, v2, "test1");
            Edge e2 = graph.addEdge(null, v2, v3, "test2");
            Edge e3 = graph.addEdge(null, v3, v1, "test3");

            this.stopWatch();
            assertEquals(graph.getEdge(e1.getId()), e1);
            assertEquals(graph.getEdge(e1.getId()).getInVertex(), v2);
            assertEquals(graph.getEdge(e1.getId()).getOutVertex(), v1);

            assertEquals(graph.getEdge(e2.getId()), e2);
            assertEquals(graph.getEdge(e2.getId()).getInVertex(), v3);
            assertEquals(graph.getEdge(e2.getId()).getOutVertex(), v2);

            assertEquals(graph.getEdge(e3.getId()), e3);
            assertEquals(graph.getEdge(e3.getId()).getInVertex(), v1);
            assertEquals(graph.getEdge(e3.getId()).getOutVertex(), v3);

            printPerformance(graph.toString(), 3, "edges retrieved", this.stopWatch());
        }
        graph.shutdown();
    }

    public void testGetNonExistantEdges() {
        Graph graph = graphTest.generateGraph();

        if (!graph.getFeatures().isRDFModel) {
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
            Vertex out = graph.addVertex(convertId(graph, "" + counter++));
            Vertex in = graph.addVertex(convertId(graph, "" + counter++));
            edges.add(graph.addEdge(null, out, in, convertId(graph, "a" + UUID.randomUUID().toString())));
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

            int i = edgeCount;
            this.stopWatch();
            for (Edge edge : edges) {
                graph.removeEdge(edge);
                i--;
                assertEquals(i, count(graph.getEdges()));
                if (graph.getFeatures().supportsVertexIteration) {
                    int x = 0;
                    for (Vertex vertex : graph.getVertices()) {
                        if (count(vertex.getOutEdges()) > 0) {
                            assertEquals(1, count(vertex.getOutEdges()));
                            assertFalse(count(vertex.getInEdges()) > 0);
                        } else if (count(vertex.getInEdges()) > 0) {
                            assertEquals(1, count(vertex.getInEdges()));
                            assertFalse(count(vertex.getOutEdges()) > 0);
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
        List<String> ids = generateIds(3);

        Vertex v1 = graph.addVertex(convertId(graph, ids.get(0)));
        Vertex v2 = graph.addVertex(convertId(graph, ids.get(1)));
        Vertex v3 = graph.addVertex(convertId(graph, ids.get(2)));
        graph.addEdge(null, v1, v2, convertId(graph, "knows"));
        graph.addEdge(null, v2, v3, convertId(graph, "pets"));
        graph.addEdge(null, v2, v3, convertId(graph, "pets"));
        graph.addEdge(null, v2, v3, convertId(graph, "pets"));
        graph.addEdge(null, v2, v3, convertId(graph, "pets"));

        if (graph.getFeatures().allowDuplicateEdges) {
            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(3, count(graph.getVertices()));
            if (graph.getFeatures().supportsEdgeIteration)
                assertEquals(5, count(graph.getEdges()));

            assertEquals(0, count(v1.getInEdges()));
            assertEquals(1, count(v1.getOutEdges()));
            assertEquals(1, count(v2.getInEdges()));
            assertEquals(4, count(v2.getOutEdges()));
            assertEquals(4, count(v3.getInEdges()));
            assertEquals(0, count(v3.getOutEdges()));
        } else {
            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 3);
            if (graph.getFeatures().supportsEdgeIteration)
                assertEquals(count(graph.getEdges()), 2);

            assertEquals(0, count(v1.getInEdges()));
            assertEquals(1, count(v1.getOutEdges()));
            assertEquals(1, count(v2.getInEdges()));
            assertEquals(1, count(v2.getOutEdges()));
            assertEquals(1, count(v3.getInEdges()));
            assertEquals(0, count(v3.getOutEdges()));
        }
        graph.shutdown();
    }

    public void testRemoveEdgesByRemovingVertex() {
        Graph graph = graphTest.generateGraph();
        List<String> ids = generateIds(3);

        Vertex v1 = graph.addVertex(convertId(graph, ids.get(0)));
        Vertex v2 = graph.addVertex(convertId(graph, ids.get(1)));
        Vertex v3 = graph.addVertex(convertId(graph, ids.get(2)));
        graph.addEdge(null, v1, v2, convertId(graph, "knows"));
        graph.addEdge(null, v2, v3, convertId(graph, "pets"));
        graph.addEdge(null, v2, v3, convertId(graph, "pets"));

        assertEquals(0, count(v1.getInEdges()));
        assertEquals(1, count(v1.getOutEdges()));
        assertEquals(1, count(v2.getInEdges()));
        assertEquals(0, count(v3.getOutEdges()));

        if (!graph.getFeatures().ignoresSuppliedIds) {
            v1 = graph.getVertex(convertId(graph, ids.get(0)));
            v2 = graph.getVertex(convertId(graph, ids.get(1)));
            v3 = graph.getVertex(convertId(graph, ids.get(2)));

            assertEquals(0, count(v1.getInEdges()));
            assertEquals(1, count(v1.getOutEdges()));
            assertEquals(1, count(v2.getInEdges()));
            assertEquals(0, count(v3.getOutEdges()));
        }

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(3, count(graph.getVertices()));

        graph.removeVertex(v1);

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(2, count(graph.getVertices()));

        if (graph.getFeatures().allowDuplicateEdges)
            assertEquals(2, count(v2.getOutEdges()));
        else
            assertEquals(1, count(v2.getOutEdges()));

        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v2.getInEdges()));

        if (graph.getFeatures().allowDuplicateEdges)
            assertEquals(2, count(v3.getInEdges()));
        else
            assertEquals(1, count(v3.getInEdges()));

        graph.shutdown();
    }

    public void testRemoveEdges() {
        Graph graph = graphTest.generateGraph();
        List<String> ids = generateIds(3);
        Vertex v1 = graph.addVertex(convertId(graph, ids.get(0)));
        Vertex v2 = graph.addVertex(convertId(graph, ids.get(1)));
        Vertex v3 = graph.addVertex(convertId(graph, ids.get(2)));
        Edge e1 = graph.addEdge(null, v1, v2, convertId(graph, "knows"));
        Edge e2 = graph.addEdge(null, v2, v3, convertId(graph, "pets"));
        Edge e3 = graph.addEdge(null, v2, v3, convertId(graph, "cares_for"));

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(3, count(graph.getVertices()));

        graph.removeEdge(e1);
        assertEquals(0, count(v1.getOutEdges()));
        assertEquals(2, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(0, count(v2.getInEdges()));
        assertEquals(2, count(v3.getInEdges()));
        if (!graph.getFeatures().ignoresSuppliedIds) {
            v1 = graph.getVertex(convertId(graph, ids.get(0)));
            v2 = graph.getVertex(convertId(graph, ids.get(1)));
            v3 = graph.getVertex(convertId(graph, ids.get(2)));
        }
        assertEquals(0, count(v1.getOutEdges()));
        assertEquals(2, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(0, count(v2.getInEdges()));
        assertEquals(2, count(v3.getInEdges()));

        graph.removeEdge(e2);
        assertEquals(0, count(v1.getOutEdges()));
        assertEquals(1, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(0, count(v2.getInEdges()));
        assertEquals(1, count(v3.getInEdges()));
        if (!graph.getFeatures().ignoresSuppliedIds) {
            v1 = graph.getVertex(convertId(graph, ids.get(0)));
            v2 = graph.getVertex(convertId(graph, ids.get(1)));
            v3 = graph.getVertex(convertId(graph, ids.get(2)));
        }
        assertEquals(0, count(v1.getOutEdges()));
        assertEquals(1, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(0, count(v2.getInEdges()));
        assertEquals(1, count(v3.getInEdges()));

        graph.removeEdge(e3);
        assertEquals(0, count(v1.getOutEdges()));
        assertEquals(0, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(0, count(v2.getInEdges()));
        assertEquals(0, count(v3.getInEdges()));
        if (!graph.getFeatures().ignoresSuppliedIds) {
            v1 = graph.getVertex(convertId(graph, ids.get(0)));
            v2 = graph.getVertex(convertId(graph, ids.get(1)));
            v3 = graph.getVertex(convertId(graph, ids.get(2)));
        }
        assertEquals(0, count(v1.getOutEdges()));
        assertEquals(0, count(v2.getOutEdges()));
        assertEquals(0, count(v3.getOutEdges()));
        assertEquals(0, count(v1.getInEdges()));
        assertEquals(0, count(v2.getInEdges()));
        assertEquals(0, count(v3.getInEdges()));
        graph.shutdown();

    }

    public void testAddingSelfLoops() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().allowSelfLoops) {
            List<String> ids = generateIds(3);
            Vertex v1 = graph.addVertex(convertId(graph, ids.get(0)));
            Vertex v2 = graph.addVertex(convertId(graph, ids.get(1)));
            Vertex v3 = graph.addVertex(convertId(graph, ids.get(2)));
            graph.addEdge(null, v1, v1, convertId(graph, "is_self"));
            graph.addEdge(null, v2, v2, convertId(graph, "is_self"));
            graph.addEdge(null, v3, v3, convertId(graph, "is_self"));

            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(3, count(graph.getVertices()));
            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(3, count(graph.getEdges()));
                int counter = 0;
                for (Edge edge : graph.getEdges()) {
                    counter++;
                    assertEquals(edge.getInVertex(), edge.getOutVertex());
                    assertEquals(edge.getInVertex().getId(), edge.getOutVertex().getId());
                }
                assertEquals(counter, 3);
            }
        }
        graph.shutdown();
    }

    public void testRemoveSelfLoops() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().allowSelfLoops) {
            List<String> ids = generateIds(3);
            Vertex v1 = graph.addVertex(convertId(graph, ids.get(0)));
            Vertex v2 = graph.addVertex(convertId(graph, ids.get(1)));
            Vertex v3 = graph.addVertex(convertId(graph, ids.get(2)));
            Edge e1 = graph.addEdge(null, v1, v1, convertId(graph, "is_self"));
            Edge e2 = graph.addEdge(null, v2, v2, convertId(graph, "is_self"));
            Edge e3 = graph.addEdge(null, v3, v3, convertId(graph, "is_self"));

            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(3, count(graph.getVertices()));
            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(3, count(graph.getEdges()));
                for (Edge edge : graph.getEdges()) {
                    assertEquals(edge.getInVertex(), edge.getOutVertex());
                    assertEquals(edge.getInVertex().getId(), edge.getOutVertex().getId());
                }
            }

            graph.removeVertex(v1);
            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(2, count(graph.getEdges()));
                for (Edge edge : graph.getEdges()) {
                    assertEquals(edge.getInVertex(), edge.getOutVertex());
                    assertEquals(edge.getInVertex().getId(), edge.getOutVertex().getId());
                }
            }

            assertEquals(1, count(v2.getOutEdges()));
            assertEquals(1, count(v2.getInEdges()));
            graph.removeEdge(e2);
            assertEquals(0, count(v2.getOutEdges()));
            assertEquals(0, count(v2.getInEdges()));

            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(count(graph.getEdges()), 1);
                for (Edge edge : graph.getEdges()) {
                    assertEquals(edge.getInVertex(), edge.getOutVertex());
                    assertEquals(edge.getInVertex().getId(), edge.getOutVertex().getId());
                }
            }
        }
        graph.shutdown();
    }

    public void testEdgeIterator() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeIteration) {
            List<String> ids = generateIds(3);
            Vertex v1 = graph.addVertex(convertId(graph, ids.get(0)));
            Vertex v2 = graph.addVertex(convertId(graph, ids.get(1)));
            Vertex v3 = graph.addVertex(convertId(graph, ids.get(2)));
            Edge e1 = graph.addEdge(null, v1, v2, convertId(graph, "test"));
            Edge e2 = graph.addEdge(null, v2, v3, convertId(graph, "test"));
            Edge e3 = graph.addEdge(null, v3, v1, convertId(graph, "test"));

            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(3, count(graph.getVertices()));
            if (graph.getFeatures().supportsEdgeIteration)
                assertEquals(3, count(graph.getEdges()));

            Set<String> edgeIds = new HashSet<String>();
            int count = 0;
            for (Edge e : graph.getEdges()) {
                count++;
                edgeIds.add(e.getId().toString());
                assertEquals(convertId(graph, "test"), e.getLabel());
                if (e.getId().toString().equals(e1.getId().toString())) {
                    assertEquals(v1, e.getOutVertex());
                    assertEquals(v2, e.getInVertex());
                } else if (e.getId().toString().equals(e2.getId().toString())) {
                    assertEquals(v2, e.getOutVertex());
                    assertEquals(v3, e.getInVertex());
                } else if (e.getId().toString().equals(e3.getId().toString())) {
                    assertEquals(v3, e.getOutVertex());
                    assertEquals(v1, e.getInVertex());
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
        if (!graph.getFeatures().isRDFModel) {
            Vertex a = graph.addVertex(convertId(graph, "1"));
            Vertex b = graph.addVertex(convertId(graph, "2"));
            Edge edge = graph.addEdge(convertId(graph, "3"), a, b, "knows");
            assertEquals(edge.getPropertyKeys().size(), 0);
            assertNull(edge.getProperty("weight"));
            edge.setProperty("weight", 0.5);
            assertEquals(edge.getPropertyKeys().size(), 1);
            assertEquals(edge.getProperty("weight"), 0.5);

            edge.setProperty("weight", 0.6);
            assertEquals(edge.getPropertyKeys().size(), 1);
            assertEquals(edge.getProperty("weight"), 0.6);
            assertEquals(edge.removeProperty("weight"), 0.6);
            assertNull(edge.getProperty("weight"));
            assertEquals(edge.getPropertyKeys().size(), 0);
            edge.setProperty("blah", "marko");
            edge.setProperty("blah2", "josh");
            assertEquals(edge.getPropertyKeys().size(), 2);
        }
        graph.shutdown();
    }

    public void testAddingLabelAndIdProperty() {
        Graph graph = graphTest.generateGraph();
        if (!graph.getFeatures().isRDFModel) {

            Edge edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "knows");
            try {
                edge.setProperty("id", "123");
                assertTrue(false);
            } catch (RuntimeException e) {
                assertTrue(true);
            }
            try {
                edge.setProperty("label", "hates");
                assertTrue(false);
            } catch (RuntimeException e) {
                assertTrue(true);
            }

        }
        graph.shutdown();
    }

    public void testNoConcurrentModificationException() {
        Graph graph = graphTest.generateGraph();
        for (int i = 0; i < 25; i++) {
            graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), convertId(graph, "test"));
        }
        if (!graph.getFeatures().isRDFModel)
            assertEquals(BaseTest.count(graph.getVertices()), 50);
        assertEquals(BaseTest.count(graph.getEdges()), 25);
        for (final Edge edge : graph.getEdges()) {
            graph.removeEdge(edge);
        }
        if (!graph.getFeatures().isRDFModel)
            assertEquals(BaseTest.count(graph.getVertices()), 50);
        assertEquals(BaseTest.count(graph.getEdges()), 0);
        graph.shutdown();
    }
}
