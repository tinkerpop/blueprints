package com.tinkerpop.blueprints.pgm;


import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.impls.sail.SailTokens;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class VertexTestSuite extends ModelTestSuite {

    public VertexTestSuite() {
    }

    public VertexTestSuite(final SuiteConfiguration config) {
        super(config);
    }

    public void testVertexEquality(final Graph graph) {
        Random random = new Random();
        String id = "" + Math.abs(random.nextInt());

        if (!config.ignoresSuppliedIds) {
            Vertex v = graph.addVertex(convertId(id));
            Vertex u = graph.getVertex(convertId(id));
            assertEquals(v, u);
        }

        this.stopWatch();
        Vertex v = graph.addVertex(null);
        Vertex u = graph.getVertex(v.getId());
        BaseTest.printPerformance(graph.toString(), 1, "vertex added and retrieved", this.stopWatch());

        assertEquals(v, u);
        assertEquals(graph.getVertex(u.getId()), graph.getVertex(u.getId()));
        assertEquals(graph.getVertex(v.getId()), graph.getVertex(u.getId()));
        assertEquals(graph.getVertex(v.getId()), graph.getVertex(v.getId()));

        graph.clear();

        if (!config.ignoresSuppliedIds) {
            v = graph.addVertex(convertId(id));
            u = graph.getVertex(convertId(id));
            Set<Vertex> set = new HashSet<Vertex>();
            set.add(v);
            set.add(v);
            set.add(u);
            set.add(u);
            set.add(graph.getVertex(convertId(id)));
            set.add(graph.getVertex(convertId(id)));
            if (config.supportsVertexIndex)
                set.add(graph.getVertices().iterator().next());
            assertEquals(set.size(), 1);
        }

    }

    public void testAddVertex(final Graph graph) {
        if (config.supportsVertexIteration) {
            graph.addVertex(convertId("1"));
            graph.addVertex(convertId("2"));
            assertEquals(count(graph.getVertices()), 2);
            graph.addVertex(convertId("3"));
            assertEquals(count(graph.getVertices()), 3);
        }

        if (config.isRDFModel && config.requiresRDFIds) {
            Vertex v1 = graph.addVertex("http://tinkerpop.com#marko");
            assertEquals(v1.getId(), "http://tinkerpop.com#marko");
            Vertex v2 = graph.addVertex("\"1\"^^<datatype:int>");
            assertEquals(v2.getId(), "\"1\"^^<datatype:int>");
            Vertex v3 = graph.addVertex("_:ABLANKNODE");
            assertEquals(v3.getId(), "_:ABLANKNODE");
            Vertex v4 = graph.addVertex("\"2.24\"^^<xsd:double>");
            assertEquals(v4.getId(), "\"2.24\"^^<http://www.w3.org/2001/XMLSchema#double>");
        }
    }

    public void testRemoveVertex(final Graph graph) {
        Random random = new Random();
        String id = "" + Math.abs(random.nextInt());

        Vertex v1 = graph.addVertex(convertId(id));
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 1);
        graph.removeVertex(v1);
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 0);

        Set<Vertex> vertices = new HashSet<Vertex>();
        for (int i = 0; i < 1000; i++) {
            vertices.add(graph.addVertex(null));
        }
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 1000);
        for (Vertex v : vertices) {
            graph.removeVertex(v);
        }
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 0);
    }

    public void testGetNonExistantVertices(final Graph graph) {
        try {
            assertNull(graph.getVertex("asbv"));
            assertNull(graph.getVertex(12.0d));
            assertNull(graph.getVertex(null));
        } catch (RuntimeException e) {
            assertTrue(true);
        }
    }

    public void testRemoveVertexNullId(final Graph graph) {
        int vertexCount = 1000;
        Vertex v1 = graph.addVertex(null);
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 1);
        graph.removeVertex(v1);
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 0);

        Set<Vertex> vertices = new HashSet<Vertex>();

        this.stopWatch();
        for (int i = 0; i < vertexCount; i++) {
            vertices.add(graph.addVertex(null));
        }
        BaseTest.printPerformance(graph.toString(), vertexCount, "vertices added", this.stopWatch());
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), vertexCount);

        this.stopWatch();
        for (Vertex v : vertices) {
            graph.removeVertex(v);
        }
        BaseTest.printPerformance(graph.toString(), vertexCount, "vertices deleted", this.stopWatch());
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 0);

    }

    public void testVertexIterator(final Graph graph) {
        int vertexCount = 5000;
        if (config.supportsVertexIteration) {
            this.stopWatch();
            Set ids = new HashSet();
            for (int i = 0; i < vertexCount; i++) {
                ids.add(graph.addVertex(null).getId());
            }
            BaseTest.printPerformance(graph.toString(), vertexCount, "vertices added", this.stopWatch());
            this.stopWatch();
            assertEquals(count(graph.getVertices()), vertexCount);
            BaseTest.printPerformance(graph.toString(), vertexCount, "vertices counted", this.stopWatch());
            // must create unique ids
            assertEquals(ids.size(), vertexCount);
        }
    }

    public void testAddVertexProperties(final Graph graph) {
        if (!config.isRDFModel) {
            Random random = new Random();
            int id1 = Math.abs(random.nextInt() - 1);
            int id2 = id1 + 1;

            Vertex v1 = graph.addVertex(convertId("" + id1));
            Vertex v2 = graph.addVertex(convertId("" + id2));

            v1.setProperty("key1", "value1");
            v1.setProperty("key2", 10);
            v2.setProperty("key2", 20);

            assertEquals(v1.getProperty("key1"), "value1");
            assertEquals(v1.getProperty("key2"), 10);
            assertEquals(v2.getProperty("key2"), 20);

        } else {
            Vertex v1 = graph.addVertex("\"1\"^^<http://www.w3.org/2001/XMLSchema#int>");
            assertEquals(v1.getProperty(SailTokens.DATATYPE), "http://www.w3.org/2001/XMLSchema#int");
            assertEquals(v1.getProperty(SailTokens.VALUE), 1);
            assertNull(v1.getProperty(SailTokens.LANGUAGE));
            assertNull(v1.getProperty("random something"));

            Vertex v2 = graph.addVertex("\"hello\"@en");
            assertEquals(v2.getProperty(SailTokens.LANGUAGE), "en");
            assertEquals(v2.getProperty(SailTokens.VALUE), "hello");
            assertNull(v2.getProperty(SailTokens.DATATYPE));
            assertNull(v2.getProperty("random something"));
        }
    }

    public void testAddManyVertexProperties(final Graph graph) {
        if (!config.isRDFModel) {
            Set<Vertex> vertices = new HashSet<Vertex>();
            this.stopWatch();
            for (int i = 0; i < 50; i++) {
                Vertex vertex = graph.addVertex(null);
                for (int j = 0; j < 15; j++) {
                    vertex.setProperty(UUID.randomUUID().toString(), UUID.randomUUID().toString());
                }
                vertices.add(vertex);
            }
            BaseTest.printPerformance(graph.toString(), 15 * 50, "vertex properties added (with vertices being added too)", this.stopWatch());

            if (config.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 50);
            assertEquals(vertices.size(), 50);
            for (Vertex vertex : vertices) {
                assertEquals(vertex.getPropertyKeys().size(), 15);
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
            BaseTest.printPerformance(graph.toString(), 15 * 50, "vertex properties added (with vertices being added too)", this.stopWatch());
            if (config.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 50);
            assertEquals(vertices.size(), 50);
            for (Vertex vertex : vertices) {
                assertEquals(vertex.getPropertyKeys().size(), 2);
                assertTrue(vertex.getPropertyKeys().contains(SailTokens.DATATYPE));
                assertEquals(vertex.getProperty(SailTokens.DATATYPE), "http://www.w3.org/2001/XMLSchema#anyURI");
                assertTrue(vertex.getPropertyKeys().contains(SailTokens.VALUE));

            }
        }
    }

    public void testRemoveVertexProperties(final Graph graph) {

        if (!config.isRDFModel) {
            Random random = new Random();
            int id1 = Math.abs(random.nextInt() - 1);
            int id2 = id1 + 1;

            Vertex v1 = graph.addVertex("" + id1);
            Vertex v2 = graph.addVertex("" + id2);
            v1.setProperty("key1", "value1");
            v1.setProperty("key2", 10);
            v2.setProperty("key2", 20);

            assertEquals(v1.removeProperty("key1"), "value1");
            assertEquals(v1.removeProperty("key2"), 10);
            assertEquals(v2.removeProperty("key2"), 20);

            assertNull(v1.removeProperty("key1"));
            assertNull(v1.removeProperty("key2"));
            assertNull(v2.removeProperty("key2"));

            v1.setProperty("key1", "value1");
            v1.setProperty("key2", 10);
            v2.setProperty("key2", 20);

            if (!config.ignoresSuppliedIds) {
                v1 = graph.getVertex("" + id1);
                v2 = graph.getVertex("" + id2);

                assertEquals(v1.removeProperty("key1"), "value1");
                assertEquals(v1.removeProperty("key2"), 10);
                assertEquals(v2.removeProperty("key2"), 20);

                assertNull(v1.removeProperty("key1"));
                assertNull(v1.removeProperty("key2"));
                assertNull(v2.removeProperty("key2"));

                v1 = graph.getVertex("" + id1);
                v2 = graph.getVertex("" + id2);

                v1.setProperty("key1", "value2");
                v1.setProperty("key2", 20);
                v2.setProperty("key2", 30);

                assertEquals(v1.removeProperty("key1"), "value2");
                assertEquals(v1.removeProperty("key2"), 20);
                assertEquals(v2.removeProperty("key2"), 30);

                assertNull(v1.removeProperty("key1"));
                assertNull(v1.removeProperty("key2"));
                assertNull(v2.removeProperty("key2"));
            }


        } else {
            Vertex v1 = graph.addVertex("\"1\"^^<http://www.w3.org/2001/XMLSchema#int>");
            assertEquals(v1.removeProperty("type"), "http://www.w3.org/2001/XMLSchema#int");
            assertEquals(v1.getProperty("value"), "1");
            assertNull(v1.getProperty("lang"));
            assertNull(v1.getProperty("random something"));

            Vertex v2 = graph.addVertex("\"hello\"@en");
            assertEquals(v2.removeProperty("lang"), "en");
            assertEquals(v2.getProperty("value"), "hello");
            assertNull(v2.getProperty("type"));
            assertNull(v2.getProperty("random something"));
        }
    }

    public void testVertexPropertyInconsistency(final Graph graph) {
        if (!config.isRDFModel) {
            Random random = new Random();
            String id = "" + Math.abs(random.nextInt());
            Vertex v1 = graph.addVertex(convertId(id));
            v1.setProperty("key1", "value1");
            if (config.supportsVertexIteration) {
                assertEquals(count(graph.getVertices()), 1);
            }
            assertEquals(v1.getProperty("key1"), "value1");

            Vertex v2 = graph.getVertex(v1.getId());
            assertEquals(v2.getProperty("key1"), "value1");

            v1.setProperty("key1", "value111");
            assertEquals(v1.getProperty("key1"), "value111");
            assertEquals(v2.getProperty("key1"), "value111");

            assertEquals(v2.removeProperty("key1"), "value111");
            assertNull(v2.getProperty("key1"));
            assertNull(v1.getProperty("key1"));
        }
    }

}
