package com.tinkerpop.blueprints.oupls;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.blueprints.oupls.jung.GraphJung;
import edu.uci.ics.jung.graph.util.EdgeType;
import junit.framework.TestCase;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GraphJungTest extends TestCase {

    public void testTinkerGraph() {
        GraphJung<TinkerGraph> jung = new GraphJung<TinkerGraph>(TinkerGraphFactory.createTinkerGraph());
        assertEquals(jung.getVertices().size(), 6);
        assertEquals(jung.getEdges().size(), 6);
        assertEquals(jung.getVertexCount(), 6);
        assertEquals(jung.getEdgeCount(), 6);
        Vertex marko = null;
        Vertex josh = null;
        Vertex vadas = null;
        for (Vertex vertex : jung.getVertices()) {
            assertTrue(jung.containsVertex(vertex));
            for (Edge edge : jung.getOutEdges(vertex)) {
                assertEquals(jung.getSource(edge), vertex);
            }
            for (Edge edge : jung.getInEdges(vertex)) {
                assertEquals(jung.getDest(edge), vertex);
            }
            if (vertex.getId().equals("1")) {
                marko = vertex;
                assertEquals(jung.getOutEdges(vertex).size(), 3);
                assertEquals(jung.getInEdges(vertex).size(), 0);
                assertEquals(jung.getNeighborCount(vertex), 3);
                int count = 0;
                for (Vertex vertex2 : jung.getNeighbors(vertex)) {
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
                assertEquals(jung.getSuccessorCount(vertex), 3);
                count = 0;
                for (Vertex vertex2 : jung.getSuccessors(vertex)) {
                    if (vertex2.getId().equals("2"))
                        count++;
                    else if (vertex2.getId().equals("4"))
                        count++;
                    else if (vertex2.getId().equals("3"))
                        count++;
                    else
                        assertTrue(false);
                }
                assertEquals(jung.getPredecessorCount(vertex), 0);
            } else if (vertex.getId().equals("2")) {
                vadas = vertex;
                assertEquals(jung.getOutEdges(vertex).size(), 0);
                assertEquals(jung.getInEdges(vertex).size(), 1);
                assertEquals(jung.getNeighborCount(vertex), 1);
                int count = 0;
                for (Vertex vertex2 : jung.getNeighbors(vertex)) {
                    if (vertex2.getId().equals("1"))
                        count++;
                    else
                        assertTrue(false);
                }
                assertEquals(count, 1);
                assertEquals(jung.getSuccessorCount(vertex), 0);
                assertEquals(jung.getPredecessorCount(vertex), 1);
                count = 0;
                for (Vertex vertex2 : jung.getPredecessors(vertex)) {
                    if (vertex2.getId().equals("1"))
                        count++;
                    else
                        assertTrue(false);
                }
                assertEquals(count, 1);
            } else if (vertex.getId().equals("4")) {
                josh = vertex;
                assertEquals(jung.getOutEdges(vertex).size(), 2);
                assertEquals(jung.getInEdges(vertex).size(), 1);
                assertEquals(jung.getNeighborCount(vertex), 3);
                int count = 0;
                for (Vertex vertex2 : jung.getNeighbors(vertex)) {
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
                assertEquals(jung.getSuccessorCount(vertex), 2);
                count = 0;
                for (Vertex vertex2 : jung.getSuccessors(vertex)) {
                    if (vertex2.getId().equals("3"))
                        count++;
                    else if (vertex2.getId().equals("5"))
                        count++;
                    else
                        assertTrue(false);
                }
                assertEquals(count, 2);
                assertEquals(jung.getPredecessorCount(vertex), 1);
                count = 0;
                for (Vertex vertex2 : jung.getPredecessors(vertex)) {
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
        assertEquals(jung.findEdgeSet(marko, josh).size(), 1);
        assertTrue(jung.findEdgeSet(marko, josh).contains(jung.findEdge(marko, josh)));
        assertEquals(jung.getDefaultEdgeType(), EdgeType.DIRECTED);
        for (Edge edge : jung.getEdges()) {
            assertTrue(jung.containsEdge(edge));
            assertEquals(jung.getEdgeType(edge), EdgeType.DIRECTED);
            assertEquals(jung.getIncidentCount(edge), 2);
        }

    }
}
