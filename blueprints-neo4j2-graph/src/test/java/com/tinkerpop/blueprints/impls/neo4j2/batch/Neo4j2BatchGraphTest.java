package com.tinkerpop.blueprints.impls.neo4j2.batch;

import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.util.PropertyFilteredIterable;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import org.neo4j.index.impl.lucene.LowerCaseKeywordAnalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4j2BatchGraphTest extends BaseTest {

    public void testFeatureCompliance() {
        final String directory = this.getWorkingDirectory();
        final Neo4j2BatchGraph batch = new Neo4j2BatchGraph(directory);
        System.out.println(batch.getFeatures());
        batch.getFeatures().checkCompliance();
        batch.shutdown();
    }

    public void testAddingVerticesEdges() {
        final String directory = this.getWorkingDirectory();
        final Neo4j2BatchGraph batch = new Neo4j2BatchGraph(directory);
        assertEquals(count(batch.getIndices()), 0); // no indices created
        final List<Long> ids = new ArrayList<Long>();
        for (int i = 0; i < 10; i++) {
            ids.add((Long) batch.addVertex(null).getId());
        }
        //System.out.println(ids);
        for (int i = 1; i < ids.size(); i++) {
            long idA = ids.get(i - 1);
            long idB = ids.get(i);
            //System.out.println(batch.getVertex(idA) + "--" + batch.getVertex(idB));
            batch.addEdge(null, batch.getVertex(idA), batch.getVertex(idB), idA + "-" + idB);
        }
        batch.shutdown();

        // native neo4j graph load

        final Neo4j2Graph graph = new Neo4j2Graph(directory);
        graph.autoStartTransaction(true);
        assertEquals(count(graph.getVertices()), 10);

        assertEquals(count(graph.getEdges()), 9);
        for (final Edge edge : graph.getEdges()) {
            long idA = (Long) edge.getVertex(Direction.OUT).getId();
            long idB = (Long) edge.getVertex(Direction.IN).getId();
            assertEquals(idA + 1, idB);
            assertEquals(edge.getLabel(), idA + "-" + idB);
        }

        assertNotNull(graph.getVertex(1L));
        assertNull(graph.getVertex(100L));
        assertNull(graph.getEdge(100L));
        graph.shutdown();
    }

    public void testAddingVerticesWithUserIdsThenSettingProperties() {
        List<Long> ids = new ArrayList<Long>(Arrays.asList(100L, 5L, 10L, 4L, 10000L));
        final String directory = this.getWorkingDirectory();
        final Neo4j2BatchGraph batch = new Neo4j2BatchGraph(directory);
        for (final Long id : ids) {
            Vertex v = batch.addVertex(id);
            v.setProperty("theKey", id);
        }
        for (final Long id : ids) {
            assertNotNull(batch.getVertex(id));
            assertEquals(batch.getVertex(id).getProperty("theKey"), id);
        }
        assertNull(batch.getVertex(1L));
        assertNull(batch.getVertex(2L));
        assertNull(batch.getVertex(200000L));
        batch.shutdown();

        // native neo4j graph load

        final Neo4j2Graph graph = new Neo4j2Graph(directory);
        graph.autoStartTransaction(true);
        assertEquals(count(graph.getVertices()), ids.size());
        for (final Long id : ids) {
            assertNotNull(graph.getVertex(id));
            assertEquals(graph.getVertex(id).getProperty("theKey"), id);
            assertEquals(graph.getVertex(id).getPropertyKeys().size(), 1);
        }
        assertNull(graph.getVertex(1L));
        assertNull(graph.getVertex(2L));
        assertNull(graph.getVertex(200000L));
        graph.shutdown();
    }

    public void testAddingVerticesWithUserIdsAsStringsThenSettingProperties() {
        List<Object> ids = new ArrayList<Object>(Arrays.asList(100L, 5.0d, "10", 4.0f, "10000.00"));
        final String directory = this.getWorkingDirectory();
        final Neo4j2BatchGraph batch = new Neo4j2BatchGraph(directory);
        for (final Object id : ids) {
            Vertex v = batch.addVertex(id);
            v.setProperty("theKey", id);
        }
        for (final Object id : ids) {
            assertNotNull(batch.getVertex(id));
            assertEquals(batch.getVertex(id).getProperty("theKey"), id);
        }
        assertNull(batch.getVertex(1L));
        assertNull(batch.getVertex(2L));
        assertNull(batch.getVertex(200000L));
        batch.shutdown();

        // native neo4j graph load

        final Neo4j2Graph graph = new Neo4j2Graph(directory);
        graph.autoStartTransaction(true);
        assertEquals(count(graph.getVertices()), ids.size());
        assertNotNull(graph.getVertex(100l));
        assertNotNull(graph.getVertex(5l));
        assertNotNull(graph.getVertex(10l));
        assertNotNull(graph.getVertex(4l));
        assertNotNull(graph.getVertex(10000));

        graph.shutdown();

    }

    public void testAddingVerticesEdgesWithIndices() {
        final String directory = this.getWorkingDirectory();
        final Neo4j2BatchGraph batch = new Neo4j2BatchGraph(directory);
        assertEquals(0, count(batch.getIndices()));
        batch.createKeyIndex("name", Vertex.class);
        batch.createKeyIndex("age", Vertex.class);
        Index<Edge> edgeIndex = batch.createIndex("edgeIdx", Edge.class);
        assertEquals(1, count(batch.getIndices()));

        for (final Index index : batch.getIndices()) {
            if (index.getIndexName().equals("edgeIdx")) {
                assertEquals(index.getIndexClass(), Edge.class);
            } else {
                throw new RuntimeException("There should not be another index.");
            }
        }

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

        // native neo4j graph load

        final Neo4j2Graph graph = new Neo4j2Graph(directory);
        graph.autoStartTransaction(true);

        assertEquals(count(graph.getIndices()), 1);

        assertEquals(graph.getIndexedKeys(Vertex.class).size(), 2);
        assertTrue(graph.getIndexedKeys(Vertex.class).contains("name"));
        assertTrue(graph.getIndexedKeys(Vertex.class).contains("age"));
        edgeIndex = graph.getIndex("edgeIdx", Edge.class);
        assertEquals(edgeIndex.getIndexClass(), Edge.class);

        assertEquals(count(graph.getVertices()), 10);

        assertTrue(graph.getVertices("nothing", 0) instanceof PropertyFilteredIterable);
        assertTrue(graph.getVertices("blah", "blop") instanceof PropertyFilteredIterable);
        assertFalse(graph.getVertices("name", "marko") instanceof PropertyFilteredIterable); // key index used
        assertFalse(graph.getVertices("age", 32) instanceof PropertyFilteredIterable); // key indexed used

        for (final Vertex vertex : graph.getVertices()) {
            int age = (Integer) vertex.getProperty("age");
            assertEquals(vertex.getProperty("name"), (age / 10) + "");

            assertTrue(graph.getVertices("nothing", 0).iterator().hasNext());
            assertEquals(count(graph.getVertices("age", age)), 1);
            assertEquals(graph.getVertices("age", age).iterator().next(), vertex);
            assertEquals(count(graph.getVertices("name", (age / 10) + "")), 1);
            assertEquals(graph.getVertices("name", (age / 10) + "").iterator().next(), vertex);
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
            long idA = (Long) edge.getVertex(Direction.OUT).getId();
            long idB = (Long) edge.getVertex(Direction.IN).getId();
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
        final Neo4j2BatchGraph batch = new Neo4j2BatchGraph(directory);
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

        // native neo4j graph load

        Neo4j2Graph graph = new Neo4j2Graph(directory);
        graph.autoStartTransaction(true);
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
        final Neo4j2BatchGraph batch = new Neo4j2BatchGraph(directory);
        System.out.println(batch.createIndex("anIdx", Vertex.class));
        System.out.println(batch.addVertex(null));
        System.out.println(batch.addEdge(null, batch.addVertex(null), batch.addVertex(null), "label"));
        batch.shutdown();
    }

    public void testGraphMLLoad() throws Exception {
        final String directory = this.getWorkingDirectory();
        final Neo4j2BatchGraph batch = new Neo4j2BatchGraph(directory);
        new GraphMLReader(batch).inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-1.xml"));
        assertNotNull(batch.getVertex(1));
        assertNotNull(batch.getVertex(2));
        assertNotNull(batch.getVertex(3));
        assertNotNull(batch.getVertex(4));
        assertNotNull(batch.getVertex(5));
        assertNotNull(batch.getVertex(6));
        assertNull(batch.getVertex(7));
        assertEquals(batch.getVertex(1).getProperty("name"), "marko");
        assertEquals(batch.getVertex(2).getProperty("name"), "vadas");
        assertEquals(batch.getVertex(3).getProperty("name"), "lop");
        assertEquals(batch.getVertex(4).getProperty("name"), "josh");
        assertEquals(batch.getVertex(5).getProperty("name"), "ripple");
        assertEquals(batch.getVertex(6).getProperty("name"), "peter");
        batch.shutdown();

        // native neo4j graph load

        Neo4j2Graph graph = new Neo4j2Graph(directory);
        graph.autoStartTransaction(true);
        assertEquals(count(graph.getVertices()), 6);
        assertEquals(count(graph.getEdges()), 6);
        assertEquals(count(graph.getVertex("1").getEdges(Direction.OUT)), 3);
        assertEquals(count(graph.getVertex("1").getEdges(Direction.IN)), 0);
        Vertex marko = graph.getVertex("1");
        assertEquals(marko.getProperty("name"), "marko");
        assertEquals(marko.getProperty("age"), 29);
        int counter = 0;

        assertEquals(count(graph.getVertex("4").getEdges(Direction.OUT)), 2);
        assertEquals(count(graph.getVertex("4").getEdges(Direction.IN)), 1);
        Vertex josh = graph.getVertex("4");
        assertEquals(josh.getProperty("name"), "josh");
        assertEquals(josh.getProperty("age"), 32);
        for (Edge e : graph.getVertex("4").getEdges(Direction.OUT)) {
            if (e.getVertex(Direction.IN).getId().equals(3l)) {
                assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                assertEquals(e.getLabel(), "created");
                counter++;
            } else if (e.getVertex(Direction.IN).getId().equals(5l)) {
                assertEquals(Math.round((Float) e.getProperty("weight")), 1);
                assertEquals(e.getLabel(), "created");
                counter++;
            }
        }
        assertEquals(counter, 2);
        graph.shutdown();
    }

    public void testIndexParameters() throws Exception {
        final String directory = this.getWorkingDirectory();
        final Neo4j2BatchGraph batch = new Neo4j2BatchGraph(directory);
        Index<Vertex> index = batch.createIndex("testIdx", Vertex.class, new Parameter("analyzer", LowerCaseKeywordAnalyzer.class.getName()));
        Vertex a = batch.addVertex(null);
        a.setProperty("name", "marko");
        index.put("name", "marko", a);
        batch.flushIndices();
        batch.shutdown();

        // native neo4j graph load

        Neo4j2Graph graph = new Neo4j2Graph(directory);
        graph.autoStartTransaction(true);
        Iterator<Vertex> itty = graph.getIndex("testIdx", Vertex.class).query("name", "*rko").iterator();
        int counter = 0;
        while (itty.hasNext()) {
            counter++;
            assertEquals(itty.next().getProperty("name"), "marko");
        }
        assertEquals(counter, 1);

        itty = graph.getIndex("testIdx", Vertex.class).query("name", "MaRkO").iterator();
        counter = 0;
        while (itty.hasNext()) {
            counter++;
            assertEquals(itty.next().getProperty("name"), "marko");
        }
        assertEquals(counter, 1);

        graph.shutdown();
    }

    private String getWorkingDirectory() {
        String directory = this.computeTestDataRoot().getAbsolutePath();
        deleteDirectory(new File(directory));
        return directory;
    }
}
