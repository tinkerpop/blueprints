package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class VertexHelperTest extends BaseTest {

    public void testEdgeSetEquality() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();

        for (Vertex v : graph.getVertices()) {
            for (Vertex u : graph.getVertices()) {
                if (ElementHelper.areEqual(v, u)) {
                    assertTrue(VertexHelper.haveEqualEdges(v, u, true));
                    assertTrue(VertexHelper.haveEqualEdges(v, u, false));
                } else {
                    assertFalse(VertexHelper.haveEqualEdges(v, u, true));
                    assertFalse(VertexHelper.haveEqualEdges(v, u, false));
                }

            }
        }

    }

    public void testNeighborhoodEquality() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();

        for (Vertex v : graph.getVertices()) {
            for (Vertex u : graph.getVertices()) {
                if (ElementHelper.areEqual(v, u)) {
                    assertTrue(VertexHelper.haveEqualNeighborhood(v, u, true));
                    assertTrue(VertexHelper.haveEqualNeighborhood(v, u, false));
                } else {
                    assertFalse(VertexHelper.haveEqualNeighborhood(v, u, true));
                    assertFalse(VertexHelper.haveEqualNeighborhood(v, u, false));
                }

            }
        }

    }
}
