package com.tinkerpop.blueprints.pgm.impls.event;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.ConsoleGraphChangedListener;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.impls.event.util.EventEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.event.util.EventIndexSequence;
import com.tinkerpop.blueprints.pgm.impls.event.util.EventVertexSequence;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class EventedGraphTest extends BaseTest {

    private StubGraphChangedListener graphChangedListener;
    private EventGraph graph;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        graphChangedListener = new StubGraphChangedListener();
        graph = new EventGraph(TinkerGraphFactory.createTinkerGraph());
    }

    public void testWrappedElementUniqueness() {
        graph.addListener(new ConsoleGraphChangedListener(graph));

        assertEquals(graph.getVertex(1), graph.getVertex(1));
        Set<Vertex> set = new HashSet<Vertex>();
        set.add(graph.getVertex(2));
        set.add(graph.getVertex(2));
        assertEquals(set.size(), 1);
        assertEquals(graph.getEdge(7).hashCode(), graph.getEdge(7).hashCode());
        assertEquals(graph.getEdge(8), graph.getEdge(8));
    }

    public void testEventedGraph() {
        graph.addListener(new ConsoleGraphChangedListener(graph));
        assertTrue(graph.getVertices() instanceof EventVertexSequence);
        assertTrue(graph.getEdges() instanceof EventEdgeSequence);
        assertEquals(count(graph.getVertices()), 6);
        assertEquals(count(graph.getEdges()), 6);

        graph.removeVertex(graph.getVertex(1));
        assertNull(graph.getVertex(1));

        graph.removeEdge(graph.getEdge(10));
        assertNull(graph.getEdge(10));

        graph.clear();
        graph.shutdown();

    }

    public void testEventedElement() {
        graph.addListener(new ConsoleGraphChangedListener(graph));
        for (Vertex vertex : graph.getVertices()) {
            assertTrue(vertex instanceof EventVertex);
            vertex.setProperty("name", "noname");

            assertEquals("noname", vertex.getProperty("name"));

            assertTrue(vertex.getOutEdges() instanceof EventEdgeSequence);
            assertTrue(vertex.getInEdges() instanceof EventEdgeSequence);
            assertTrue(vertex.getOutEdges("knows") instanceof EventEdgeSequence);
            assertTrue(vertex.getInEdges("created") instanceof EventEdgeSequence);
        }

        for (Edge edge : graph.getEdges()) {
            assertTrue(edge instanceof EventEdge);
            edge.removeProperty("weight");
            assertNull(edge.getProperty("weight"));

            assertTrue(edge.getOutVertex() instanceof EventVertex);
            assertTrue(edge.getInVertex() instanceof EventVertex);
        }
    }

    public void testManageListeners() {
        EventGraph graph = this.graph;
        ConsoleGraphChangedListener listener1 = new ConsoleGraphChangedListener(graph);
        ConsoleGraphChangedListener listener2 = new ConsoleGraphChangedListener(graph);
        graph.addListener(listener1);
        graph.addListener(listener2);
        Iterator<GraphChangedListener> itty = graph.getListenerIterator();

        int counter = 0;
        while (itty.hasNext()) {
            itty.next();
            counter++;
        }

        assertEquals(2, counter);

        graph.removeListener(listener2);

        itty = graph.getListenerIterator();

        counter = 0;
        while (itty.hasNext()) {
            itty.next();
            counter++;
        }

        assertEquals(1, counter);

        graph.removeAllListeners();

        itty = graph.getListenerIterator();

        counter = 0;
        while (itty.hasNext()) {
            itty.next();
            counter++;
        }

        assertEquals(0, counter);
    }

    public void testEventIndices() {
        IndexableGraph graph = new EventIndexableGraph(TinkerGraphFactory.createTinkerGraph());
        assertTrue(graph.getIndices() instanceof EventIndexSequence);
        Index<Vertex> index = graph.getIndex(Index.VERTICES, Vertex.class);
        assertTrue(index instanceof EventIndex);
        assertTrue(index instanceof EventAutomaticIndex);
        assertTrue(index.get("name", "marko") instanceof EventVertexSequence);

        assertTrue(Vertex.class.isAssignableFrom(index.getIndexClass()));
        assertEquals(index.getIndexType(), Index.Type.AUTOMATIC);
        assertEquals(index.getIndexName(), Index.VERTICES);
    }

    public void testFireVertexAdded() {
        graph.addListener(graphChangedListener);

        Vertex vertex = createVertex();

        assertTrue(graphChangedListener.addVertexEventRecorded());

        graphChangedListener.reset();

        graph.getVertex(vertex.getId());

        assertFalse(graphChangedListener.addVertexEventRecorded());
    }

    public void testFireVertexPropertyChanged() {
        graph.addListener(graphChangedListener);

        Vertex vertex = createVertex();
        vertex.setProperty("name", "marko");

        assertTrue(graphChangedListener.vertexPropertyChangedEventRecorded());

        graphChangedListener.reset();

        vertex.getProperty("name");

        assertFalse(graphChangedListener.vertexPropertyChangedEventRecorded());
    }

    public void testFireVertexPropertyRemoved() {
        graph.addListener(graphChangedListener);

        Vertex vertex = createVertex();
        vertex.setProperty("name", "marko");
        vertex.removeProperty("name");

        assertTrue(graphChangedListener.vertexPropertyRemovedEventRecorded());
    }

    public void testFireVertexRemoved() {
        graph.addListener(graphChangedListener);

        Vertex vertex = createVertex();
        graph.removeVertex(vertex);

        assertTrue(graphChangedListener.vertexRemovedEventRecorded());
    }

    public void testFireEdgeAdded() {
        graph.addListener(graphChangedListener);

        Edge edge = createEdge();

        assertTrue(graphChangedListener.addEdgeEventRecorded());

        graphChangedListener.reset();

        graph.getEdge(edge.getId());

        assertFalse(graphChangedListener.addEdgeEventRecorded());

    }

    public void testFireEdgePropertyChanged() {
        graph.addListener(graphChangedListener);

        Edge edge = createEdge();

        edge.setProperty("weight", System.currentTimeMillis());

        assertTrue(graphChangedListener.edgePropertyChangedEventRecorded());

        graphChangedListener.reset();

        edge.getProperty("weight");

        assertFalse(graphChangedListener.edgePropertyChangedEventRecorded());
    }

    public void testFireEdgePropertyRemoved() {
        graph.addListener(graphChangedListener);

        Edge edge = createEdge();

        edge.setProperty("weight", System.currentTimeMillis());

        edge.removeProperty("weight");

        assertTrue(graphChangedListener.edgePropertyRemovedEventRecorded());
    }

    public void testFireEdgeRemoved() {
        graph.addListener(graphChangedListener);

        Edge edge = createEdge();

        graph.removeEdge(edge);

        assertTrue(graphChangedListener.edgeRemovedEventRecorded());
    }

    public void testFireGraphCleared() {
        graph.addListener(graphChangedListener);

        graph.clear();

        assertTrue(graphChangedListener.graphClearedEventRecorded());
    }

    private Edge createEdge() {
        return graph.addEdge(null, createVertex(), createVertex(), "knows");
    }

    private Vertex createVertex() {
        return graph.addVertex(null);
    }

}

