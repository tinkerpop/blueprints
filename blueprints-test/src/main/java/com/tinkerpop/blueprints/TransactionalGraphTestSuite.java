package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.impls.GraphTest;

import java.util.ArrayList;
import java.util.List;
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

    public void testTrue() {
        assertTrue(true);
    }

    public void testRepeatedTransactionStopException() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        graph.stopTransaction(Conclusion.SUCCESS);
        graph.stopTransaction(Conclusion.FAILURE);
        graph.stopTransaction(Conclusion.SUCCESS);
        graph.shutdown();
    }

    public void testAutoStartTransaction() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        Vertex v1 = graph.addVertex(null);
        vertexCount(graph,1);
        assertEquals(v1.getId(), graph.getVertex(v1.getId()).getId());
        graph.stopTransaction(Conclusion.SUCCESS);
        vertexCount(graph,1);
        assertEquals(v1.getId(),graph.getVertex(v1.getId()).getId());
        graph.shutdown();

    }


    public void testTransactionsForVertices() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        List<Vertex> vin = new ArrayList<Vertex>();
        List<Vertex> vout = new ArrayList<Vertex>();
        vin.add(graph.addVertex(null));
        graph.stopTransaction(Conclusion.SUCCESS);
        vertexCount(graph,1);
        containsVertices(graph,vin);
        
        this.stopWatch();
        vout.add(graph.addVertex(null));
        vertexCount(graph,2);
        containsVertices(graph,vin); containsVertices(graph,vout);
        graph.stopTransaction(Conclusion.FAILURE);

        vertexExistence(graph, vin, vout);
        vertexCount(graph,1);
        printPerformance(graph.toString(), 1, "vertex not added in failed transaction", this.stopWatch());

        this.stopWatch();
        vin.add(graph.addVertex(null));
        vertexCount(graph,2);
        vertexExistence(graph, vin, vout);
        graph.stopTransaction(Conclusion.SUCCESS);
        printPerformance(graph.toString(), 1, "vertex added in successful transaction", this.stopWatch());
        vertexCount(graph,2);
        vertexExistence(graph, vin, vout);

        graph.shutdown();
    }

    public void testBruteVertexTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        List<Vertex> vin = new ArrayList<Vertex>(), vout = new ArrayList<Vertex>();
        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            vin.add(graph.addVertex(null));
            graph.stopTransaction(Conclusion.SUCCESS);
        }
        printPerformance(graph.toString(), 100, "vertices added in 100 successful transactions", this.stopWatch());
        vertexCount(graph,100);
        containsVertices(graph,vin);

        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            vout.add(graph.addVertex(null));
            graph.stopTransaction(Conclusion.FAILURE);
        }
        printPerformance(graph.toString(), 100, "vertices not added in 100 failed transactions", this.stopWatch());

        vertexCount(graph,100);
        vertexExistence(graph,vin,vout);
        graph.stopTransaction(Conclusion.FAILURE);
        vertexCount(graph,100);
        vertexExistence(graph,vin,vout);

        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            vin.add(graph.addVertex(null));
        }
        vertexCount(graph,200);
        vertexExistence(graph,vin,vout);
        graph.stopTransaction(Conclusion.SUCCESS);
        printPerformance(graph.toString(), 100, "vertices added in 1 successful transactions", this.stopWatch());
        vertexCount(graph,200);
        vertexExistence(graph,vin,vout);

        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            vout.add(graph.addVertex(null));
        }
        vertexCount(graph,300);
        vertexExistence(graph,vin,vout.subList(0,100)); containsVertices(graph,vout.subList(100,200));
        graph.stopTransaction(Conclusion.FAILURE);
        printPerformance(graph.toString(), 100, "vertices not added in 1 failed transactions", this.stopWatch());
        vertexCount(graph,200);
        vertexExistence(graph,vin,vout);
        graph.shutdown();
    }

    public void testTransactionsForEdges() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();

        Vertex v = graph.addVertex(null);
        Vertex u = graph.addVertex(null);
        graph.stopTransaction(Conclusion.SUCCESS);

        this.stopWatch();
        Edge e = graph.addEdge(null, graph.getVertex(v.getId()), graph.getVertex(u.getId()), convertId(graph, "test"));


        assertEquals(graph.getVertex(v.getId()), v);
        assertEquals(graph.getVertex(u.getId()), u);
        if (graph.getFeatures().supportsEdgeRetrieval)
            assertEquals(graph.getEdge(e.getId()), e);

        vertexCount(graph,2);
        edgeCount(graph,1);

        graph.stopTransaction(Conclusion.FAILURE);
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
        
        e = graph.addEdge(null, graph.getVertex(u.getId()), graph.getVertex(v.getId()), convertId(graph, "test"));

        assertEquals(graph.getVertex(v.getId()), v);
        assertEquals(graph.getVertex(u.getId()), u);
        if (graph.getFeatures().supportsEdgeRetrieval)
            assertEquals(graph.getEdge(e.getId()), e);

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 2);
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 1);
        assertEquals(e,getOnlyElement(graph.getVertex(u.getId()).getEdges(Direction.OUT)));
        graph.stopTransaction(Conclusion.SUCCESS);
        printPerformance(graph.toString(), 1, "edge added in successful transaction (w/ iteration)", this.stopWatch());

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 2);
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 1);

        assertEquals(graph.getVertex(v.getId()), v);
        assertEquals(graph.getVertex(u.getId()), u);
        if (graph.getFeatures().supportsEdgeRetrieval)
            assertEquals(graph.getEdge(e.getId()), e);
        assertEquals(e,getOnlyElement(graph.getVertex(u.getId()).getEdges(Direction.OUT)));

        graph.shutdown();
    }

    public void testBruteEdgeTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, convertId(graph, "test"));
            graph.stopTransaction(Conclusion.SUCCESS);
        }
        printPerformance(graph.toString(), 100, "edges added in 100 successful transactions (2 vertices added for each edge)", this.stopWatch());
        vertexCount(graph,200);
        edgeCount(graph,100);

        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, convertId(graph, "test"));
            graph.stopTransaction(Conclusion.FAILURE);
        }
        printPerformance(graph.toString(), 100, "edges not added in 100 failed transactions (2 vertices added for each edge)", this.stopWatch());
        vertexCount(graph,200);
        edgeCount(graph,100);

        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, convertId(graph, "test"));
        }
        vertexCount(graph,400);
        edgeCount(graph,200);
        graph.stopTransaction(Conclusion.SUCCESS);
        printPerformance(graph.toString(), 100, "edges added in 1 successful transactions (2 vertices added for each edge)", this.stopWatch());
        vertexCount(graph,400);
        edgeCount(graph,200);

        this.stopWatch();
        for (int i = 0; i < 100; i++) {
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, convertId(graph, "test"));
        }
        vertexCount(graph,600);
        edgeCount(graph,300);

        graph.stopTransaction(Conclusion.FAILURE);
        printPerformance(graph.toString(), 100, "edges not added in 1 failed transactions (2 vertices added for each edge)", this.stopWatch());
        vertexCount(graph,400);
        edgeCount(graph,200);


        graph.shutdown();
    }

    public void testPropertyTransactions() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        if (graph.getFeatures().supportsElementProperties()) {
            this.stopWatch();
            Vertex v = graph.addVertex(null);
            Object id = v.getId();
            v.setProperty("name", "marko");
            graph.stopTransaction(Conclusion.SUCCESS);
            printPerformance(graph.toString(), 1, "vertex added with string property in a successful transaction", this.stopWatch());


            this.stopWatch();
            v = graph.getVertex(id);
            assertNotNull(v);
            assertEquals(v.getProperty("name"), "marko");
            v.setProperty("age", 30);
            assertEquals(v.getProperty("age"), 30);
            graph.stopTransaction(Conclusion.FAILURE);
            printPerformance(graph.toString(), 1, "integer property not added in a failed transaction", this.stopWatch());

            this.stopWatch();
            v = graph.getVertex(id);
            assertNotNull(v);
            assertEquals(v.getProperty("name"), "marko");
            assertNull(v.getProperty("age"));
            printPerformance(graph.toString(), 2, "vertex properties checked in a successful transaction", this.stopWatch());

            Edge edge = graph.addEdge(null, v, graph.addVertex(null), "test");
            edgeCount(graph,1);
            graph.stopTransaction(Conclusion.SUCCESS);
            edgeCount(graph,1);
            edge = getOnlyElement(graph.getVertex(v.getId()).getEdges(Direction.OUT));
            assertNotNull(edge);

            this.stopWatch();
            edge.setProperty("transaction-1", "success");
            assertEquals(edge.getProperty("transaction-1"), "success");
            graph.stopTransaction(Conclusion.SUCCESS);
            printPerformance(graph.toString(), 1, "edge property added and checked in a successful transaction", this.stopWatch());
            edge = getOnlyElement(graph.getVertex(v.getId()).getEdges(Direction.OUT));
            assertEquals(edge.getProperty("transaction-1"), "success");

            this.stopWatch();
            edge.setProperty("transaction-2", "failure");
            assertEquals(edge.getProperty("transaction-1"), "success");
            assertEquals(edge.getProperty("transaction-2"), "failure");
            graph.stopTransaction(Conclusion.FAILURE);
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
            vertexCount(graph,1);
            v = getOnlyElement(((IndexableGraph) graph).getIndex("txIdx", Vertex.class).get("name", "marko"));
            assertEquals(v.getId(), id);
            assertEquals(v.getProperty("name"), "marko");
            graph.stopTransaction(Conclusion.SUCCESS);
            printPerformance(graph.toString(), 1, "vertex added and retrieved from index in a successful transaction", this.stopWatch());


            this.stopWatch();
            vertexCount(graph,1);
            v = getOnlyElement(((IndexableGraph) graph).getIndex("txIdx", Vertex.class).get("name", "marko"));
            assertEquals(v.getId(), id);
            assertEquals(v.getProperty("name"), "marko");
            printPerformance(graph.toString(), 1, "vertex retrieved from index outside successful transaction", this.stopWatch());


            this.stopWatch();
            v = graph.addVertex(null);
            v.setProperty("name", "pavel");
            index.put("name", "pavel", v);
            vertexCount(graph,2);
            v = getOnlyElement(((IndexableGraph) graph).getIndex("txIdx", Vertex.class).get("name", "marko"));
            assertEquals(v.getProperty("name"), "marko");
            v = getOnlyElement(((IndexableGraph) graph).getIndex("txIdx", Vertex.class).get("name", "pavel"));
            assertEquals(v.getProperty("name"), "pavel");
            graph.stopTransaction(Conclusion.FAILURE);
            printPerformance(graph.toString(), 1, "vertex not added in a failed transaction", this.stopWatch());

            this.stopWatch();
            vertexCount(graph,1);
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


    public void testBulkTransactionsOnEdges() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        for (int i = 0; i < 5; i++) {
            graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), convertId(graph, "test"));
        }
        edgeCount(graph,5);
        graph.stopTransaction(Conclusion.FAILURE);
        edgeCount(graph,0);

        for (int i = 0; i < 4; i++) {
            graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), convertId(graph, "test"));
        }
        edgeCount(graph,4);
        graph.stopTransaction(Conclusion.FAILURE);
        edgeCount(graph,0);


        for (int i = 0; i < 3; i++) {
            graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), convertId(graph, "test"));
        }
        edgeCount(graph,3);
        graph.stopTransaction(Conclusion.SUCCESS);
        edgeCount(graph,3);

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
                        Edge e = graph.addEdge(null, a, b, convertId(graph, "friend"));

                        if (graph.getFeatures().supportsElementProperties()) {
                            a.setProperty("test", this.getId());
                            b.setProperty("blah", random.nextFloat());
                            e.setProperty("bloop", random.nextInt());
                        }
                        vertices.getAndAdd(2);
                        edges.getAndAdd(1);
                        graph.stopTransaction(Conclusion.SUCCESS);
                    } else {
                        Vertex a = graph.addVertex(null);
                        Vertex b = graph.addVertex(null);
                        Edge e = graph.addEdge(null, a, b, convertId(graph, "friend"));
                        if (graph.getFeatures().supportsElementProperties()) {
                            a.setProperty("test", this.getId());
                            b.setProperty("blah", random.nextFloat());
                            e.setProperty("bloop", random.nextInt());
                        }
                        if (random.nextBoolean()) {
                            graph.stopTransaction(Conclusion.SUCCESS);
                            vertices.getAndAdd(2);
                            edges.getAndAdd(1);
                        } else {
                            graph.stopTransaction(Conclusion.FAILURE);
                        }
                    }
                    completedThreads.getAndAdd(1);
                }
            }.start();
        }

        while (completedThreads.get() < totalThreads) {
        }
        assertEquals(completedThreads.get(), 250);
        edgeCount(graph,edges.get());
        vertexCount(graph,vertices.get());
        graph.shutdown();
    }

    public void testRemoveInTransaction() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        edgeCount(graph,0);

        Vertex v1 = graph.addVertex(null);
        Object v1id = v1.getId();
        Vertex v2 = graph.addVertex(null);
        Edge e1 = graph.addEdge(null, v1, v2, convertId(graph, "test-edge"));
        graph.stopTransaction(Conclusion.SUCCESS);

        edgeCount(graph,1);
        e1 = getOnlyElement(graph.getVertex(v1id).getEdges(Direction.OUT));
        assertNotNull(e1);
        graph.removeEdge(e1);
        edgeCount(graph,0);
        assertNull(getOnlyElement(graph.getVertex(v1id).getEdges(Direction.OUT)));
        graph.stopTransaction(Conclusion.FAILURE);

        edgeCount(graph,1);
        e1 = getOnlyElement(graph.getVertex(v1id).getEdges(Direction.OUT));
        assertNotNull(e1);

        graph.removeEdge(e1);
        graph.stopTransaction(Conclusion.SUCCESS);

        edgeCount(graph,0);
        assertNull(getOnlyElement(graph.getVertex(v1id).getEdges(Direction.OUT)));
        graph.shutdown();
    }

}
