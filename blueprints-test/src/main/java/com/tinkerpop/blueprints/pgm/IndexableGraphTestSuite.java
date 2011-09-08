package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.pgm.impls.GraphTest;

import java.util.HashSet;
import java.util.Set;

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
            assertEquals(index.getIndexType(), Index.Type.AUTOMATIC);
        }
        printPerformance(graph.toString(), count, "indices iterated through", this.stopWatch());
        graph.shutdown();
    }

    public void testAutomaticIndicesOnConstruction() {
        IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
        this.stopWatch();
        if (graphTest.supportsVertexIndex) {
            assertNotNull(graph.getIndex(Index.VERTICES, Vertex.class));
            assertEquals(graph.getIndex(Index.VERTICES, Vertex.class).getIndexType(), Index.Type.AUTOMATIC);
        } else {
            assertNull(graph.getIndex(Index.VERTICES, Vertex.class));
        }
        if (graphTest.supportsEdgeIndex) {
            assertNotNull(graph.getIndex(Index.EDGES, Edge.class));
            assertEquals(graph.getIndex(Index.EDGES, Edge.class).getIndexType(), Index.Type.AUTOMATIC);
        } else {
            assertNull(graph.getIndex(Index.EDGES, Edge.class));
        }

        printPerformance(graph.toString(), 2, "automatic indices retrieved", this.stopWatch());
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
        printPerformance(graph.toString(), count, "indices dropped", this.stopWatch());
        assertEquals(count(graph.getIndices()), 0);

        this.stopWatch();
        Index<Vertex> index1 = graph.createManualIndex("index1", Vertex.class);
        Index<Edge> index2 = graph.createManualIndex("index2", Edge.class);
        Index<Vertex> index3 = graph.createAutomaticIndex("index3", Vertex.class, null);
        printPerformance(graph.toString(), 3, "indices created", this.stopWatch());

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
        printPerformance(graph.toString(), 3, "indices dropped and index iterable checked for consistency", this.stopWatch());
        graph.shutdown();
    }

    public void testNonExistentIndices() {
        IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
        assertNotNull(graph.getIndex(Index.VERTICES, Vertex.class));
        assertNotNull(graph.getIndex(Index.EDGES, Edge.class));
        this.stopWatch();
        graph.dropIndex(Index.VERTICES);
        graph.dropIndex(Index.EDGES);
        printPerformance(graph.toString(), 2, "indices dropped", this.stopWatch());

        this.stopWatch();

        assertNull(graph.getIndex(Index.VERTICES, Vertex.class));
        assertNull(graph.getIndex(Index.EDGES, Edge.class));
        assertNull(graph.getIndex("blah blah", Edge.class));

        printPerformance(graph.toString(), 3, "non-existent indices retrieved with runtime exceptions", this.stopWatch());
        graph.shutdown();
    }

    public void testIndexPersistence() {
        if (graphTest.isPersistent && graphTest.supportsVertexIndex && !graphTest.isRDFModel) {
            IndexableGraph graph = (IndexableGraph) this.graphTest.getGraphInstance();
            graph.dropIndex(Index.EDGES);
            graph.dropIndex(Index.VERTICES);

            this.stopWatch();
            graph.createManualIndex("testIndex", Vertex.class);
            graph.createAutomaticIndex(Index.VERTICES, Vertex.class, null);
            Index<Vertex> manualIndex = graph.getIndex("testIndex", Vertex.class);
            assertEquals(graph.getIndex("testIndex", Vertex.class).getIndexType(), Index.Type.MANUAL);
            Index<Vertex> autoIndex = graph.getIndex(Index.VERTICES, Vertex.class);
            assertEquals(graph.getIndex(Index.VERTICES, Vertex.class).getIndexType(), Index.Type.AUTOMATIC);
            Vertex vertex = graph.addVertex(null);
            vertex.setProperty("name", "marko");
            Object id = vertex.getId();
            manualIndex.put("key", "value", vertex);
            assertEquals(count(manualIndex.get("key", "value")), 1);
            assertEquals(manualIndex.get("key", "value").iterator().next().getId(), id);
            assertEquals(count(autoIndex.get("name", "marko")), 1);
            assertEquals(autoIndex.get("name", "marko").iterator().next().getId(), id);
            printPerformance(graph.toString(), 2, "indices created and 1 vertex added and checked", this.stopWatch());
            graph.shutdown();

            graph = (IndexableGraph) this.graphTest.getGraphInstance();
            this.stopWatch();
            manualIndex = graph.getIndex("testIndex", Vertex.class);
            autoIndex = graph.getIndex(Index.VERTICES, Vertex.class);
            assertTrue(!(manualIndex instanceof AutomaticIndex));
            assertTrue(autoIndex instanceof AutomaticIndex);
            assertEquals(count(manualIndex.get("key", "value")), 1);
            assertEquals(manualIndex.get("key", "value").iterator().next().getId(), id);
            assertEquals(count(autoIndex.get("name", "marko")), 1);
            assertEquals(autoIndex.get("name", "marko").iterator().next().getId(), id);
            printPerformance(graph.toString(), 2, "indices reloaded and 1 vertex checked", this.stopWatch());
            graph.shutdown();

            graph = (IndexableGraph) this.graphTest.getGraphInstance();
            this.stopWatch();
            manualIndex = graph.getIndex("testIndex", Vertex.class);
            autoIndex = graph.getIndex(Index.VERTICES, Vertex.class);
            vertex = manualIndex.get("key", "value").iterator().next();
            assertEquals(vertex.getId(), id);
            vertex = autoIndex.get("name", "marko").iterator().next();
            assertEquals(vertex.getId(), id);
            graph.removeVertex(vertex);
            assertEquals(0, count(manualIndex.get("key", "value")));
            assertEquals(0, count(autoIndex.get("key", "value")));
            printPerformance(graph.toString(), 2, "indices reloaded and 1 vertex checked and then removed", this.stopWatch());
            graph.shutdown();

            graph = (IndexableGraph) this.graphTest.getGraphInstance();
            this.stopWatch();
            manualIndex = graph.getIndex("testIndex", Vertex.class);
            autoIndex = graph.getIndex(Index.VERTICES, Vertex.class);
            assertEquals(count(manualIndex.get("key", "value")), 0);
            assertEquals(count(autoIndex.get("key", "value")), 0);
            printPerformance(graph.toString(), 2, "indices reloaded and checked to ensure empty", this.stopWatch());
            graph.shutdown();
        }
    }

    public void testIndicesDroppedAndRecreatedOnClear() {
        this.stopWatch();
        IndexableGraph graph = (IndexableGraph) this.graphTest.getGraphInstance();
        int count = 0;
        if (graphTest.supportsVertexIndex)
            count++;
        if (graphTest.supportsEdgeIndex)
            count++;
        assertEquals(count(graph.getIndices()), count);

        graph.clear();
        assertEquals(count(graph.getIndices()), 2);
        if (graphTest.supportsVertexIndex) {
            assertNotNull(graph.getIndex(Index.VERTICES, Vertex.class));
            assertEquals(graph.getIndex(Index.VERTICES, Vertex.class).getIndexType(), Index.Type.AUTOMATIC);
        } else {
            assertNull(graph.getIndex(Index.VERTICES, Vertex.class));
        }
        if (graphTest.supportsEdgeIndex) {
            assertNotNull(graph.getIndex(Index.EDGES, Edge.class));
            assertEquals(graph.getIndex(Index.EDGES, Edge.class).getIndexType(), Index.Type.AUTOMATIC);
        } else {
            assertNull(graph.getIndex(Index.EDGES, Edge.class));
        }

        printPerformance(graph.toString(), 2, "automatic indices retrieved after graph cleared", this.stopWatch());
        graph.shutdown();
    }

    public void testIndexDropPersistence() {
        if (graphTest.isPersistent) {
            IndexableGraph graph = (IndexableGraph) this.graphTest.getGraphInstance();
            Set<String> indexNames = new HashSet<String>();
            for (Index index : graph.getIndices()) {
                indexNames.add(index.getIndexName());
            }
            assertEquals(count(graph.getIndices()), indexNames.size());
            this.stopWatch();
            for (String indexName : indexNames) {
                graph.dropIndex(indexName);
            }
            printPerformance(graph.toString(), indexNames.size(), "indices dropped", this.stopWatch());
            assertEquals(count(graph.getIndices()), 0);
            graph.shutdown();

            graph = (IndexableGraph) this.graphTest.getGraphInstance();
            assertEquals(count(graph.getIndices()), 0);
            graph.shutdown();

        }
    }

    public void testExceptionOnIndexOverwrite() {
        int loop = 1;
        if (graphTest.isPersistent)
            loop = 5;

        this.stopWatch();
        String graphName = "";
        for (int i = 0; i < loop; i++) {
            IndexableGraph graph = (IndexableGraph) this.graphTest.getGraphInstance();
            graphName = graph.toString();
            int counter = 0;
            int exceptionCounter = 0;
            for (Index index : graph.getIndices()) {
                try {
                    counter++;
                    if (index.getIndexType().equals(Index.Type.MANUAL))
                        graph.createManualIndex(index.getIndexName(), index.getIndexClass());
                    else
                        graph.createAutomaticIndex(index.getIndexName(), index.getIndexClass(), null);
                } catch (RuntimeException e) {
                    exceptionCounter++;
                }
            }
            assertEquals(counter, exceptionCounter);
            graph.shutdown();
        }
        printPerformance(graphName, loop, "attempt(s) to overwrite existing indices", this.stopWatch());
    }
}
