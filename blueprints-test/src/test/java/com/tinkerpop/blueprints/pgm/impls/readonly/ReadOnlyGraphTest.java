package com.tinkerpop.blueprints.pgm.impls.readonly;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.readonly.util.ReadOnlyEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.readonly.util.ReadOnlyIndexSequence;
import com.tinkerpop.blueprints.pgm.impls.readonly.util.ReadOnlyVertexSequence;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyGraphTest extends BaseTest {

    public void testReadOnlyGraph() {
        Graph graph = new ReadOnlyGraph(TinkerGraphFactory.createTinkerGraph());
        assertTrue(graph.getVertices() instanceof ReadOnlyVertexSequence);
        assertTrue(graph.getEdges() instanceof ReadOnlyEdgeSequence);
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
            graph.clear();
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
        Graph graph = new ReadOnlyGraph(TinkerGraphFactory.createTinkerGraph());
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
            assertTrue(vertex.getOutEdges() instanceof ReadOnlyEdgeSequence);
            assertTrue(vertex.getInEdges() instanceof ReadOnlyEdgeSequence);
            assertTrue(vertex.getOutEdges("knows") instanceof ReadOnlyEdgeSequence);
            assertTrue(vertex.getInEdges("created") instanceof ReadOnlyEdgeSequence);
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
            assertTrue(edge.getOutVertex() instanceof ReadOnlyVertex);
            assertTrue(edge.getInVertex() instanceof ReadOnlyVertex);
        }
    }

    public void testReadOnlyIndices() {
        IndexableGraph graph = new ReadOnlyIndexableGraph(TinkerGraphFactory.createTinkerGraph());
        assertTrue(graph.getIndices() instanceof ReadOnlyIndexSequence);
        Index<Vertex> index = graph.getIndex(Index.VERTICES, Vertex.class);
        assertTrue(index instanceof ReadOnlyIndex);
        assertTrue(index instanceof ReadOnlyAutomaticIndex);
        assertTrue(index.get("name", "marko") instanceof ReadOnlyVertexSequence);
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
        assertEquals(index.getIndexType(), Index.Type.AUTOMATIC);
        assertEquals(index.getIndexName(), Index.VERTICES);
    }
}
