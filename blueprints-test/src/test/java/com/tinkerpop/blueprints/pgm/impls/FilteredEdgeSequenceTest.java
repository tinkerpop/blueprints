package com.tinkerpop.blueprints.pgm.impls;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Filter;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FilteredEdgeSequenceTest extends BaseTest {

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
        FilteredEdgeSequence edges = new FilteredEdgeSequence(marko.getOutEdges().iterator());
        int count = 0;
        for (final Edge edge : edges) {
            count++;
            assertTrue(edge.equals(likes) || edge.equals(knows));
        }
        assertEquals(count, 2);
    }

    public void testStringFilters() {
        Vertex marko = graph.getVertex("marko");
        FilteredEdgeSequence edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), "likes");
        int count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);
    }

    public void testFilterPredicates() {
        Vertex marko = graph.getVertex("marko");
        FilteredEdgeSequence edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), new Filter("date", 1));
        int count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), new Filter("date", 1, Filter.Compare.EQUAL));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), new Filter("date", 1, Filter.Compare.GREATER_THAN));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, knows);
            assertNotSame(edge, likes);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), new Filter("date", 1, Filter.Compare.GREATER_THAN_EQUAL));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertTrue(edge.equals(likes) || edge.equals(knows));
        }
        assertEquals(count, 2);

        edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), new Filter("date", 2, Filter.Compare.LESS_THAN));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), new Filter("date", 2, Filter.Compare.LESS_THAN_EQUAL));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertTrue(edge.equals(likes) || edge.equals(knows));
        }
        assertEquals(count, 2);

        edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), new Filter("date", 2, Filter.Compare.NOT_EQUAL));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), new Filter("stars", 4, Filter.Compare.EQUAL));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);
    }

    public void testStringAndFilterFilters() {
        Vertex marko = graph.getVertex("marko");

        FilteredEdgeSequence edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), new Filter("date", 1), "likes");
        int count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), new Filter("date", 1), "likes", "knows");
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), new Filter("date", 1), "likes", "knows", new Filter("stars", 4, Filter.Compare.GREATER_THAN_EQUAL));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, likes);
            assertNotSame(edge, knows);
        }
        assertEquals(count, 1);

        edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), new Filter("blah", null), "likes", "knows");
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertTrue(edge.equals(likes) || edge.equals(knows));
        }
        assertEquals(count, 2);

        edges = new FilteredEdgeSequence(marko.getOutEdges().iterator(), new Filter("blah", null), "likes", "knows", new Filter("date", 2, Filter.Compare.EQUAL), new Filter("weight", 0.8f));
        count = 0;
        for (final Edge edge : edges) {
            count++;
            assertEquals(edge, knows);
            assertNotSame(edge, likes);
        }
        assertEquals(count, 1);
    }
}
