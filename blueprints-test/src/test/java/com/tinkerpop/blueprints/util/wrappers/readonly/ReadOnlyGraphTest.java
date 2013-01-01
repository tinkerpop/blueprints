package com.tinkerpop.blueprints.util.wrappers.readonly;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyGraphTest extends BaseTest {

    public void testWrappedElementUniqueness() {
        Graph graph = new ReadOnlyGraph<TinkerGraph>(TinkerGraphFactory.createTinkerGraph());
        assertEquals(graph.getVertex(1), graph.getVertex(1));
        Set<Vertex> set = new HashSet<Vertex>();
        set.add(graph.getVertex(2));
        set.add(graph.getVertex(2));
        assertEquals(set.size(), 1);
        assertEquals(graph.getEdge(7).hashCode(), graph.getEdge(7).hashCode());
        assertEquals(graph.getEdge(8), graph.getEdge(8));
    }

    public void testReadOnlyGraph() {
        Graph graph = new ReadOnlyGraph<TinkerGraph>(TinkerGraphFactory.createTinkerGraph());
        assertTrue(graph.getVertices() instanceof ReadOnlyVertexIterable);
        assertTrue(graph.getEdges() instanceof ReadOnlyEdgeIterable);
        assertEquals(count(graph.getVertices()), 6);
        assertEquals(count(graph.getEdges()), 6);
        try {
            graph.addVertex(null);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
        try {
            graph.addEdge(null, null, null, "knows");
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
        try {
            graph.removeVertex(graph.getVertex(1));
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
        try {
            graph.removeEdge(graph.getEdge(10));
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
        try {
            graph.shutdown();
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
    }

    public void testReadOnlyElement() {
        Graph graph = new ReadOnlyGraph<TinkerGraph>(TinkerGraphFactory.createTinkerGraph());
        for (Vertex vertex : graph.getVertices()) {
            assertTrue(vertex instanceof ReadOnlyVertex);
            try {
                vertex.setProperty("name", "noname");
                assertTrue(false);
            } catch (UnsupportedOperationException e) {
                assertTrue(true);
            }
            vertex.getProperty("name");
            vertex.getPropertyKeys();
            assertTrue(vertex.getEdges(Direction.OUT) instanceof ReadOnlyEdgeIterable);
            assertTrue(vertex.getEdges(Direction.IN) instanceof ReadOnlyEdgeIterable);
            assertTrue(vertex.getEdges(Direction.OUT, "knows") instanceof ReadOnlyEdgeIterable);
            assertTrue(vertex.getEdges(Direction.IN, "created") instanceof ReadOnlyEdgeIterable);
        }
        for (Edge edge : graph.getEdges()) {
            assertTrue(edge instanceof ReadOnlyEdge);
            try {
                edge.removeProperty("weight");
                assertTrue(false);
            } catch (UnsupportedOperationException e) {
                assertTrue(true);
            }
            edge.getProperty("weight");
            edge.getPropertyKeys();
            assertTrue(edge.getVertex(Direction.OUT) instanceof ReadOnlyVertex);
            assertTrue(edge.getVertex(Direction.IN) instanceof ReadOnlyVertex);
        }
    }

    public void testReadOnlyIndices() {
        IndexableGraph graph = new ReadOnlyIndexableGraph<TinkerGraph>(TinkerGraphFactory.createTinkerGraph());
        Index<Vertex> index = ((WrapperGraph<IndexableGraph>) graph).getBaseGraph().createIndex("blah", Vertex.class);
        index.put("name", "marko", graph.getVertex(1));
        assertTrue(graph.getIndices() instanceof ReadOnlyIndexIterable);
        index = graph.getIndex("blah", Vertex.class);
        assertTrue(index instanceof ReadOnlyIndex);
        assertTrue(index.get("name", "marko") instanceof ReadOnlyVertexIterable);
        try {
            index.put("name", "noname", graph.getVertex(1));
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
        try {
            index.remove("name", "marko", graph.getVertex(1));
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
        assertTrue(Vertex.class.isAssignableFrom(index.getIndexClass()));
        assertEquals(index.getIndexName(), "blah");
    }

    public void testReadOnlyKeyIndices() {
        KeyIndexableGraph graph = new ReadOnlyKeyIndexableGraph<TinkerGraph>(TinkerGraphFactory.createTinkerGraph());
        ((KeyIndexableGraph) ((WrapperGraph<IndexableGraph>) graph).getBaseGraph()).createKeyIndex("blah", Vertex.class);
        assertTrue(graph.getIndexedKeys(Vertex.class) instanceof Set);
        assertEquals(graph.getIndexedKeys(Vertex.class).size(), 1);
        assertTrue(graph.getIndexedKeys(Vertex.class).contains("blah"));
        try {
            graph.createKeyIndex("whatever", Vertex.class);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
        try {
            graph.dropKeyIndex("blah", Vertex.class);
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
    }
}
