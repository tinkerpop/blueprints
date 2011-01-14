package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ElementHelperTest extends BaseTest {

    public void testCopyElementProperties() {
        Graph graph = new TinkerGraph();
        Vertex v = graph.addVertex(null);
        v.setProperty("name", "marko");
        v.setProperty("age", 31);
        Vertex u = graph.addVertex(null);
        assertEquals(u.getPropertyKeys().size(), 0);
        ElementHelper.copyElementProperties(v, u);
        assertEquals(u.getPropertyKeys().size(), 2);
        assertEquals(u.getProperty("name"), "marko");
        assertEquals(u.getProperty("age"), 31);
    }
}
