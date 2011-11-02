package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4jbatch.Neo4jBatchGraph;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jBatchGraphTest extends BaseTest {

    public void testAddingVerticesEdges() {
        final String directory = this.getWorkingDirectory();
        final Neo4jBatchGraph batch = new Neo4jBatchGraph(directory);
        assertEquals(count(batch.getIndices()), 0); // no indices created
        final List<Long> ids = new ArrayList<Long>();
        for (int i = 0; i < 10; i++) {
            ids.add((Long) batch.addVertex(null).getId());
        }
        for (int i = 1; i < ids.size(); i++) {
            long idA = ids.get(i - 1);
            long idB = ids.get(i);
            batch.addEdge(null, batch.getVertex(idA), batch.getVertex(idB), idA + "-" + idB);
        }
        batch.shutdown();

        final Graph graph = new Neo4jGraph(directory);
        graph.removeVertex(graph.getVertex(0)); // remove reference node
        assertEquals(count(graph.getVertices()), 10);

        assertEquals(count(graph.getEdges()), 9);
        for (final Edge edge : graph.getEdges()) {
            long idA = (Long) edge.getOutVertex().getId();
            long idB = (Long) edge.getInVertex().getId();
            assertEquals(idA + 1, idB);
            assertEquals(edge.getLabel(), idA + "-" + idB);
        }

        assertNotNull(graph.getVertex(1L));
        assertNull(graph.getVertex(100L));
        assertNull(graph.getEdge(100L));

        graph.shutdown();
    }

    public void testAddingVerticesEdgesWithIndices() {
        final String directory = this.getWorkingDirectory();
        final Neo4jBatchGraph batch = new Neo4jBatchGraph(directory);
        assertEquals(0, count(batch.getIndices()));
        batch.createAutomaticIndex(Index.VERTICES, Vertex.class, new HashSet<String>(Arrays.asList("name", "age")));
        assertEquals(1, count(batch.getIndices()));
        Index<Edge> edgeIndex = batch.createManualIndex(Index.EDGES, Edge.class);
        assertEquals(2, count(batch.getIndices()));

        final List<Long> ids = new ArrayList<Long>();
        for (int i = 0; i < 10; i++) {
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", i + "");
            map.put("age", i * 10);
            map.put("nothing", 0);
            ids.add((Long) batch.addVertex(map).getId());
        }
        for (int i = 1; i < ids.size(); i++) {
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put("weight", 0.5f);
            long idA = ids.get(i - 1);
            long idB = ids.get(i);
            final Edge edge = batch.addEdge(map, batch.getVertex(idA), batch.getVertex(idB), idA + "-" + idB);
            edgeIndex.put("unique", idA + "-" + idB, edge);
            edgeIndex.put("full", "blah", edge);
        }
        batch.flushIndices();
        batch.shutdown();

        final IndexableGraph graph = new Neo4jGraph(directory);
        assertEquals(count(graph.getIndices()), 2);
        Index<Vertex> vertexIndex = graph.getIndex(Index.VERTICES, Vertex.class);
        edgeIndex = graph.getIndex(Index.EDGES, Edge.class);
        graph.removeVertex(graph.getVertex(0)); // remove reference node
        assertEquals(count(graph.getVertices()), 10);

        for (final Vertex vertex : graph.getVertices()) {
            int age = (Integer) vertex.getProperty("age");
            assertEquals(vertex.getProperty("name"), (age / 10) + "");

            assertEquals(vertexIndex.count("nothing", 0), 0);
            assertEquals(vertexIndex.count("age", age), 1);
            assertEquals(vertexIndex.get("age", age).iterator().next(), vertex);
            assertEquals(vertexIndex.count("name", (age / 10) + ""), 1);
            assertEquals(vertexIndex.get("name", (age / 10) + "").iterator().next(), vertex);
            assertEquals(vertex.getPropertyKeys().size(), 3);
            vertex.setProperty("NEW", age);
            assertEquals(vertex.getPropertyKeys().size(), 4);
        }

        for (final Vertex vertex : graph.getVertices()) {
            int age = (Integer) vertex.getProperty("age");
            assertEquals(vertex.getProperty("NEW"), age);
            assertEquals(vertex.getPropertyKeys().size(), 4);
            vertex.removeProperty("NEW");
        }

        for (final Vertex vertex : graph.getVertices()) {
            assertNull(vertex.getProperty("NEW"));
            assertEquals(vertex.getPropertyKeys().size(), 3);
        }

        assertEquals(count(graph.getEdges()), 9);
        assertEquals(count(edgeIndex.get("full", "blah")), 9);
        Set<Edge> edges = new HashSet<Edge>();
        for (Edge edge : edgeIndex.get("full", "blah")) {
            edges.add(edge);
        }
        assertEquals(edges.size(), 9);
        for (final Edge edge : graph.getEdges()) {
            long idA = (Long) edge.getOutVertex().getId();
            long idB = (Long) edge.getInVertex().getId();
            assertEquals(idA + 1, idB);
            assertEquals(edge.getLabel(), idA + "-" + idB);
            assertEquals(edge.getPropertyKeys().size(), 1);
            assertEquals(edge.getProperty("weight"), 0.5f);

            assertEquals(edgeIndex.count("weight", 0.5f), 0);
            assertEquals(edgeIndex.count("unique", idA + "-" + idB), 1);
            assertEquals(edgeIndex.get("unique", idA + "-" + idB).iterator().next(), edge);
            assertTrue(edges.contains(edge));
        }

        graph.shutdown();
    }

    public void testElementPropertyManipulation() {
        final String directory = this.getWorkingDirectory();
        final Neo4jBatchGraph batch = new Neo4jBatchGraph(directory);
        List<Long> vertexIds = new ArrayList<Long>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("a", 1);
            map.put("b", 2);
            Vertex vertex = batch.addVertex(map);
            assertEquals(vertex.getProperty("a"), 1);
            assertEquals(vertex.getProperty("b"), 2);
            assertEquals(vertex.getPropertyKeys().size(), 2);
            vertex.setProperty("b", 3);
            vertex.setProperty("c", 4);
            assertEquals(vertex.getProperty("a"), 1);
            assertEquals(vertex.getProperty("b"), 3);
            assertEquals(vertex.getProperty("c"), 4);
            assertEquals(vertex.getPropertyKeys().size(), 3);
            assertEquals(vertex.removeProperty("a"), 1);
            assertNull(vertex.getProperty("a"));
            assertEquals(vertex.getProperty("b"), 3);
            assertEquals(vertex.getProperty("c"), 4);
            assertEquals(vertex.getPropertyKeys().size(), 2);
            vertexIds.add((Long) vertex.getId());
        }
        assertEquals(vertexIds.size(), 10);

        List<Long> edgeIds = new ArrayList<Long>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            Edge edge = batch.addEdge(null, batch.getVertex(vertexIds.get(random.nextInt(vertexIds.size()))), batch.getVertex(vertexIds.get(random.nextInt(vertexIds.size()))), "test");
            edgeIds.add((Long) edge.getId());
            assertEquals(edge.getPropertyKeys().size(), 0);
            edge.setProperty("weight", 0.5);
            assertEquals(edge.getProperty("weight"), 0.5);
            assertEquals(edge.getPropertyKeys().size(), 1);
            edge.setProperty("blah", "dah");
            assertEquals(edge.getPropertyKeys().size(), 2);
            assertEquals(edge.getProperty("blah"), "dah");
            edge.setProperty("blah", "blue");
            assertEquals(edge.getPropertyKeys().size(), 2);
            assertEquals(edge.getProperty("blah"), "blue");
            assertEquals(edge.removeProperty("blah"), "blue");
            assertEquals(edge.getPropertyKeys().size(), 1);
        }

        batch.shutdown();

        Neo4jGraph graph = new Neo4jGraph(directory);
        graph.removeVertex(graph.getVertex(0)); // remove reference vertex
        assertEquals(count(graph.getVertices()), 10);
        for (final Long id : vertexIds) {
            Vertex vertex = graph.getVertex(id);
            assertNull(vertex.getProperty("a"));
            assertEquals(vertex.getProperty("b"), 3);
            assertEquals(vertex.getProperty("c"), 4);
            assertEquals(vertex.getPropertyKeys().size(), 2);
        }

        for (final Long id : edgeIds) {
            Edge edge = graph.getEdge(id);
            assertNull(edge.getProperty("blah"));
            assertEquals(edge.getPropertyKeys().size(), 1);
            assertEquals(edge.getProperty("weight"), 0.5);
        }
        graph.shutdown();

    }

    public void testToStringMethods() {
        final String directory = this.getWorkingDirectory();
        final Neo4jBatchGraph batch = new Neo4jBatchGraph(directory);
        System.out.println(batch.createManualIndex("manual", Vertex.class));
        System.out.println(batch.createAutomaticIndex("automatic", Edge.class, new HashSet<String>(Arrays.asList("key1", "key2"))));
        System.out.println(batch.addVertex(null));
        System.out.println(batch.addEdge(null, batch.addVertex(null), batch.addVertex(null), "label"));
        batch.shutdown();
    }

    private String getWorkingDirectory() {
        String directory = System.getProperty("neo4jBatchGraphDirectory");
        if (directory == null) {
            if (System.getProperty("os.name").toUpperCase().contains("WINDOWS"))
                directory = "C:/temp/blueprints_test";
            else
                directory = "/tmp/blueprints_test";
        }
        deleteDirectory(new File(directory));
        return directory;
    }
}
