package com.tinkerpop.blueprints.util.wrappers.batch;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.MockTransactionalTinkerGraph;
import junit.framework.TestCase;

/**
 * Tests {@link BatchGraph} by creating a variable length chain and verifying that the chain is correctly inserted into the wrapped TinkerGraph.
 * <p/>
 * Tests the various different Vertex caches and different length of chains.
 * <p/>
 * (c) Matthias Broecheler (http://www.matthiasb.com)
 */

public class BatchGraphTest extends TestCase {

    private static final String UID = "uid";

    private static final String vertexIDKey = "vid";
    private static final String edgeIDKey = "eid";
    private static boolean assignKeys = false;

    public void testNumberIdLoading() {
        loadingTest(5000, 100, BatchGraph.IDType.NUMBER, new NumberLoadingFactory());
        loadingTest(200000, 10000, BatchGraph.IDType.NUMBER, new NumberLoadingFactory());

        assignKeys = true;
        loadingTest(5000, 100, BatchGraph.IDType.NUMBER, new NumberLoadingFactory());
        loadingTest(50000, 10000, BatchGraph.IDType.NUMBER, new NumberLoadingFactory());
        assignKeys = false;
    }

    public void testObjectIdLoading() {
        loadingTest(5000, 100, BatchGraph.IDType.OBJECT, new StringLoadingFactory());
        loadingTest(200000, 10000, BatchGraph.IDType.OBJECT, new StringLoadingFactory());
    }

    public void testStringIdLoading() {
        loadingTest(5000, 100, BatchGraph.IDType.STRING, new StringLoadingFactory());
        loadingTest(200000, 10000, BatchGraph.IDType.STRING, new StringLoadingFactory());
    }

    public void testURLIdLoading() {
        loadingTest(5000, 100, BatchGraph.IDType.URL, new URLLoadingFactory());
        loadingTest(200000, 10000, BatchGraph.IDType.URL, new URLLoadingFactory());
    }


    public void loadingTest(int total, int bufferSize, BatchGraph.IDType type, LoadingFactory ids) {
        final VertexEdgeCounter counter = new VertexEdgeCounter();
        BLGraph graph = new BLGraph(counter, ids);
        BatchGraph<BLGraph> loader = new BatchGraph<BLGraph>(graph, type, bufferSize);
        if (assignKeys) {
            loader.setVertexIdKey(vertexIDKey);
            loader.setEdgeIdKey(edgeIDKey);
        }

        //Create a chain
        int chainLength = total;
        Vertex previous = null;
        for (int i = 0; i <= chainLength; i++) {
            Vertex next = loader.addVertex(ids.getVertexID(i));
            next.setProperty(UID, i);
            counter.numVertices++;
            counter.totalVertices++;
            if (previous != null) {
                Edge e = loader.addEdge(ids.getEdgeID(i), loader.getVertex(previous.getId()), loader.getVertex(next.getId()), "next");
                e.setProperty(UID, i);
                counter.numEdges++;
            }
            previous = next;
        }

        loader.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        assertEquals(0, graph.getNumTransactionsAborted());
        assertEquals(graph.getNumTransactionStarted(), graph.getNumTransactionsCommitted());
        loader.shutdown();
    }

    static class VertexEdgeCounter {

        int numVertices = 0;
        int numEdges = 0;
        int totalVertices = 0;

    }

    static class BLGraph extends MockTransactionalTinkerGraph {

        private static final int keepLast = 10;

        private final VertexEdgeCounter counter;
        private boolean first = true;
        private final LoadingFactory ids;

        BLGraph(final VertexEdgeCounter counter, LoadingFactory ids) {
            this.counter = counter;
            this.ids = ids;
        }

        private static final Object parseID(Object id) {
            if (id instanceof String) {
                try {
                    return Integer.parseInt((String) id);
                } catch (NumberFormatException e) {
                    return id;
                }
            } else return id;
        }

        @Override
        public void stopTransaction(Conclusion conclusion) {
            super.stopTransaction(conclusion);
            //System.out.println("Committed (vertices/edges): " + counter.numVertices + " / " + counter.numEdges);
            assertEquals(counter.numVertices, BaseTest.count(super.getVertices()) - (first ? 0 : keepLast));
            assertEquals(counter.numEdges, BaseTest.count(super.getEdges()));
            for (Edge e : getEdges()) {
                int id = ((Number) e.getProperty(UID)).intValue();
                assertEquals(ids.getEdgeID(id), parseID(e.getId()));
                if (assignKeys) {
                    assertEquals(ids.getEdgeID(id), e.getProperty(edgeIDKey));
                }
            }
            for (Vertex v : getVertices()) {
                int id = ((Number) v.getProperty(UID)).intValue();
                assertEquals(ids.getVertexID(id), parseID(v.getId()));
                if (assignKeys) {
                    assertEquals(ids.getVertexID(id), v.getProperty(vertexIDKey));
                }
                if (id < counter.totalVertices - keepLast) {
                    removeVertex(v);
                }
            }
            for (Edge e : getEdges()) removeEdge(e);
            assertEquals(keepLast, BaseTest.count(super.getVertices()));
            counter.numVertices = 0;
            counter.numEdges = 0;
            first = false;
            //System.out.println("------");
        }

    }

    interface LoadingFactory {

        public Object getVertexID(int id);

        public Object getEdgeID(int id);

    }

    class StringLoadingFactory implements LoadingFactory {

        @Override
        public Object getVertexID(int id) {
            return "V" + id;
        }

        @Override
        public Object getEdgeID(int id) {
            return "E" + id;
        }
    }

    class NumberLoadingFactory implements LoadingFactory {

        @Override
        public Object getVertexID(int id) {
            return Integer.valueOf(id * 2);
        }

        @Override
        public Object getEdgeID(int id) {
            return Integer.valueOf(id * 2 + 1);
        }
    }

    class URLLoadingFactory implements LoadingFactory {

        @Override
        public Object getVertexID(int id) {
            return "http://www.tinkerpop.com/rdf/ns/vertex/" + id;
        }

        @Override
        public Object getEdgeID(int id) {
            return "http://www.tinkerpop.com/rdf/ns/edge#" + id;
        }
    }

}
