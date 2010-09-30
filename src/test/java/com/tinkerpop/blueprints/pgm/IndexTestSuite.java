package com.tinkerpop.blueprints.pgm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IndexTestSuite extends ModelTestSuite {

    public IndexTestSuite() {
    }

    public IndexTestSuite(final SuiteConfiguration config) {
        super(config);
    }


    public void testVertexIndexRemove(final Graph graph) {
        if (config.supportsVertexIndex) {
            List<String> ids = generateIds(2);
            Vertex v1 = graph.addVertex(ids.get(0));
            Vertex v2 = graph.addVertex(ids.get(1));

            v1.setProperty("name", "marko");
            assertEquals(count(graph.getIndex().get("name", "marko")), 1);
            assertEquals(graph.getIndex().get("name", "marko").iterator().next(), v1);
            v1.setProperty("name", "marko a. rodriguez");
            assertNull(graph.getIndex().get("name", "marko"));
            assertEquals(graph.getIndex().count("name", "marko"), 0);
            assertEquals(count(graph.getIndex().get("name", "marko a. rodriguez")), 1);
            assertEquals(graph.getIndex().count("name", "marko a. rodriguez"), 1);

            assertNull(graph.getIndex().get("name", "jen"));
            v2.setProperty("name", "jen");
            assertEquals(count(graph.getIndex().get("name", "jen")), 1);
            assertEquals(graph.getIndex().get("name", "jen").iterator().next(), v2);

            v1.setProperty("location", 87501);
            v2.setProperty("location", 87501);
            assertEquals(count(graph.getIndex().get("location", 87501)), 2);
            v1.removeProperty("location");
            assertEquals(count(graph.getIndex().get("location", 87501)), 1);
            v2.removeProperty("location");
            assertNull(graph.getIndex().get("location", 87501));
        }
    }

    public void testVertexIndexAdding(final Graph graph) {
        if (config.supportsVertexIndex && !config.isRDFModel) {
            Set<Vertex> vertices = new HashSet<Vertex>();
            for (int i = 0; i < 10; i++) {
                Vertex vertex = graph.addVertex(null);
                vertex.setProperty("key1", "value1");
                vertex.setProperty("key2", "value2");
                vertices.add(vertex);
            }
            assertEquals(vertices.size(), 10);
            if (config.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 10);

            assertEquals(count(graph.getIndex().get("key1", "value1")), 10);
            assertEquals(count(graph.getIndex().get("key2", "value2")), 10);
            assertNull(graph.getIndex().get("key3", "value3"));

            for (Element element : graph.getIndex().get("key1", "value1")) {
                assertTrue(vertices.contains(element));
            }

            for (Element element : graph.getIndex().get("key2", "value2")) {
                assertTrue(vertices.contains(element));
            }

            for (int i = 0; i < 10; i++) {
                Vertex vertex = graph.addVertex(null);
                vertex.setProperty("key3", "value3");
                vertices.add(vertex);
            }

            assertEquals(count(graph.getIndex().get("key1", "value1")), 10);
            assertEquals(count(graph.getIndex().get("key2", "value2")), 10);
            assertEquals(count(graph.getIndex().get("key3", "value3")), 10);

            for (Element element : graph.getIndex().get("key1", "value1")) {
                assertTrue(vertices.contains(element));
            }

            for (Element element : graph.getIndex().get("key2", "value2")) {
                assertTrue(vertices.contains(element));
            }

            for (Element element : graph.getIndex().get("key3", "value3")) {
                assertTrue(vertices.contains(element));
            }
        }
    }

    public void testEdgeIndexAdding(final Graph graph) {
        if (config.supportsEdgeIndex && !config.isRDFModel) {
            Set<Edge> edges = new HashSet<Edge>();
            for (int i = 0; i < 10; i++) {
                Edge edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "test1");
                edge.setProperty("key1", "value1");
                edge.setProperty("key2", "value2");
                edges.add(edge);
            }
            assertEquals(edges.size(), 10);
            if (config.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 20);
            if (config.supportsEdgeIteration) {
                assertEquals(count(graph.getEdges()), 10);
                for (Edge edge : graph.getEdges()) {
                    assertEquals(edge.getLabel(), "test1");
                }
            }


            assertEquals(count(graph.getIndex().get("key1", "value1")), 10);
            assertEquals(count(graph.getIndex().get("key2", "value2")), 10);
            assertNull(graph.getIndex().get("key3", "value3"));

            for (Element element : graph.getIndex().get("key1", "value1")) {
                assertTrue(edges.contains(element));
            }

            for (Element element : graph.getIndex().get("key2", "value2")) {
                assertTrue(edges.contains(element));
            }

            for (int i = 0; i < 10; i++) {
                Edge edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "test2");
                edge.setProperty("key3", "value3");
                edges.add(edge);
            }

            assertEquals(count(graph.getIndex().get("key1", "value1")), 10);
            assertEquals(count(graph.getIndex().get("key2", "value2")), 10);
            assertEquals(count(graph.getIndex().get("key3", "value3")), 10);

            for (Element element : graph.getIndex().get("key1", "value1")) {
                assertTrue(edges.contains(element));
            }

            for (Element element : graph.getIndex().get("key2", "value2")) {
                assertTrue(edges.contains(element));
            }

            for (Element element : graph.getIndex().get("key3", "value3")) {
                assertTrue(edges.contains(element));
            }
        }
    }
}
