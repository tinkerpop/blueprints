package com.tinkerpop.blueprints.pgm.oupls.jung;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import edu.uci.ics.jung.graph.util.EdgeType;
import junit.framework.TestCase;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GraphJungTest extends TestCase {

    public void testTinkerGraph() {
        GraphJung graph = new GraphJung(TinkerGraphFactory.createTinkerGraph());
        assertEquals(graph.getVertices().size(), 6);
        assertEquals(graph.getEdges().size(), 6);
        assertEquals(graph.getVertexCount(), 6);
        assertEquals(graph.getEdgeCount(), 6);
        Vertex marko = null;
        Vertex josh = null;
        Vertex vadas = null;
        for (Vertex vertex : graph.getVertices()) {
            assertTrue(graph.containsVertex(vertex));
            for (Edge edge : graph.getOutEdges(vertex)) {
                assertEquals(graph.getSource(edge), vertex);
            }
            for (Edge edge : graph.getInEdges(vertex)) {
                assertEquals(graph.getDest(edge), vertex);
            }
            if (vertex.getId().equals("1")) {
                marko = vertex;
                assertEquals(graph.getOutEdges(vertex).size(), 3);
                assertEquals(graph.getInEdges(vertex).size(), 0);
                assertEquals(graph.getNeighborCount(vertex), 3);
                int count = 0;
                for (Vertex vertex2 : graph.getNeighbors(vertex)) {
                    if (vertex2.getId().equals("2"))
                        count++;
                    else if (vertex2.getId().equals("4"))
                        count++;
                    else if (vertex2.getId().equals("3"))
                        count++;
                    else
                        assertTrue(false);
                }
                assertEquals(count, 3);
                assertEquals(graph.getSuccessorCount(vertex), 3);
                count = 0;
                for (Vertex vertex2 : graph.getSuccessors(vertex)) {
                    if (vertex2.getId().equals("2"))
                        count++;
                    else if (vertex2.getId().equals("4"))
                        count++;
                    else if (vertex2.getId().equals("3"))
                        count++;
                    else
                        assertTrue(false);
                }
                assertEquals(graph.getPredecessorCount(vertex), 0);
            } else if (vertex.getId().equals("2")) {
                vadas = vertex;
                assertEquals(graph.getOutEdges(vertex).size(), 0);
                assertEquals(graph.getInEdges(vertex).size(), 1);
                assertEquals(graph.getNeighborCount(vertex), 1);
                int count = 0;
                for (Vertex vertex2 : graph.getNeighbors(vertex)) {
                    if (vertex2.getId().equals("1"))
                        count++;
                    else
                        assertTrue(false);
                }
                assertEquals(count, 1);
                assertEquals(graph.getSuccessorCount(vertex), 0);
                assertEquals(graph.getPredecessorCount(vertex), 1);
                count = 0;
                for (Vertex vertex2 : graph.getPredecessors(vertex)) {
                    if (vertex2.getId().equals("1"))
                        count++;
                    else
                        assertTrue(false);
                }
                assertEquals(count, 1);
            } else if (vertex.getId().equals("4")) {
                josh = vertex;
                assertEquals(graph.getOutEdges(vertex).size(), 2);
                assertEquals(graph.getInEdges(vertex).size(), 1);
                assertEquals(graph.getNeighborCount(vertex), 3);
                int count = 0;
                for (Vertex vertex2 : graph.getNeighbors(vertex)) {
                    if (vertex2.getId().equals("1"))
                        count++;
                    else if (vertex2.getId().equals("3"))
                        count++;
                    else if (vertex2.getId().equals("5"))
                        count++;
                    else
                        assertTrue(false);
                }
                assertEquals(count, 3);
                assertEquals(graph.getSuccessorCount(vertex), 2);
                count = 0;
                for (Vertex vertex2 : graph.getSuccessors(vertex)) {
                    if (vertex2.getId().equals("3"))
                        count++;
                    else if (vertex2.getId().equals("5"))
                        count++;
                    else
                        assertTrue(false);
                }
                assertEquals(count, 2);
                assertEquals(graph.getPredecessorCount(vertex), 1);
                count = 0;
                for (Vertex vertex2 : graph.getPredecessors(vertex)) {
                    if (vertex2.getId().equals("1"))
                        count++;
                    else
                        assertTrue(false);
                }
                assertEquals(count, 1);
            }

        }
        assertTrue(null != marko);
        assertTrue(null != vadas);
        assertTrue(null != josh);
        assertEquals(graph.findEdgeSet(marko, josh).size(), 1);
        assertTrue(graph.findEdgeSet(marko, josh).contains(graph.findEdge(marko, josh)));
        assertEquals(graph.getDefaultEdgeType(), EdgeType.DIRECTED);
        for (Edge edge : graph.getEdges()) {
            assertTrue(graph.containsEdge(edge));
            assertEquals(graph.getEdgeType(edge), EdgeType.DIRECTED);
            assertEquals(graph.getIncidentCount(edge), 2);
        }

    }
}
