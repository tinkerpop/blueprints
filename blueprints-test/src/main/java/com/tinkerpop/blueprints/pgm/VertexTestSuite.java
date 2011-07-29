package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.pgm.impls.GraphTest;
import com.tinkerpop.blueprints.pgm.impls.sail.SailTokens;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


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
        Graph graph = graphTest.getGraphInstance();
        List<String> ids = generateIds(1);

        if (!graphTest.ignoresSuppliedIds) {
            Vertex v = graph.addVertex(convertId(ids.get(0)));
            Vertex u = graph.getVertex(convertId(ids.get(0)));
            assertEquals(v, u);
        }

        this.stopWatch();
        Vertex v = graph.addVertex(null);
        Vertex u = graph.getVertex(v.getId());
        printPerformance(graph.toString(), 1, "vertex added and retrieved", this.stopWatch());

        assertEquals(v, u);
        assertEquals(graph.getVertex(u.getId()), graph.getVertex(u.getId()));
        assertEquals(graph.getVertex(v.getId()), graph.getVertex(u.getId()));
        assertEquals(graph.getVertex(v.getId()), graph.getVertex(v.getId()));

        graph.clear();

        if (!graphTest.ignoresSuppliedIds) {
            v = graph.addVertex(convertId(ids.get(0)));
            u = graph.getVertex(convertId(ids.get(0)));
            Set<Vertex> set = new HashSet<Vertex>();
            set.add(v);
            set.add(v);
            set.add(u);
            set.add(u);
            set.add(graph.getVertex(convertId(ids.get(0))));
            set.add(graph.getVertex(convertId(ids.get(0))));
            if (graphTest.supportsVertexIndex)
                set.add(graph.getVertices().iterator().next());
            assertEquals(1, set.size());
        }
        graph.shutdown();
    }

    public void testAddVertex() {
        Graph graph = graphTest.getGraphInstance();
        if (graphTest.supportsVertexIteration) {
            List<String> ids = generateIds(3);
            graph.addVertex(convertId(ids.get(0)));
            graph.addVertex(convertId(ids.get(1)));
            assertEquals(2, count(graph.getVertices()));
            graph.addVertex(convertId(ids.get(2)));
            assertEquals(3, count(graph.getVertices()));
        }

        if (graphTest.isRDFModel) {
            Vertex v1 = graph.addVertex("http://tinkerpop.com#marko");
            assertEquals("http://tinkerpop.com#marko", v1.getId());
            Vertex v2 = graph.addVertex("\"1\"^^<datatype:int>");
            assertEquals("\"1\"^^<datatype:int>", v2.getId());
            Vertex v3 = graph.addVertex("_:ABLANKNODE");
            assertEquals(v3.getId(), "_:ABLANKNODE");
            Vertex v4 = graph.addVertex("\"2.24\"^^<xsd:double>");
            assertEquals("\"2.24\"^^<http://www.w3.org/2001/XMLSchema#double>", v4.getId());
        }
        graph.shutdown();
    }

    public void testRemoveVertex() {
        Graph graph = graphTest.getGraphInstance();
        List<String> ids = generateIds(1);

        Vertex v1 = graph.addVertex(convertId(ids.get(0)));
        if (graphTest.supportsVertexIteration)
            assertEquals(1, count(graph.getVertices()));
        graph.removeVertex(v1);
        if (graphTest.supportsVertexIteration)
            assertEquals(0, count(graph.getVertices()));

        Set<Vertex> vertices = new HashSet<Vertex>();
        for (int i = 0; i < 1000; i++) {
            vertices.add(graph.addVertex(null));
        }
        if (graphTest.supportsVertexIteration)
            assertEquals(1000, count(graph.getVertices()));
        for (Vertex v : vertices) {
            graph.removeVertex(v);
        }
        if (graphTest.supportsVertexIteration)
            assertEquals(0, count(graph.getVertices()));
        graph.shutdown();
    }

    public void testRemoveVertexWithEdges() {
        Graph graph = graphTest.getGraphInstance();
        List<String> ids = generateIds(2);
        Vertex v1 = graph.addVertex(convertId(ids.get(0)));
        Vertex v2 = graph.addVertex(convertId(ids.get(1)));
        graph.addEdge(null, v1, v2, convertId("knows"));
        if (graphTest.supportsVertexIteration)
            assertEquals(2, count(graph.getVertices()));
        if (graphTest.supportsEdgeIteration)
            assertEquals(1, count(graph.getEdges()));

        graph.removeVertex(v1);
        if (graphTest.supportsVertexIteration)
            assertEquals(1, count(graph.getVertices()));
        if (graphTest.supportsEdgeIteration)
            assertEquals(0, count(graph.getEdges()));

        graph.removeVertex(v2);
        if (graphTest.supportsVertexIteration)
            assertEquals(0, count(graph.getVertices()));
        if (graphTest.supportsEdgeIteration)
            assertEquals(0, count(graph.getEdges()));
        graph.shutdown();

    }

    public void testGetNonExistantVertices() {
        Graph graph = graphTest.getGraphInstance();
        assertNull(graph.getVertex(null));
        try {
            assertNull(graph.getVertex("asbv"));
            assertNull(graph.getVertex(12.0d));
        } catch (Exception e) {
            assertTrue(true);
        }
        graph.shutdown();
    }

    public void testRemoveVertexNullId() {
        Graph graph = graphTest.getGraphInstance();
        int vertexCount = 1000;
        Vertex v1 = graph.addVertex(null);
        if (graphTest.supportsVertexIteration)
            assertEquals(1, count(graph.getVertices()));
        graph.removeVertex(v1);
        if (graphTest.supportsVertexIteration)
            assertEquals(0, count(graph.getVertices()));

        Set<Vertex> vertices = new HashSet<Vertex>();

        this.stopWatch();
        for (int i = 0; i < vertexCount; i++) {
            vertices.add(graph.addVertex(null));
        }
        printPerformance(graph.toString(), vertexCount, "vertices added", this.stopWatch());
        if (graphTest.supportsVertexIteration)
            assertEquals(vertexCount, count(graph.getVertices()));

        this.stopWatch();
        for (Vertex v : vertices) {
            graph.removeVertex(v);
        }
        printPerformance(graph.toString(), vertexCount, "vertices deleted", this.stopWatch());
        if (graphTest.supportsVertexIteration)
            assertEquals(0, count(graph.getVertices()));
        graph.shutdown();
    }

    public void testVertexIterator() {
        Graph graph = graphTest.getGraphInstance();
        int vertexCount = 5000;
        if (graphTest.supportsVertexIteration) {
            this.stopWatch();
            Set ids = new HashSet();
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

    public void testAddVertexProperties() {
        Graph graph = graphTest.getGraphInstance();
        if (!graphTest.isRDFModel) {
            List<String> ids = generateIds(3);
            Vertex v1 = graph.addVertex(convertId(ids.get(0)));
            Vertex v2 = graph.addVertex(convertId(ids.get(1)));

            v1.setProperty("key1", "value1");
            v1.setProperty("key2", 10);
            v2.setProperty("key2", 20);

            assertEquals("value1", v1.getProperty("key1"));
            assertEquals(10, v1.getProperty("key2"));
            assertEquals(20, v2.getProperty("key2"));

        } else {
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
        Graph graph = graphTest.getGraphInstance();
        if (!graphTest.isRDFModel) {
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

            if (graphTest.supportsVertexIteration)
                assertEquals(50, count(graph.getVertices()));
            assertEquals(50, vertices.size());
            for (Vertex vertex : vertices) {
                assertEquals(15, vertex.getPropertyKeys().size());
            }
        } else {
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
            if (graphTest.supportsVertexIteration)
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
        Graph graph = graphTest.getGraphInstance();
        if (!graphTest.isRDFModel) {
            List<String> ids = generateIds(2);

            Vertex v1 = graph.addVertex(ids.get(0));
            Vertex v2 = graph.addVertex(ids.get(1));
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

            if (!graphTest.ignoresSuppliedIds) {
                v1 = graph.getVertex(ids.get(0));
                v2 = graph.getVertex(ids.get(1));

                assertEquals("value1", v1.removeProperty("key1"));
                assertEquals(10, v1.removeProperty("key2"));
                assertEquals(20, v2.removeProperty("key2"));

                assertNull(v1.removeProperty("key1"));
                assertNull(v1.removeProperty("key2"));
                assertNull(v2.removeProperty("key2"));

                v1 = graph.getVertex(ids.get(0));
                v2 = graph.getVertex(ids.get(1));

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


        } else {
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
        if (!graphTest.isRDFModel) {
            Graph graph = graphTest.getGraphInstance();
            Vertex vertex = graph.addVertex(null);
            try {
                vertex.setProperty("id", "123");
                assertTrue(false);
            } catch (RuntimeException e) {
                assertTrue(true);
            }
            graph.shutdown();
        }
    }


    //TODO: Is this test appropriate for a graph database? Assumes strong sychronization between Blueprints and backend
    /*public void testVertexPropertyInconsistency() {
        Graph graph = graphTest.getGraphInstance();
        if (!graphTest.isRDFModel) {
            List<String> ids = generateIds(1);
            Vertex v1 = graph.addVertex(convertId(ids.get(0)));
            v1.setProperty("key1", "value1");
            if (graphTest.supportsVertexIteration) {
                assertEquals(count(graph.getVertices()), 1);
            }
            assertEquals("value1", v1.getProperty("key1"));

            Vertex v2 = graph.getVertex(v1.getId());
            assertEquals("value1", v2.getProperty("key1"));

            v1.setProperty("key1", "value111");
            assertEquals("value111", v1.getProperty("key1"));
            assertEquals("value111", v2.getProperty("key1"));

            assertEquals("value111", v2.removeProperty("key1"));
            assertNull(v2.getProperty("key1"));
            assertNull(v1.getProperty("key1"));
        }
        graph.shutdown();
    }*/

}
