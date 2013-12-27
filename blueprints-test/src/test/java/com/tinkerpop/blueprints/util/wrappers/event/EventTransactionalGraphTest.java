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
import com.tinkerpop.blueprints.util.io.csv.CSVReaderTestSuite;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;
import com.tinkerpop.blueprints.util.wrappers.event.listener.ConsoleGraphChangedListener;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.util.wrappers.event.listener.StubGraphChangedListener;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class EventTransactionalGraphTest extends GraphTest {

    private StubGraphChangedListener graphChangedListener;
    private EventTransactionalGraph<TinkerTransactionalGraph> graph;

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

    public void testCSVReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new CSVReaderTestSuite(this));
        printTestPerformance("CSVReaderTestSuite", this.stopWatch());
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

    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }

    public Graph generateGraph() {
        return generateGraph("");
    }

    public Graph generateGraph(final String graphDirectoryName) {
        return new EventTransactionalIndexableGraph<TinkerTransactionalGraph>(new TinkerTransactionalGraph());
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
        graph = new EventTransactionalGraph<TinkerTransactionalGraph>(TinkerTransactionalGraph.createTinkerGraph());
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

        assertEquals(0, graphChangedListener.addVertexEventRecorded());
        ((EventTransactionalGraph) graph).commit();

        assertEquals(1, graphChangedListener.addVertexEventRecorded());

        graphChangedListener.reset();

        graph.getVertex(vertex.getId());

        assertEquals(0, graphChangedListener.addVertexEventRecorded());
    }

    public void testFireVertexPropertyChanged() {
        graph.addListener(graphChangedListener);

        Vertex vertex = createVertex();
        vertex.setProperty("name", "marko");

        assertEquals(0, graphChangedListener.vertexPropertyChangedEventRecorded());
        ((EventTransactionalGraph) graph).commit();
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

        assertEquals(0, graphChangedListener.vertexPropertyRemovedEventRecorded());
        ((EventTransactionalGraph) graph).commit();
        assertEquals(1, graphChangedListener.vertexPropertyRemovedEventRecorded());
    }

    public void testFireVertexRemoved() {
        graph.addListener(graphChangedListener);

        Vertex vertex = createVertex();
        graph.removeVertex(vertex);

        assertEquals(0, graphChangedListener.vertexRemovedEventRecorded());
        ((EventTransactionalGraph) graph).commit();
        assertEquals(1, graphChangedListener.vertexRemovedEventRecorded());
    }

    public void testFireEdgeAdded() {
        graph.addListener(graphChangedListener);

        Edge edge = createEdge();

        assertEquals(0, graphChangedListener.addEdgeEventRecorded());
        ((EventTransactionalGraph) graph).commit();
        assertEquals(1, graphChangedListener.addEdgeEventRecorded());

        graphChangedListener.reset();

        graph.getEdge(edge.getId());

        assertEquals(0, graphChangedListener.addEdgeEventRecorded());

    }

    public void testFireEdgePropertyChanged() {
        graph.addListener(graphChangedListener);

        Edge edge = createEdge();

        edge.setProperty("weight", System.currentTimeMillis());

        assertEquals(0, graphChangedListener.edgePropertyChangedEventRecorded());
        ((EventTransactionalGraph) graph).commit();
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

        assertEquals(0, graphChangedListener.edgePropertyRemovedEventRecorded());
        ((EventTransactionalGraph) graph).commit();
        assertEquals(1, graphChangedListener.edgePropertyRemovedEventRecorded());
    }

    public void testFireEdgeRemoved() {
        graph.addListener(graphChangedListener);

        Edge edge = createEdge();

        graph.removeEdge(edge);

        assertEquals(0, graphChangedListener.edgeRemovedEventRecorded());
        ((EventTransactionalGraph) graph).commit();
        assertEquals(1, graphChangedListener.edgeRemovedEventRecorded());
    }

    public void testTransactionSeriesWithSuccess() {
        graph.addListener(graphChangedListener);

        createEdge();
        Edge e = createEdge();
        e.setProperty("test", "it");
        e.setProperty("test", "that");

        Vertex v = createVertex();
        v.setProperty("test", "it");

        assertEquals(0, graphChangedListener.addEdgeEventRecorded());
        assertEquals(0, graphChangedListener.addVertexEventRecorded());
        assertEquals(0, graphChangedListener.edgePropertyChangedEventRecorded());
        assertEquals(0, graphChangedListener.vertexPropertyChangedEventRecorded());

        ((EventTransactionalGraph) graph).commit();

        assertEquals(2, graphChangedListener.addEdgeEventRecorded());
        assertEquals(5, graphChangedListener.addVertexEventRecorded());
        assertEquals(2, graphChangedListener.edgePropertyChangedEventRecorded());
        assertEquals(1, graphChangedListener.vertexPropertyChangedEventRecorded());
    }

    public void testTransactionSeriesWithFailure() {
        graph.addListener(graphChangedListener);

        createEdge();
        Edge e = createEdge();
        e.setProperty("test", "it");
        e.setProperty("test", "that");

        Vertex v = createVertex();
        v.setProperty("test", "it");

        assertEquals(0, graphChangedListener.addEdgeEventRecorded());
        assertEquals(0, graphChangedListener.addVertexEventRecorded());
        assertEquals(0, graphChangedListener.edgePropertyChangedEventRecorded());
        assertEquals(0, graphChangedListener.vertexPropertyChangedEventRecorded());

        ((EventTransactionalGraph) graph).rollback();

        assertEquals(0, graphChangedListener.addEdgeEventRecorded());
        assertEquals(0, graphChangedListener.addVertexEventRecorded());
        assertEquals(0, graphChangedListener.edgePropertyChangedEventRecorded());
        assertEquals(0, graphChangedListener.vertexPropertyChangedEventRecorded());
    }

    public void testTransactionSeriesOrder() {
        graph.addListener(graphChangedListener);
        graph.addListener(new ConsoleGraphChangedListener(graph));

        Vertex v1 = graph.addVertex(10);
        v1.setProperty("aaa", "bbb");
        v1.setProperty("ccc", "ddd");
        v1.removeProperty("aaa");

        Vertex v2 = graph.addVertex(20);
        Vertex v3 = graph.addVertex(30);

        Edge e1 = graph.addEdge(100, v1, v2, "friend");
        e1.setProperty("eee", "fff");
        e1.setProperty("ggg", "hhh");
        e1.setProperty("ggg", "hhhh");
        e1.removeProperty("eee");

        Edge e2 = graph.addEdge(101, v1, v2, "enemy");

        graph.removeEdge(e2);
        graph.removeVertex(v3);

        assertEquals(0, graphChangedListener.getOrder().size());

        ((EventTransactionalGraph) graph).commit();

        List<String> order = graphChangedListener.getOrder();
        assertEquals("v-added-10", order.get(0));
        assertEquals("v-property-changed-10-aaa:null->bbb", order.get(1));
        assertEquals("v-property-changed-10-ccc:null->ddd", order.get(2));
        assertEquals("v-property-removed-10-aaa:bbb", order.get(3));
        assertEquals("v-added-20", order.get(4));
        assertEquals("v-added-30", order.get(5));
        assertEquals("e-added-100", order.get(6));
        assertEquals("e-property-changed-100-eee:null->fff", order.get(7));
        assertEquals("e-property-changed-100-ggg:null->hhh", order.get(8));
        assertEquals("e-property-changed-100-ggg:hhh->hhhh", order.get(9));
        assertEquals("e-property-removed-100-eee:fff", order.get(10));
        assertEquals("e-added-101", order.get(11));
        assertEquals("e-removed-101", order.get(12));
        assertEquals("v-removed-30", order.get(13));

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
        graph.commit();
        assertEquals(12345, vertex.getProperty("setInListener"));
        assertEquals(2, listener.vertexPropertyChangedEventRecorded());
    }

}


