package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class AutomaticIndexHelperTest extends BaseTest {

    public void testAddRemoveElements() {
        IndexableGraph graph = new TinkerGraph();
        graph.dropIndex(Index.VERTICES);
        graph.dropIndex(Index.EDGES);
        Vertex a = graph.addVertex(null);
        a.setProperty("name", "marko");
        Edge b = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "friend");

        AutomaticIndex idxVertex = graph.createAutomaticIndex(Index.VERTICES, Vertex.class, null);
        AutomaticIndex idxEdge = graph.createAutomaticIndex(Index.EDGES, Edge.class, null);
        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 0);
        assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get(AutomaticIndex.LABEL, "friend")), 0);

        AutomaticIndexHelper.addElement(idxVertex, a);
        AutomaticIndexHelper.addElement(graph, b);

        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 1);
        assertEquals(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko").iterator().next(), a);
        assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get(AutomaticIndex.LABEL, "friend")), 1);
        assertEquals(graph.getIndex(Index.EDGES, Edge.class).get("label", "friend").iterator().next(), b);

        AutomaticIndexHelper.removeElement(graph, a);
        AutomaticIndexHelper.removeElement(idxEdge, b);

        assertEquals(count(graph.getIndex(Index.VERTICES, Vertex.class).get("name", "marko")), 0);
        assertEquals(count(graph.getIndex(Index.EDGES, Edge.class).get(AutomaticIndex.LABEL, "friend")), 0);

        graph.shutdown();
    }

    public void testReIndexElements() {
        IndexableGraph graph = new TinkerGraph();
        graph.dropIndex(Index.VERTICES);
        graph.dropIndex(Index.EDGES);
        Vertex a = graph.addVertex(null);
        a.setProperty("name", "marko");
        a.setProperty("age", 31);
        AutomaticIndex index = graph.createAutomaticIndex(Index.VERTICES, Vertex.class, null);
        assertEquals(count(index.get("name", "marko")), 0);
        assertEquals(count(index.get("age", 31)), 0);
        AutomaticIndexHelper.reIndexElements(graph, (Iterable) graph.getVertices());
        assertEquals(count(index.get("name", "marko")), 1);
        assertEquals(index.get("name", "marko").iterator().next(), a);
        assertEquals(count(index.get("age", 31)), 1);
        assertEquals(index.get("age", 31).iterator().next(), a);

        graph.dropIndex(Index.VERTICES);
        Set<String> indexKeys = new HashSet<String>();
        indexKeys.add("name");
        index = graph.createAutomaticIndex(Index.VERTICES, Vertex.class, indexKeys);
        assertEquals(count(index.get("name", "marko")), 0);
        assertEquals(count(index.get("age", 31)), 0);
        index = AutomaticIndexHelper.reIndexElements(graph, index, (Iterable) graph.getVertices());
        assertEquals(count(index.get("name", "marko")), 1);
        assertEquals(index.get("name", "marko").iterator().next(), a);
        assertEquals(count(index.get("age", 31)), 0);

        graph.shutdown();
    }
}
