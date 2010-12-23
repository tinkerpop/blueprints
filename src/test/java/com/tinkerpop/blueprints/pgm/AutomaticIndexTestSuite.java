package com.tinkerpop.blueprints.pgm;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.junit.Assert.assertThat;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class AutomaticIndexTestSuite extends TestSuite {

    public AutomaticIndexTestSuite() {
    }

    public AutomaticIndexTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testAutoIndexKeyManagement() {
        IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
        if (graphTest.supportsVertexIndex) {
            AutomaticIndex index = (AutomaticIndex) graph.getIndex(Index.VERTICES, Vertex.class);

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
        graph.shutdown();
    }

    public void testAutoIndexPutGetRemoveVertex() {
        if (graphTest.supportsVertexIndex && !graphTest.isRDFModel) {
            IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
            Set<Vertex> vertices = new HashSet<Vertex>();
            for (int i = 0; i < 10; i++) {
                Vertex vertex = graph.addVertex(null);
                vertex.setProperty("key1", "value1");
                vertex.setProperty("key2", "value2");
                vertices.add(vertex);
            }
            assertEquals(vertices.size(), 10);
            if (graphTest.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 10);

            AutomaticIndex<Vertex> index = (AutomaticIndex) graph.getIndex(Index.VERTICES, Vertex.class);

            assertThat(index.getAutoIndexKeys(), hasItems("key1", "key2"));
            assertThat(index.getAutoIndexKeys(), not(hasItem("key3")));
            this.stopWatch();
            assertEquals(10, count(index.get("key1", "value1")));
            BaseTest.printPerformance(graph.toString(), 10, "vertices retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(10, count(index.get("key2", "value2")));
            BaseTest.printPerformance(graph.toString(), 10, "vertices retrieved from automatic index", this.stopWatch());
            this.stopWatch();
            assertEquals(0, count(index.get("key3", "value3")));
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
            graph.shutdown();
        }
    }

    public void testAutoIndexPutGetRemoveEdge() {
        IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
        if (graphTest.supportsEdgeIndex && !graphTest.isRDFModel) {
            Set<Edge> edges = new HashSet<Edge>();
            for (int i = 0; i < 10; i++) {
                Edge edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "test1");
                edge.setProperty("key1", "value1");
                edge.setProperty("key2", "value2");
                edges.add(edge);
            }
            assertEquals(edges.size(), 10);
            if (graphTest.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 20);
            if (graphTest.supportsEdgeIteration) {
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
        graph.shutdown();
    }

    public void testAutoIndexSpecificKeysVertex() {
        IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
        if (graphTest.supportsVertexIndex && !graphTest.isRDFModel) {
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
        graph.shutdown();
    }

    public void testAutoIndexSpecificKeysEdge() {
        IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
        if (graphTest.supportsEdgeIndex && !graphTest.isRDFModel) {
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
        graph.shutdown();
    }

    public void testAutomaticIndexKeysPersistent() {
        if (graphTest.isPersistent && graphTest.supportsVertexIndex && !graphTest.isRDFModel) {
            IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
            graph.dropIndex(Index.EDGES);
            AutomaticIndex<Vertex> index = (AutomaticIndex) graph.getIndex(Index.VERTICES, Vertex.class);
            graph.shutdown();

            graph = (IndexableGraph) graphTest.getGraphInstance();
            index = (AutomaticIndex) graph.getIndex(Index.VERTICES, Vertex.class);
            index.addAutoIndexKey("name");
            assertEquals(index.getAutoIndexKeys().size(), 1);
            assertThat(index.getAutoIndexKeys(), hasItem("name"));
            graph.shutdown();

            graph = (IndexableGraph) graphTest.getGraphInstance();
            index = (AutomaticIndex) graph.getIndex(Index.VERTICES, Vertex.class);
            assertEquals(1, index.getAutoIndexKeys().size());
            assertThat(index.getAutoIndexKeys(), hasItem("name"));
            Vertex vertex = graph.addVertex(null);
            vertex.setProperty("name", "marko");
            vertex.setProperty("location", "santa fe");
            assertThat(index.getAutoIndexKeys(), not(hasItem("location")));
            if (graphTest.supportsVertexIteration)
                assertEquals(1, count(graph.getVertices()));
            assertEquals(1, count(index.get("name", "marko")));
            assertEquals(0, count(index.get("location", "santa fe")));
            if (graphTest.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 1);
            graph.shutdown();

            graph = (IndexableGraph) graphTest.getGraphInstance();
            if (graphTest.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 1);
            index = (AutomaticIndex) graph.getIndex(Index.VERTICES, Vertex.class);
            assertEquals(index.getAutoIndexKeys().size(), 1);
            assertTrue(index.getAutoIndexKeys().contains("name"));
            assertEquals(count(index.get("name", "marko")), 1);
            assertEquals(count(index.get("location", "santa fe")), 0);
            graph.shutdown();
        }
    }

    public void testEdgeLabelIndexing() {
        IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
        AutomaticIndex<Edge> edgeIndex = (AutomaticIndex<Edge>) graph.getIndex(Index.EDGES, Edge.class);
        Edge edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "knows");
        assertThat(edgeIndex.getAutoIndexKeys(), hasItem(AutomaticIndex.LABEL));
        assertThat(asList(edgeIndex.get(AutomaticIndex.LABEL, "knows")), hasItem(edge));

        Set<Object> edgeKnowsIds = new HashSet<Object>();
        edgeKnowsIds.add(edge.getId());
        for (int i = 0; i < 9; i++) {
            edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "knows");
            edgeKnowsIds.add(edge.getId());
        }

        Set<Object> edgeHatesIds = new HashSet<Object>();
        for (int i = 0; i < 10; i++) {
            edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "hates");
            edgeHatesIds.add(edge.getId());
        }

        int counter = 0;
        for (Edge e : edgeIndex.get(AutomaticIndex.LABEL, "knows")) {
            assertTrue(edgeKnowsIds.contains(e.getId()));
            counter++;
        }
        assertEquals(counter, 10);

        counter = 0;
        for (Edge e : edgeIndex.get(AutomaticIndex.LABEL, "hates")) {
            assertTrue(edgeHatesIds.contains(e.getId()));
            counter++;
        }
        assertEquals(counter, 10);

        Set<Object> edgeRemoveKnowsIds = new HashSet<Object>();
        counter = 0;
        for (Object id : edgeKnowsIds) {
            if (counter % 2 == 0) {
                edgeRemoveKnowsIds.add(id);
                graph.removeEdge(graph.getEdge(id));
            }
            counter++;
        }
        Set<Object> edgeRemoveHatesIds = new HashSet<Object>();
        counter = 0;
        for (Object id : edgeHatesIds) {
            if (counter % 2 == 0) {
                edgeRemoveHatesIds.add(id);
                graph.removeEdge(graph.getEdge(id));
            }
            counter++;
        }

        counter = 0;
        for (Edge e : edgeIndex.get(AutomaticIndex.LABEL, "knows")) {
            assertThat(edgeKnowsIds, hasItem(e.getId()));
            assertThat(edgeRemoveKnowsIds, not(hasItem(e.getId())));
            counter++;
        }
        assertThat(counter, is(5));

        counter = 0;
        for (Edge e : edgeIndex.get(AutomaticIndex.LABEL, "hates")) {
            assertThat(edgeHatesIds, hasItem(e.getId()));
            assertThat(edgeRemoveHatesIds, not(hasItem(e.getId())));
            counter++;
        }
        assertThat(counter, is(5));

        graph.shutdown();
    }
}
