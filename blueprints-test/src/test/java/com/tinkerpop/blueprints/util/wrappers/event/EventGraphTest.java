package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.IndexTestSuite;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;
import com.tinkerpop.blueprints.util.wrappers.event.listener.ConsoleGraphChangedListener;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.util.wrappers.event.listener.StubGraphChangedListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EventGraphTest extends GraphTest {

    private StubGraphChangedListener graphChangedListener;
    private EventGraph<TinkerGraph> graph;

    public void testVertexTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new VertexTestSuite(this));
        printTestPerformance("VertexTestSuite", this.stopWatch());
    }

    public void testEdgeTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new EdgeTestSuite(this));
        printTestPerformance("EdgeTestSuite", this.stopWatch());
    }

    public void testGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphTestSuite(this));
        printTestPerformance("GraphTestSuite", this.stopWatch());
    }

    public void testIndexableGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new IndexableGraphTestSuite(this));
        printTestPerformance("IndexableGraphTestSuite", this.stopWatch());
    }

    public void testIndexTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new IndexTestSuite(this));
        printTestPerformance("IndexTestSuite", this.stopWatch());
    }

    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }

    public void testGMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GMLReaderTestSuite(this));
        printTestPerformance("GMLReaderTestSuite", this.stopWatch());
    }

    public void testGraphSONReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphSONReaderTestSuite(this));
        printTestPerformance("GraphSONReaderTestSuite", this.stopWatch());
    }

    public Graph generateGraph() {
        return generateGraph("");
    }

    public Graph generateGraph(final String graphDirectoryName) {
        return new EventIndexableGraph<TinkerGraph>(new TinkerGraph());
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                method.invoke(testSuite);
            }
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        graphChangedListener = new StubGraphChangedListener();
        graph = new EventGraph<TinkerGraph>(TinkerGraphFactory.createTinkerGraph());
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
        assertTrue(graph.getVertices() instanceof EventVertexIterable);
        assertTrue(graph.getEdges() instanceof EventEdgeIterable);
        assertEquals(count(graph.getVertices()), 6);
        assertEquals(count(graph.getEdges()), 6);

        graph.removeVertex(graph.getVertex(1));
        assertNull(graph.getVertex(1));

        graph.removeEdge(graph.getEdge(10));
        assertNull(graph.getEdge(10));

        graph.shutdown();

    }

    public void testEventedElement() {
        graph.addListener(new ConsoleGraphChangedListener(graph));
        for (Vertex vertex : graph.getVertices()) {
            assertTrue(vertex instanceof EventVertex);
            vertex.setProperty("name", "noname");

            assertEquals("noname", vertex.getProperty("name"));

            assertTrue(vertex.getEdges(Direction.OUT) instanceof EventEdgeIterable);
            assertTrue(vertex.getEdges(Direction.IN) instanceof EventEdgeIterable);
            assertTrue(vertex.getEdges(Direction.OUT, "knows") instanceof EventEdgeIterable);
            assertTrue(vertex.getEdges(Direction.IN, "created") instanceof EventEdgeIterable);
        }

        for (Edge edge : graph.getEdges()) {
            assertTrue(edge instanceof EventEdge);
            edge.removeProperty("weight");
            assertNull(edge.getProperty("weight"));

            assertTrue(edge.getVertex(Direction.OUT) instanceof EventVertex);
            assertTrue(edge.getVertex(Direction.IN) instanceof EventVertex);
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

    public void testFireVertexAdded() {
        graph.addListener(graphChangedListener);

        Vertex vertex = createVertex();

        assertEquals(1, graphChangedListener.addVertexEventRecorded());

        graphChangedListener.reset();

        graph.getVertex(vertex.getId());

        assertEquals(0, graphChangedListener.addVertexEventRecorded());
    }

    public void testFireVertexPropertyChanged() {
        graph.addListener(graphChangedListener);

        Vertex vertex = createVertex();
        vertex.setProperty("name", "marko");

        assertEquals(1, graphChangedListener.vertexPropertyChangedEventRecorded());

        graphChangedListener.reset();

        vertex.getProperty("name");

        assertEquals(0, graphChangedListener.vertexPropertyChangedEventRecorded());
    }

    public void testFireVertexPropertyRemoved() {
        graph.addListener(graphChangedListener);

        Vertex vertex = createVertex();
        vertex.setProperty("name", "marko");
        vertex.removeProperty("name");

        assertEquals(1, graphChangedListener.vertexPropertyRemovedEventRecorded());
    }

    public void testFireVertexRemoved() {
        graph.addListener(graphChangedListener);

        Vertex vertex = createVertex();
        graph.removeVertex(vertex);

        assertEquals(1, graphChangedListener.vertexRemovedEventRecorded());
    }

    public void testFireVertexRemovedAndValidateProperties() {

        final List<Boolean> fired = new ArrayList<Boolean>();
        graph.addListener(new GraphChangedListener() {
            @Override
            public void vertexAdded(Vertex vertex) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void vertexPropertyChanged(Vertex vertex, String key, Object oldValue, Object setValue) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void vertexPropertyRemoved(Vertex vertex, String key, Object removedValue) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void vertexRemoved(Vertex vertex, Map<String, Object> props) {
                assertTrue(props.containsKey("name"));
                assertEquals("marko", props.get("name"));

                fired.add(true);
            }

            @Override
            public void edgeAdded(Edge edge) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void edgePropertyChanged(Edge edge, String key, Object oldValue, Object setValue) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void edgePropertyRemoved(Edge edge, String key, Object removedValue) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void edgeRemoved(Edge edge, Map<String, Object> props) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        Vertex vertex = createVertex();
        vertex.setProperty("name", "marko");
        graph.removeVertex(vertex);

        assertEquals(1, fired.size());
        assertTrue(fired.get(0));
    }

    public void testFireEdgeAdded() {
        graph.addListener(graphChangedListener);

        Edge edge = createEdge();

        assertEquals(1, graphChangedListener.addEdgeEventRecorded());

        graphChangedListener.reset();

        graph.getEdge(edge.getId());

        assertEquals(0, graphChangedListener.addEdgeEventRecorded());

    }

    public void testFireEdgePropertyChanged() {
        graph.addListener(graphChangedListener);

        Edge edge = createEdge();

        edge.setProperty("weight", System.currentTimeMillis());

        assertEquals(1, graphChangedListener.edgePropertyChangedEventRecorded());

        graphChangedListener.reset();

        edge.getProperty("weight");

        assertEquals(0, graphChangedListener.edgePropertyChangedEventRecorded());
    }

    public void testFireEdgePropertyRemoved() {
        graph.addListener(graphChangedListener);

        Edge edge = createEdge();

        edge.setProperty("weight", System.currentTimeMillis());

        edge.removeProperty("weight");

        assertEquals(1, graphChangedListener.edgePropertyRemovedEventRecorded());
    }

    public void testFireEdgeRemoved() {
        graph.addListener(graphChangedListener);

        Edge edge = createEdge();

        graph.removeEdge(edge);

        assertEquals(1, graphChangedListener.edgeRemovedEventRecorded());
    }

    public void testFireEdgeRemovedAndValidateProperties() {
        final List<Boolean> fired = new ArrayList<Boolean>();
        graph.addListener(new GraphChangedListener() {
            @Override
            public void vertexAdded(Vertex vertex) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void vertexPropertyChanged(Vertex vertex, String key, Object oldValue, Object setValue) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void vertexPropertyRemoved(Vertex vertex, String key, Object removedValue) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void vertexRemoved(Vertex vertex, Map<String, Object> props) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void edgeAdded(Edge edge) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void edgePropertyChanged(Edge edge, String key, Object oldValue, Object setValue) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void edgePropertyRemoved(Edge edge, String key, Object removedValue) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void edgeRemoved(Edge edge, Map<String, Object> props) {
                assertTrue(props.containsKey("weight"));
                assertEquals(0.5f, props.get("weight"));
                fired.add(true);
            }
        });

        final Edge e = createEdge();
        e.setProperty("weight", 0.5f);

        graph.removeEdge(e);

        assertEquals(1, fired.size());
        assertTrue(fired.get(0));
    }

    private Edge createEdge() {
        return graph.addEdge(null, createVertex(), createVertex(), "knows");
    }

    private Vertex createVertex() {
        return graph.addVertex(null);
    }

    public void testMutateInListener() {
        StubGraphChangedListener listener = new StubGraphChangedListener() {

            @Override
            public void vertexPropertyChanged(Vertex vertex, String key, Object oldValue, Object setValue) {
                if (!"setInListener".equals(key)) {
                    vertex.setProperty("setInListener", 12345);
                }
                super.vertexPropertyChanged(vertex, key, oldValue, setValue);
            }
        };
        graph.addListener(listener);
        Vertex vertex = createVertex();
        vertex.setProperty("test", 123);

        assertEquals(12345, vertex.getProperty("setInListener"));
        assertEquals(2, listener.vertexPropertyChangedEventRecorded());
    }
}
