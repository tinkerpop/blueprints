package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IndexableGraphTestSuite extends TestSuite {

    public IndexableGraphTestSuite() {
    }

    public IndexableGraphTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testNoManualIndicesOnConstruction() {
        IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
        int count = 0;
        this.stopWatch();
        for (Index index : graph.getIndices()) {
            count++;
            assertTrue(index instanceof AutomaticIndex);
        }
        BaseTest.printPerformance(graph.toString(), count, "indices iterated through", this.stopWatch());
        graph.shutdown();
    }

    public void testAutomaticIndicesOnConstruction() {
        IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
        this.stopWatch();
        if (graphTest.supportsVertexIndex) {
            assertNotNull(graph.getIndex(Index.VERTICES, Vertex.class));
        } else {
            assertNull(graph.getIndex(Index.VERTICES, Vertex.class));
        }
        if (graphTest.supportsEdgeIndex) {
            assertNotNull(graph.getIndex(Index.EDGES, Edge.class));
        } else {
            assertNull(graph.getIndex(Index.EDGES, Edge.class));
        }

        BaseTest.printPerformance(graph.toString(), 2, "automatic indices retrieved", this.stopWatch());
        graph.shutdown();
    }

    public void testCreateDropIndices() {
        IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
        int count = 0;
        this.stopWatch();
        for (Index index : graph.getIndices()) {
            count++;
            graph.dropIndex(index.getIndexName());
        }
        BaseTest.printPerformance(graph.toString(), count, "indices dropped", this.stopWatch());
        assertEquals(count(graph.getIndices()), 0);

        this.stopWatch();
        Index<Vertex> index1 = graph.createIndex("index1", Vertex.class, Index.Type.MANUAL);
        Index<Edge> index2 = graph.createIndex("index2", Edge.class, Index.Type.MANUAL);
        Index<Vertex> index3 = graph.createIndex("index3", Vertex.class, Index.Type.AUTOMATIC);
        BaseTest.printPerformance(graph.toString(), 3, "indices created", this.stopWatch());

        assertEquals(count(graph.getIndices()), 3);
        assertEquals(graph.getIndex("index1", Vertex.class).getIndexName(), "index1");
        assertEquals(graph.getIndex("index2", Edge.class).getIndexName(), "index2");
        assertEquals(graph.getIndex("index3", Vertex.class).getIndexName(), "index3");
        assertEquals(graph.getIndex("index1", Vertex.class).getIndexClass(), Vertex.class);
        assertEquals(graph.getIndex("index2", Edge.class).getIndexClass(), Edge.class);
        assertEquals(graph.getIndex("index3", Vertex.class).getIndexClass(), Vertex.class);
        try {
            assertEquals(graph.getIndex("index1", Edge.class).getIndexClass(), Edge.class);
            assertFalse(true);
        } catch (RuntimeException e) {
            assertTrue(true);
        }


        this.stopWatch();
        graph.dropIndex(index1.getIndexName());
        assertEquals(count(graph.getIndices()), 2);
        assertTrue(asList(graph.getIndices()).contains(index2));
        assertTrue(asList(graph.getIndices()).contains(index3));

        graph.dropIndex(index2.getIndexName());
        assertEquals(count(graph.getIndices()), 1);
        assertTrue(asList(graph.getIndices()).contains(index3));

        graph.dropIndex(index3.getIndexName());
        assertEquals(count(graph.getIndices()), 0);
        BaseTest.printPerformance(graph.toString(), 3, "indices dropped and index iterable checked for consistency", this.stopWatch());
        graph.shutdown();
    }

    public void testNonExistentIndices() {
        IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
        assertNotNull(graph.getIndex(Index.VERTICES, Vertex.class));
        assertNotNull(graph.getIndex(Index.EDGES, Edge.class));
        this.stopWatch();
        graph.dropIndex(Index.VERTICES);
        graph.dropIndex(Index.EDGES);
        BaseTest.printPerformance(graph.toString(), 2, "indices dropped", this.stopWatch());

        this.stopWatch();
        try {
            graph.getIndex(Index.VERTICES, Vertex.class);
            assertFalse(true);
        } catch (RuntimeException e) {
            assertTrue(true);
        }

        try {
            graph.getIndex(Index.EDGES, Edge.class);
            assertFalse(true);
        } catch (RuntimeException e) {
            assertTrue(true);
        }

        try {
            graph.getIndex("blah blah", Edge.class);
            assertFalse(true);
        } catch (RuntimeException e) {
            assertTrue(true);
        }
        BaseTest.printPerformance(graph.toString(), 2, "non-existent indices retrieved with runtime exceptions", this.stopWatch());
        graph.shutdown();
    }

    /*public void testIndicesExistAfterShutdown() {
        IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
        graph.dropIndex(Index.VERTICES);
        graph.dropIndex(Index.EDGES);
        this.stopWatch();
        graph.createIndex("testIndex", Vertex.class, Index.Type.MANUAL);
        Index<Vertex> index = graph.getIndex("testIndex", Vertex.class);
        Vertex vertex = graph.addVertex(null);
        Object id = vertex.getId();
        index.put("key", "value", vertex);
        assertEquals(count(index.get("key", "value")), 1);
        assertEquals(index.get("key", "value").iterator().next().getId(), id);
        BaseTest.printPerformance(graph.toString(), 1, "index created and 1 vertex added and checked", this.stopWatch());
        graph.shutdown();

        graph = (IndexableGraph) graphTest.getGraphInstance();
        assertNotNull(graph.getIndex("testIndex", Vertex.class));
        assertEquals(count(index.get("key", "value")), 1);
        assertEquals(index.get("key", "value").iterator().next().getId(), id);
        graph.shutdown();

    }*/

}
