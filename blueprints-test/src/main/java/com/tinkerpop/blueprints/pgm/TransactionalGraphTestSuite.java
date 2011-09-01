package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TransactionalGraphTestSuite extends TestSuite {

    public TransactionalGraphTestSuite() {
    }

    public TransactionalGraphTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testConstructionBufferSizeOne() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.getGraphInstance();
        this.stopWatch();
        assertEquals(graph.getMaxBufferSize(), 1);
        printPerformance(graph.toString(), 1, "transaction mode retrieved", this.stopWatch());
        graph.shutdown();
    }

    public void testTransactionsForVertices() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.getGraphInstance();
        if (graphTest.supportsVertexIteration) {
            graph.setMaxBufferSize(1);
            graph.addVertex(null);
            graph.setMaxBufferSize(0);

            this.stopWatch();
            graph.startTransaction();
            try {
                graph.addVertex(null);
                assertTrue(true);
            } catch (Exception e) {
                System.out.println(e);
                assertTrue(false);
            }
            assertEquals(count(graph.getVertices()), 2);
            graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            printPerformance(graph.toString(), 1, "vertex not added in failed transaction", this.stopWatch());

            assertEquals(count(graph.getVertices()), 1);

            this.stopWatch();
            graph.startTransaction();
            try {
                graph.addVertex(null);
                assertTrue(true);
            } catch (Exception e) {
                assertTrue(false);
            }
            assertEquals(count(graph.getVertices()), 2);
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            printPerformance(graph.toString(), 1, "vertex added in successful transaction", this.stopWatch());
            assertEquals(count(graph.getVertices()), 2);
        }
        graph.shutdown();
    }

    public void testBruteVertexTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.getGraphInstance();
        if (graphTest.supportsVertexIteration) {
            graph.setMaxBufferSize(0);

            this.stopWatch();
            for (int i = 0; i < 100; i++) {
                graph.startTransaction();
                graph.addVertex(null);
                graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            }
            printPerformance(graph.toString(), 100, "vertices added in 100 successful transactions", this.stopWatch());
            assertEquals(count(graph.getVertices()), 100);

            this.stopWatch();
            for (int i = 0; i < 100; i++) {
                graph.startTransaction();
                graph.addVertex(null);
                graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            }
            printPerformance(graph.toString(), 100, "vertices not added in 100 failed transactions", this.stopWatch());

            assertEquals(count(graph.getVertices()), 100);
            graph.startTransaction();
            assertEquals(count(graph.getVertices()), 100);
            graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            assertEquals(count(graph.getVertices()), 100);

            this.stopWatch();
            graph.startTransaction();
            for (int i = 0; i < 100; i++) {
                graph.addVertex(null);
            }
            assertEquals(count(graph.getVertices()), 200);
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            printPerformance(graph.toString(), 100, "vertices added in 1 successful transactions", this.stopWatch());
            assertEquals(count(graph.getVertices()), 200);

            this.stopWatch();
            graph.startTransaction();
            for (int i = 0; i < 100; i++) {
                graph.addVertex(null);
            }
            assertEquals(count(graph.getVertices()), 300);
            graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            printPerformance(graph.toString(), 100, "vertices not added in 1 failed transactions", this.stopWatch());
            assertEquals(count(graph.getVertices()), 200);
        }
        graph.shutdown();
    }

    public void testTransactionsForEdges() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.getGraphInstance();
        graph.setMaxBufferSize(1);
        Vertex v = graph.addVertex(null);
        Vertex u = graph.addVertex(null);
        graph.setMaxBufferSize(0);
        assertEquals(graph.getMaxBufferSize(), 0);

        this.stopWatch();
        graph.startTransaction();
        try {
            graph.addEdge(null, v, u, convertId("test"));
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }
        if (graphTest.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 2);
        if (graphTest.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 1);
        graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
        printPerformance(graph.toString(), 1, "edge not added in failed transaction (w/ iteration)", this.stopWatch());
        if (graphTest.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 2);
        if (graphTest.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 0);

        this.stopWatch();
        graph.startTransaction();
        try {
            graph.addEdge(null, u, v, convertId("test"));
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }
        if (graphTest.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 2);
        if (graphTest.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 1);
        graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        printPerformance(graph.toString(), 1, "edge added in successful transaction (w/ iteration)", this.stopWatch());

        if (graphTest.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 2);
        if (graphTest.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 1);

        graph.shutdown();
    }

    public void testBruteEdgeTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.getGraphInstance();
        graph.setMaxBufferSize(0);
        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            graph.startTransaction();
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, convertId("test"));
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        }
        printPerformance(graph.toString(), 100, "edges added in 100 successful transactions (2 vertices added for each edge)", this.stopWatch());
        if (graphTest.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 200);
        if (graphTest.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 100);

        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            graph.startTransaction();
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, convertId("test"));
            graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
        }
        printPerformance(graph.toString(), 100, "edges not added in 100 failed transactions (2 vertices added for each edge)", this.stopWatch());
        if (graphTest.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 200);
        if (graphTest.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 100);

        this.stopWatch();
        graph.startTransaction();
        for (int i = 0; i < 100; i++) {
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, convertId("test"));
        }
        if (graphTest.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 400);
        if (graphTest.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 200);
        graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        printPerformance(graph.toString(), 100, "edges added in 1 successful transactions (2 vertices added for each edge)", this.stopWatch());
        if (graphTest.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 400);
        if (graphTest.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 200);

        this.stopWatch();
        graph.startTransaction();
        for (int i = 0; i < 100; i++) {
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, convertId("test"));
        }
        if (graphTest.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 600);
        if (graphTest.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 300);
        graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
        printPerformance(graph.toString(), 100, "edges not added in 1 failed transactions (2 vertices added for each edge)", this.stopWatch());
        if (graphTest.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 400);
        if (graphTest.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 200);

        graph.shutdown();
    }

    public void testPropertyTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.getGraphInstance();
        if (!graphTest.isRDFModel) {
            graph.setMaxBufferSize(0);

            this.stopWatch();
            graph.startTransaction();
            Vertex v = graph.addVertex(null);
            Object id = v.getId();
            v.setProperty("name", "marko");
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            printPerformance(graph.toString(), 1, "vertex added with string property in a successful transaction", this.stopWatch());


            this.stopWatch();
            graph.startTransaction();
            v = graph.getVertex(id);
            assertNotNull(v);
            assertEquals(v.getProperty("name"), "marko");
            v.setProperty("age", 30);
            assertEquals(v.getProperty("age"), 30);
            graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            printPerformance(graph.toString(), 1, "integer property not added in a failed transaction", this.stopWatch());

            this.stopWatch();
            v = graph.getVertex(id);
            assertNotNull(v);
            assertEquals(v.getProperty("name"), "marko");
            assertNull(v.getProperty("age"));
            printPerformance(graph.toString(), 2, "vertex properties checked in a successful transaction", this.stopWatch());

            graph.startTransaction();
            Edge edge = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "test");
            if (graphTest.supportsEdgeIteration)
                assertEquals(count(graph.getEdges()), 1);
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            if (graphTest.supportsEdgeIteration)
                assertEquals(count(graph.getEdges()), 1);

            this.stopWatch();
            graph.startTransaction();
            edge.setProperty("transaction-1", "success");
            assertEquals(edge.getProperty("transaction-1"), "success");
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            printPerformance(graph.toString(), 1, "edge property added and checked in a successful transaction", this.stopWatch());
            assertEquals(edge.getProperty("transaction-1"), "success");

            this.stopWatch();
            graph.startTransaction();
            edge.setProperty("transaction-2", "failure");
            assertEquals(edge.getProperty("transaction-1"), "success");
            assertEquals(edge.getProperty("transaction-2"), "failure");
            graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            printPerformance(graph.toString(), 1, "edge property added and checked in a failed transaction", this.stopWatch());
            assertEquals(edge.getProperty("transaction-1"), "success");
            assertNull(edge.getProperty("transaction-2"));
        }
        graph.shutdown();
    }

    public void testIndexTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.getGraphInstance();
        if (graphTest.supportsVertexIndex) {
            graph.setMaxBufferSize(0);

            this.stopWatch();
            graph.startTransaction();
            Vertex v = graph.addVertex(null);
            Object id = v.getId();
            v.setProperty("name", "marko");
            if (graphTest.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 1);
            v = ((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "marko").iterator().next();
            assertEquals(v.getId(), id);
            assertEquals(v.getProperty("name"), "marko");
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            printPerformance(graph.toString(), 1, "vertex added and retrieved from index in a successful transaction", this.stopWatch());


            this.stopWatch();
            if (graphTest.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 1);
            v = ((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "marko").iterator().next();
            assertEquals(v.getId(), id);
            assertEquals(v.getProperty("name"), "marko");
            printPerformance(graph.toString(), 1, "vertex retrieved from index outside successful transaction", this.stopWatch());


            this.stopWatch();
            graph.startTransaction();
            v = graph.addVertex(null);
            v.setProperty("name", "pavel");
            if (graphTest.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 2);
            v = ((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "marko").iterator().next();
            assertEquals(v.getProperty("name"), "marko");
            v = ((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "pavel").iterator().next();
            assertEquals(v.getProperty("name"), "pavel");
            graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            printPerformance(graph.toString(), 1, "vertex not added in a failed transaction", this.stopWatch());

            this.stopWatch();
            if (graphTest.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 1);
            assertEquals(count(((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "pavel")), 0);
            printPerformance(graph.toString(), 1, "vertex not retrieved in a successful transaction", this.stopWatch());
            v = ((IndexableGraph) graph).getIndex(Index.VERTICES, Vertex.class).get("name", "marko").iterator().next();
            assertEquals(v.getProperty("name"), "marko");
        }
        graph.shutdown();
    }

    // public void testAutomaticIndexKeysRollback()

    public void testAutomaticIndexExceptionRollback() {
        if (graphTest.isPersistent && !graphTest.isRDFModel) {
            TransactionalGraph graph = (TransactionalGraph) graphTest.getGraphInstance();
            Vertex v = graph.addVertex(null);
            Object id = v.getId();
            v.setProperty("count", "1");
            try {
                // This raises an exception in Neo4j
                v.setProperty("count", null);
            } catch (Exception e) {
            }
            v.setProperty("count", "2");
            graph.shutdown();
            graph = (TransactionalGraph) graphTest.getGraphInstance();
            Vertex reloadedV = graph.getVertex(id);
            assertEquals("2", reloadedV.getProperty("count"));
            graph.shutdown();
        }
    }

    public void testNestedManualTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.getGraphInstance();
        graph.setMaxBufferSize(0);
        graph.startTransaction();
        RuntimeException ex = null;
        try {
            graph.startTransaction();
        } catch (RuntimeException e) {
            ex = e;
        }
        assertNotNull(ex);
        assertEquals(TransactionalGraph.NESTED_MESSAGE, ex.getMessage());
        graph.shutdown();
    }

    public void testRollbackOpenTxOnGraphClose() {
        if (graphTest.isPersistent) {
            TransactionalGraph graph = (TransactionalGraph) graphTest.getGraphInstance();
            Object v1id = graph.addVertex(null).getId();
            graph.setMaxBufferSize(0);
            graph.startTransaction();
            Object v2id = graph.addVertex(null).getId();
            graph.shutdown();
            graph = (TransactionalGraph) graphTest.getGraphInstance();
            assertNotNull("Vertex 1 should be persisted", graph.getVertex(v1id));
            assertNull("Vertex 2 should not be persisted", graph.getVertex(v2id));
            graph.shutdown();
        }
    }

    public void testBulkTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.getGraphInstance();
        graph.setMaxBufferSize(15);
        for (int i = 0; i < 3; i++) {
            graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), convertId("test"));
        }
        assertEquals(BaseTest.count(graph.getEdges()), 3);
        graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
        assertEquals(BaseTest.count(graph.getEdges()), 0);

        graph.shutdown();
    }

    public void testCompetingThreads() {
        final TransactionalGraph graph = (TransactionalGraph) graphTest.getGraphInstance();
        int totalThreads = 250;
        final AtomicInteger vertices = new AtomicInteger(0);
        final AtomicInteger edges = new AtomicInteger(0);
        final AtomicInteger completedThreads = new AtomicInteger(0);
        for (int i = 0; i < totalThreads; i++) {
            new Thread() {
                public void run() {
                    try {
                        Random random = new Random();
                        if (random.nextBoolean()) {
                            Vertex a = graph.addVertex(null);
                            Vertex b = graph.addVertex(null);
                            Edge e = graph.addEdge(null, a, b, convertId("friend"));

                            if (!graphTest.isRDFModel) {
                                a.setProperty("test", this.getId());
                                b.setProperty("blah", random.nextFloat());
                                e.setProperty("bloop", random.nextInt());
                            }

                            vertices.getAndAdd(2);
                            edges.getAndAdd(1);

                        } else {
                            graph.setMaxBufferSize(0);
                            graph.startTransaction();
                            Vertex a = graph.addVertex(null);
                            Vertex b = graph.addVertex(null);
                            Edge e = graph.addEdge(null, a, b, convertId("friend"));
                            if (!graphTest.isRDFModel) {
                                a.setProperty("test", this.getId());
                                b.setProperty("blah", random.nextFloat());
                                e.setProperty("bloop", random.nextInt());
                            }
                            if (random.nextBoolean()) {
                                graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
                                vertices.getAndAdd(2);
                                edges.getAndAdd(1);
                            } else {
                                graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
                            }
                        }
                    } catch (Throwable e) {
                        System.out.println(e);
                        assertTrue(false);
                    }
                    completedThreads.getAndAdd(1);
                }
            }.start();
        }

        while (completedThreads.get() < totalThreads) {
        }
        if (!graphTest.isRDFModel)
            assertEquals(count(graph.getVertices()), vertices.get());
        assertEquals(count(graph.getEdges()), edges.get());
        graph.shutdown();
    }

}
