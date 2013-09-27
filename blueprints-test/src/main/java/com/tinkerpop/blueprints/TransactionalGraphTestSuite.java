package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.TransactionRetryHelper;
import com.tinkerpop.blueprints.util.TransactionRetryStrategy;
import com.tinkerpop.blueprints.util.TransactionWork;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONMode;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONUtility;
import org.codehaus.jettison.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TransactionalGraphTestSuite extends TestSuite {

    public TransactionalGraphTestSuite() {
    }

    public TransactionalGraphTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testTrue() {
        assertTrue(true);
    }

    public void testRepeatedTransactionStopException() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        graph.commit();
        graph.rollback();
        graph.commit();
        graph.shutdown();
    }

    public void testAutoStartTransaction() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        Vertex v1 = graph.addVertex(null);
        vertexCount(graph, 1);
        assertEquals(v1.getId(), graph.getVertex(v1.getId()).getId());
        graph.commit();
        vertexCount(graph, 1);
        assertEquals(v1.getId(), graph.getVertex(v1.getId()).getId());
        graph.shutdown();

    }


    public void testTransactionsForVertices() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        List<Vertex> vin = new ArrayList<Vertex>();
        List<Vertex> vout = new ArrayList<Vertex>();
        vin.add(graph.addVertex(null));
        graph.commit();
        vertexCount(graph, 1);
        containsVertices(graph, vin);

        this.stopWatch();
        vout.add(graph.addVertex(null));
        vertexCount(graph, 2);
        containsVertices(graph, vin);
        containsVertices(graph, vout);
        graph.rollback();

        containsVertices(graph, vin);
        vertexCount(graph, 1);
        printPerformance(graph.toString(), 1, "vertex not added in failed transaction", this.stopWatch());

        this.stopWatch();
        vin.add(graph.addVertex(null));
        vertexCount(graph, 2);
        containsVertices(graph, vin);
        graph.commit();
        printPerformance(graph.toString(), 1, "vertex added in successful transaction", this.stopWatch());
        vertexCount(graph, 2);
        containsVertices(graph, vin);

        graph.shutdown();
    }

    public void testBasicVertexEdgeTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        Vertex v = graph.addVertex(null);
        graph.addEdge(null, v, v, graphTest.convertLabel("self"));
        assertEquals(count(v.getEdges(Direction.IN)), 1);
        assertEquals(count(v.getEdges(Direction.OUT)), 1);
        assertEquals(v.getEdges(Direction.IN).iterator().next(), v.getEdges(Direction.OUT).iterator().next());
        graph.commit();
        v = graph.getVertex(v.getId());
        assertEquals(count(v.getEdges(Direction.IN)), 1);
        assertEquals(count(v.getEdges(Direction.OUT)), 1);
        assertEquals(v.getEdges(Direction.IN).iterator().next(), v.getEdges(Direction.OUT).iterator().next());
        graph.commit();
        v = graph.getVertex(v.getId());
        assertEquals(count(v.getVertices(Direction.IN)), 1);
        assertEquals(count(v.getVertices(Direction.OUT)), 1);
        assertEquals(v.getVertices(Direction.IN).iterator().next(), v.getVertices(Direction.OUT).iterator().next());
        graph.commit();
        graph.shutdown();
    }

    public void testBruteVertexTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        List<Vertex> vin = new ArrayList<Vertex>(), vout = new ArrayList<Vertex>();
        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            vin.add(graph.addVertex(null));
            graph.commit();
        }
        printPerformance(graph.toString(), 100, "vertices added in 100 successful transactions", this.stopWatch());
        vertexCount(graph, 100);
        containsVertices(graph, vin);

        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            vout.add(graph.addVertex(null));
            graph.rollback();
        }
        printPerformance(graph.toString(), 100, "vertices not added in 100 failed transactions", this.stopWatch());

        vertexCount(graph, 100);
        containsVertices(graph, vin);
        graph.rollback();
        vertexCount(graph, 100);
        containsVertices(graph, vin);


        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            vin.add(graph.addVertex(null));
        }
        vertexCount(graph, 200);
        containsVertices(graph, vin);
        graph.commit();
        printPerformance(graph.toString(), 100, "vertices added in 1 successful transactions", this.stopWatch());
        vertexCount(graph, 200);
        containsVertices(graph, vin);

        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            vout.add(graph.addVertex(null));
        }
        vertexCount(graph, 300);
        containsVertices(graph, vin);
        containsVertices(graph, vout.subList(100, 200));
        graph.rollback();
        printPerformance(graph.toString(), 100, "vertices not added in 1 failed transactions", this.stopWatch());
        vertexCount(graph, 200);
        containsVertices(graph, vin);
        graph.shutdown();
    }

    public void testTransactionsForEdges() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();

        Vertex v = graph.addVertex(null);
        Vertex u = graph.addVertex(null);
        graph.commit();

        this.stopWatch();
        Edge e = graph.addEdge(null, graph.getVertex(v.getId()), graph.getVertex(u.getId()), graphTest.convertLabel("test"));


        assertEquals(graph.getVertex(v.getId()), v);
        assertEquals(graph.getVertex(u.getId()), u);
        if (graph.getFeatures().supportsEdgeRetrieval)
            assertEquals(graph.getEdge(e.getId()), e);

        vertexCount(graph, 2);
        edgeCount(graph, 1);

        graph.rollback();
        printPerformance(graph.toString(), 1, "edge not added in failed transaction (w/ iteration)", this.stopWatch());

        assertEquals(graph.getVertex(v.getId()), v);
        assertEquals(graph.getVertex(u.getId()), u);
        if (graph.getFeatures().supportsEdgeRetrieval)
            assertNull(graph.getEdge(e.getId()));

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 2);
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 0);

        this.stopWatch();

        e = graph.addEdge(null, graph.getVertex(u.getId()), graph.getVertex(v.getId()), graphTest.convertLabel("test"));

        assertEquals(graph.getVertex(v.getId()), v);
        assertEquals(graph.getVertex(u.getId()), u);
        if (graph.getFeatures().supportsEdgeRetrieval)
            assertEquals(graph.getEdge(e.getId()), e);

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 2);
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 1);
        assertEquals(e, getOnlyElement(graph.getVertex(u.getId()).getEdges(Direction.OUT)));
        graph.commit();
        printPerformance(graph.toString(), 1, "edge added in successful transaction (w/ iteration)", this.stopWatch());

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 2);
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 1);

        assertEquals(graph.getVertex(v.getId()), v);
        assertEquals(graph.getVertex(u.getId()), u);
        if (graph.getFeatures().supportsEdgeRetrieval)
            assertEquals(graph.getEdge(e.getId()), e);
        assertEquals(e, getOnlyElement(graph.getVertex(u.getId()).getEdges(Direction.OUT)));

        graph.shutdown();
    }

    public void testBruteEdgeTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, graphTest.convertLabel("test"));
            graph.commit();
        }
        printPerformance(graph.toString(), 100, "edges added in 100 successful transactions (2 vertices added for each edge)", this.stopWatch());
        vertexCount(graph, 200);
        edgeCount(graph, 100);

        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, graphTest.convertLabel("test"));
            graph.rollback();
        }
        printPerformance(graph.toString(), 100, "edges not added in 100 failed transactions (2 vertices added for each edge)", this.stopWatch());
        vertexCount(graph, 200);
        edgeCount(graph, 100);

        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, graphTest.convertLabel("test"));
        }
        vertexCount(graph, 400);
        edgeCount(graph, 200);
        graph.commit();
        printPerformance(graph.toString(), 100, "edges added in 1 successful transactions (2 vertices added for each edge)", this.stopWatch());
        vertexCount(graph, 400);
        edgeCount(graph, 200);

        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, graphTest.convertLabel("test"));
        }
        vertexCount(graph, 600);
        edgeCount(graph, 300);

        graph.rollback();
        printPerformance(graph.toString(), 100, "edges not added in 1 failed transactions (2 vertices added for each edge)", this.stopWatch());
        vertexCount(graph, 400);
        edgeCount(graph, 200);


        graph.shutdown();
    }

    public void testPropertyTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsElementProperties()) {
            this.stopWatch();
            Vertex v = graph.addVertex(null);
            Object id = v.getId();
            v.setProperty("name", "marko");
            graph.commit();
            printPerformance(graph.toString(), 1, "vertex added with string property in a successful transaction", this.stopWatch());


            this.stopWatch();
            v = graph.getVertex(id);
            assertNotNull(v);
            assertEquals(v.getProperty("name"), "marko");
            v.setProperty("age", 30);
            assertEquals(v.getProperty("age"), 30);
            graph.rollback();
            printPerformance(graph.toString(), 1, "integer property not added in a failed transaction", this.stopWatch());

            this.stopWatch();
            v = graph.getVertex(id);
            assertNotNull(v);
            assertEquals(v.getProperty("name"), "marko");
            assertNull(v.getProperty("age"));
            printPerformance(graph.toString(), 2, "vertex properties checked in a successful transaction", this.stopWatch());

            Edge edge = graph.addEdge(null, v, graph.addVertex(null), "test");
            edgeCount(graph, 1);
            graph.commit();
            edgeCount(graph, 1);
            edge = getOnlyElement(graph.getVertex(v.getId()).getEdges(Direction.OUT));
            assertNotNull(edge);

            this.stopWatch();
            edge.setProperty("transaction-1", "success");
            assertEquals(edge.getProperty("transaction-1"), "success");
            graph.commit();
            printPerformance(graph.toString(), 1, "edge property added and checked in a successful transaction", this.stopWatch());
            edge = getOnlyElement(graph.getVertex(v.getId()).getEdges(Direction.OUT));
            assertEquals(edge.getProperty("transaction-1"), "success");

            this.stopWatch();
            edge.setProperty("transaction-2", "failure");
            assertEquals(edge.getProperty("transaction-1"), "success");
            assertEquals(edge.getProperty("transaction-2"), "failure");
            graph.rollback();
            printPerformance(graph.toString(), 1, "edge property added and checked in a failed transaction", this.stopWatch());
            edge = getOnlyElement(graph.getVertex(v.getId()).getEdges(Direction.OUT));
            assertEquals(edge.getProperty("transaction-1"), "success");
            assertNull(edge.getProperty("transaction-2"));
        }
        graph.shutdown();
    }

    public void testIndexTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIndex) {
            this.stopWatch();
            Index<Vertex> index = ((IndexableGraph) graph).createIndex("txIdx", Vertex.class);
            Vertex v = graph.addVertex(null);
            Object id = v.getId();
            v.setProperty("name", "marko");
            index.put("name", "marko", v);
            vertexCount(graph, 1);
            v = getOnlyElement(((IndexableGraph) graph).getIndex("txIdx", Vertex.class).get("name", "marko"));
            assertEquals(v.getId(), id);
            assertEquals(v.getProperty("name"), "marko");
            graph.commit();
            printPerformance(graph.toString(), 1, "vertex added and retrieved from index in a successful transaction", this.stopWatch());


            this.stopWatch();
            vertexCount(graph, 1);
            v = getOnlyElement(((IndexableGraph) graph).getIndex("txIdx", Vertex.class).get("name", "marko"));
            assertEquals(v.getId(), id);
            assertEquals(v.getProperty("name"), "marko");
            printPerformance(graph.toString(), 1, "vertex retrieved from index outside successful transaction", this.stopWatch());


            this.stopWatch();
            v = graph.addVertex(null);
            v.setProperty("name", "pavel");
            index.put("name", "pavel", v);
            vertexCount(graph, 2);
            v = getOnlyElement(((IndexableGraph) graph).getIndex("txIdx", Vertex.class).get("name", "marko"));
            assertEquals(v.getProperty("name"), "marko");
            v = getOnlyElement(((IndexableGraph) graph).getIndex("txIdx", Vertex.class).get("name", "pavel"));
            assertEquals(v.getProperty("name"), "pavel");
            graph.rollback();
            printPerformance(graph.toString(), 1, "vertex not added in a failed transaction", this.stopWatch());

            this.stopWatch();
            vertexCount(graph, 1);
            assertEquals(count(((IndexableGraph) graph).getIndex("txIdx", Vertex.class).get("name", "pavel")), 0);
            printPerformance(graph.toString(), 1, "vertex not retrieved in a successful transaction", this.stopWatch());
            v = getOnlyElement(((IndexableGraph) graph).getIndex("txIdx", Vertex.class).get("name", "marko"));
            assertEquals(v.getProperty("name"), "marko");
        }
        graph.shutdown();
    }

    // public void testAutomaticIndexKeysRollback()

    public void testAutomaticSuccessfulTransactionOnShutdown() {

        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        if (graph.getFeatures().isPersistent && graph.getFeatures().supportsVertexProperties) {
            Vertex v = graph.addVertex(null);
            Object id = v.getId();
            v.setProperty("count", "1");
            v.setProperty("count", "2");
            graph.shutdown();
            graph = (TransactionalGraph) graphTest.generateGraph();
            Vertex reloadedV = graph.getVertex(id);
            assertEquals("2", reloadedV.getProperty("count"));

        }
        graph.shutdown();
    }

    public void testVertexCountOnPreTransactionCommit() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        Vertex v1 = graph.addVertex(null);
        graph.commit();

        vertexCount(graph, 1);

        Vertex v2 = graph.addVertex(null);
        v1 = graph.getVertex(v1.getId());
        graph.addEdge(null, v1, v2, graphTest.convertLabel("friend"));

        vertexCount(graph, 2);

        graph.commit();

        vertexCount(graph, 2);
        graph.shutdown();
    }

    public void testVertexPropertiesOnPreTransactionCommit() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties) {
            Vertex v1 = graph.addVertex(null);
            v1.setProperty("name", "marko");

            assertEquals(1, v1.getPropertyKeys().size());
            assertTrue(v1.getPropertyKeys().contains("name"));
            assertEquals("marko", v1.getProperty("name"));

            graph.commit();

            assertEquals("marko", v1.getProperty("name"));
        }
        graph.shutdown();
    }

    public void testBulkTransactionsOnEdges() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        for (int i = 0; i < 5; i++) {
            graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), graphTest.convertLabel("test"));
        }
        edgeCount(graph, 5);
        graph.rollback();
        edgeCount(graph, 0);

        for (int i = 0; i < 4; i++) {
            graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), graphTest.convertLabel("test"));
        }
        edgeCount(graph, 4);
        graph.rollback();
        edgeCount(graph, 0);


        for (int i = 0; i < 3; i++) {
            graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), graphTest.convertLabel("test"));
        }
        edgeCount(graph, 3);
        graph.commit();
        edgeCount(graph, 3);

        graph.shutdown();
    }


    public void testCompetingThreads() {
        final TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        int totalThreads = 250;
        final AtomicInteger vertices = new AtomicInteger(0);
        final AtomicInteger edges = new AtomicInteger(0);
        final AtomicInteger completedThreads = new AtomicInteger(0);
        for (int i = 0; i < totalThreads; i++) {
            new Thread() {
                public void run() {
                    Random random = new Random();
                    if (random.nextBoolean()) {
                        Vertex a = graph.addVertex(null);
                        Vertex b = graph.addVertex(null);
                        Edge e = graph.addEdge(null, a, b, graphTest.convertLabel("friend"));

                        if (graph.getFeatures().supportsElementProperties()) {
                            a.setProperty("test", this.getId());
                            b.setProperty("blah", random.nextFloat());
                            e.setProperty("bloop", random.nextInt());
                        }
                        vertices.getAndAdd(2);
                        edges.getAndAdd(1);
                        graph.commit();
                    } else {
                        Vertex a = graph.addVertex(null);
                        Vertex b = graph.addVertex(null);
                        Edge e = graph.addEdge(null, a, b, graphTest.convertLabel("friend"));
                        if (graph.getFeatures().supportsElementProperties()) {
                            a.setProperty("test", this.getId());
                            b.setProperty("blah", random.nextFloat());
                            e.setProperty("bloop", random.nextInt());
                        }
                        if (random.nextBoolean()) {
                            graph.commit();
                            vertices.getAndAdd(2);
                            edges.getAndAdd(1);
                        } else {
                            graph.rollback();
                        }
                    }
                    completedThreads.getAndAdd(1);
                }
            }.start();
        }

        while (completedThreads.get() < totalThreads) {
        }
        assertEquals(completedThreads.get(), 250);
        edgeCount(graph, edges.get());
        vertexCount(graph, vertices.get());
        graph.shutdown();
    }

    public void testCompetingThreadsOnMultipleDbInstances() throws Exception {
        // the idea behind this test is to simulate a rexster environment where two graphs of the same type
        // are being mutated by multiple threads.  the test itself surfaced issues with OrientDB in such
        // an environment and remains relevant for any graph that might be exposed through rexster.

        graphTest.dropGraph("first");
        graphTest.dropGraph("second");

        final TransactionalGraph graph1 = (TransactionalGraph) graphTest.generateGraph("first");
        final TransactionalGraph graph2 = (TransactionalGraph) graphTest.generateGraph("second");


        final Thread threadModFirstGraph = new Thread() {
            public void run() {
                final Vertex v = graph1.addVertex(null);
                // v.setProperty("name", "stephen");
                graph1.commit();
            }
        };

        threadModFirstGraph.start();
        threadModFirstGraph.join();

        final Thread threadReadBothGraphs = new Thread() {
            public void run() {
                int counter = 0;
                for (Vertex v : graph1.getVertices()) {
                    counter++;
                }

                assertEquals(1, counter);

                counter = 0;
                for (Vertex v : graph2.getVertices()) {
                    counter++;
                }

                assertEquals(0, counter);
            }
        };

        threadReadBothGraphs.start();
        threadReadBothGraphs.join();


        graph1.shutdown();
        graphTest.dropGraph("first");

        graph2.shutdown();
        graphTest.dropGraph("second");
    }

    public void testTransactionIsolationCommitCheck() throws Exception {
        // the purpose of this test is to simulate rexster access to a graph instance, where one thread modifies
        // the graph and a separate thread cannot affect the transaction of the first
        final TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();

        final CountDownLatch latchCommittedInOtherThread = new CountDownLatch(1);
        final CountDownLatch latchCommitInOtherThread = new CountDownLatch(1);

        // this thread starts a transaction then waits while the second thread tries to commit it.
        final Thread threadTxStarter = new Thread() {
            public void run() {
                final Vertex v = graph.addVertex(null);

                // System.out.println("added vertex");

                latchCommitInOtherThread.countDown();

                try {
                    latchCommittedInOtherThread.await();
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }

                graph.rollback();

                // there should be no vertices here
                // System.out.println("reading vertex before tx");
                assertFalse(graph.getVertices().iterator().hasNext());
                // System.out.println("read vertex before tx");
            }
        };

        threadTxStarter.start();

        // this thread tries to commit the transaction started in the first thread above.
        final Thread threadTryCommitTx = new Thread() {
            public void run() {
                try {
                    latchCommitInOtherThread.await();
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }

                // try to commit the other transaction
                graph.commit();

                latchCommittedInOtherThread.countDown();
            }
        };

        threadTryCommitTx.start();

        threadTxStarter.join();
        threadTryCommitTx.join();
        graph.shutdown();

    }

    public void testRemoveInTransaction() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        edgeCount(graph, 0);

        Vertex v1 = graph.addVertex(null);
        Object v1id = v1.getId();
        Vertex v2 = graph.addVertex(null);
        graph.addEdge(null, v1, v2, graphTest.convertLabel("test-edge"));
        graph.commit();

        edgeCount(graph, 1);
        Edge e1 = getOnlyElement(graph.getVertex(v1id).getEdges(Direction.OUT));
        assertNotNull(e1);
        graph.removeEdge(e1);
        edgeCount(graph, 0);
        assertNull(getOnlyElement(graph.getVertex(v1id).getEdges(Direction.OUT)));
        graph.rollback();

        edgeCount(graph, 1);
        e1 = getOnlyElement(graph.getVertex(v1id).getEdges(Direction.OUT));
        assertNotNull(e1);

        graph.removeEdge(e1);
        graph.commit();

        edgeCount(graph, 0);
        assertNull(getOnlyElement(graph.getVertex(v1id).getEdges(Direction.OUT)));
        graph.shutdown();
    }

    public void testTransactionGraphHelperFireAndForget() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();

        // first fail the tx
        new TransactionRetryHelper.Builder<Vertex>(graph).perform(new TransactionWork<Vertex>() {
           @Override
           public Vertex execute(final TransactionalGraph graph) throws Exception {
               graph.addVertex(null);
               throw new Exception("fail");
           }
        }).build().fireAndForget();
        vertexCount(graph, 0);

        // this tx will work
        List<Vertex> vin = new ArrayList<Vertex>();
        TransactionRetryHelper<Vertex> trh = new TransactionRetryHelper.Builder<Vertex>(graph).perform(new TransactionWork<Vertex>() {
            @Override
            public Vertex execute(final TransactionalGraph graph) throws Exception{
                return graph.addVertex(null);
            }
        }).build();
        vin.add(trh.fireAndForget());



        vertexCount(graph, 1);
        containsVertices(graph, vin);
    }

    public void testTransactionGraphHelperOneAndDone() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();

        // first fail the tx
        try {
            new TransactionRetryHelper.Builder<Vertex>(graph).perform(new TransactionWork<Vertex>() {
                @Override
                public Vertex execute(final TransactionalGraph graph) throws Exception{
                    graph.addVertex(null);
                    throw new Exception("fail");
                }
            }).build().oneAndDone();
        } catch (Exception ex) {
            assertEquals("fail", ex.getCause().getMessage());
        }

        vertexCount(graph, 0);

        // this tx will work
        List<Vertex> vin = new ArrayList<Vertex>();
        vin.add(new TransactionRetryHelper.Builder<Vertex>(graph).perform(new TransactionWork<Vertex>() {
            @Override
            public Vertex execute(final TransactionalGraph graph) throws Exception{
                return graph.addVertex(null);
            }
        }).build().oneAndDone());

        vertexCount(graph, 1);
        containsVertices(graph, vin);
    }

    public void testTransactionGraphHelperExponentialBackoff() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();

        // first fail the tx
        final AtomicInteger attempts = new AtomicInteger(0);
        try {
            new TransactionRetryHelper.Builder<Vertex>(graph).perform(new TransactionWork<Vertex>() {
                @Override
                public Vertex execute(final TransactionalGraph graph) throws Exception {
                    graph.addVertex(null);
                    attempts.incrementAndGet();
                    throw new Exception("fail");
                }
            }).build().exponentialBackoff();
        } catch (Exception ex) {
            assertEquals("fail", ex.getCause().getMessage());
        }

        assertEquals(TransactionRetryStrategy.DelayedRetry.DEFAULT_TRIES, attempts.get());
        vertexCount(graph, 0);

        // this tx will work after several tries
        final AtomicInteger tries = new AtomicInteger(0);
        List<Vertex> vin = new ArrayList<Vertex>();
        vin.add(new TransactionRetryHelper.Builder<Vertex>(graph).perform(new TransactionWork<Vertex>() {
            @Override
            public Vertex execute(final TransactionalGraph graph) throws Exception{
                final int tryNumber = tries.incrementAndGet();
                if (tryNumber == TransactionRetryStrategy.DelayedRetry.DEFAULT_TRIES - 2)
                    return graph.addVertex(null);
                else
                    throw new Exception("fail");
            }
        }).build().exponentialBackoff());

        vertexCount(graph, 1);
        containsVertices(graph, vin);
    }

    public void testTransactionGraphHelperExponentialBackoffWithExceptionChecks() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        Set<Class> exceptions = new HashSet<Class>() {{
            add(IllegalStateException.class);
        }};

        // immediately fail the tx
        final AtomicInteger attempts = new AtomicInteger(0);
        try {
            new TransactionRetryHelper.Builder<Vertex>(graph).perform(new TransactionWork<Vertex>() {
                @Override
                public Vertex execute(final TransactionalGraph graph) throws Exception {
                    graph.addVertex(null);
                    attempts.incrementAndGet();
                    throw new Exception("fail");
                }
            }).build().exponentialBackoff(TransactionRetryStrategy.DelayedRetry.DEFAULT_TRIES, 20, exceptions);
        } catch (Exception ex) {
            assertEquals("fail", ex.getCause().getMessage());
        }

        assertEquals(1, attempts.get());
        vertexCount(graph, 0);

        // this tx will fail after a few tries due to exception raised not in set
        final AtomicInteger setOfTries = new AtomicInteger(0);
        try {
            new TransactionRetryHelper.Builder<Vertex>(graph).perform(new TransactionWork<Vertex>() {
                @Override
                public Vertex execute(final TransactionalGraph graph) throws Exception{
                    final int tryNumber = setOfTries.incrementAndGet();
                    if (tryNumber == TransactionRetryStrategy.DelayedRetry.DEFAULT_TRIES - 2)
                        throw new Exception("fail");
                    else
                        throw new IllegalStateException("fail");
                }
            }).build().exponentialBackoff(TransactionRetryStrategy.DelayedRetry.DEFAULT_TRIES, 20, exceptions);
        } catch (Exception ex) {
            assertEquals("fail", ex.getCause().getMessage());
        }

        assertEquals(TransactionRetryStrategy.DelayedRetry.DEFAULT_TRIES - 2, setOfTries.get());
        vertexCount(graph, 0);

        // this tx will work after several tries
        final AtomicInteger tries = new AtomicInteger(0);
        List<Vertex> vin = new ArrayList<Vertex>();
        vin.add(new TransactionRetryHelper.Builder<Vertex>(graph).perform(new TransactionWork<Vertex>() {
            @Override
            public Vertex execute(final TransactionalGraph graph) throws Exception{
                final int tryNumber = tries.incrementAndGet();
                if (tryNumber == TransactionRetryStrategy.DelayedRetry.DEFAULT_TRIES - 2)
                    return graph.addVertex(null);
                else
                    throw new IllegalStateException("fail");
            }
        }).build().exponentialBackoff(TransactionRetryStrategy.DelayedRetry.DEFAULT_TRIES, 20, exceptions));

        vertexCount(graph, 1);
        containsVertices(graph, vin);
    }

    public void testTransactionGraphHelperRetry() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();

        // first fail the tx
        final AtomicInteger attempts = new AtomicInteger(0);
        try {
            new TransactionRetryHelper.Builder<Vertex>(graph).perform(new TransactionWork<Vertex>() {
                @Override
                public Vertex execute(final TransactionalGraph graph) throws Exception {
                    graph.addVertex(null);
                    attempts.incrementAndGet();
                    throw new Exception("fail");
                }
            }).build().retry();
        } catch (Exception ex) {
            assertEquals("fail", ex.getCause().getMessage());
        }

        assertEquals(TransactionRetryStrategy.DelayedRetry.DEFAULT_TRIES, attempts.get());
        vertexCount(graph, 0);

        // this tx will work after several tries
        final AtomicInteger tries = new AtomicInteger(0);
        List<Vertex> vin = new ArrayList<Vertex>();
        vin.add(new TransactionRetryHelper.Builder<Vertex>(graph).perform(new TransactionWork<Vertex>() {
            @Override
            public Vertex execute(final TransactionalGraph graph) throws Exception {
                final int tryNumber = tries.incrementAndGet();
                if (tryNumber == TransactionRetryStrategy.DelayedRetry.DEFAULT_TRIES - 2)
                    return graph.addVertex(null);
                else
                    throw new Exception("fail");
            }
        }).build().retry());

        vertexCount(graph, 1);
        containsVertices(graph, vin);
    }

    public void untestSimulateRexsterIntegrationTests() throws Exception {
        // this test simulates the flow of rexster integration test.  integration tests requests are generally not made
        // in parallel, but it is expected each request they may be processed by different threads from a thread pool
        // for each request.  this test fails for orientdb given it's optimnisitc locking strategy.
        final TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsKeyIndices) {
            final String id = "_ID";
            ((KeyIndexableGraph) graph).createKeyIndex(id, Vertex.class);

            final int numberOfVerticesToCreate = 100;
            final Random rand = new Random(12356);
            final List<String> graphAssignedIds = new ArrayList<String>();

            final ExecutorService executorService = Executors.newFixedThreadPool(4);

            for (int ix = 0; ix < numberOfVerticesToCreate; ix++) {
                final int id1 = ix;
                final int id2 = ix + numberOfVerticesToCreate + rand.nextInt();

                // add a vertex and block for the thread to complete
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        final Vertex v = graph.addVertex(null);
                        v.setProperty(id, id1);
                        graph.commit();

                        graphAssignedIds.add(v.getId().toString());
                    }
                }).get();

                if (ix > 0) {
                    // add a vertex and block for the thread to complete
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            final Vertex v = graph.addVertex(null);
                            v.setProperty(id, id2);
                            graph.commit();

                            graphAssignedIds.add(v.getId().toString());
                        }
                    }).get();

                    // add an edge to two randomly selected vertices and block for the thread to complete. integration
                    // tests tend to fail here, so the code is replicated pretty closely to what is in rexster
                    // (i.e. serialization to JSON) even though that may have nothing to do with failures.
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            final Vertex vActual1 = graph.getVertex(graphAssignedIds.get(rand.nextInt(graphAssignedIds.size())));
                            final Vertex vActual2 = graph.getVertex(graphAssignedIds.get(rand.nextInt(graphAssignedIds.size())));
                            final Edge e = graph.addEdge(null, vActual1, vActual2, "knows");
                            e.setProperty("weight", rand.nextFloat());

                            JSONObject elementJson = null;
                            try {
                                // just replicating rexster
                                elementJson = GraphSONUtility.jsonFromElement(e, null, GraphSONMode.NORMAL);
                            } catch (Exception ex) {
                                fail();
                            }

                            graph.commit();

                            try {
                                if (elementJson != null) {
                                    // just replicating rexster
                                    elementJson.put("_ID", e.getId());
                                }
                            } catch (Exception ex) {
                                fail();
                            }
                        }
                    }).get();
                }
            }

            final Set<String> ids = new HashSet<String>();
            for (final Vertex v : graph.getVertices()) {
                ids.add(v.getId().toString());
            }

            for (final String idToRemove : ids) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        final Vertex toRemove = graph.getVertex(idToRemove);
                        graph.removeVertex(toRemove);

                        graph.commit();
                    }
                });
            }

            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        }

        graph.shutdown();
    }

    public void untestSimulateRexsterIntegrationTestsWithRetries() throws Exception {
        // this test simulates the flow of rexster integration test. integration tests requests are generally not made
        // in parallel, but it is expected each request they may be processed by different threads from a thread pool
        // for each request...this test is similar to the previous one but includes retries.  in this case,
        // orientdb passes, but this isn't currently how Rexster integration tests work.
        final TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsKeyIndices) {
            final String id = "_ID";
            ((KeyIndexableGraph) graph).createKeyIndex(id, Vertex.class);

            final int maxRetries = 10;
            final int numberOfVerticesToCreate = 100;
            final Random rand = new Random(12356);
            final List<String> graphAssignedIds = new ArrayList<String>();

            final ExecutorService executorService = Executors.newFixedThreadPool(4);

            for (int ix = 0; ix < numberOfVerticesToCreate; ix++) {
                final int id1 = ix;
                final int id2 = ix + numberOfVerticesToCreate + rand.nextInt();

                // add a vertex and block for the thread to complete
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        final Vertex v = graph.addVertex(null);
                        v.setProperty(id, id1);
                        graph.commit();

                        graphAssignedIds.add(v.getId().toString());
                    }
                }).get();

                if (ix > 0) {
                    // add a vertex and block for the thread to complete
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            final Vertex v = graph.addVertex(null);
                            v.setProperty(id, id2);
                            graph.commit();

                            graphAssignedIds.add(v.getId().toString());
                        }
                    }).get();

                    // add an edge to two randomly selected vertices and block for the thread to complete. integration
                    // tests tend to fail here, so the code is replicated pretty closely to what is in rexster
                    // (i.e. serialization to JSON) even though that may have nothing to do with failures.
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            int v1 = rand.nextInt(graphAssignedIds.size());
                            int v2 = rand.nextInt(graphAssignedIds.size());

                            for (int retry = 0; retry < maxRetries; ++retry) {
                                try {
                                    final Vertex vActual1 = graph.getVertex(graphAssignedIds.get(v1));
                                    final Vertex vActual2 = graph.getVertex(graphAssignedIds.get(v2));
                                    final Edge e = graph.addEdge(null, vActual1, vActual2, "knows");
                                    e.setProperty("weight", rand.nextFloat());

                                    // just replicating rexster
                                    final JSONObject elementJson = GraphSONUtility.jsonFromElement(e, null, GraphSONMode.NORMAL);

                                    graph.commit();

                                    if (elementJson != null) {
                                        // just replicating rexster
                                        elementJson.put("_ID", e.getId());
                                    }
                                    break;
                                } catch (Exception ex) {
                                    if (!ex.getClass().getSimpleName().equals("OConcurrentModificationException"))
                                        fail(ex.getMessage());
                                }
                            }
                        }
                    }).get();
                }
            }

            final Set<String> ids = new HashSet<String>();
            for (final Vertex v : graph.getVertices()) {
                ids.add(v.getId().toString());
            }

            for (final String idToRemove : ids) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        final Vertex toRemove = graph.getVertex(idToRemove);
                        graph.removeVertex(toRemove);

                        graph.commit();
                    }
                });
            }

            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        }

        graph.shutdown();
    }

    public void untestTransactionVertexPropertiesAcrossThreads() throws Exception {
        // the purpose of this test is to ensure that properties of a element are available prior to commit()
        // across threads
        final TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();

        final AtomicReference<Vertex> v = new AtomicReference<Vertex>();
        final Thread thread = new Thread() {
            public void run() {
                final Vertex vertex = graph.addVertex(null);
                vertex.setProperty("name", "stephen");
                v.set(vertex);
            }
        };

        thread.start();
        thread.join();

        Set<String> k = v.get().getPropertyKeys();
        assertTrue(k.contains("name"));
        assertEquals("stephen", v.get().getProperty("name"));
    }

    public void untestTransactionIsolationWithSeparateThreads() throws Exception {
        // the purpose of this test is to simulate rexster access to a graph instance, where one thread modifies
        // the graph and a separate thread reads before the transaction is committed.  the expectation is that
        // the changes in the transaction are isolated to the thread that made the change and the second thread
        // should not see the change until commit() in the first thread.
        final TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();

        final CountDownLatch latchCommit = new CountDownLatch(1);
        final CountDownLatch latchFirstRead = new CountDownLatch(1);
        final CountDownLatch latchSecondRead = new CountDownLatch(1);

        final Thread threadMod = new Thread() {
            public void run() {
                final Vertex v = graph.addVertex(null);
                //v.setProperty("name", "stephen");

                // System.out.println("added vertex");

                latchFirstRead.countDown();

                try {
                    latchCommit.await();
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }

                graph.commit();

                // System.out.println("committed vertex");

                latchSecondRead.countDown();
            }
        };

        threadMod.start();

        final Thread threadRead = new Thread() {
            public void run() {
                try {
                    latchFirstRead.await();
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }

                // System.out.println("reading vertex before tx");
                assertFalse(graph.getVertices().iterator().hasNext());
                // System.out.println("read vertex before tx");

                latchCommit.countDown();

                try {
                    latchSecondRead.await();
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }

                // System.out.println("reading vertex after tx");
                assertTrue(graph.getVertices().iterator().hasNext());
                // System.out.println("read vertex after tx");
            }
        };

        threadRead.start();

        threadMod.join();
        threadRead.join();


        graph.shutdown();

    }
}