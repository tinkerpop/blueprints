package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.impls.GraphTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        if (graph.getFeatures().supportsEdgeProperties) {

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

            assertEquals(count(a.query().labels("friend").has("date", null).edges()), 1);
            assertEquals(a.query().labels("friend").has("date", null).edges().iterator().next().getProperty("amount"), 0.5);

            // out edges

            List results = asList(a.query().direction(Direction.OUT).edges());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            assertTrue(results.contains(aHateC));
            results = asList(a.query().direction(Direction.OUT).vertices());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Direction.OUT).count(), 3);


            results = asList(a.query().direction(Direction.OUT).labels("hate", "friend").edges());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            assertTrue(results.contains(aHateC));
            results = asList(a.query().direction(Direction.OUT).labels("hate", "friend").vertices());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Direction.OUT).labels("hate", "friend").count(), 3);

            results = asList(a.query().direction(Direction.OUT).labels("friend").edges());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Direction.OUT).labels("friend").vertices());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Direction.OUT).labels("friend").count(), 2);

            results = asList(a.query().direction(Direction.OUT).labels("friend").has("amount", 1.0).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendB));
            results = asList(a.query().direction(Direction.OUT).labels("friend").has("amount", 1.0).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(b));
            assertEquals(a.query().direction(Direction.OUT).labels("friend").has("amount", 1.0).count(), 1);

            results = asList(a.query().direction(Direction.OUT).labels("friend").has("amount", 1.0, VertexQuery.Compare.NOT_EQUAL).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Direction.OUT).labels("friend").has("amount", 1.0, VertexQuery.Compare.NOT_EQUAL).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Direction.OUT).labels("friend").has("amount", 1.0, VertexQuery.Compare.NOT_EQUAL).count(), 1);

            results = asList(a.query().direction(Direction.OUT).labels("friend").has("amount", 1.0, VertexQuery.Compare.LESS_THAN_EQUAL).edges());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Direction.OUT).labels("friend").has("amount", 1.0, VertexQuery.Compare.LESS_THAN_EQUAL).vertices());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Direction.OUT).labels("friend").has("amount", 1.0, VertexQuery.Compare.LESS_THAN_EQUAL).count(), 2);

            results = asList(a.query().direction(Direction.OUT).has("amount", 1.0, VertexQuery.Compare.LESS_THAN).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Direction.OUT).has("amount", 1.0, VertexQuery.Compare.LESS_THAN).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Direction.OUT).has("amount", 1.0, VertexQuery.Compare.LESS_THAN).count(), 1);

            results = asList(a.query().direction(Direction.OUT).labels("friend").has("amount", 0.5).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Direction.OUT).labels("friend").has("amount", 0.5).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));

            results = asList(a.query().direction(Direction.IN).labels("hate", "friend").has("amount", 0.5, VertexQuery.Compare.GREATER_THAN).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(cHateA));
            results = asList(a.query().direction(Direction.IN).labels("hate", "friend").has("amount", 0.5, VertexQuery.Compare.GREATER_THAN).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Direction.IN).labels("hate", "friend").has("amount", 0.5, VertexQuery.Compare.GREATER_THAN).count(), 1);

            results = asList(a.query().direction(Direction.IN).labels("hate").has("amount", 1.0, VertexQuery.Compare.GREATER_THAN).edges());
            assertEquals(results.size(), 0);
            results = asList(a.query().direction(Direction.IN).labels("hate").has("amount", 1.0, VertexQuery.Compare.GREATER_THAN).vertices());
            assertEquals(results.size(), 0);
            assertEquals(a.query().direction(Direction.IN).labels("hate").has("amount", 1.0, VertexQuery.Compare.GREATER_THAN).count(), 0);

            results = asList(a.query().direction(Direction.IN).labels("hate").has("amount", 1.0, VertexQuery.Compare.GREATER_THAN_EQUAL).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(cHateA));
            results = asList(a.query().direction(Direction.IN).labels("hate").has("amount", 1.0, VertexQuery.Compare.GREATER_THAN_EQUAL).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Direction.IN).labels("hate").has("amount", 1.0, VertexQuery.Compare.GREATER_THAN_EQUAL).count(), 1);

            results = asList(a.query().direction(Direction.OUT).interval("date", 5, 10).edges());
            assertEquals(results.size(), 0);
            results = asList(a.query().direction(Direction.OUT).interval("date", 5, 10).vertices());
            assertEquals(results.size(), 0);
            assertEquals(a.query().direction(Direction.OUT).interval("date", 5, 10).count(), 0);

            results = asList(a.query().direction(Direction.OUT).interval("date", 5, 11).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendB));
            results = asList(a.query().direction(Direction.OUT).interval("date", 5, 11).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(b));
            assertEquals(a.query().direction(Direction.OUT).interval("date", 5, 11).count(), 1);

            results = asList(a.query().direction(Direction.OUT).labels("friend").interval("date", 5, 11).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendB));
            results = asList(a.query().direction(Direction.OUT).labels("friend").interval("date", 5, 11).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(b));
            assertEquals(a.query().direction(Direction.OUT).labels("friend").interval("date", 5, 11).count(), 1);

            results = asList(a.query().direction(Direction.BOTH).labels("friend", "hate").edges());
            assertEquals(results.size(), 4);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            assertTrue(results.contains(aHateC));
            assertTrue(results.contains(cHateA));
            results = asList(a.query().direction(Direction.BOTH).labels("friend", "hate").vertices());
            assertEquals(results.size(), 4);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertFalse(results.contains(a));
            assertEquals(a.query().direction(Direction.BOTH).labels("friend", "hate").count(), 4);

            results = asList(a.query().direction(Direction.OUT).labels("friend", "hate").limit(2).edges());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(aFriendB) || results.contains(aHateC) || results.contains(aFriendC));
            results = asList(a.query().direction(Direction.OUT).labels("friend", "hate").limit(2).vertices());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(b) || results.contains(c));
            assertFalse(results.contains(a));
            assertEquals(a.query().labels("friend", "hate").limit(2).count(), 2);

            results = asList(a.query().direction(Direction.OUT).labels("friend").limit(0).edges());
            assertEquals(results.size(), 0);
            results = asList(a.query().direction(Direction.OUT).labels("friend").limit(0).vertices());
            assertEquals(results.size(), 0);
            assertEquals(a.query().direction(Direction.OUT).labels("friend").limit(0).count(), 0);
        }
        graph.shutdown();

    }

    public void testGraphQueryForVertices() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties && graph.getFeatures().supportsVertexIndex && graph instanceof KeyIndexableGraph) {
            ((KeyIndexableGraph) graph).createKeyIndex("name", Vertex.class);

            Vertex vertex = graph.addVertex(null);
            vertex.setProperty(convertId(graph, "name"), "marko");
            vertex.setProperty(convertId(graph, "age"), 33);
            vertex = graph.addVertex(null);
            vertex.setProperty(convertId(graph, "name"), "matthias");
            vertex.setProperty(convertId(graph, "age"), 28);
            graph.addVertex(null);

            Iterable<Vertex> vertices = graph.query().vertices();
            assertEquals(count(vertices), 3);
            assertEquals(count(vertices), 3);
            Set<String> names = new HashSet<String>();
            for (Vertex v : vertices) {
                names.add((String) v.getProperty(convertId(graph, "name")));
            }
            assertEquals(names.size(), 3);
            assertTrue(names.contains("marko"));
            assertTrue(names.contains(null));
            assertTrue(names.contains("matthias"));

            vertices = graph.query().has("name", "marko").vertices();
            assertEquals(count(vertices), 1);
//            assertEquals(vertices.iterator().next().getProperty("name"), "marko");

            vertices = graph.query().has("age", 29, Query.Compare.GREATER_THAN_EQUAL).vertices();
            assertEquals(count(vertices), 1);
            assertEquals(vertices.iterator().next().getProperty("name"), "marko");
            assertEquals(vertices.iterator().next().getProperty("age"), 33);

            vertices = graph.query().has("age", 28, Query.Compare.GREATER_THAN_EQUAL).vertices();
            assertEquals(count(vertices), 2);
            names = new HashSet<String>();
            for (Vertex v : vertices) {
                names.add((String) v.getProperty(convertId(graph, "name")));
            }
            assertEquals(names.size(), 2);
            assertTrue(names.contains("marko"));
            assertTrue(names.contains("matthias"));

            vertices = graph.query().interval("age", 28, 33).vertices();
            assertEquals(count(vertices), 1);
            assertEquals(vertices.iterator().next().getProperty("name"), "matthias");

            assertEquals(count(graph.query().has("age", null).vertices()), 1);
            assertEquals(count(graph.query().has("age", 28).has("name", "matthias").vertices()), 1);
            assertEquals(count(graph.query().has("age", 28).has("name", "matthias").has("name", "matthias").vertices()), 1);
            assertEquals(count(graph.query().interval("age", 28, 32).has("name", "marko").vertices()), 0);
            graph.shutdown();
        }
    }
}