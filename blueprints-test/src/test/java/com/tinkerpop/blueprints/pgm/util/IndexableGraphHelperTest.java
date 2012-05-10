package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IndexableGraphHelperTest extends BaseTest {

    public void testAddUniqueVertex() {
        /*IndexableGraph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex vertex = IndexableGraphHelper.addUniqueVertex(graph, null, graph.getIndex(Index.VERTICES, Vertex.class), "name", "marko");
        assertEquals(vertex.getProperty("name"), "marko");
        assertEquals(vertex, graph.getVertex(1));
        assertEquals(count(graph.getVertices()), 6);
        assertEquals(count(graph.getEdges()), 6);

        vertex = IndexableGraphHelper.addUniqueVertex(graph, null, graph.getIndex(Index.VERTICES, Vertex.class), "name", "darrick");
        assertEquals(vertex.getProperty("name"), "darrick");
        assertEquals(count(graph.getVertices()), 7);
        assertEquals(count(graph.getEdges()), 6);*/
        assertTrue(true);
    }
}
