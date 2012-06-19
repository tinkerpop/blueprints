package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.impls.sail.SailTokens;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.tinkerpop.blueprints.Direction.BOTH;
import static com.tinkerpop.blueprints.Direction.IN;
import static com.tinkerpop.blueprints.Direction.OUT;


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
            Vertex v = graph.addVertex(convertId(graph, "1"));
            Vertex u = graph.getVertex(convertId(graph, "1"));
            assertEquals(v, u);
        }

        this.stopWatch();
        Vertex v = graph.addVertex(null);
        assertFalse(v.equals(null));
        Vertex u = graph.getVertex(v.getId());
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

            Vertex v = graph.addVertex(convertId(graph, "1"));
            Vertex u = graph.getVertex(convertId(graph, "1"));
            Set<Vertex> set = new HashSet<Vertex>();
            set.add(v);
            set.add(v);
            set.add(u);
            set.add(u);
            set.add(graph.getVertex(convertId(graph, "1")));
            set.add(graph.getVertex(convertId(graph, "1")));
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
            graph.addVertex(convertId(graph, "1"));
            graph.addVertex(convertId(graph, "2"));
            assertEquals(2, count(graph.getVertices()));
            graph.addVertex(convertId(graph, "3"));
            assertEquals(3, count(graph.getVertices()));
        }

        if (graph.getFeatures().isRDFModel) {
            Vertex v1 = graph.addVertex("http://tinkerpop.com#marko");
            assertEquals("http://tinkerpop.com#marko", v1.getId());
            Vertex v2 = graph.addVertex("\"1\"^^<datatype:int>");
            assertEquals("\"1\"^^<datatype:int>", v2.getId());
            Vertex v3 = graph.addVertex("_:ABLANKNODE");
            assertEquals(v3.getId(), "_:ABLANKNODE");
            Vertex v4 = graph.addVertex("\"2.24\"^^<http://www.w3.org/2001/XMLSchema#double>");
            assertEquals("\"2.24\"^^<http://www.w3.org/2001/XMLSchema#double>", v4.getId());
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

        Vertex v1 = graph.addVertex(convertId(graph, "1"));
        if (!graph.getFeatures().ignoresSuppliedIds)
            assertEquals(graph.getVertex(convertId(graph, "1")), v1);

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
        Vertex v1 = graph.addVertex(convertId(graph, "1"));
        Vertex v2 = graph.addVertex(convertId(graph, "2"));
        graph.addEdge(null, v1, v2, convertId(graph, "knows"));
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

    public void testGetNonExistantVertices() {
        Graph graph = graphTest.generateGraph();
        assertNull(graph.getVertex("asbv"));
        assertNull(graph.getVertex(12.0d));
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
            graph.addEdge(null, v1, graph.addVertex(null), convertId(graph, "knows"));
        }
        Iterable<Edge> edges = v1.getEdges(Direction.OUT, convertId(graph, "knows"));
        assertEquals(count(edges), 10);
        assertEquals(count(edges), 10);
        assertEquals(count(edges), 10);
        graph.shutdown();
    }

    public void testAddVertexProperties() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties) {
            Vertex v1 = graph.addVertex(convertId(graph, "1"));
            Vertex v2 = graph.addVertex(convertId(graph, "2"));

            v1.setProperty("key1", "value1");
            v1.setProperty("key2", 10);
            v2.setProperty("key2", 20);

            assertEquals("value1", v1.getProperty("key1"));
            assertEquals(10, v1.getProperty("key2"));
            assertEquals(20, v2.getProperty("key2"));
        } else if (graph.getFeatures().isRDFModel) {
            Vertex v1 = graph.addVertex("\"1\"^^<http://www.w3.org/2001/XMLSchema#int>");
            assertEquals("http://www.w3.org/2001/XMLSchema#int", v1.getProperty(SailTokens.DATATYPE));
            assertEquals(1, v1.getProperty(SailTokens.VALUE));
            assertNull(v1.getProperty(SailTokens.LANGUAGE));
            assertNull(v1.getProperty("random something"));

            Vertex v2 = graph.addVertex("\"hello\"@en");
            assertEquals("en", v2.getProperty(SailTokens.LANGUAGE));
            assertEquals("hello", v2.getProperty(SailTokens.VALUE));
            assertNull(v2.getProperty(SailTokens.DATATYPE));
            assertNull(v2.getProperty("random something"));
        }
        graph.shutdown();
    }

    public void testAddManyVertexProperties() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties) {
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
        } else if (graph.getFeatures().isRDFModel) {
            Set<Vertex> vertices = new HashSet<Vertex>();
            this.stopWatch();
            for (int i = 0; i < 50; i++) {
                Vertex vertex = graph.addVertex("\"" + UUID.randomUUID().toString() + "\"");
                for (int j = 0; j < 15; j++) {
                    vertex.setProperty(SailTokens.DATATYPE, "http://www.w3.org/2001/XMLSchema#anyURI");
                }
                vertices.add(vertex);
            }
            printPerformance(graph.toString(), 15 * 50, "vertex properties added (with vertices being added too)", this.stopWatch());
            if (graph.getFeatures().supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 50);
            assertEquals(vertices.size(), 50);
            for (Vertex vertex : vertices) {
                assertEquals(3, vertex.getPropertyKeys().size());
                assertTrue(vertex.getPropertyKeys().contains(SailTokens.DATATYPE));
                assertEquals("http://www.w3.org/2001/XMLSchema#anyURI", vertex.getProperty(SailTokens.DATATYPE));
                assertTrue(vertex.getPropertyKeys().contains(SailTokens.VALUE));
                assertEquals("literal", vertex.getProperty(SailTokens.KIND));

            }
        }
        graph.shutdown();
    }

    public void testRemoveVertexProperties() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties) {

            Vertex v1 = graph.addVertex("1");
            Vertex v2 = graph.addVertex("2");

            assertNull(v1.removeProperty("key1"));
            assertNull(v1.removeProperty("key2"));
            assertNull(v2.removeProperty("key2"));

            v1.setProperty("key1", "value1");
            v1.setProperty("key2", 10);
            v2.setProperty("key2", 20);

            assertEquals("value1", v1.removeProperty("key1"));
            assertEquals(10, v1.removeProperty("key2"));
            assertEquals(20, v2.removeProperty("key2"));

            assertNull(v1.removeProperty("key1"));
            assertNull(v1.removeProperty("key2"));
            assertNull(v2.removeProperty("key2"));

            v1.setProperty("key1", "value1");
            v1.setProperty("key2", 10);
            v2.setProperty("key2", 20);

            if (!graph.getFeatures().ignoresSuppliedIds) {
                v1 = graph.getVertex("1");
                v2 = graph.getVertex("2");

                assertEquals("value1", v1.removeProperty("key1"));
                assertEquals(10, v1.removeProperty("key2"));
                assertEquals(20, v2.removeProperty("key2"));

                assertNull(v1.removeProperty("key1"));
                assertNull(v1.removeProperty("key2"));
                assertNull(v2.removeProperty("key2"));

                v1 = graph.getVertex("1");
                v2 = graph.getVertex("2");

                v1.setProperty("key1", "value2");
                v1.setProperty("key2", 20);
                v2.setProperty("key2", 30);

                assertEquals("value2", v1.removeProperty("key1"));
                assertEquals(20, v1.removeProperty("key2"));
                assertEquals(30, v2.removeProperty("key2"));

                assertNull(v1.removeProperty("key1"));
                assertNull(v1.removeProperty("key2"));
                assertNull(v2.removeProperty("key2"));
            }
        } else if (graph.getFeatures().isRDFModel) {
            Vertex v1 = graph.addVertex("\"1\"^^<http://www.w3.org/2001/XMLSchema#int>");
            assertEquals("http://www.w3.org/2001/XMLSchema#int", v1.removeProperty("type"));
            assertEquals("1", v1.getProperty("value"));
            assertNull(v1.getProperty("lang"));
            assertNull(v1.getProperty("random something"));

            Vertex v2 = graph.addVertex("\"hello\"@en");
            assertEquals("en", v2.removeProperty("lang"));
            assertEquals("hello", v2.getProperty("value"));
            assertNull(v2.getProperty("type"));
            assertNull(v2.getProperty("random something"));
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
        Edge w = graph.addEdge(null, a, b, convertId(graph, "knows"));
        Edge x = graph.addEdge(null, b, c, convertId(graph, "knows"));
        Edge y = graph.addEdge(null, a, c, convertId(graph, "hates"));
        Edge z = graph.addEdge(null, a, b, convertId(graph, "hates"));
        Edge zz = graph.addEdge(null, c, c, convertId(graph, "hates"));

        assertEquals(count(a.getEdges(OUT)), 3);
        assertEquals(count(a.getEdges(OUT, convertId(graph, "hates"))), 2);
        assertEquals(count(a.getEdges(OUT, convertId(graph, "knows"))), 1);
        assertEquals(count(a.getVertices(OUT)), 3);
        assertEquals(count(a.getVertices(OUT, convertId(graph, "hates"))), 2);
        assertEquals(count(a.getVertices(OUT, convertId(graph, "knows"))), 1);
        assertEquals(count(a.getVertices(BOTH)), 3);
        assertEquals(count(a.getVertices(BOTH, convertId(graph, "hates"))), 2);
        assertEquals(count(a.getVertices(BOTH, convertId(graph, "knows"))), 1);

        assertTrue(asList(a.getEdges(OUT)).contains(w));
        assertTrue(asList(a.getEdges(OUT)).contains(y));
        assertTrue(asList(a.getEdges(OUT)).contains(z));
        assertTrue(asList(a.getVertices(OUT)).contains(b));
        assertTrue(asList(a.getVertices(OUT)).contains(c));

        assertTrue(asList(a.getEdges(OUT, convertId(graph, "knows"))).contains(w));
        assertFalse(asList(a.getEdges(OUT, convertId(graph, "knows"))).contains(y));
        assertFalse(asList(a.getEdges(OUT, convertId(graph, "knows"))).contains(z));
        assertTrue(asList(a.getVertices(OUT, convertId(graph, "knows"))).contains(b));
        assertFalse(asList(a.getVertices(OUT, convertId(graph, "knows"))).contains(c));

        assertFalse(asList(a.getEdges(OUT, convertId(graph, "hates"))).contains(w));
        assertTrue(asList(a.getEdges(OUT, convertId(graph, "hates"))).contains(y));
        assertTrue(asList(a.getEdges(OUT, convertId(graph, "hates"))).contains(z));
        assertTrue(asList(a.getVertices(OUT, convertId(graph, "hates"))).contains(b));
        assertTrue(asList(a.getVertices(OUT, convertId(graph, "hates"))).contains(c));

        assertEquals(count(a.getVertices(IN)), 0);
        assertEquals(count(a.getVertices(IN, convertId(graph, "knows"))), 0);
        assertEquals(count(a.getVertices(IN, convertId(graph, "hates"))), 0);
        assertTrue(asList(a.getEdges(OUT)).contains(w));
        assertTrue(asList(a.getEdges(OUT)).contains(y));
        assertTrue(asList(a.getEdges(OUT)).contains(z));

        assertEquals(count(b.getEdges(BOTH)), 3);
        assertEquals(count(b.getEdges(BOTH, convertId(graph, "knows"))), 2);
        assertTrue(asList(b.getEdges(BOTH, convertId(graph, "knows"))).contains(x));
        assertTrue(asList(b.getEdges(BOTH, convertId(graph, "knows"))).contains(w));
        assertTrue(asList(b.getVertices(BOTH, convertId(graph, "knows"))).contains(a));
        assertTrue(asList(b.getVertices(BOTH, convertId(graph, "knows"))).contains(c));

        assertEquals(count(c.getEdges(BOTH, convertId(graph, "hates"))), 3);
        assertEquals(count(c.getVertices(BOTH, convertId(graph, "hates"))), 3);
        assertEquals(count(c.getEdges(BOTH, convertId(graph, "knows"))), 1);
        assertTrue(asList(c.getEdges(BOTH, convertId(graph, "hates"))).contains(y));
        assertTrue(asList(c.getEdges(BOTH, convertId(graph, "hates"))).contains(zz));
        assertTrue(asList(c.getVertices(BOTH, convertId(graph, "hates"))).contains(a));
        assertTrue(asList(c.getVertices(BOTH, convertId(graph, "hates"))).contains(c));
        assertEquals(count(c.getEdges(IN, convertId(graph, "hates"))), 2);
        assertEquals(count(c.getEdges(OUT, convertId(graph, "hates"))), 1);

        try {
            x.getVertex(BOTH);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        graph.shutdown();
    }
}
