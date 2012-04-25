package com.tinkerpop.blueprints.pgm.impls;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Filter;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FilteredEdgeIterableTest extends BaseTest {

    private final static TinkerGraph graph = new TinkerGraph();
    private final static Vertex marko;
    private final static Vertex stephen;
    private final static Edge knows;
    private final static Edge likes;

    static {
        marko = graph.addVertex("marko");
        stephen = graph.addVertex("stephen");

        knows = graph.addEdge(1, marko, stephen, "knows");
        knows.setProperty("weight", 0.8f);
        knows.setProperty("date", 2);

        likes = graph.addEdge(2, marko, stephen, "likes");
        likes.setProperty("stars", 4);
        likes.setProperty("date", 1);
    }

    public void testNoFilter() {
        Vertex marko = graph.getVertex("marko");
        FilteredEdgeIterable edges = new FilteredEdgeIterable(marko.getOutEdges());
        int count = 0;
        for (final Edge edge : edges) {
            count++;
            assertTrue(edge.equals(likes) || edge.equals(knows));
        }
        assertEquals(count, 2);
    }

    public void testStringFilters() {
        Vertex marko = graph.getVertex("marko");
        FilteredEdgeIterable edges = new FilteredEdgeIterable(marko.getOutEdges(), "likes");
        int count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);
    }

    public void testPropertyFilters() {
        Vertex marko = graph.getVertex("marko");
        FilteredEdgeIterable edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().property("date", 1));
        int count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().property("date", 1, Filter.Compare.EQUAL));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().property("date", 1, Filter.Compare.GREATER_THAN));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, knows);
            assertNotSame(edge, likes);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().property("date", 1, Filter.Compare.GREATER_THAN_EQUAL));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertTrue(edge.equals(likes) || edge.equals(knows));
        }
        assertEquals(count, 2);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().property("date", 2, Filter.Compare.LESS_THAN));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().property("date", 2, Filter.Compare.LESS_THAN_EQUAL));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertTrue(edge.equals(likes) || edge.equals(knows));
        }
        assertEquals(count, 2);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().property("date", 2, Filter.Compare.NOT_EQUAL));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().property("stars", 4, Filter.Compare.EQUAL));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);
    }

    public void testStringAndPropertyFilters() {
        Vertex marko = graph.getVertex("marko");

        FilteredEdgeIterable edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().property("date", 1), "likes");
        int count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().property("date", 1), "likes", "knows");
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().property("date", 1).property("stars", 4, Filter.Compare.GREATER_THAN_EQUAL), "likes", "knows");
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().property("blah", null), "likes", "knows");
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertTrue(edge.equals(likes) || edge.equals(knows));
        }
        assertEquals(count, 2);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().property("blah", null).property("date", 2, Filter.Compare.EQUAL).property("weight", 0.8f), "likes", "knows");
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, knows);
            assertNotSame(edge, likes);
        }
        assertEquals(count, 1);
    }

    public void testRangeFilter() {
        Vertex marko = graph.getVertex("marko");
        FilteredEdgeIterable edges = new FilteredEdgeIterable(marko.getOutEdges(), "knows", new Filter().range("date", 1, 3));
        int count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), "knows", new Filter().range("date", 5, 10));
        count = 0;
        for (final Edge edge : edges) {
            count++;
        }
        assertEquals(count, 0);
    }

    public void testLabelFilter() {
        Vertex marko = graph.getVertex("marko");
        FilteredEdgeIterable edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().label("knows"));
        int count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().label(Filter.Compare.NOT_EQUAL, "knows"));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().label(Filter.Compare.NOT_EQUAL, "knows", "blah", "bloop"));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().label("knows").property("date", 2));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeIterable(marko.getOutEdges(), new Filter().label("knows").property("stars", 4));
        count = 0;
        for (final Edge edge : edges) {
            count++;
        }
        assertEquals(count, 0);
    }
}
