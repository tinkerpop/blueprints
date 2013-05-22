package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.impls.GraphTest;

import java.util.List;

import static com.tinkerpop.blueprints.Direction.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class VertexQueryTestSuite extends TestSuite {

    Vertex a;
    Vertex b;
    Vertex c;
    Edge aFriendB;
    Edge aFriendC;
    Edge aHateC;
    Edge cHateA;
    Edge cHateB;

    public VertexQueryTestSuite() {
    }

    public VertexQueryTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    private void createGraph(final Graph graph) {
        a = graph.addVertex(null);
        b = graph.addVertex(null);
        c = graph.addVertex(null);
        aFriendB = graph.addEdge(null, a, b, graphTest.convertLabel("friend"));
        aFriendC = graph.addEdge(null, a, c, graphTest.convertLabel("friend"));
        aHateC = graph.addEdge(null, a, c, graphTest.convertLabel("hate"));
        cHateA = graph.addEdge(null, c, a, graphTest.convertLabel("hate"));
        cHateB = graph.addEdge(null, c, b, graphTest.convertLabel("hate"));
        aFriendB.setProperty("amount", 1.0);
        aFriendB.setProperty("date", 10);
        aFriendC.setProperty("amount", 0.5);
        aHateC.setProperty("amount", 1.0);
        cHateA.setProperty("amount", 1.0);
        cHateB.setProperty("amount", 0.4);
    }

    public void testBasicVertexQuery() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeProperties) {
            createGraph(graph);
            assertEquals(count(a.query().labels(graphTest.convertLabel("friend")).hasNot("date").edges()), 1);
            assertEquals(a.query().labels(graphTest.convertLabel("friend")).hasNot("date").edges().iterator().next().getProperty("amount"), 0.5);
        }
        graph.shutdown();
    }

    public void testDirectionVertexQuery() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeProperties) {
            createGraph(graph);
            List results = asList(a.query().direction(OUT).edges());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            assertTrue(results.contains(aHateC));
            results = asList(a.query().direction(OUT).vertices());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(OUT).count(), 3);
        }
        graph.shutdown();
    }

    public void testVertexQueryLabels() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeProperties) {
            createGraph(graph);
            List results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("hate"), graphTest.convertLabel("friend")).edges());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            assertTrue(results.contains(aHateC));
            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("hate"), graphTest.convertLabel("friend")).vertices());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(OUT).labels(graphTest.convertLabel("hate"), graphTest.convertLabel("friend")).count(), 3);

            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).edges());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).vertices());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).count(), 2);

            results = asList(a.query().direction(BOTH).labels(graphTest.convertLabel("friend"), graphTest.convertLabel("hate")).edges());
            assertEquals(results.size(), 4);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            assertTrue(results.contains(aHateC));
            assertTrue(results.contains(cHateA));
            results = asList(a.query().direction(BOTH).labels(graphTest.convertLabel("friend"), graphTest.convertLabel("hate")).vertices());
            assertEquals(results.size(), 4);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertFalse(results.contains(a));
            assertEquals(a.query().direction(BOTH).labels(graphTest.convertLabel("friend"), graphTest.convertLabel("hate")).count(), 4);

        }
        graph.shutdown();
    }

    public void testHasVertexQuery() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeProperties) {
            createGraph(graph);
            List results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).has("amount", 1.0).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendB));
            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).has("amount", 1.0).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(b));
            assertEquals(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).has("amount", 1.0).count(), 1);

            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).has("amount", VertexQuery.Compare.NOT_EQUAL, 1.0).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).has("amount", VertexQuery.Compare.NOT_EQUAL, 1.0).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).has("amount", VertexQuery.Compare.NOT_EQUAL, 1.0).count(), 1);

            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).has("amount", VertexQuery.Compare.LESS_THAN_EQUAL, 1.0).edges());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).has("amount", VertexQuery.Compare.LESS_THAN_EQUAL, 1.0).vertices());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).has("amount", VertexQuery.Compare.LESS_THAN_EQUAL, 1.0).count(), 2);

            results = asList(a.query().direction(OUT).has("amount", VertexQuery.Compare.LESS_THAN, 1.0).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(OUT).has("amount", VertexQuery.Compare.LESS_THAN, 1.0).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(OUT).has("amount", VertexQuery.Compare.LESS_THAN, 1.0).count(), 1);

            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).has("amount", 0.5).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).has("amount", 0.5).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));

            results = asList(a.query().direction(IN).labels(graphTest.convertLabel("hate"), graphTest.convertLabel("friend")).has("amount", VertexQuery.Compare.GREATER_THAN, 0.5).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(cHateA));
            results = asList(a.query().direction(IN).labels(graphTest.convertLabel("hate"), graphTest.convertLabel("friend")).has("amount", VertexQuery.Compare.GREATER_THAN, 0.5).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(IN).labels(graphTest.convertLabel("friend"), graphTest.convertLabel("hate")).has("amount", VertexQuery.Compare.GREATER_THAN, 0.5).count(), 1);

            results = asList(a.query().direction(IN).labels(graphTest.convertLabel("hate")).has("amount", VertexQuery.Compare.GREATER_THAN, 1.0).edges());
            assertEquals(results.size(), 0);
            results = asList(a.query().direction(IN).labels(graphTest.convertLabel("hate")).has("amount", VertexQuery.Compare.GREATER_THAN, 1.0).vertices());
            assertEquals(results.size(), 0);
            assertEquals(a.query().direction(IN).labels(graphTest.convertLabel("hate")).has("amount", VertexQuery.Compare.GREATER_THAN, 1.0).count(), 0);

            results = asList(a.query().direction(IN).labels(graphTest.convertLabel("hate")).has("amount", VertexQuery.Compare.GREATER_THAN_EQUAL, 1.0).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(cHateA));
            results = asList(a.query().direction(IN).labels(graphTest.convertLabel("hate")).has("amount", VertexQuery.Compare.GREATER_THAN_EQUAL, 1.0).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(IN).labels(graphTest.convertLabel("hate")).has("amount", VertexQuery.Compare.GREATER_THAN_EQUAL, 1.0).count(), 1);
        }
        graph.shutdown();
    }

    public void testHasORVertexQuery() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeProperties) {
            createGraph(graph);

            // EQUALS

            List result = asList(a.query().direction(OUT).has("amount", 1.0).edges());
            assertEquals(result.size(), 2);
            assertTrue(result.contains(aFriendB));
            assertTrue(result.contains(aHateC));

            result = asList(a.query().direction(OUT).has("amount", 1.0, 0.5).edges());
            assertEquals(result.size(), 3);
            assertTrue(result.contains(aFriendB));
            assertTrue(result.contains(aFriendC));
            assertTrue(result.contains(aHateC));

            result = asList(a.query().direction(OUT).has("amount", 1.0, 0.5, "marko", 13, 'a', 32.13d).edges());
            assertEquals(result.size(), 3);
            assertTrue(result.contains(aFriendB));
            assertTrue(result.contains(aFriendC));
            assertTrue(result.contains(aHateC));

            result = asList(a.query().direction(OUT).has("amount", 1.0, 0.5, "marko", 13, 'a', 32.13d).vertices());
            assertEquals(result.size(), 3);
            assertTrue(result.contains(b));
            assertTrue(result.contains(c));

            // OTHER COMPARATORS

            result = asList(a.query().direction(OUT).has("amount", Query.Compare.LESS_THAN, 1.0, 0.5).edges());
            assertEquals(result.size(), 1);
            assertTrue(result.contains(aFriendC));

            result = asList(a.query().direction(OUT).has("amount", Query.Compare.LESS_THAN_EQUAL, 1.0, 0.5).edges());
            assertEquals(result.size(), 3);
            assertTrue(result.contains(aFriendB));
            assertTrue(result.contains(aFriendC));
            assertTrue(result.contains(aHateC));

            // TODO: Check more comparators

        }
        graph.shutdown();
    }

    public void testIntervalVertexQuery() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeProperties) {
            createGraph(graph);
            List results = asList(a.query().direction(OUT).interval("date", 5, 10).edges());
            assertEquals(results.size(), 0);
            results = asList(a.query().direction(OUT).interval("date", 5, 10).vertices());
            assertEquals(results.size(), 0);
            assertEquals(a.query().direction(OUT).interval("date", 5, 10).count(), 0);

            results = asList(a.query().direction(OUT).interval("date", 5, 11).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendB));
            results = asList(a.query().direction(OUT).interval("date", 5, 11).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(b));
            assertEquals(a.query().direction(OUT).interval("date", 5, 11).count(), 1);

            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).interval("date", 5, 11).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendB));
            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).interval("date", 5, 11).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(b));
            assertEquals(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).interval("date", 5, 11).count(), 1);
        }
        graph.shutdown();
    }

    public void testLimitVertexQuery() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeProperties) {
            createGraph(graph);
            List results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend"), graphTest.convertLabel("hate")).limit(2).edges());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(aFriendB) || results.contains(aHateC) || results.contains(aFriendC));
            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend"), graphTest.convertLabel("hate")).limit(2).vertices());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(b) || results.contains(c));
            assertFalse(results.contains(a));
            assertEquals(a.query().labels(graphTest.convertLabel("friend"), graphTest.convertLabel("hate")).limit(2).count(), 2);

            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).limit(0).edges());
            assertEquals(results.size(), 0);
            results = asList(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).limit(0).vertices());
            assertEquals(results.size(), 0);
            assertEquals(a.query().direction(OUT).labels(graphTest.convertLabel("friend")).limit(0).count(), 0);

            assertEquals(a.query().direction(OUT).limit(0, 0).count(), a.query().direction(OUT).limit(0).count());
            BaseTest.equalIterators(a.query().direction(OUT).limit(0, 0).vertices().iterator(), a.query().direction(OUT).limit(0).vertices().iterator());
            assertEquals(a.query().direction(OUT).limit(0, 1).count(), a.query().direction(OUT).limit(1).count());
            BaseTest.equalIterators(a.query().direction(OUT).limit(0, 1).vertices().iterator(), a.query().direction(OUT).limit(1).vertices().iterator());
            assertEquals(a.query().direction(OUT).limit(0, 2).count(), a.query().direction(OUT).limit(2).count());
            BaseTest.equalIterators(a.query().direction(OUT).limit(0, 2).vertices().iterator(), a.query().direction(OUT).limit(2).vertices().iterator());

            assertEquals(a.query().direction(OUT).limit(1, 2).count(), 2);
            assertEquals(a.query().direction(OUT).limit(2, 2).count(), 1);
            assertEquals(a.query().direction(OUT).limit(2, 1).count(), 1);
            assertEquals(a.query().direction(OUT).limit(3, 0).count(), 0);
        }
        graph.shutdown();

    }
}