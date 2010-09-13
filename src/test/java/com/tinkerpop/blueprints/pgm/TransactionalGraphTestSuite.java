package com.tinkerpop.blueprints.pgm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TransactionalGraphTestSuite extends ModelTestSuite {

    public TransactionalGraphTestSuite() {
    }

    public TransactionalGraphTestSuite(final SuiteConfiguration config) {
        super(config);
    }


    public void testTransactionsForVertices(TransactionalGraph graph) {

        if (config.supportsVertexIteration) {
            graph.addVertex(null);
            graph.setTransactionMode(TransactionalGraph.Mode.MANUAL);

            graph.startTransaction();
            try {
                graph.addVertex(null);
                assertTrue(true);
            } catch (Exception e) {
                assertTrue(false);
            }
            graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);

            graph.startTransaction();
            assertEquals(count(graph.getVertices()), 1);
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

            graph.startTransaction();
            try {
                graph.addVertex(null);
                assertTrue(true);
            } catch (Exception e) {
                assertTrue(false);
            }
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

            graph.startTransaction();
            assertEquals(count(graph.getVertices()), 2);
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

        }
    }

    public void testBruteVertexTransactions(final TransactionalGraph graph) {

        if (config.supportsVertexIteration) {
            graph.setTransactionMode(TransactionalGraph.Mode.MANUAL);
            for (int i = 0; i < 100; i++) {
                graph.startTransaction();
                graph.addVertex(null);
                graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            }
            graph.startTransaction();
            assertEquals(count(graph.getVertices()), 100);
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            for (int i = 100; i < 200; i++) {
                graph.startTransaction();
                graph.addVertex(null);
                graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
            }
            graph.startTransaction();
            assertEquals(count(graph.getVertices()), 100);
            graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
        }
    }

    public void testTransactionsForEdges(TransactionalGraph graph) {

        Vertex v = graph.addVertex(null);
        Vertex u = graph.addVertex(null);
        graph.setTransactionMode(TransactionalGraph.Mode.MANUAL);

        graph.startTransaction();
        try {
            graph.addEdge(null, v, u, convertId("test"));
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }
        graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
        graph.startTransaction();
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 2);
        if (config.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 0);
        try {
            graph.addEdge(null, u, v, convertId("test"));
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }
        graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

        graph.startTransaction();
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 2);
        if (config.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 1);
        graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);


    }

    public void testBruteEdgeTransactions(TransactionalGraph graph) {

        graph.setTransactionMode(TransactionalGraph.Mode.MANUAL);
        for (int i = 0; i < 100; i++) {
            graph.startTransaction();
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, convertId("test"));
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        }
        graph.startTransaction();
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 200);
        if (config.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 100);
        graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        for (int i = 0; i < 100; i++) {
            graph.startTransaction();
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, convertId("test"));
            graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);
        }
        graph.startTransaction();
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 200);
        if (config.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 100);
        graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
    }

    public void testPropertyTransactions(TransactionalGraph graph) {
        graph.setTransactionMode(TransactionalGraph.Mode.MANUAL);
        graph.startTransaction();
        Vertex v = graph.addVertex(null);
        Object id = v.getId();
        v.setProperty("name", "marko");
        graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

        graph.startTransaction();
        v = graph.getVertex(id);
        assertNotNull(v);
        assertEquals(v.getProperty("name"), "marko");
        v.setProperty("age", 30);
        assertEquals(v.getProperty("age"), 30);
        graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);

        graph.startTransaction();
        v = graph.getVertex(id);
        assertNotNull(v);
        assertEquals(v.getProperty("name"), "marko");
        assertNull(v.getProperty("age"));
        graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

    }

    public void testIndexTransactions(TransactionalGraph graph) {
        if (!config.isRDFModel && config.supportsVertexIndex) {

            graph.setTransactionMode(TransactionalGraph.Mode.MANUAL);
            graph.startTransaction();
            Vertex v = graph.addVertex(null);
            Object id = v.getId();
            v.setProperty("name", "marko");
            if (config.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 1);
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

            graph.startTransaction();
            v = (Vertex) graph.getIndex().get("name", "marko").iterator().next();
            assertEquals(v.getId(), id);
            assertEquals(v.getProperty("name"), "marko");
            if (config.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 1);
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);

            graph.startTransaction();
            v = graph.addVertex(null);
            v.setProperty("name", "pavel");
            if (config.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 2);
            graph.stopTransaction(TransactionalGraph.Conclusion.FAILURE);

            graph.startTransaction();
            if (config.supportsVertexIteration)
                assertEquals(count(graph.getVertices()), 1);
            assertNull(graph.getIndex().get("name", "pavel"));
            graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);


        }

    }


}
