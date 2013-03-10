package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.impls.GraphTest;

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

    public void testNoIndicesOnStartup() {
        IndexableGraph graph = (IndexableGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIndex) {

            assertEquals(count(graph.getIndices()), 0);
            graph.createIndex("myIdx", Vertex.class);
            assertEquals(count(graph.getIndices()), 1);

            // test to make sure its a semantically correct iterable
            Iterable<Index<? extends Element>> idx = graph.getIndices();
            assertEquals(count(idx), 1);
            assertEquals(count(idx), 1);
            assertEquals(count(idx), 1);

        }
        graph.shutdown();
    }

    public void testKeyIndicesAreNotIndices() {
        IndexableGraph graph = (IndexableGraph) graphTest.generateGraph();
        assertEquals(count(graph.getIndices()), 0);
        if (!graph.getFeatures().isWrapper && graph.getFeatures().supportsKeyIndices && graph.getFeatures().supportsVertexKeyIndex) {
            ((KeyIndexableGraph) graph).createKeyIndex("name", Vertex.class);
            ((KeyIndexableGraph) graph).createKeyIndex("age", Vertex.class);
            assertEquals(((KeyIndexableGraph) graph).getIndexedKeys(Vertex.class).size(), 2);
        }
        if (!graph.getFeatures().isWrapper && graph.getFeatures().supportsKeyIndices && graph.getFeatures().supportsEdgeKeyIndex) {
            ((KeyIndexableGraph) graph).createKeyIndex("weight", Edge.class);
            ((KeyIndexableGraph) graph).createKeyIndex("since", Edge.class);
            assertEquals(((KeyIndexableGraph) graph).getIndexedKeys(Edge.class).size(), 2);
        }
        assertEquals(count(graph.getIndices()), 0);
        graph.shutdown();
    }

    public void testCreateDropIndices() {
        IndexableGraph graph = (IndexableGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIndex && graph.getFeatures().supportsIndices) {

            this.stopWatch();
            for (int i = 0; i < 10; i++) {
                graph.createIndex(i + "blah", Vertex.class);
            }
            assertEquals(count(graph.getIndices()), 10);
            for (int i = 0; i < 10; i++) {
                graph.dropIndex(i + "blah");
            }
            assertEquals(count(graph.getIndices()), 0);
            printPerformance(graph.toString(), 10, "indices created and then dropped", this.stopWatch());

            this.stopWatch();
            Index<Vertex> index1 = graph.createIndex("index1", Vertex.class);
            Index<Vertex> index2 = graph.createIndex("index2", Vertex.class);
            printPerformance(graph.toString(), 2, "indices created", this.stopWatch());
            assertEquals(count(graph.getIndices()), 2);
            assertEquals(graph.getIndex("index1", Vertex.class).getIndexName(), "index1");
            assertEquals(graph.getIndex("index2", Vertex.class).getIndexName(), "index2");
            assertEquals(graph.getIndex("index1", Vertex.class).getIndexClass(), Vertex.class);
            assertEquals(graph.getIndex("index2", Vertex.class).getIndexClass(), Vertex.class);
            try {
                assertEquals(graph.getIndex("index1", Edge.class).getIndexClass(), Edge.class);
                assertFalse(true);
            } catch (RuntimeException e) {
                assertTrue(true);
            }


            this.stopWatch();
            graph.dropIndex(index1.getIndexName());
            assertNull(graph.getIndex("index1", Vertex.class));
            assertEquals(count(graph.getIndices()), 1);
            for (Index index : graph.getIndices()) {
                assertEquals(index.getIndexName(), index2.getIndexName());
            }

            graph.dropIndex(index2.getIndexName());
            assertNull(graph.getIndex("index1", Vertex.class));
            assertNull(graph.getIndex("index2", Vertex.class));
            assertEquals(count(graph.getIndices()), 0);

            printPerformance(graph.toString(), 2, "indices dropped and index iterable checked for consistency", this.stopWatch());

        }
        graph.shutdown();
    }

    public void testNonExistentIndices() {
        IndexableGraph graph = (IndexableGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIndex && graph.getFeatures().supportsEdgeIndex && graph.getFeatures().supportsIndices) {

            this.stopWatch();
            assertNull(graph.getIndex("bloop", Vertex.class));
            assertNull(graph.getIndex("bam", Edge.class));
            assertNull(graph.getIndex("blah blah", Edge.class));
            printPerformance(graph.toString(), 3, "non-existent indices retrieved", this.stopWatch());

        }
        graph.shutdown();
    }

    public void testIndexPersistence() {
        IndexableGraph graph = (IndexableGraph) this.graphTest.generateGraph();
        if (graph.getFeatures().isPersistent && graph.getFeatures().supportsVertexIndex && graph.getFeatures().supportsElementProperties() && graph.getFeatures().supportsIndices) {

            this.stopWatch();
            graph.createIndex("testIndex", Vertex.class);
            Index<Vertex> manualIndex = graph.getIndex("testIndex", Vertex.class);
            assertEquals(manualIndex.getIndexName(), "testIndex");
            Vertex vertex = graph.addVertex(null);
            vertex.setProperty("name", "marko");
            Object id = vertex.getId();
            manualIndex.put("key", "value", vertex);
            assertEquals(count(manualIndex.get("key", "value")), 1);
            assertEquals(manualIndex.get("key", "value").iterator().next().getId(), id);
            printPerformance(graph.toString(), 1, "index created and 1 vertex added and checked", this.stopWatch());
            graph.shutdown();

            graph = (IndexableGraph) this.graphTest.generateGraph();
            this.stopWatch();
            manualIndex = graph.getIndex("testIndex", Vertex.class);
            assertEquals(count(manualIndex.get("key", "value")), 1);
            assertEquals(manualIndex.get("key", "value").iterator().next().getId(), id);
            printPerformance(graph.toString(), 1, "index reloaded and 1 vertex checked", this.stopWatch());
            graph.shutdown();

            graph = (IndexableGraph) this.graphTest.generateGraph();
            this.stopWatch();
            manualIndex = graph.getIndex("testIndex", Vertex.class);
            vertex = manualIndex.get("key", "value").iterator().next();
            assertEquals(vertex.getId(), id);
            graph.removeVertex(vertex);
            assertEquals(0, count(manualIndex.get("key", "value")));
            printPerformance(graph.toString(), 1, "index reloaded and 1 vertex checked and then removed", this.stopWatch());
            graph.shutdown();

            graph = (IndexableGraph) this.graphTest.generateGraph();
            this.stopWatch();
            manualIndex = graph.getIndex("testIndex", Vertex.class);
            assertEquals(count(manualIndex.get("key", "value")), 0);
            printPerformance(graph.toString(), 1, "index reloaded and checked to ensure empty", this.stopWatch());

        }
        graph.shutdown();
    }

    public void testExceptionOnIndexOverwrite() {
        IndexableGraph graph = (IndexableGraph) this.graphTest.generateGraph();
        if (graph.getFeatures().supportsIndices && graph.getFeatures().supportsVertexIndex) {
            int loop = 1;
            if (graph.getFeatures().isPersistent)
                loop = 5;
            graph.shutdown();
            this.stopWatch();
            String graphName = "";
            for (int i = 0; i < loop; i++) {
                graph = (IndexableGraph) this.graphTest.generateGraph();
                graph.createIndex(i + "atest", Vertex.class);
                graphName = graph.toString();
                int counter = 0;
                int exceptionCounter = 0;
                for (Index index : graph.getIndices()) {
                    try {
                        counter++;
                        graph.createIndex(index.getIndexName(), index.getIndexClass());
                    } catch (RuntimeException e) {
                        exceptionCounter++;
                    }
                }
                assertEquals(counter, exceptionCounter);
                assertTrue(counter > 0);
                graph.shutdown();
            }
            printPerformance(graphName, loop, "attempt(s) to overwrite existing indices", this.stopWatch());
        }

        graph.shutdown();
    }

    public void testIndexDropPersistence() {
        IndexableGraph graph = (IndexableGraph) this.graphTest.generateGraph();
        if (graph.getFeatures().isPersistent && graph.getFeatures().supportsIndices && graph.getFeatures().supportsVertexIndex) {

            graph.createIndex("blah", Vertex.class);
            graph.createIndex("bleep", Vertex.class);
            Set<String> indexNames = new HashSet<String>();
            for (Index index : graph.getIndices()) {
                indexNames.add(index.getIndexName());
            }
            assertEquals(count(graph.getIndices()), 2);
            assertEquals(count(graph.getIndices()), indexNames.size());
            this.stopWatch();
            for (String indexName : indexNames) {
                graph.dropIndex(indexName);
            }
            printPerformance(graph.toString(), indexNames.size(), "indices dropped", this.stopWatch());
            assertEquals(count(graph.getIndices()), 0);
            graph.shutdown();

            graph = (IndexableGraph) this.graphTest.generateGraph();
            assertEquals(count(graph.getIndices()), 0);

        }
        graph.shutdown();
    }
}
