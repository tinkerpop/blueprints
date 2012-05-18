package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IndexableGraphHelperTest extends BaseTest {

    public void testAddUniqueVertex() {
        IndexableGraph graph = new TinkerGraph();
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
    }
}
