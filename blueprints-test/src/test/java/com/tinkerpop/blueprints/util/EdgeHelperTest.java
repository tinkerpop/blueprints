package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;

import java.util.Arrays;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeHelperTest extends BaseTest {

    public void testRelabelEdge() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        EdgeHelper.relabelEdge(graph, graph.getEdge(7), "1234", "use_to_know");
        assertEquals(count(graph.getVertices()), 6);
        assertEquals(count(graph.getEdges()), 6);
        int counter = 0;
        int counter2 = 0;
        Edge temp = null;
        for (Edge edge : graph.getVertex(1).getOutEdges()) {
            if (edge.getLabel().equals("use_to_know")) {
                counter++;
                assertEquals(edge.getId(), "1234");
                assertEquals(edge.getProperty("weight"), 0.5f);
                temp = edge;
            }

            counter2++;
        }
        assertEquals(counter, 1);
        assertEquals(counter2, 3);

        counter = 0;
        counter2 = 0;
        for (Edge edge : graph.getVertex(2).getInEdges()) {
            if (edge.getLabel().equals("use_to_know")) {
                counter++;
                assertEquals(edge.getId(), "1234");
                assertEquals(edge.getProperty("weight"), 0.5f);
                assertEquals(edge, temp);
            }
            counter2++;
        }
        assertEquals(counter, 1);
        assertEquals(counter2, 1);
    }

    public void testRelabelEdges() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        EdgeHelper.relabelEdges(graph, Arrays.asList(graph.getEdge(7)), "use_to_know");
        assertEquals(count(graph.getVertices()), 6);
        assertEquals(count(graph.getEdges()), 6);
        int counter = 0;
        int counter2 = 0;
        Edge temp = null;
        for (Edge edge : graph.getVertex(1).getOutEdges()) {
            if (edge.getLabel().equals("use_to_know")) {
                counter++;
                assertEquals(edge.getProperty("weight"), 0.5f);
                temp = edge;
            }

            counter2++;
        }
        assertEquals(counter, 1);
        assertEquals(counter2, 3);

        counter = 0;
        counter2 = 0;
        for (Edge edge : graph.getVertex(2).getInEdges()) {
            if (edge.getLabel().equals("use_to_know")) {
                counter++;
                assertEquals(edge.getProperty("weight"), 0.5f);
                assertEquals(edge, temp);
            }
            counter2++;
        }
        assertEquals(counter, 1);
        assertEquals(counter2, 1);
    }
}
