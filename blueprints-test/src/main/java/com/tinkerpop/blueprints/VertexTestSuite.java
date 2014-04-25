package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.StringFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.tinkerpop.blueprints.Direction.*;


/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class VertexTestSuite extends TestSuite {

    public VertexTestSuite() {
    }

    public VertexTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testVertexEquality() {
        Graph graph = graphTest.generateGraph();

        if (!graph.getFeatures().ignoresSuppliedIds) {
            Vertex v = graph.addVertex(graphTest.convertId("1"));
            Vertex u = graph.getVertex(graphTest.convertId("1"));
            assertEquals(v, u);
        }

        this.stopWatch();
        Vertex v = graph.addVertex(null);
        assertNotNull(v);
        Vertex u = graph.getVertex(v.getId());
        assertNotNull(u);
        assertEquals(v, u);
        printPerformance(graph.toString(), 1, "vertex added and retrieved", this.stopWatch());

        assertEquals(graph.getVertex(u.getId()), graph.getVertex(u.getId()));
        assertEquals(graph.getVertex(v.getId()), graph.getVertex(u.getId()));
        assertEquals(graph.getVertex(v.getId()), graph.getVertex(v.getId()));

        graph.shutdown();
    }

    public void testVertexEqualityForSuppliedIdsAndHashCode() {
        Graph graph = graphTest.generateGraph();
        if (!graph.getFeatures().ignoresSuppliedIds) {

            Vertex v = graph.addVertex(graphTest.convertId("1"));
            Vertex u = graph.getVertex(graphTest.convertId("1"));
            Set<Vertex> set = new HashSet<Vertex>();
            set.add(v);
            set.add(v);
            set.add(u);
            set.add(u);
            set.add(graph.getVertex(graphTest.convertId("1")));
            set.add(graph.getVertex(graphTest.convertId("1")));
            if (graph.getFeatures().supportsVertexIndex)
                set.add(graph.getVertices().iterator().next());
            assertEquals(1, set.size());
            assertEquals(v.hashCode(), u.hashCode());
        }
        graph.shutdown();
    }

    public void testBasicAddVertex() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIteration) {
            graph.addVertex(graphTest.convertId("1"));
            graph.addVertex(graphTest.convertId("2"));
            assertEquals(2, count(graph.getVertices()));
            graph.addVertex(graphTest.convertId("3"));
            assertEquals(3, count(graph.getVertices()));
        }
        graph.shutdown();
    }

    public void testGetVertexWithNull() {
        Graph graph = graphTest.generateGraph();
        try {
            graph.getVertex(null);
            assertFalse(true);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        graph.shutdown();
    }

    public void testRemoveVertex() {
        Graph graph = graphTest.generateGraph();

        Vertex v1 = graph.addVertex(graphTest.convertId("1"));
        if (!graph.getFeatures().ignoresSuppliedIds)
            assertEquals(graph.getVertex(graphTest.convertId("1")), v1);

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(1, count(graph.getVertices()));
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(0, count(graph.getEdges()));

        graph.removeVertex(v1);
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(0, count(graph.getVertices()));
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(0, count(graph.getEdges()));

        Set<Vertex> vertices = new HashSet<Vertex>();
        for (int i = 0; i < 100; i++) {
            vertices.add(graph.addVertex(null));
        }
        assertEquals(vertices.size(), 100);
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(100, count(graph.getVertices()));
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(0, count(graph.getEdges()));

        for (Vertex v : vertices) {
            graph.removeVertex(v);
        }
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(0, count(graph.getVertices()));
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(0, count(graph.getEdges()));

        graph.shutdown();
    }

    public void testRemoveVertexWithEdges() {
        Graph graph = graphTest.generateGraph();
        Vertex v1 = graph.addVertex(graphTest.convertId("1"));
        Vertex v2 = graph.addVertex(graphTest.convertId("2"));
        graph.addEdge(null, v1, v2, graphTest.convertLabel("knows"));
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(2, count(graph.getVertices()));
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(1, count(graph.getEdges()));

        graph.removeVertex(v1);
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(1, count(graph.getVertices()));
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(0, count(graph.getEdges()));

        graph.removeVertex(v2);
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(0, count(graph.getVertices()));
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(0, count(graph.getEdges()));
        graph.shutdown();

    }

    public void testGetNonExistentVertices() {
        Graph graph = graphTest.generateGraph();
        if (!graph.getFeatures().hasImplicitElements) {
            assertNull(graph.getVertex(graphTest.convertId("asbv")));
            assertNull(graph.getVertex(graphTest.convertId(12.0d)));
        }
        graph.shutdown();
    }

    public void testRemoveVertexNullId() {
        Graph graph = graphTest.generateGraph();

        Vertex v1 = graph.addVertex(null);
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(1, count(graph.getVertices()));
        graph.removeVertex(v1);
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(0, count(graph.getVertices()));

        Set<Vertex> vertices = new HashSet<Vertex>();

        this.stopWatch();
        int vertexCount = 100;
        for (int i = 0; i < vertexCount; i++) {
            vertices.add(graph.addVertex(null));
        }
        printPerformance(graph.toString(), vertexCount, "vertices added", this.stopWatch());
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(vertexCount, count(graph.getVertices()));

        this.stopWatch();
        for (Vertex v : vertices) {
            graph.removeVertex(v);
        }
        printPerformance(graph.toString(), vertexCount, "vertices removed", this.stopWatch());
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(0, count(graph.getVertices()));
        graph.shutdown();
    }

    public void testVertexIterator() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIteration) {
            this.stopWatch();
            int vertexCount = 1000;
            Set ids = new HashSet(1000);
            for (int i = 0; i < vertexCount; i++) {
                ids.add(graph.addVertex(null).getId());
            }
            printPerformance(graph.toString(), vertexCount, "vertices added", this.stopWatch());
            this.stopWatch();
            assertEquals(vertexCount, count(graph.getVertices()));
            printPerformance(graph.toString(), vertexCount, "vertices counted", this.stopWatch());
            // must create unique ids
            assertEquals(vertexCount, ids.size());
        }
        graph.shutdown();
    }

    public void testLegalVertexEdgeIterables() {
        Graph graph = graphTest.generateGraph();
        Vertex v1 = graph.addVertex(null);
        for (int i = 0; i < 10; i++) {
            graph.addEdge(null, v1, graph.addVertex(null), graphTest.convertLabel("knows"));
        }
        Iterable<Edge> edges = v1.getEdges(Direction.OUT, graphTest.convertLabel("knows"));
        assertEquals(count(edges), 10);
        assertEquals(count(edges), 10);
        assertEquals(count(edges), 10);
        graph.shutdown();
    }

    public void testAddVertexProperties() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties) {
            Vertex v1 = graph.addVertex(graphTest.convertId("1"));
            Vertex v2 = graph.addVertex(graphTest.convertId("2"));

            if (graph.getFeatures().supportsStringProperty) {
                v1.setProperty("key1", "value1");
                assertEquals("value1", v1.getProperty("key1"));
            }

            if (graph.getFeatures().supportsIntegerProperty) {
                v1.setProperty("key2", 10);
                v2.setProperty("key2", 20);

                assertEquals(10, v1.getProperty("key2"));
                assertEquals(20, v2.getProperty("key2"));
            }

        }
        graph.shutdown();
    }

    public void testAddManyVertexProperties() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties && graph.getFeatures().supportsStringProperty) {
            Set<Vertex> vertices = new HashSet<Vertex>();
            this.stopWatch();
            for (int i = 0; i < 50; i++) {
                Vertex vertex = graph.addVertex(null);
                for (int j = 0; j < 15; j++) {
                    vertex.setProperty(UUID.randomUUID().toString(), UUID.randomUUID().toString());
                }
                vertices.add(vertex);
            }
            printPerformance(graph.toString(), 15 * 50, "vertex properties added (with vertices being added too)", this.stopWatch());

            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(50, count(graph.getVertices()));
            assertEquals(50, vertices.size());
            for (Vertex vertex : vertices) {
                assertEquals(15, vertex.getPropertyKeys().size());
            }
        }
        graph.shutdown();
    }

    public void testRemoveVertexProperties() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties) {

            Vertex v1 = graph.addVertex(graphTest.convertId("1"));
            Vertex v2 = graph.addVertex(graphTest.convertId("2"));

            assertNull(v1.removeProperty("key1"));
            assertNull(v1.removeProperty("key2"));
            assertNull(v2.removeProperty("key2"));

            if (graph.getFeatures().supportsStringProperty) {
                v1.setProperty("key1", "value1");
                assertEquals("value1", v1.removeProperty("key1"));
            }

            if (graph.getFeatures().supportsIntegerProperty) {
                v1.setProperty("key2", 10);
                v2.setProperty("key2", 20);

                assertEquals(10, v1.removeProperty("key2"));
                assertEquals(20, v2.removeProperty("key2"));
            }

            assertNull(v1.removeProperty("key1"));
            assertNull(v1.removeProperty("key2"));
            assertNull(v2.removeProperty("key2"));

            if (graph.getFeatures().supportsStringProperty) {
                v1.setProperty("key1", "value1");
            }

            if (graph.getFeatures().supportsIntegerProperty) {
                v1.setProperty("key2", 10);
                v2.setProperty("key2", 20);
            }

            if (!graph.getFeatures().ignoresSuppliedIds) {
                v1 = graph.getVertex(graphTest.convertId("1"));
                v2 = graph.getVertex(graphTest.convertId("2"));

                if (graph.getFeatures().supportsStringProperty) {
                    assertEquals("value1", v1.removeProperty("key1"));
                }

                if (graph.getFeatures().supportsIntegerProperty) {
                    assertEquals(10, v1.removeProperty("key2"));
                    assertEquals(20, v2.removeProperty("key2"));
                }

                assertNull(v1.removeProperty("key1"));
                assertNull(v1.removeProperty("key2"));
                assertNull(v2.removeProperty("key2"));

                v1 = graph.getVertex(graphTest.convertId("1"));
                v2 = graph.getVertex(graphTest.convertId("2"));

                if (graph.getFeatures().supportsStringProperty) {
                    v1.setProperty("key1", "value2");
                    assertEquals("value2", v1.removeProperty("key1"));
                }

                if (graph.getFeatures().supportsIntegerProperty) {
                    v1.setProperty("key2", 20);
                    v2.setProperty("key2", 30);

                    assertEquals(20, v1.removeProperty("key2"));
                    assertEquals(30, v2.removeProperty("key2"));
                }

                assertNull(v1.removeProperty("key1"));
                assertNull(v1.removeProperty("key2"));
                assertNull(v2.removeProperty("key2"));
            }
        }
        graph.shutdown();
    }

    public void testAddingIdProperty() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties) {
            Vertex vertex = graph.addVertex(null);
            try {
                vertex.setProperty("id", "123");
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        }
        graph.shutdown();
    }


    public void testNoConcurrentModificationException() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIteration) {

            for (int i = 0; i < 25; i++) {
                graph.addVertex(null);
            }
            assertEquals(count(graph.getVertices()), 25);
            for (final Vertex vertex : graph.getVertices()) {
                graph.removeVertex(vertex);
            }
            assertEquals(count(graph.getVertices()), 0);
        }
        graph.shutdown();
    }

    public void testGettingEdgesAndVertices() {
        Graph graph = graphTest.generateGraph();
        Vertex a = graph.addVertex(null);
        Vertex b = graph.addVertex(null);
        Vertex c = graph.addVertex(null);
        Edge w = graph.addEdge(null, a, b, graphTest.convertLabel("knows"));
        Edge x = graph.addEdge(null, b, c, graphTest.convertLabel("knows"));
        Edge y = graph.addEdge(null, a, c, graphTest.convertLabel("hates"));
        Edge z = graph.addEdge(null, a, b, graphTest.convertLabel("hates"));
        Edge zz = graph.addEdge(null, c, c, graphTest.convertLabel("hates"));

        assertEquals(count(a.getEdges(OUT)), 3);
        assertEquals(count(a.getEdges(OUT, graphTest.convertLabel("hates"))), 2);
        assertEquals(count(a.getEdges(OUT, graphTest.convertLabel("knows"))), 1);
        assertEquals(count(a.getVertices(OUT)), 3);
        assertEquals(count(a.getVertices(OUT, graphTest.convertLabel("hates"))), 2);
        assertEquals(count(a.getVertices(OUT, graphTest.convertLabel("knows"))), 1);
        assertEquals(count(a.getVertices(BOTH)), 3);
        assertEquals(count(a.getVertices(BOTH, graphTest.convertLabel("hates"))), 2);
        assertEquals(count(a.getVertices(BOTH, graphTest.convertLabel("knows"))), 1);

        assertTrue(asList(a.getEdges(OUT)).contains(w));
        assertTrue(asList(a.getEdges(OUT)).contains(y));
        assertTrue(asList(a.getEdges(OUT)).contains(z));
        assertTrue(asList(a.getVertices(OUT)).contains(b));
        assertTrue(asList(a.getVertices(OUT)).contains(c));

        assertTrue(asList(a.getEdges(OUT, graphTest.convertLabel("knows"))).contains(w));
        assertFalse(asList(a.getEdges(OUT, graphTest.convertLabel("knows"))).contains(y));
        assertFalse(asList(a.getEdges(OUT, graphTest.convertLabel("knows"))).contains(z));
        assertTrue(asList(a.getVertices(OUT, graphTest.convertLabel("knows"))).contains(b));
        assertFalse(asList(a.getVertices(OUT, graphTest.convertLabel("knows"))).contains(c));

        assertFalse(asList(a.getEdges(OUT, graphTest.convertLabel("hates"))).contains(w));
        assertTrue(asList(a.getEdges(OUT, graphTest.convertLabel("hates"))).contains(y));
        assertTrue(asList(a.getEdges(OUT, graphTest.convertLabel("hates"))).contains(z));
        assertTrue(asList(a.getVertices(OUT, graphTest.convertLabel("hates"))).contains(b));
        assertTrue(asList(a.getVertices(OUT, graphTest.convertLabel("hates"))).contains(c));

        assertEquals(count(a.getVertices(IN)), 0);
        assertEquals(count(a.getVertices(IN, graphTest.convertLabel("knows"))), 0);
        assertEquals(count(a.getVertices(IN, graphTest.convertLabel("hates"))), 0);
        assertTrue(asList(a.getEdges(OUT)).contains(w));
        assertTrue(asList(a.getEdges(OUT)).contains(y));
        assertTrue(asList(a.getEdges(OUT)).contains(z));

        assertEquals(count(b.getEdges(BOTH)), 3);
        assertEquals(count(b.getEdges(BOTH, graphTest.convertLabel("knows"))), 2);
        assertTrue(asList(b.getEdges(BOTH, graphTest.convertLabel("knows"))).contains(x));
        assertTrue(asList(b.getEdges(BOTH, graphTest.convertLabel("knows"))).contains(w));
        assertTrue(asList(b.getVertices(BOTH, graphTest.convertLabel("knows"))).contains(a));
        assertTrue(asList(b.getVertices(BOTH, graphTest.convertLabel("knows"))).contains(c));

        assertEquals(count(c.getEdges(BOTH, graphTest.convertLabel("hates"))), 3);
        assertEquals(count(c.getVertices(BOTH, graphTest.convertLabel("hates"))), 3);
        assertEquals(count(c.getEdges(BOTH, graphTest.convertLabel("knows"))), 1);
        assertTrue(asList(c.getEdges(BOTH, graphTest.convertLabel("hates"))).contains(y));
        assertTrue(asList(c.getEdges(BOTH, graphTest.convertLabel("hates"))).contains(zz));
        assertTrue(asList(c.getVertices(BOTH, graphTest.convertLabel("hates"))).contains(a));
        assertTrue(asList(c.getVertices(BOTH, graphTest.convertLabel("hates"))).contains(c));
        assertEquals(count(c.getEdges(IN, graphTest.convertLabel("hates"))), 2);
        assertEquals(count(c.getEdges(OUT, graphTest.convertLabel("hates"))), 1);

        try {
            x.getVertex(BOTH);
            fail("Getting edge vertex with direction BOTH should fail");
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            fail("Getting edge vertex with direction BOTH should should throw " +
                    IllegalArgumentException.class.getSimpleName());
        }

        graph.shutdown();
    }

    public void testEmptyKeyProperty() {
        final Graph graph = graphTest.generateGraph();

        // no point in testing graph features for setting string properties because the intent is for it to
        // fail based on the empty key.
        if (graph.getFeatures().supportsVertexProperties) {
            final Vertex v = graph.addVertex(null);
            try {
                v.setProperty("", "value");
                fail("Setting a vertex property with an empty string key should fail");
            } catch (IllegalArgumentException e) {
            }
        }
        graph.shutdown();
    }

    public void testVertexCentricLinking() {
        final Graph graph = graphTest.generateGraph();

        final Vertex v = graph.addVertex(null);
        final Vertex a = graph.addVertex(null);
        final Vertex b = graph.addVertex(null);

        v.addEdge(graphTest.convertLabel("knows"), a);
        v.addEdge(graphTest.convertLabel("knows"), b);

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 3);
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 2);

        assertEquals(count(v.getEdges(OUT, graphTest.convertLabel("knows"))), 2);
        assertEquals(count(a.getEdges(OUT, graphTest.convertLabel("knows"))), 0);
        assertEquals(count(a.getEdges(IN, graphTest.convertLabel("knows"))), 1);

        assertEquals(count(b.getEdges(OUT, graphTest.convertLabel("knows"))), 0);
        assertEquals(count(b.getEdges(IN, graphTest.convertLabel("knows"))), 1);

        graph.shutdown();
    }

    public void testVertexCentricRemoving() {
        final Graph graph = graphTest.generateGraph();

        final Vertex a = graph.addVertex(null);
        final Vertex b = graph.addVertex(null);
        final Vertex c = graph.addVertex(null);

        Object cId = c.getId();

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 3);

        a.remove();
        b.remove();

        assertNotNull(graph.getVertex(cId));

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 1);

        graph.shutdown();

    }

    public void testConcurrentModificationOnProperties() {
        final Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties) {
            Vertex a = graph.addVertex(null);
            a.setProperty("test1", 1);
            a.setProperty("test2", 2);
            a.setProperty("test3", 3);
            a.setProperty("test4", 4);
            for (String key : a.getPropertyKeys()) {
                a.removeProperty(key);
            }
        }
        graph.shutdown();
    }

    public void testSettingBadVertexProperties() {
        final Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties) {
            Vertex v = graph.addVertex(null);
            try {
                v.setProperty(null, -1);
                assertFalse(true);
            } catch (RuntimeException e) {
                assertTrue(true);
            }
            try {
                v.setProperty("", -1);
                assertFalse(true);
            } catch (RuntimeException e) {
                assertTrue(true);
            }
            try {
                v.setProperty(StringFactory.ID, -1);
                assertFalse(true);
            } catch (RuntimeException e) {
                assertTrue(true);
            }
            try {
                v.setProperty("good", null);
                assertFalse(true);
            } catch (RuntimeException e) {
                assertTrue(true);
            }
        }
        graph.shutdown();
    }
}
