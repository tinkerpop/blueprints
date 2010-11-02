package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.BaseTest;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class AutomaticIndexTestSuite extends ModelTestSuite {

    public AutomaticIndexTestSuite() {
    }

    public AutomaticIndexTestSuite(final SuiteConfiguration config) {
        super(config);
    }

    public void testAutoIndexKeyManagement(IndexableGraph graph) {
        if (config.supportsVertexIndex) {
            AutomaticIndex index = (AutomaticIndex) graph.getIndex(Index.VERTICES, Vertex.class);
            assertNull(index.getAutoIndexKeys());

            this.stopWatch();
            index.addAutoIndexKey("name");
            index.addAutoIndexKey("location");
            BaseTest.printPerformance(graph.toString(), 2, "automatic index keys added", this.stopWatch());
            assertEquals(index.getAutoIndexKeys().size(), 2);

            this.stopWatch();
            index.addAutoIndexKey("name");
            index.addAutoIndexKey("location");
            BaseTest.printPerformance(graph.toString(), 2, "same automatic index keys added", this.stopWatch());
            assertEquals(index.getAutoIndexKeys().size(), 2);

            this.stopWatch();
            assertTrue(index.getAutoIndexKeys().contains("name"));
            assertTrue(index.getAutoIndexKeys().contains("location"));
            BaseTest.printPerformance(graph.toString(), 2, "automatic index keys retrieved", this.stopWatch());
        }
    }

    public void testAutoIndexPutGetRemoveVertex(IndexableGraph graph) {
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

            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("key1", "value1")), 10);
            BaseTest.printPerformance(graph.toString(), 10, "vertices retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("key2", "value2")), 10);
            BaseTest.printPerformance(graph.toString(), 10, "vertices retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("key3", "value3")), 0);
            BaseTest.printPerformance(graph.toString(), 0, "vertices retrieved from automatic index", this.stopWatch());

            for (Vertex vertex : graph.getIndex(Index.VERTICES, Vertex.class).get("key1", "value1")) {
                assertTrue(vertices.contains(vertex));
            }

            for (Vertex vertex : graph.getIndex(Index.VERTICES, Vertex.class).get("key2", "value2")) {
                assertTrue(vertices.contains(vertex));
            }

            // make sure the vertex is not indexed 'twice.'
            for (Vertex vertex : vertices) {
                vertex.setProperty("key1", "value1");
                vertex.setProperty("key2", "value2");
            }

            for (int i = 0; i < 10; i++) {
                Vertex vertex = graph.addVertex(null);
                vertex.setProperty("key3", "value3");
                vertices.add(vertex);
            }

            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("key1", "value1")), 10);
            BaseTest.printPerformance(graph.toString(), 10, "vertices retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("key2", "value2")), 10);
            BaseTest.printPerformance(graph.toString(), 10, "vertices retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("key3", "value3")), 10);
            BaseTest.printPerformance(graph.toString(), 10, "vertices retrieved from automatic index", this.stopWatch());

            for (Vertex vertex : graph.getIndex(Index.VERTICES, Vertex.class).get("key1", "value1")) {
                assertTrue(vertices.contains(vertex));
            }

            for (Vertex vertex : graph.getIndex(Index.VERTICES, Vertex.class).get("key2", "value2")) {
                assertTrue(vertices.contains(vertex));
            }

            for (Vertex vertex : graph.getIndex(Index.VERTICES, Vertex.class).get("key3", "value3")) {
                assertTrue(vertices.contains(vertex));
            }

            for (Vertex vertex : vertices) {
                vertex.removeProperty("key1");
                vertex.removeProperty("key3");
            }

            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("key1", "value1")), 0);
            BaseTest.printPerformance(graph.toString(), 0, "vertices retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("key2", "value2")), 10);
            BaseTest.printPerformance(graph.toString(), 10, "vertices retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("key3", "value3")), 0);
            BaseTest.printPerformance(graph.toString(), 0, "vertices retrieved from automatic index", this.stopWatch());
        }
    }

    public void testAutoIndexPutGetRemoveEdge(final IndexableGraph graph) {
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

            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("key1", "value1")), 10);
            BaseTest.printPerformance(graph.toString(), 10, "edges retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("key2", "value2")), 10);
            BaseTest.printPerformance(graph.toString(), 10, "edges retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("key3", "value3")), 0);
            BaseTest.printPerformance(graph.toString(), 0, "edges retrieved from automatic index", this.stopWatch());

            for (Edge edge : graph.getIndex(Index.EDGES, Edge.class).get("key1", "value1")) {
                assertTrue(edges.contains(edge));
            }

            for (Edge edge : graph.getIndex(Index.EDGES, Edge.class).get("key2", "value2")) {
                assertTrue(edges.contains(edge));
            }

            // make sure the edge is not indexed 'twice.'
            for (Edge edge : edges) {
                edge.setProperty("key1", "value1");
                edge.setProperty("key2", "value2");
            }

            for (int i = 0; i < 10; i++) {
                Edge edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "test2");
                edge.setProperty("key3", "value3");
                edges.add(edge);
            }

            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("key1", "value1")), 10);
            BaseTest.printPerformance(graph.toString(), 10, "edges retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("key2", "value2")), 10);
            BaseTest.printPerformance(graph.toString(), 10, "edges retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("key3", "value3")), 10);
            BaseTest.printPerformance(graph.toString(), 10, "edges retrieved from automatic index", this.stopWatch());

            for (Edge edge : graph.getIndex(Index.EDGES, Edge.class).get("key1", "value1")) {
                assertTrue(edges.contains(edge));
            }

            for (Edge edge : graph.getIndex(Index.EDGES, Edge.class).get("key2", "value2")) {
                assertTrue(edges.contains(edge));
            }

            for (Edge edge : graph.getIndex(Index.EDGES, Edge.class).get("key3", "value3")) {
                assertTrue(edges.contains(edge));
            }

            for (Edge edge : edges) {
                edge.removeProperty("key1");
                edge.removeProperty("key3");
            }

            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("key1", "value1")), 0);
            BaseTest.printPerformance(graph.toString(), 0, "edges retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("key2", "value2")), 10);
            BaseTest.printPerformance(graph.toString(), 10, "edges retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("key3", "value3")), 0);
            BaseTest.printPerformance(graph.toString(), 0, "edges retrieved from automatic index", this.stopWatch());
        }
    }

    public void testAutoIndexSpecificKeysVertex(final IndexableGraph graph) {
        if (config.supportsVertexIndex && !config.isRDFModel) {
            Vertex v1 = graph.addVertex(null);
            ((AutomaticIndex) graph.getIndex(Index.VERTICES, Vertex.class)).addAutoIndexKey("name");
            this.stopWatch();
            v1.setProperty("name", "marko");
            v1.setProperty("location", 87506);
            BaseTest.printPerformance(graph.toString(), 2, "properties added to vertex and automatic index", this.stopWatch());

            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 1);
            assertEquals(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko").iterator().next(), v1);
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("location", 87506)), 0);

            this.stopWatch();
            v1.setProperty("name", "luca");
            BaseTest.printPerformance(graph.toString(), 1, "properties updated on vertex and automatic index", this.stopWatch());
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 0);
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "luca")), 1);
            assertEquals(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "luca").iterator().next(), v1);
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("location", 87506)), 0);

            this.stopWatch();
            graph.removeVertex(v1);
            BaseTest.printPerformance(graph.toString(), 1, "vertex removed and from automatic index", this.stopWatch());
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 0);
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "luca")), 0);
            assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("location", 87506)), 0);
        }
    }

    public void testAutoIndexSpecificKeysEdge(final IndexableGraph graph) {
        if (config.supportsEdgeIndex && !config.isRDFModel) {
            Edge e1 = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "test");
            ((AutomaticIndex) graph.getIndex(Index.EDGES, Edge.class)).addAutoIndexKey("name");
            this.stopWatch();
            e1.setProperty("name", "marko");
            e1.setProperty("location", 87506);
            BaseTest.printPerformance(graph.toString(), 2, "properties added to edge and automatic index", this.stopWatch());

            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("name", "marko")), 1);
            assertEquals(graph.getIndex(Index.EDGES, Edge.class).get("name", "marko").iterator().next(), e1);
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("location", 87506)), 0);

            this.stopWatch();
            e1.setProperty("name", "luca");
            BaseTest.printPerformance(graph.toString(), 1, "properties updated on edge and automatic index", this.stopWatch());
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("name", "marko")), 0);
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("name", "luca")), 1);
            assertEquals(graph.getIndex(Index.EDGES, Edge.class).get("name", "luca").iterator().next(), e1);
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("location", 87506)), 0);

            this.stopWatch();
            graph.removeEdge(e1);
            BaseTest.printPerformance(graph.toString(), 1, "edge removed and from automatic index", this.stopWatch());
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("name", "marko")), 0);
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("name", "luca")), 0);
            assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get("location", 87506)), 0);
        }
    }


}
