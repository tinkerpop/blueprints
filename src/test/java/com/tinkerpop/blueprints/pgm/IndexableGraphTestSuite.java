package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.BaseTest;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IndexableGraphTestSuite extends ModelTestSuite {

    public IndexableGraphTestSuite() {
    }

    public IndexableGraphTestSuite(final SuiteConfiguration config) {
        super(config);
    }

    public void testNoManualIndicesOnConstruction(final IndexableGraph graph) {
        int count = 0;
        this.stopWatch();
        for (Index index : graph.getIndices()) {
            count++;
            assertTrue(index instanceof AutomaticIndex);
        }
        BaseTest.printPerformance(graph.toString(), count, "indices iterated through", this.stopWatch());
    }

    public void testAutomaticIndicesOnConstruction(IndexableGraph graph) {
        this.stopWatch();
        if (config.supportsVertexIndex) {
            assertNotNull(graph.getIndex(Index.VERTICES, Vertex.class));
        } else {
            assertNull(graph.getIndex(Index.VERTICES, Vertex.class));
        }
        if (config.supportsEdgeIndex) {
            assertNotNull(graph.getIndex(Index.EDGES, Edge.class));
        } else {
            assertNull(graph.getIndex(Index.EDGES, Edge.class));
        }

        BaseTest.printPerformance(graph.toString(), 2, "automatic indices retrieved", this.stopWatch());
    }

    public void testCreateDropIndices(final IndexableGraph graph) {
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
    }

    //public void testCreateIndicesWithDuplicateNames(final IndexableGraph graph) {}

}
