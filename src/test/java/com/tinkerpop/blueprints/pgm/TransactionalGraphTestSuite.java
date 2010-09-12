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
            graph.setAutoTransactions(false);

            graph.startTransaction();
            try {
                graph.addVertex(convertId("1"));
                assertTrue(true);
            } catch (Exception e) {
                assertTrue(false);
            }
            graph.stopTransaction(false);
            graph.startTransaction();
            assertEquals(count(graph.getVertices()), 1);
            graph.startTransaction();
            try {
                graph.addVertex(convertId("2"));
                assertTrue(true);
            } catch (Exception e) {
                assertTrue(false);
            }
            graph.stopTransaction(true);
            assertEquals(count(graph.getVertices()), 2);

        }
    }

    public void testBruteVertexTransactions(final TransactionalGraph graph) {

        if (config.supportsVertexIteration) {
            graph.setAutoTransactions(false);
            for (int i = 0; i < 100; i++) {
                graph.startTransaction();
                graph.addVertex(convertId(i + ""));
                graph.stopTransaction(true);
            }
            graph.startTransaction();
            assertEquals(count(graph.getVertices()), 100);
            graph.stopTransaction(true);
            for (int i = 100; i < 200; i++) {
                graph.startTransaction();
                graph.addVertex(convertId(i + ""));
                graph.stopTransaction(false);
            }
            graph.startTransaction();
            assertEquals(count(graph.getVertices()), 100);
            graph.stopTransaction(true);
        }
    }

    public void testTransactionsForEdges(TransactionalGraph graph) {

        Vertex v = graph.addVertex(convertId("1"));
        Vertex u = graph.addVertex(convertId("2"));
        graph.setAutoTransactions(false);

        graph.startTransaction();
        try {
            graph.addEdge(null, v, u, convertId("test"));
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }
        graph.stopTransaction(false);
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
        graph.stopTransaction(true);
        graph.startTransaction();
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 2);
        if (config.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 1);
        graph.stopTransaction(true);


    }

    public void testBruteEdgeTransactions(TransactionalGraph graph) {

        graph.setAutoTransactions(false);
        for (int i = 0; i < 100; i++) {
            graph.startTransaction();
            Vertex v = graph.addVertex(convertId("" + i));
            Vertex u = graph.addVertex(convertId("" + (100 + i)));
            graph.addEdge(null, v, u, convertId("test"));
            graph.stopTransaction(true);
        }
        graph.startTransaction();
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 200);
        if (config.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 100);
        graph.stopTransaction(true);
        for (int i = 0; i < 100; i++) {
            graph.startTransaction();
            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            graph.addEdge(null, v, u, convertId("test"));
            graph.stopTransaction(false);
        }
        graph.startTransaction();
        if (config.supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 200);
        if (config.supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 100);
        graph.stopTransaction(true);
    }

    public void testPropertyTransactions(TransactionalGraph graph) {
        graph.setAutoTransactions(false);
        graph.startTransaction();
        Vertex v = graph.addVertex(convertId("1"));
        Object id = v.getId();
        v.setProperty("name", "marko");
        graph.stopTransaction(true);

        graph.startTransaction();
        v = graph.getVertex(id);
        assertNotNull(v);
        assertEquals(v.getProperty("name"), "marko");
        v.setProperty("age", 30);
        assertEquals(v.getProperty("age"), 30);
        graph.stopTransaction(false);

        graph.startTransaction();
        v = graph.getVertex(id);
        assertNotNull(v);
        assertEquals(v.getProperty("name"), "marko");
        assertNull(v.getProperty("age"));
        graph.stopTransaction(true);

    }


}
