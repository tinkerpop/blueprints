package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;

import java.util.Arrays;
import java.util.Map;

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
        ElementHelper.copyProperties(v, u);
        assertEquals(u.getPropertyKeys().size(), 2);
        assertEquals(u.getProperty("name"), "marko");
        assertEquals(u.getProperty("age"), 31);
    }

    public void testRemoveProperties() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex vertex = graph.getVertex(1);
        assertEquals(vertex.getProperty("name"), "marko");
        assertEquals(vertex.getProperty("age"), 29);
        assertEquals(vertex.getPropertyKeys().size(), 2);

        ElementHelper.removeProperties(Arrays.asList((Element) vertex));
        assertNull(vertex.getProperty("name"));
        assertNull(vertex.getProperty("age"));
        assertEquals(vertex.getPropertyKeys().size(), 0);

        ElementHelper.removeProperties(Arrays.asList((Element) vertex));
        assertNull(vertex.getProperty("name"));
        assertNull(vertex.getProperty("age"));
        assertEquals(vertex.getPropertyKeys().size(), 0);
    }

    public void testRemoveProperty() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        ElementHelper.removeProperty("name", (Iterable) graph.getVertices());
        for (Vertex v : graph.getVertices()) {
            assertNull(v.getProperty("name"));
        }
    }

    public void testRenameProperty() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        ElementHelper.renameProperty("name", "name2", (Iterable) graph.getVertices());
        for (Vertex v : graph.getVertices()) {
            assertNull(v.getProperty("name"));
            assertNotNull(v.getProperty("name2"));
            String name2 = (String) v.getProperty("name2");
            assertTrue(name2.equals("marko") || name2.equals("josh") || name2.equals("vadas") || name2.equals("ripple") || name2.equals("lop") || name2.equals("peter"));
        }
    }

    public void testTypecastProperty() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        for (Edge e : graph.getEdges()) {
            assertTrue(e.getProperty("weight") instanceof Float);
        }
        ElementHelper.typecastProperty("weight", Double.class, (Iterable) graph.getEdges());
        for (Edge e : graph.getEdges()) {
            assertTrue(e.getProperty("weight") instanceof Double);
        }
    }

    public void testHaveEqualProperties() {
        Graph graph = new TinkerGraph();
        Vertex a = graph.addVertex(null);
        Vertex b = graph.addVertex(null);
        Vertex c = graph.addVertex(null);
        Vertex d = graph.addVertex(null);

        a.setProperty("name", "marko");
        a.setProperty("age", 31);
        b.setProperty("name", "marko");
        b.setProperty("age", 31);
        c.setProperty("name", "marko");
        d.setProperty("name", "pavel");
        d.setProperty("age", 31);

        assertTrue(ElementHelper.haveEqualProperties(a, b));
        assertTrue(ElementHelper.haveEqualProperties(a, a));
        assertFalse(ElementHelper.haveEqualProperties(a, c));
        assertFalse(ElementHelper.haveEqualProperties(c, a));
        assertFalse(ElementHelper.haveEqualProperties(a, d));
        assertFalse(ElementHelper.haveEqualProperties(a, c));

    }

    public void testGetProperties() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex vertex = graph.getVertex(1);
        Map<String, Object> map = ElementHelper.getProperties(vertex);
        assertEquals(map.size(), 2);
        assertEquals(map.get("name"), "marko");
        assertEquals(map.get("age"), 29);

        map.put("name", "pavel");
        assertEquals(map.get("name"), "pavel");

        assertEquals(vertex.getProperty("name"), "marko");
    }
}
