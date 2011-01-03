package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TestSuite;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IndexHelperTestSuite extends TestSuite {
    public IndexHelperTestSuite() {
    }

    public IndexHelperTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testReIndex() {
        IndexableGraph graph = (IndexableGraph) graphTest.getGraphInstance();
        graph.dropIndex(Index.VERTICES);
        graph.dropIndex(Index.EDGES);
        Vertex a = graph.addVertex(null);
        a.setProperty("name", "marko");
        Index index = graph.createIndex("vertices", Vertex.class, Index.Type.AUTOMATIC);
        assertEquals(count(index.get("name", "marko")), 0);
        IndexHelper.reAutoIndex(graph, (Iterable) graph.getVertices());
        assertEquals(count(index.get("name", "marko")), 1);
        assertEquals(index.get("name", "marko").iterator().next(), a);
    }
}
