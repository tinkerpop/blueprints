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

    public void testNoIndicesOnStartup() {
        if (graphTest.supportsVertexIndex && !graphTest.isRDFModel) {
            IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
            assertEquals(count(graph.getIndices()), 0);
            graph.createKeyIndex("name", Vertex.class);
            assertEquals(count(graph.getIndices()), 0);
            graph.createIndex("myIdx", Vertex.class);
            assertEquals(count(graph.getIndices()), 1);

            // test to make sure its a semantically correct iterable
            Iterable<Index<? extends Element>> idx = graph.getIndices();
            assertEquals(count(idx), 1);
            assertEquals(count(idx), 1);
            assertEquals(count(idx), 1);
            graph.shutdown();
        }
    }

    public void testAutoIndexKeyManagement() {
        if (graphTest.supportsVertexIndex) {
            IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
            this.stopWatch();
            graph.createKeyIndex("name", Vertex.class);
            graph.createKeyIndex("location", Vertex.class);
            printPerformance(graph.toString(), 2, "automatic index keys added", this.stopWatch());
            assertEquals(graph.getIndexedKeys(Vertex.class).size(), 2);
            assertTrue(graph.getIndexedKeys(Vertex.class).contains("name"));
            assertTrue(graph.getIndexedKeys(Vertex.class).contains("location"));
            graph.shutdown();
        }
    }

    public void testCreateDropIndices() {
        if (graphTest.supportsVertexIndex && graphTest.supportsManualIndices) {
            IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
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
            graph.shutdown();
        }
    }

    public void testNonExistentIndices() {
        if (graphTest.supportsVertexIndex && graphTest.supportsEdgeIndex && graphTest.supportsManualIndices) {
            IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
            this.stopWatch();
            assertNull(graph.getIndex("bloop", Vertex.class));
            assertNull(graph.getIndex("bam", Edge.class));
            assertNull(graph.getIndex("blah blah", Edge.class));
            printPerformance(graph.toString(), 3, "non-existent indices retrieved", this.stopWatch());
            graph.shutdown();
        }
    }

    public void testIndexPersistence() {
        if (graphTest.isPersistent && graphTest.supportsVertexIndex && !graphTest.isRDFModel && graphTest.supportsManualIndices) {
            IndexableGraph graph = (IndexableGraph) this.graphTest.getGraphInstance();

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

            graph = (IndexableGraph) this.graphTest.getGraphInstance();
            this.stopWatch();
            manualIndex = graph.getIndex("testIndex", Vertex.class);
            assertEquals(count(manualIndex.get("key", "value")), 1);
            assertEquals(manualIndex.get("key", "value").iterator().next().getId(), id);
            printPerformance(graph.toString(), 1, "index reloaded and 1 vertex checked", this.stopWatch());
            graph.shutdown();

            graph = (IndexableGraph) this.graphTest.getGraphInstance();
            this.stopWatch();
            manualIndex = graph.getIndex("testIndex", Vertex.class);
            vertex = manualIndex.get("key", "value").iterator().next();
            assertEquals(vertex.getId(), id);
            graph.removeVertex(vertex);
            assertEquals(0, count(manualIndex.get("key", "value")));
            printPerformance(graph.toString(), 1, "index reloaded and 1 vertex checked and then removed", this.stopWatch());
            graph.shutdown();

            graph = (IndexableGraph) this.graphTest.getGraphInstance();
            this.stopWatch();
            manualIndex = graph.getIndex("testIndex", Vertex.class);
            assertEquals(count(manualIndex.get("key", "value")), 0);
            printPerformance(graph.toString(), 1, "index reloaded and checked to ensure empty", this.stopWatch());
            graph.shutdown();
        }
    }

    public void testManualIndicesPersist() {
        if (graphTest.isPersistent && graphTest.supportsManualIndices && graphTest.supportsVertexIndex && graphTest.supportsEdgeIndex) {
            IndexableGraph graph = (IndexableGraph) this.graphTest.getGraphInstance();
            Vertex a = graph.addVertex(null);
            Vertex b = graph.addVertex(null);
            Edge e = graph.addEdge(null, a, b, "related");

            Index index = graph.createIndex("vertexIdx", Vertex.class);
            index.put("boo", "blop", a);
            index = graph.createIndex("edgeIdx", Edge.class);
            index.put("boo", "blop", e);

            graph.shutdown();

            //// check persistence
            graph = (IndexableGraph) this.graphTest.getGraphInstance();
            index = graph.getIndex("vertexIdx", Vertex.class);
            assertEquals(index.get("boo", "blop").iterator().next(), a);

            index = graph.getIndex("edgeIdx", Edge.class);
            assertEquals(index.get("boo", "blop").iterator().next(), e);
            graph.shutdown();
        }
    }

    public void testExceptionOnIndexOverwrite() {
        if (graphTest.supportsManualIndices && graphTest.supportsVertexIndex) {
            int loop = 1;
            if (graphTest.isPersistent)
                loop = 5;

            this.stopWatch();
            String graphName = "";
            for (int i = 0; i < loop; i++) {
                IndexableGraph graph = (IndexableGraph) this.graphTest.getGraphInstance();
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
    }

    public void testIndexDropPersistence() {
        if (graphTest.isPersistent && graphTest.supportsManualIndices && graphTest.supportsVertexIndex) {
            IndexableGraph graph = (IndexableGraph) this.graphTest.getGraphInstance();
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

            graph = (IndexableGraph) this.graphTest.getGraphInstance();
            assertEquals(count(graph.getIndices()), 0);
            graph.shutdown();
        }
    }


    /*public void testAutomaticTransactionsOnIndices() {
        IndexableGraph graph = (IndexableGraph) this.graphTest.getGraphInstance();
        if (graphTest.supportsTransactions && graph instanceof TransactionalGraph) {
            TransactionalGraph txGraph = (TransactionalGraph) graph;
            assertEquals(txGraph.getCurrentBufferSize(), 0);
            txGraph.setMaxBufferSize(5);
            assertEquals(txGraph.getCurrentBufferSize(), 0);
            Index<Vertex> index = graph.createIndex("aManualIndex", Vertex.class);
            assertEquals(txGraph.getCurrentBufferSize(), 0);

            Vertex v = graph.addVertex(null);
            assertEquals(txGraph.getCurrentBufferSize(), 1);
            index.put("key", "value", v);
            assertEquals(txGraph.getCurrentBufferSize(), 2);
            assertEquals(count(index.get("key", "value")), 1);
            assertEquals(index.get("key", "value").iterator().next(), v);
            txGraph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            assertEquals(txGraph.getCurrentBufferSize(), 0);
            assertEquals(count(index.get("key", "value")), 0);
            assertEquals(count(graph.getVertices()), 0);

            assertEquals(txGraph.getCurrentBufferSize(), 0);
            txGraph.setMaxBufferSize(2);
            assertEquals(txGraph.getCurrentBufferSize(), 0);
            v = graph.addVertex(null);
            assertEquals(txGraph.getCurrentBufferSize(), 1);
            index.put("key", "value", v);
            assertEquals(txGraph.getCurrentBufferSize(), 0);
            assertEquals(count(index.get("key", "value")), 1);
            assertEquals(txGraph.getCurrentBufferSize(), 0);
            index.remove("key", "value", v);
            assertEquals(txGraph.getCurrentBufferSize(), 1);
            assertEquals(count(index.get("key", "value")), 0);
            txGraph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            assertEquals(txGraph.getCurrentBufferSize(), 0);
            assertEquals(txGraph.getMaxBufferSize(), 2);
            assertEquals(count(index.get("key", "value")), 1);
        }

        graph.shutdown();
    }*/

}
