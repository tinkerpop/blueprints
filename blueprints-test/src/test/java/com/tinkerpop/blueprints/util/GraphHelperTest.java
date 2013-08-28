package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;

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

    public void testCopyGraph() {
        Graph g = TinkerGraphFactory.createTinkerGraph();
        Graph h = new TinkerGraph();

        GraphHelper.copyGraph(g, h);
        assertEquals(count(h.getVertices()), 6);
        assertEquals(count(h.getEdges()), 6);
        assertEquals(count(h.getVertex("1").getEdges(Direction.OUT)), 3);
        assertEquals(count(h.getVertex("1").getEdges(Direction.IN)), 0);
        Vertex marko = h.getVertex("1");
        assertEquals(marko.getProperty("name"), "marko");
        assertEquals(marko.getProperty("age"), 29);
        int counter = 0;
        for (Edge e : h.getVertex("1").getEdges(Direction.OUT)) {
            if (e.getVertex(Direction.IN).getId().equals("2")) {
                assertEquals(e.getProperty("weight"), 0.5f);
                assertEquals(e.getLabel(), "knows");
                assertEquals(e.getId(), "7");
                counter++;
            } else if (e.getVertex(Direction.IN).getId().equals("3")) {
                assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                assertEquals(e.getLabel(), "created");
                assertEquals(e.getId(), "9");
                counter++;
            } else if (e.getVertex(Direction.IN).getId().equals("4")) {
                assertEquals(Math.round((Float) e.getProperty("weight")), 1);
                assertEquals(e.getLabel(), "knows");
                assertEquals(e.getId(), "8");
                counter++;
            }
        }

        assertEquals(count(h.getVertex("4").getEdges(Direction.OUT)), 2);
        assertEquals(count(h.getVertex("4").getEdges(Direction.IN)), 1);
        Vertex josh = h.getVertex("4");
        assertEquals(josh.getProperty("name"), "josh");
        assertEquals(josh.getProperty("age"), 32);
        for (Edge e : h.getVertex("4").getEdges(Direction.OUT)) {
            if (e.getVertex(Direction.IN).getId().equals("3")) {
                assertEquals(Math.round((Float) e.getProperty("weight")), 0);
                assertEquals(e.getLabel(), "created");
                assertEquals(e.getId(), "11");
                counter++;
            } else if (e.getVertex(Direction.IN).getId().equals("5")) {
                assertEquals(Math.round((Float) e.getProperty("weight")), 1);
                assertEquals(e.getLabel(), "created");
                assertEquals(e.getId(), "10");
                counter++;
            }
        }

        assertEquals(counter, 5);
    }

    public void testReIndexElements() {
        TinkerGraph graph = TinkerGraphFactory.createTinkerGraph();
        assertTrue(graph.getVertices("name", "marko") instanceof PropertyFilteredIterable);
        assertEquals(count(graph.getVertices("name", "marko")), 1);
        assertEquals(graph.getVertices("name", "marko").iterator().next(), graph.getVertex(1));
        graph.createIndex("name", Vertex.class);
        //KeyIndexableGraphHelper.reIndexElements(graph, graph.getVertices(), new HashSet<String>(Arrays.asList("name")));
        assertFalse(graph.getVertices("name", "marko") instanceof PropertyFilteredIterable);
        assertEquals(count(graph.getVertices("name", "marko")), 1);
        assertEquals(graph.getVertices("name", "marko").iterator().next(), graph.getVertex(1));
    }

   /*TODO
   public void testAddUniqueVertex() {
        Graph graph = new TinkerGraph();
        Vertex marko = graph.addVertex(0);
        marko.setProperty("name", "marko");
        Index<Vertex> index = graph.createIndex("txIdx", Vertex.class);
        index.put("name", "marko", marko);
        Vertex vertex = IndexableGraphHelper.addUniqueVertex(graph, null, index, "name", "marko");
        assertEquals(vertex.getProperty("name"), "marko");
        assertEquals(vertex, graph.getVertex(0));
        assertEquals(count(graph.getVertices()), 1);
        assertEquals(count(graph.getEdges()), 0);

        vertex = IndexableGraphHelper.addUniqueVertex(graph, null, index, "name", "darrick");
        assertEquals(vertex.getProperty("name"), "darrick");
        assertEquals(count(graph.getVertices()), 2);
        assertEquals(vertex.getId(), "1");
    }    */

}
