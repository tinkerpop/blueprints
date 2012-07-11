package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

import java.util.Arrays;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PropertyFilteredIterableTest extends BaseTest {

    public void testBasicFunctionality() {
        TinkerGraph graph = new TinkerGraph();
        Vertex a = graph.addVertex("a");
        a.setProperty("age", 29);
        Vertex b = graph.addVertex("b");
        b.setProperty("age", 29);
        Vertex c = graph.addVertex("c");
        c.setProperty("age", 30);
        Vertex d = graph.addVertex("d");
        d.setProperty("age", 31);

        // throw a vertex without the expected key in the mix
        Vertex e = graph.addVertex("e");
        List<Vertex> list = Arrays.asList(a, b, c, d, e);

        PropertyFilteredIterable<Vertex> iterable = new PropertyFilteredIterable<Vertex>("age", 29, list);
        assertEquals(count(iterable), 2);
        assertEquals(count(iterable), 2);
        for (Vertex vertex : iterable) {
            assertTrue(vertex.equals(a) || vertex.equals(b));
        }
        iterable = new PropertyFilteredIterable<Vertex>("age", 30, list);
        assertEquals(count(iterable), 1);
        assertEquals(iterable.iterator().next(), c);

        iterable = new PropertyFilteredIterable<Vertex>("age", 30, graph.getVertices());
        assertEquals(count(iterable), 1);
        assertEquals(iterable.iterator().next(), c);

        iterable = new PropertyFilteredIterable<Vertex>("age", 37, list);
        assertEquals(count(iterable), 0);
    }

}
