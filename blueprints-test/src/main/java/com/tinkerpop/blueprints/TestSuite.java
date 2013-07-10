package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.impls.GraphTest;

import java.util.Collection;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class TestSuite extends BaseTest {

    protected GraphTest graphTest;

    public TestSuite() {
    }

    public TestSuite(final GraphTest graphTest) {
        this.graphTest = graphTest;
    }

    protected void vertexCount(final Graph graph, int expectedCount) {
        if (graph.getFeatures().supportsVertexIteration) assertEquals(count(graph.getVertices()), expectedCount);
    }

    protected void containsVertices(final Graph graph, final Collection<Vertex> vertices) {
        for (Vertex v : vertices) {
            Vertex vp = graph.getVertex(v.getId());
            if (vp == null || !vp.getId().equals(v.getId()))
                fail("Graph does not contain vertex: '" + v + "'");
        }
    }

    protected void edgeCount(final Graph graph, int expectedCount) {
        if (graph.getFeatures().supportsEdgeIteration) assertEquals(count(graph.getEdges()), expectedCount);
    }


}
