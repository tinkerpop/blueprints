package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GraphHelperTest extends BaseTest {

    public void testAddVertex() {
        Graph graph = new TinkerGraph();
        Vertex vertex = GraphHelper.addVertex(graph, null, "name", "marko", "age", 31);
        assertEquals(vertex.getProperty("name"), "marko");
        assertEquals(vertex.getProperty("age"), 31);
        assertEquals(vertex.getPropertyKeys().size(), 2);
        assertEquals(count(graph.getVertices()), 1);

        try {
            vertex = GraphHelper.addVertex(graph, null, "name", "marko", "age");
            assertTrue(false);
        } catch (Exception e) {
            assertFalse(false);
            assertEquals(count(graph.getVertices()), 1);
        }
    }

    public void testAddEdge() {
        Graph graph = new TinkerGraph();
        Edge edge = GraphHelper.addEdge(graph, null, graph.addVertex(null), graph.addVertex(null), "knows", "weight", 10.0f);
        assertEquals(edge.getProperty("weight"), 10.0f);
        assertEquals(edge.getLabel(), "knows");
        assertEquals(edge.getPropertyKeys().size(), 1);
        assertEquals(count(graph.getVertices()), 2);
        assertEquals(count(graph.getEdges()), 1);

        try {
            edge = GraphHelper.addEdge(graph, null, graph.addVertex(null), graph.addVertex(null), "knows", "weight");
            assertTrue(false);
        } catch (Exception e) {
            assertFalse(false);
            assertEquals(count(graph.getVertices()), 4);
            assertEquals(count(graph.getEdges()), 1);
        }
    }
}
