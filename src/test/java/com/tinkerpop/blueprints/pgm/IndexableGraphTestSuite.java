package com.tinkerpop.blueprints.pgm;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IndexableGraphTestSuite extends ModelTestSuite {

    public IndexableGraphTestSuite() {
    }

    public IndexableGraphTestSuite(final SuiteConfiguration config) {
        super(config);
    }

    public void testAssignableClasses(final IndexableGraph graph) {
        assertTrue(Element.class.isAssignableFrom(Vertex.class));
        assertTrue(Element.class.isAssignableFrom(Element.class));
    }

    public void testVertexAutoIndexAllKeys(final IndexableGraph graph) {
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

            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key1", "value1")), 10);
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key2", "value2")), 10);
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key3", "value3")), 0);

            for (Vertex vertex : graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key1", "value1")) {
                assertTrue(vertices.contains(vertex));
            }

            for (Vertex vertex : graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key2", "value2")) {
                assertTrue(vertices.contains(vertex));
            }

            for (int i = 0; i < 10; i++) {
                Vertex vertex = graph.addVertex(null);
                vertex.setProperty("key3", "value3");
                vertices.add(vertex);
            }

            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key1", "value1")), 10);
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key2", "value2")), 10);
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key3", "value3")), 10);

            for (Vertex vertex : graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key1", "value1")) {
                assertTrue(vertices.contains(vertex));
            }

            for (Vertex vertex : graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key2", "value2")) {
                assertTrue(vertices.contains(vertex));
            }

            for (Vertex vertex : graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key3", "value3")) {
                assertTrue(vertices.contains(vertex));
            }

            for(Vertex vertex : vertices) {
                vertex.removeProperty("key1");
                vertex.removeProperty("key3");
            }

            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key1", "value1")), 0);
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key2", "value2")), 10);
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("key3", "value3")), 0);

        }
    }

    public void testVertexAutoIndexSpecificKeys(final IndexableGraph graph) {
        if (config.supportsVertexIndex) {
            Vertex v1 = graph.addVertex(null);
            ((AutomaticIndex) graph.getIndex(IndexableGraph.VERTICES, Vertex.class)).addAutoIndexKey("name");
            v1.setProperty("name", "marko");
            v1.setProperty("location", 87506);
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("name", "marko")), 1);
            assertEquals(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("name", "marko").iterator().next(), v1);
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("location", 87506)), 0);
            v1.setProperty("name", "luca");
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("name", "marko")), 0);
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("name", "luca")), 1);
            assertEquals(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("name", "luca").iterator().next(), v1);
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("location", 87506)), 0);

            graph.removeVertex(v1);
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("name", "marko")), 0);
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("name", "luca")), 0);
            assertEquals(count(graph.getIndex(IndexableGraph.VERTICES, Vertex.class).get("location", 87506)), 0);
        }
    }

    public void testEdgeIndexAdding(final IndexableGraph graph) {
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


            assertEquals(count(graph.getIndex(IndexableGraph.EDGES, Edge.class).get("key1", "value1")), 10);
            assertEquals(count(graph.getIndex(IndexableGraph.EDGES, Edge.class).get("key2", "value2")), 10);
            assertEquals(count(graph.getIndex(IndexableGraph.EDGES, Edge.class).get("key3", "value3")), 0);

            for (Edge edge : graph.getIndex(IndexableGraph.EDGES, Edge.class).get("key1", "value1")) {
                assertTrue(edges.contains(edge));
            }

            for (Edge edge : graph.getIndex(IndexableGraph.EDGES, Edge.class).get("key2", "value2")) {
                assertTrue(edges.contains(edge));
            }

            for (int i = 0; i < 10; i++) {
                Edge edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "test2");
                edge.setProperty("key3", "value3");
                edges.add(edge);
            }

            assertEquals(count(graph.getIndex(IndexableGraph.EDGES, Edge.class).get("key1", "value1")), 10);
            assertEquals(count(graph.getIndex(IndexableGraph.EDGES, Edge.class).get("key2", "value2")), 10);
            assertEquals(count(graph.getIndex(IndexableGraph.EDGES, Edge.class).get("key3", "value3")), 10);

            for (Edge edge : graph.getIndex(IndexableGraph.EDGES, Edge.class).get("key1", "value1")) {
                assertTrue(edges.contains(edge));
            }

            for (Edge edge : graph.getIndex(IndexableGraph.EDGES, Edge.class).get("key2", "value2")) {
                assertTrue(edges.contains(edge));
            }

            for (Edge edge : graph.getIndex(IndexableGraph.EDGES, Edge.class).get("key3", "value3")) {
                assertTrue(edges.contains(edge));
            }
        }
    }
}
