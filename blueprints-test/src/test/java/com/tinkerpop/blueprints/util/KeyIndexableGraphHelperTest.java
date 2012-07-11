package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class KeyIndexableGraphHelperTest extends BaseTest {

    public void testReIndexElements() {
        TinkerGraph graph = TinkerGraphFactory.createTinkerGraph();
        assertTrue(graph.getVertices("name", "marko") instanceof PropertyFilteredIterable);
        assertEquals(count(graph.getVertices("name", "marko")), 1);
        assertEquals(graph.getVertices("name", "marko").iterator().next(), graph.getVertex(1));
        graph.createKeyIndex("name", Vertex.class);
        //KeyIndexableGraphHelper.reIndexElements(graph, graph.getVertices(), new HashSet<String>(Arrays.asList("name")));
        assertFalse(graph.getVertices("name", "marko") instanceof PropertyFilteredIterable);
        assertEquals(count(graph.getVertices("name", "marko")), 1);
        assertEquals(graph.getVertices("name", "marko").iterator().next(), graph.getVertex(1));
    }
}
