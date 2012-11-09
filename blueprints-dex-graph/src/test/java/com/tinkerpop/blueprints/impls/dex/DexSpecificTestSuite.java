package com.tinkerpop.blueprints.impls.dex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;

/**
 */
public class DexSpecificTestSuite extends TestSuite {

    public DexSpecificTestSuite() {
    }

    public DexSpecificTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testDexVertexLabel() throws Exception {
        Graph graph = graphTest.generateGraph();
        this.stopWatch();

        assertTrue(graph.addVertex(null).getProperty(StringFactory.LABEL).equals(DexGraph.DEFAULT_DEX_VERTEX_LABEL));
        ((DexGraph) graph).label.set("people");
        assertTrue(graph.addVertex(null).getProperty(StringFactory.LABEL).equals("people"));
        ((DexGraph) graph).label.set("thing");
        assertTrue(graph.addVertex(null).getProperty(StringFactory.LABEL).equals("thing"));
        assertTrue(graph.addVertex("whatever").getProperty(StringFactory.LABEL).equals("thing"));
        ((DexGraph) graph).label.set(null);
        assertTrue(graph.addVertex(null).getProperty(StringFactory.LABEL).equals(DexGraph.DEFAULT_DEX_VERTEX_LABEL));

        ((DexGraph) graph).label.set("mylabel");
        Vertex v1 = graph.addVertex("mylabel");
        boolean excep = false;
        try {
            v1.setProperty(StringFactory.LABEL, "otherlabel");
        } catch (IllegalArgumentException e) {
            excep = true;
        } finally {
            assertTrue(excep);
        }

        printPerformance(graph.toString(), null, "testDexVertexLabel", this.stopWatch());
        graph.shutdown();
    }

    public void testKeyIndex() {
        KeyIndexableGraph graph = (KeyIndexableGraph) graphTest.generateGraph();
        this.stopWatch();

        ((DexGraph) graph).label.set("people");
        graph.createKeyIndex("name", Vertex.class);

        ((DexGraph) graph).label.set("thing");
        graph.createKeyIndex("name", Vertex.class);

        assertTrue(graph.getIndexedKeys(Edge.class).isEmpty());
        assertTrue(graph.getIndexedKeys(Vertex.class).size() == 1);
        assertTrue(graph.getIndexedKeys(Vertex.class).contains("name"));

        ((DexGraph) graph).label.set("people");
        Vertex v1 = graph.addVertex(null);
        v1.setProperty("name", "foo");
        Vertex v2 = graph.addVertex(null);
        v2.setProperty("name", "boo");

        ((DexGraph) graph).label.set("thing");
        Vertex v10 = graph.addVertex(null);
        v10.setProperty("name", "foo");
        Vertex v20 = graph.addVertex(null);
        v20.setProperty("name", "boo");

        ((DexGraph) graph).label.set("people");
        assertTrue(graph.getVertices("name", "foo").iterator().next().equals(v1));
        ((DexGraph) graph).label.set("thing");
        assertTrue(graph.getVertices("name", "foo").iterator().next().equals(v10));

        ArrayList<Vertex> result = new ArrayList<Vertex>(Arrays.asList(v1, v10));
        ((DexGraph) graph).label.set(null); // all types!
        for (Vertex current : graph.getVertices("name", "foo")) {
            assertTrue(result.contains(current));
            result.remove(current);
        }
        assertTrue(result.size() == 0);

        result = new ArrayList<Vertex>(Arrays.asList(v1, v2));
        for (Vertex current : graph.getVertices(StringFactory.LABEL, "people")) {
            assertTrue(result.contains(current));
            result.remove(current);
        }
        assertTrue(result.size() == 0);

        // table scan
        v1.setProperty("age", 99);
        ((DexGraph) graph).label.set("people");
        assertTrue(graph.getVertices("age", 99).iterator().next().equals(v1));

        printPerformance(graph.toString(), null, "testKeyIndex", this.stopWatch());
        graph.shutdown();
    }

    public void testTx() {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        this.stopWatch();

        graph.stopTransaction(Conclusion.SUCCESS);

        graph.addVertex(null).setProperty("name", "sergio");
        graph.addVertex(null).setProperty("name", "marko");
        assertTrue(graph.getVertices("name", "sergio").iterator().next()
                .getProperty("name").equals("sergio"));
        graph.stopTransaction(Conclusion.SUCCESS);
        assertTrue(((DexGraph) graph).getRawSession(false) == null);
        
        assertTrue(graph.getVertices("name", "sergio").iterator().next().getProperty("name").equals("sergio"));
        graph.stopTransaction(Conclusion.SUCCESS);
        assertTrue(((DexGraph) graph).getRawSession(false) == null);
        graph.stopTransaction(Conclusion.SUCCESS);
        assertTrue(((DexGraph) graph).getRawSession(false) == null);

        graph.addVertex(null);
        graph.shutdown();
        assertTrue(((DexGraph) graph).getRawSession(false) == null);
        printPerformance(graph.toString(), null, "testTx", this.stopWatch());
        graph.shutdown();
    }

    private static class SessionThread extends Thread {
        private TransactionalGraph tg = null;
        public volatile boolean stop = false;
        public volatile boolean finished = false;
        public int counter = 0;

        public SessionThread(TransactionalGraph g) {
            tg = g;
        }

        @Override
        public void run() {
            while (!stop) {
                for (Vertex vertex : tg.getVertices()) {
                    for (Edge edge : vertex.getEdges(Direction.OUT)) {
                        counter++;
                    }
                }
            }
            tg.stopTransaction(Conclusion.SUCCESS);
            finished = true;
        }
    }

    public void testMultipleSessions() throws InterruptedException, IOException {
        TransactionalGraph graph = (TransactionalGraph) graphTest.generateGraph();
        this.stopWatch();

        // This test requires a multiple sessions license, 
        // so just executed if a license has been given, 
        // see DexGraphTest#generateGraph(...) -> blueprints-dex.cfg

        com.sparsity.dex.gdb.DexConfig cfg = new com.sparsity.dex.gdb.DexConfig();
        if (cfg.getLicense() == null || cfg.getLicense().length() == 0) {
            printPerformance(graph.toString(), null, "skip because no license", this.stopWatch());
            graph.shutdown();
            return;
        }

        new GraphMLReader(graph).inputGraph(GraphMLReader.class.getResourceAsStream("graph-example-2.xml"));
        printPerformance(graph.toString(), null, "load", this.stopWatch());

        List<SessionThread> threads = new ArrayList<SessionThread>();
        for (int i = 0; i < 10; i++)
            threads.add(new SessionThread(graph));
        this.stopWatch();
        for (SessionThread th : threads)
            th.start();

        Thread.sleep(5000);

        for (SessionThread th : threads)
            th.stop = true;
        int acum = 0;
        for (SessionThread th : threads) {
            while (!th.finished)
                ;
            acum += th.counter;
        }

        printPerformance(graph.toString(), acum, "tx (sessions)", this.stopWatch());
        graph.shutdown();
    }
}
