package com.tinkerpop.blueprints.util.wrappers.batch;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.IgnoreIdTinkerGraph;
import com.tinkerpop.blueprints.impls.tg.MockTransactionalGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import junit.framework.TestCase;

import java.util.Random;

/**
 * Tests {@link BatchGraph} by creating a variable length chain and verifying that the chain is correctly inserted into the wrapped TinkerGraph.
 * <br />
 * Tests the various different Vertex caches and different length of chains.
 * <br />
 *
 * @author Matthias Broecheler (http://www.matthiasb.com)
 */

public class BatchGraphTest extends TestCase {

    private static final String UID = "uid";

    private static final String vertexIDKey = "vid";
    private static final String edgeIDKey = "eid";
    private static boolean assignKeys = false;
    private static boolean ignoreIDs = false;

    public void testNumberIdLoading() {
        loadingTest(5000, 100, VertexIDType.NUMBER, new NumberLoadingFactory());
        loadingTest(200000, 10000, VertexIDType.NUMBER, new NumberLoadingFactory());

        assignKeys = true;
        loadingTest(5000, 100, VertexIDType.NUMBER, new NumberLoadingFactory());
        loadingTest(50000, 10000, VertexIDType.NUMBER, new NumberLoadingFactory());
        assignKeys = false;

        ignoreIDs = true;
        loadingTest(5000, 100, VertexIDType.NUMBER, new NumberLoadingFactory());
        loadingTest(50000, 10000, VertexIDType.NUMBER, new NumberLoadingFactory());
        ignoreIDs = false;
    }

    public void testObjectIdLoading() {
        loadingTest(5000, 100, VertexIDType.OBJECT, new StringLoadingFactory());
        loadingTest(200000, 10000, VertexIDType.OBJECT, new StringLoadingFactory());
    }

    public void testStringIdLoading() {
        loadingTest(5000, 100, VertexIDType.STRING, new StringLoadingFactory());
        loadingTest(200000, 10000, VertexIDType.STRING, new StringLoadingFactory());
    }

    public void testURLIdLoading() {
        loadingTest(5000, 100, VertexIDType.URL, new URLLoadingFactory());
        loadingTest(200000, 10000, VertexIDType.URL, new URLLoadingFactory());
    }

    public void testQuadLoading() {
        int numEdges = 10000;
        String[][] quads = generateQuads(100, numEdges, new String[]{"knows", "friend"});
        TinkerGraph graph = new TinkerGraph();
        BatchGraph bgraph = new BatchGraph(new WritethroughGraph(graph), VertexIDType.STRING, 1000);
        for (String[] quad : quads) {
            Vertex[] vertices = new Vertex[2];
            for (int i = 0; i < 2; i++) {
                vertices[i] = bgraph.getVertex(quad[i]);
                if (vertices[i] == null) vertices[i] = bgraph.addVertex(quad[i]);
            }
            Edge edge = bgraph.addEdge(null, vertices[0], vertices[1], quad[2]);
            edge.setProperty("annotation", quad[3]);
        }
        assertEquals(numEdges, BaseTest.count(graph.getEdges()));

        bgraph.shutdown();
    }

    public void testLoadingWithExisting1() {
        int numEdges = 1000;
        String[][] quads = generateQuads(100, numEdges, new String[]{"knows", "friend"});
        TinkerGraph tg = new TinkerGraph();
        BatchGraph bg = new BatchGraph(new WritethroughGraph(tg), VertexIDType.STRING, 100);
        bg.setLoadingFromScratch(false);
        Graph graph = null;
        int counter = 0;
        for (String[] quad : quads) {
            if (counter < numEdges / 2) graph = tg;
            else graph = bg;

            Vertex[] vertices = new Vertex[2];
            for (int i = 0; i < 2; i++) {
                vertices[i] = graph.getVertex(quad[i]);
                if (vertices[i] == null) vertices[i] = graph.addVertex(quad[i]);
            }
            Edge edge = graph.addEdge(null, vertices[0], vertices[1], quad[2]);
            edge.setProperty("annotation", quad[3]);
            counter++;
        }
        assertEquals(numEdges, BaseTest.count(tg.getEdges()));

        bg.shutdown();
    }

    public void testLoadingWithExisting2() {
        int numEdges = 1000;
        String[][] quads = generateQuads(100, numEdges, new String[]{"knows", "friend"});
        TinkerGraph tg = new IgnoreIdTinkerGraph();
        BatchGraph bg = new BatchGraph(new WritethroughGraph(tg), VertexIDType.STRING, 100);
        try {
            bg.setLoadingFromScratch(false);
            fail();
        } catch (IllegalStateException e) {
        }
        bg.setVertexIdKey("uid");
        bg.setLoadingFromScratch(false);
        try {
            bg.setVertexIdKey(null);
            fail();
        } catch (IllegalStateException e) {
        }

        Graph graph = null;
        int counter = 0;
        for (String[] quad : quads) {
            if (counter < numEdges / 2) graph = tg;
            else graph = bg;

            Vertex[] vertices = new Vertex[2];
            for (int i = 0; i < 2; i++) {
                vertices[i] = graph.getVertex(quad[i]);
                if (vertices[i] == null) vertices[i] = graph.addVertex(quad[i]);
            }
            Edge edge = graph.addEdge(null, vertices[0], vertices[1], quad[2]);
            edge.setProperty("annotation", quad[3]);
            counter++;
        }
        assertEquals(numEdges, BaseTest.count(tg.getEdges()));

        bg.shutdown();
    }


    public static String[][] generateQuads(int numVertices, int numEdges, String[] labels) {
        Random random = new Random();
        String[][] edges = new String[numEdges][4];
        for (int i = 0; i < numEdges; i++) {
            edges[i][0] = "v" + random.nextInt(numVertices) + 1;
            edges[i][1] = "v" + random.nextInt(numVertices) + 1;
            edges[i][2] = labels[random.nextInt(labels.length)];
            edges[i][3] = "" + random.nextInt();
        }
        return edges;
    }


    public void loadingTest(int total, int bufferSize, VertexIDType type, LoadingFactory ids) {
        final VertexEdgeCounter counter = new VertexEdgeCounter();

        MockTransactionalGraph tgraph = null;
        if (ignoreIDs) {
            tgraph = new MockTransactionalGraph(new IgnoreIdTinkerGraph());
        } else {
            tgraph = new MockTransactionalGraph(new TinkerGraph());
        }

        BLGraph graph = new BLGraph(tgraph, counter, ids);
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
        assertTrue(tgraph.allSuccessful());

        loader.shutdown();
    }

    static class VertexEdgeCounter {

        int numVertices = 0;
        int numEdges = 0;
        int totalVertices = 0;

    }


    static class BLGraph implements TransactionalGraph {

        private static final int keepLast = 10;

        private final VertexEdgeCounter counter;
        private boolean first = true;
        private final LoadingFactory ids;

        private final TransactionalGraph graph;

        BLGraph(TransactionalGraph graph, final VertexEdgeCounter counter, LoadingFactory ids) {
            this.graph = graph;
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
        public void commit() {
            graph.commit();
            verifyCounts();
        }

        @Override
        public void rollback() {
            graph.rollback();
            verifyCounts();
        }

        @Override
        public void stopTransaction(Conclusion conclusion) {
            if (Conclusion.SUCCESS == conclusion)
                commit();
            else
                rollback();
        }

        private void verifyCounts() {
            //System.out.println("Committed (vertices/edges): " + counter.numVertices + " / " + counter.numEdges);
            assertEquals(counter.numVertices, BaseTest.count(graph.getVertices()) - (first ? 0 : keepLast));
            assertEquals(counter.numEdges, BaseTest.count(graph.getEdges()));
            for (Edge e : getEdges()) {
                int id = ((Number) e.getProperty(UID)).intValue();
                if (!ignoreIDs) {
                    assertEquals(ids.getEdgeID(id), parseID(e.getId()));
                }
                assertEquals(1, (Integer) e.getVertex(Direction.IN).getProperty(UID) - (Integer) e.getVertex(Direction.OUT).getProperty(UID));
                if (assignKeys) {
                    assertEquals(ids.getEdgeID(id), e.getProperty(edgeIDKey));
                }
            }
            for (Vertex v : getVertices()) {
                int id = ((Number) v.getProperty(UID)).intValue();
                if (!ignoreIDs) {
                    assertEquals(ids.getVertexID(id), parseID(v.getId()));
                }
                assertTrue(2 >= BaseTest.count(v.getEdges(Direction.BOTH)));
                assertTrue(1 >= BaseTest.count(v.getEdges(Direction.IN)));
                assertTrue(1 >= BaseTest.count(v.getEdges(Direction.OUT)));

                if (assignKeys) {
                    assertEquals(ids.getVertexID(id), v.getProperty(vertexIDKey));
                }

            }
            for (Vertex v : getVertices()) {
                int id = ((Number) v.getProperty(UID)).intValue();
                if (id < counter.totalVertices - keepLast) {
                    removeVertex(v);
                }
            }
            for (Edge e : getEdges()) removeEdge(e);
            assertEquals(keepLast, BaseTest.count(graph.getVertices()));
            counter.numVertices = 0;
            counter.numEdges = 0;
            first = false;
            //System.out.println("------");
        }

        @Override
        public Features getFeatures() {
            return graph.getFeatures();
        }

        @Override
        public Vertex addVertex(Object id) {
            return graph.addVertex(id);
        }

        @Override
        public Vertex getVertex(Object id) {
            return graph.getVertex(id);
        }

        @Override
        public void removeVertex(Vertex vertex) {
            graph.removeVertex(vertex);
        }

        @Override
        public Iterable<Vertex> getVertices() {
            return graph.getVertices();
        }

        @Override
        public Iterable<Vertex> getVertices(String key, Object value) {
            return graph.getVertices(key, value);
        }

        @Override
        public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
            return graph.addEdge(id, outVertex, inVertex, label);
        }

        @Override
        public Edge getEdge(Object id) {
            return graph.getEdge(id);
        }

        @Override
        public void removeEdge(Edge edge) {
            graph.removeEdge(edge);
        }

        @Override
        public Iterable<Edge> getEdges() {
            return graph.getEdges();
        }

        @Override
        public Iterable<Edge> getEdges(String key, Object value) {
            return graph.getEdges(key, value);
        }

        @Override
        public void shutdown() {
            graph.shutdown();
        }

        @Override
        public GraphQuery query() {
            return graph.query();
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