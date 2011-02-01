package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class AutomaticIndexHelperTest extends BaseTest {

    public void testReIndexElements() {
        IndexableGraph graph = new TinkerGraph();
        graph.dropIndex(Index.VERTICES);
        graph.dropIndex(Index.EDGES);
        Vertex a = graph.addVertex(null);
        a.setProperty("name", "marko");
        Index index = graph.createAutomaticIndex("vertices", Vertex.class, null);
        assertEquals(count(index.get("name", "marko")), 0);
        AutomaticIndexHelper.reIndexElements(graph, (Iterable) graph.getVertices());
        assertEquals(count(index.get("name", "marko")), 1);
        assertEquals(index.get("name", "marko").iterator().next(), a);
    }
}
