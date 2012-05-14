package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.impls.GraphTest;

import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class QueryTestSuite extends TestSuite {

    public QueryTestSuite() {
    }

    public QueryTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testVertexQuery() {
        Graph graph = graphTest.generateGraph();
        if (!graph.getFeatures().isRDFModel) {


            Vertex a = graph.addVertex(null);
            Vertex b = graph.addVertex(null);
            Vertex c = graph.addVertex(null);
            Edge aFriendB = graph.addEdge(null, a, b, convertId(graph, "friend"));
            Edge aFriendC = graph.addEdge(null, a, c, convertId(graph, "friend"));
            Edge aHateC = graph.addEdge(null, a, c, convertId(graph, "hate"));
            Edge cHateA = graph.addEdge(null, c, a, convertId(graph, "hate"));
            Edge cHateB = graph.addEdge(null, c, b, convertId(graph, "hate"));
            aFriendB.setProperty("amount", 1.0);
            aFriendB.setProperty("date", 10);
            aFriendC.setProperty("amount", 0.5);
            aHateC.setProperty("amount", 1.0);
            cHateA.setProperty("amount", 1.0);
            cHateB.setProperty("amount", 0.4);

            // out edges

            List results = asList(a.query().direction(Query.Direction.OUT).edges());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            assertTrue(results.contains(aHateC));
            results = asList(a.query().direction(Query.Direction.OUT).vertices());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.OUT).count(), 3);


            results = asList(a.query().direction(Query.Direction.OUT).labels("hate", "friend").edges());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            assertTrue(results.contains(aHateC));
            results = asList(a.query().direction(Query.Direction.OUT).labels("hate", "friend").vertices());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.OUT).labels("hate", "friend").count(), 3);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").edges());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").vertices());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.OUT).labels("friend").count(), 2);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendB));
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(b));
            assertEquals(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0).count(), 1);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0, Query.Compare.NOT_EQUAL).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0, Query.Compare.NOT_EQUAL).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0, Query.Compare.NOT_EQUAL).count(), 1);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0, Query.Compare.LESS_THAN_EQUAL).edges());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0, Query.Compare.LESS_THAN_EQUAL).vertices());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0, Query.Compare.LESS_THAN_EQUAL).count(), 2);

            results = asList(a.query().direction(Query.Direction.OUT).has("amount", 1.0, Query.Compare.LESS_THAN).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Query.Direction.OUT).has("amount", 1.0, Query.Compare.LESS_THAN).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.OUT).has("amount", 1.0, Query.Compare.LESS_THAN).count(), 1);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 0.5).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 0.5).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));

            results = asList(a.query().direction(Query.Direction.IN).labels("hate", "friend").has("amount", 0.5, Query.Compare.GREATER_THAN).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(cHateA));
            results = asList(a.query().direction(Query.Direction.IN).labels("hate", "friend").has("amount", 0.5, Query.Compare.GREATER_THAN).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.IN).labels("hate", "friend").has("amount", 0.5, Query.Compare.GREATER_THAN).count(), 1);

            results = asList(a.query().direction(Query.Direction.IN).labels("hate").has("amount", 1.0, Query.Compare.GREATER_THAN).edges());
            assertEquals(results.size(), 0);
            results = asList(a.query().direction(Query.Direction.IN).labels("hate").has("amount", 1.0, Query.Compare.GREATER_THAN).vertices());
            assertEquals(results.size(), 0);
            assertEquals(a.query().direction(Query.Direction.IN).labels("hate").has("amount", 1.0, Query.Compare.GREATER_THAN).count(), 0);

            results = asList(a.query().direction(Query.Direction.IN).labels("hate").has("amount", 1.0, Query.Compare.GREATER_THAN_EQUAL).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(cHateA));
            results = asList(a.query().direction(Query.Direction.IN).labels("hate").has("amount", 1.0, Query.Compare.GREATER_THAN_EQUAL).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.IN).labels("hate").has("amount", 1.0, Query.Compare.GREATER_THAN_EQUAL).count(), 1);

            results = asList(a.query().direction(Query.Direction.OUT).interval("date", 5, 10).edges());
            assertEquals(results.size(), 0);
            results = asList(a.query().direction(Query.Direction.OUT).interval("date", 5, 10).vertices());
            assertEquals(results.size(), 0);
            assertEquals(a.query().direction(Query.Direction.OUT).interval("date", 5, 10).count(), 0);

            results = asList(a.query().direction(Query.Direction.OUT).interval("date", 5, 11).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendB));
            results = asList(a.query().direction(Query.Direction.OUT).interval("date", 5, 11).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(b));
            assertEquals(a.query().direction(Query.Direction.OUT).interval("date", 5, 11).count(), 1);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").interval("date", 5, 11).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendB));
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").interval("date", 5, 11).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(b));
            assertEquals(a.query().direction(Query.Direction.OUT).labels("friend").interval("date", 5, 11).count(), 1);

            results = asList(a.query().direction(Query.Direction.BOTH).labels("friend", "hate").edges());
            assertEquals(results.size(), 4);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            assertTrue(results.contains(aHateC));
            assertTrue(results.contains(cHateA));
            results = asList(a.query().direction(Query.Direction.BOTH).labels("friend", "hate").vertices());
            assertEquals(results.size(), 4);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertFalse(results.contains(a));
            assertEquals(a.query().direction(Query.Direction.BOTH).labels("friend", "hate").count(), 4);

            results = asList(a.query().labels("friend", "hate").limit(2).edges());
            assertEquals(results.size(), 2);
            results = asList(a.query().labels("friend", "hate").limit(2).vertices());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertFalse(results.contains(a));
            assertEquals(a.query().labels("friend", "hate").limit(2).count(), 2);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").limit(0).edges());
            assertEquals(results.size(), 0);
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").limit(0).vertices());
            assertEquals(results.size(), 0);
            assertEquals(a.query().direction(Query.Direction.OUT).labels("friend").limit(0).count(), 0);


        }
        graph.shutdown();

    }
}