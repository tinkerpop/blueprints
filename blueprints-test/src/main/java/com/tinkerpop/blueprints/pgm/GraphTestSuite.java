package com.tinkerpop.blueprints.pgm;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.impls.GraphTest;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;


/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GraphTestSuite extends TestSuite {

    public GraphTestSuite() {
    }

    public GraphTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }
    
    public void testFeatureCompliance() {
        Graph graph = graphTest.generateGraph();
        graph.getFeatures().checkCompliance();
        System.out.println(graph.getFeatures());
        graph.shutdown();
    }

    public void testEmptyOnConstruction() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(0, count(graph.getVertices()));
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(0, count(graph.getEdges()));
        graph.shutdown();
    }

    public void testStringRepresentation() {
        Graph graph = graphTest.generateGraph();
        try {
            this.stopWatch();
            assertNotNull(graph.toString());
            assertTrue(graph.toString().startsWith(graph.getClass().getSimpleName().toLowerCase()));
            printPerformance(graph.toString(), 1, "graph string representation generated", this.stopWatch());
        } catch (Exception e) {
            assertFalse(true);
        }
        graph.shutdown();
    }

    public void testSemanticallyCorrectIterables() {
        Graph graph = graphTest.generateGraph();
        for (int i = 0; i < 15; i++) {
            graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), convertId(graph,"knows"));
        }
        if (graph.getFeatures().supportsVertexIteration) {
            Iterable<Vertex> vertices = graph.getVertices();
            assertEquals(count(vertices), 30);
            assertEquals(count(vertices), 30);
            Iterator<Vertex> itty = vertices.iterator();
            int counter = 0;
            while (itty.hasNext()) {
                itty.next();
                counter++;
            }
            assertEquals(counter, 30);
        }
        if (graph.getFeatures().supportsEdgeIteration) {
            Iterable<Edge> edges = graph.getEdges();
            assertEquals(count(edges), 15);
            assertEquals(count(edges), 15);
            Iterator<Edge> itty = edges.iterator();
            int counter = 0;
            while (itty.hasNext()) {
                itty.next();
                counter++;
            }
            assertEquals(counter, 15);
        }
        graph.shutdown();
    }

    public void testGettingVerticesAndEdgesWithKeyValue() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexIteration && !graph.getFeatures().isRDFModel) {
            Vertex v1 = graph.addVertex(null);
            v1.setProperty("name", "marko");
            v1.setProperty("location", "everywhere");
            Vertex v2 = graph.addVertex(null);
            v2.setProperty("name", "stephen");
            v2.setProperty("location", "everywhere");

            assertEquals(count(graph.getVertices("location", "everywhere")), 2);
            assertEquals(count(graph.getVertices("name", "marko")), 1);
            assertEquals(count(graph.getVertices("name", "stephen")), 1);
            assertEquals(graph.getVertices("name", "marko").iterator().next(), v1);
            assertEquals(graph.getVertices("name", "stephen").iterator().next(), v2);
        }

        if (graph.getFeatures().supportsEdgeIteration && !graph.getFeatures().isRDFModel) {
            Edge e1 = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), convertId(graph,"knows"));
            e1.setProperty("name", "marko");
            e1.setProperty("location", "everywhere");
            Edge e2 = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), convertId(graph,"knows"));
            e2.setProperty("name", "stephen");
            e2.setProperty("location", "everywhere");

            assertEquals(count(graph.getEdges("location", "everywhere")), 2);
            assertEquals(count(graph.getEdges("name", "marko")), 1);
            assertEquals(count(graph.getEdges("name", "stephen")), 1);
            assertEquals(graph.getEdges("name", "marko").iterator().next(), e1);
            assertEquals(graph.getEdges("name", "stephen").iterator().next(), e2);
        }
        graph.shutdown();
    }

    public void testAddingVerticesAndEdges() {
        Graph graph = graphTest.generateGraph();
        Vertex a = graph.addVertex(null);
        Vertex b = graph.addVertex(null);
        Edge edge = graph.addEdge(null, a, b, convertId(graph,"knows"));
        if (graph.getFeatures().supportsEdgeIteration) {
            assertEquals(1, count(graph.getEdges()));
        }
        if (graph.getFeatures().supportsVertexIteration) {
            assertEquals(2, count(graph.getVertices()));
        }
        graph.removeVertex(a);
        if (graph.getFeatures().supportsEdgeIteration) {
            assertEquals(0, count(graph.getEdges()));
        }
        if (graph.getFeatures().supportsVertexIteration) {
            assertEquals(1, count(graph.getVertices()));
        }
        try {
            graph.removeEdge(edge);
            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(0, count(graph.getEdges()));
            }
            if (graph.getFeatures().supportsVertexIteration) {
                assertEquals(1, count(graph.getVertices()));
            }
        } catch (Exception e) {
            assertTrue(true);
        }

        graph.shutdown();
    }

    public void testSettingProperties() {
        Graph graph = graphTest.generateGraph();
        if (!graph.getFeatures().isRDFModel) {
            Vertex a = graph.addVertex(null);
            Vertex b = graph.addVertex(null);
            graph.addEdge(null, a, b, convertId(graph,"knows"));
            graph.addEdge(null, a, b, convertId(graph,"knows"));
            for (Edge edge : b.getInEdges()) {
                edge.setProperty("key", "value");
            }
        }
        graph.shutdown();
    }

    public void testDataTypeValidationOnProperties() {
        Graph graph = graphTest.generateGraph();
        if (!graph.getFeatures().isRDFModel && !graph.getFeatures().isWrapper) {
            Vertex vertexA = graph.addVertex(null);
            Vertex vertexB = graph.addVertex(null);
            Edge edge = graph.addEdge(null, vertexA, vertexB, convertId(graph,"knows"));

            trySetProperty(vertexA, "keyString", "value", graph.getFeatures().allowStringProperty);
            trySetProperty(edge, "keyString", "value", graph.getFeatures().allowStringProperty);

            trySetProperty(vertexA, "keyInteger", 100, graph.getFeatures().allowIntegerProperty);
            trySetProperty(edge, "keyInteger", 100, graph.getFeatures().allowIntegerProperty);

            trySetProperty(vertexA, "keyLong", 10000L, graph.getFeatures().allowLongProperty);
            trySetProperty(edge, "keyLong", 10000L, graph.getFeatures().allowLongProperty);

            trySetProperty(vertexA, "keyDouble", 100.321d, graph.getFeatures().allowDoubleProperty);
            trySetProperty(edge, "keyDouble", 100.321d, graph.getFeatures().allowDoubleProperty);

            trySetProperty(vertexA, "keyFloat", 100.321f, graph.getFeatures().allowFloatProperty);
            trySetProperty(edge, "keyFloat", 100.321f, graph.getFeatures().allowFloatProperty);

            trySetProperty(vertexA, "keyBoolean", true, graph.getFeatures().allowBooleanProperty);
            trySetProperty(edge, "keyBoolean", true, graph.getFeatures().allowBooleanProperty);

            trySetProperty(vertexA, "keyDate", new Date(), graph.getFeatures().allowSerializableObjectProperty);
            trySetProperty(edge, "keyDate", new Date(), graph.getFeatures().allowSerializableObjectProperty);

            trySetProperty(vertexA, "keyListString", new ArrayList() {{
                add("try1");
                add("try2");
            }}, graph.getFeatures().allowUniformListProperty);
            trySetProperty(edge, "keyListString", new ArrayList() {{
                add("try1");
                add("try2");
            }}, graph.getFeatures().allowUniformListProperty);

            trySetProperty(vertexA, "keyListMixed", new ArrayList() {{
                add("try1");
                add(2);
            }}, graph.getFeatures().allowMixedListProperty);
            trySetProperty(edge, "keyListMixed", new ArrayList() {{
                add("try1");
                add(2);
            }}, graph.getFeatures().allowMixedListProperty);

            trySetProperty(vertexA, "keyArrayString", new String[]{"try1", "try2"}, graph.getFeatures().allowPrimitiveArrayProperty);
            trySetProperty(edge, "keyArrayString", new String[]{"try1", "try2"}, graph.getFeatures().allowPrimitiveArrayProperty);

            trySetProperty(vertexA, "keyArrayInteger", new int[]{1, 2}, graph.getFeatures().allowPrimitiveArrayProperty);
            trySetProperty(edge, "keyArrayInteger", new int[]{1, 2}, graph.getFeatures().allowPrimitiveArrayProperty);

            trySetProperty(vertexA, "keyArrayLong", new long[]{1000l, 2000l}, graph.getFeatures().allowPrimitiveArrayProperty);
            trySetProperty(edge, "keyArrayLong", new long[]{1000l, 2000l}, graph.getFeatures().allowPrimitiveArrayProperty);

            trySetProperty(vertexA, "keyArrayFloat", new float[]{1000.321f, 2000.321f}, graph.getFeatures().allowPrimitiveArrayProperty);
            trySetProperty(edge, "keyArrayFloat", new float[]{1000.321f, 2000.321f}, graph.getFeatures().allowPrimitiveArrayProperty);

            trySetProperty(vertexA, "keyArrayDouble", new double[]{1000.321d, 2000.321d}, graph.getFeatures().allowPrimitiveArrayProperty);
            trySetProperty(edge, "keyArrayDouble", new double[]{1000.321d, 2000.321d}, graph.getFeatures().allowPrimitiveArrayProperty);

            trySetProperty(vertexA, "keyArrayBoolean", new boolean[]{false, true}, graph.getFeatures().allowPrimitiveArrayProperty);
            trySetProperty(edge, "keyArrayBoolean", new boolean[]{false, true}, graph.getFeatures().allowPrimitiveArrayProperty);

            final Map map = new HashMap() {{
                put("testString", "try");
                put("testInteger", "string");
            }};
            trySetProperty(vertexA, "keyMap", map, graph.getFeatures().allowMapProperty);
            trySetProperty(edge, "keyMap", map, graph.getFeatures().allowMapProperty);

            MockSerializable mockSerializable = new MockSerializable();
            mockSerializable.setTestField("test");
            trySetProperty(vertexA, "keySerializable", mockSerializable, graph.getFeatures().allowSerializableObjectProperty);
            trySetProperty(edge, "keySerializable", mockSerializable, graph.getFeatures().allowSerializableObjectProperty);

        }

        // TODO: clearing the graph until serialization issues for map with TinkerGraph are sorted.
        if (graph instanceof TinkerGraph) {
            ((TinkerGraph) graph).clear();
        }
        graph.shutdown();
    }

    private void trySetProperty(final Element element, final String key, final Object value, final boolean allowDataType) {
        boolean exceptionTossed = false;
        try {
            element.setProperty(key, value);
        } catch (Throwable t) {
            exceptionTossed = true;
            if (!allowDataType) {
                assertTrue(t instanceof IllegalArgumentException);
            } else {
                fail("setProperty should not have thrown an exception as this data type is accepted according to the GraphTest settings.");
            }
        }

        if (!allowDataType && !exceptionTossed) {
            fail("setProperty threw an exception but the data type should have been accepted.");
        }
    }

    public void testRemovingEdges() {
        Graph graph = graphTest.generateGraph();
        int vertexCount = 100;
        int edgeCount = 200;
        List<Vertex> vertices = new ArrayList<Vertex>();
        List<Edge> edges = new ArrayList<Edge>();
        Random random = new Random();
        this.stopWatch();
        for (int i = 0; i < vertexCount; i++) {
            vertices.add(graph.addVertex(null));
        }
        printPerformance(graph.toString(), vertexCount, "vertices added", this.stopWatch());
        this.stopWatch();
        for (int i = 0; i < edgeCount; i++) {
            Vertex a = vertices.get(random.nextInt(vertices.size()));
            Vertex b = vertices.get(random.nextInt(vertices.size()));
            if (a != b) {
                edges.add(graph.addEdge(null, a, b, convertId(graph,"a" + UUID.randomUUID())));
            }
        }
        printPerformance(graph.toString(), edgeCount, "edges added", this.stopWatch());
        this.stopWatch();
        int counter = 0;
        for (Edge e : edges) {
            counter = counter + 1;
            graph.removeEdge(e);
            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(edges.size() - counter, count(graph.getEdges()));
            }
            if (graph.getFeatures().supportsVertexIteration) {
                assertEquals(vertices.size(), count(graph.getVertices()));
            }
        }
        printPerformance(graph.toString(), edgeCount, "edges deleted (with size check on each delete)", this.stopWatch());
        graph.shutdown();

    }

    public void testRemovingVertices() {
        Graph graph = graphTest.generateGraph();
        int vertexCount = 500;
        List<Vertex> vertices = new ArrayList<Vertex>();
        List<Edge> edges = new ArrayList<Edge>();

        this.stopWatch();
        for (int i = 0; i < vertexCount; i++) {
            vertices.add(graph.addVertex(null));
        }
        printPerformance(graph.toString(), vertexCount, "vertices added", this.stopWatch());

        this.stopWatch();
        for (int i = 0; i < vertexCount; i = i + 2) {
            Vertex a = vertices.get(i);
            Vertex b = vertices.get(i + 1);
            edges.add(graph.addEdge(null, a, b, convertId(graph,"a" + UUID.randomUUID())));

        }
        printPerformance(graph.toString(), vertexCount / 2, "edges added", this.stopWatch());

        this.stopWatch();
        int counter = 0;
        for (Vertex v : vertices) {
            counter = counter + 1;
            graph.removeVertex(v);
            if ((counter + 1) % 2 == 0) {
                if (graph.getFeatures().supportsEdgeIteration) {
                    assertEquals(edges.size() - ((counter + 1) / 2), count(graph.getEdges()));
                }
            }

            if (graph.getFeatures().supportsVertexIteration) {
                assertEquals(vertices.size() - counter, count(graph.getVertices()));
            }
        }
        printPerformance(graph.toString(), vertexCount, "vertices deleted (with size check on each delete)", this.stopWatch());
        graph.shutdown();
    }

    public void testConnectivityPatterns() {
        Graph graph = graphTest.generateGraph();
        List<String> ids = generateIds(4);

        Vertex a = graph.addVertex(convertId(graph,ids.get(0)));
        Vertex b = graph.addVertex(convertId(graph,ids.get(1)));
        Vertex c = graph.addVertex(convertId(graph,ids.get(2)));
        Vertex d = graph.addVertex(convertId(graph,ids.get(3)));

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(4, count(graph.getVertices()));

        Edge e = graph.addEdge(null, a, b, convertId(graph,"knows"));
        Edge f = graph.addEdge(null, b, c, convertId(graph,"knows"));
        Edge g = graph.addEdge(null, c, d, convertId(graph,"knows"));
        Edge h = graph.addEdge(null, d, a, convertId(graph,"knows"));

        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(4, count(graph.getEdges()));

        if (graph.getFeatures().supportsVertexIteration) {
            for (Vertex v : graph.getVertices()) {
                assertEquals(1, count(v.getOutEdges()));
                assertEquals(1, count(v.getInEdges()));
            }
        }

        if (graph.getFeatures().supportsEdgeIteration) {
            for (Edge x : graph.getEdges()) {
                assertEquals(convertId(graph,"knows"), x.getLabel());
            }
        }
        if (!graph.getFeatures().ignoresSuppliedIds) {
            a = graph.getVertex(convertId(graph,ids.get(0)));
            b = graph.getVertex(convertId(graph,ids.get(1)));
            c = graph.getVertex(convertId(graph,ids.get(2)));
            d = graph.getVertex(convertId(graph,ids.get(3)));

            assertEquals(1, count(a.getInEdges()));
            assertEquals(1, count(a.getOutEdges()));
            assertEquals(1, count(b.getInEdges()));
            assertEquals(1, count(b.getOutEdges()));
            assertEquals(1, count(c.getInEdges()));
            assertEquals(1, count(c.getOutEdges()));
            assertEquals(1, count(d.getInEdges()));
            assertEquals(1, count(d.getOutEdges()));

            Edge i = graph.addEdge(null, a, b, convertId(graph,"hates"));

            assertEquals(1, count(a.getInEdges()));
            assertEquals(2, count(a.getOutEdges()));
            assertEquals(2, count(b.getInEdges()));
            assertEquals(1, count(b.getOutEdges()));
            assertEquals(1, count(c.getInEdges()));
            assertEquals(1, count(c.getOutEdges()));
            assertEquals(1, count(d.getInEdges()));
            assertEquals(1, count(d.getOutEdges()));

            assertEquals(1, count(a.getInEdges()));
            assertEquals(2, count(a.getOutEdges()));
            for (Edge x : a.getOutEdges()) {
                assertTrue(x.getLabel().equals(convertId(graph,"knows")) || x.getLabel().equals(convertId(graph,"hates")));
            }
            assertEquals(convertId(graph,"hates"), i.getLabel());
            assertEquals(i.getInVertex().getId().toString(), convertId(graph,ids.get(1)));
            assertEquals(i.getOutVertex().getId().toString(), convertId(graph,ids.get(0)));
        }

        Set<Object> vertexIds = new HashSet<Object>();
        vertexIds.add(a.getId());
        vertexIds.add(a.getId());
        vertexIds.add(b.getId());
        vertexIds.add(b.getId());
        vertexIds.add(c.getId());
        vertexIds.add(d.getId());
        vertexIds.add(d.getId());
        vertexIds.add(d.getId());
        assertEquals(4, vertexIds.size());
        graph.shutdown();

    }

    public void testVertexEdgeLabels() {
        Graph graph = graphTest.generateGraph();
        Vertex a = graph.addVertex(null);
        Vertex b = graph.addVertex(null);
        Vertex c = graph.addVertex(null);
        Edge aFriendB = graph.addEdge(null, a, b, convertId(graph,"friend"));
        Edge aFriendC = graph.addEdge(null, a, c, convertId(graph,"friend"));
        Edge aHateC = graph.addEdge(null, a, c, convertId(graph,"hate"));
        Edge cHateA = graph.addEdge(null, c, a, convertId(graph,"hate"));
        Edge cHateB = graph.addEdge(null, c, b, convertId(graph,"hate"));

        List<Edge> results = asList(a.getOutEdges());
        assertEquals(results.size(), 3);
        assertTrue(results.contains(aFriendB));
        assertTrue(results.contains(aFriendC));
        assertTrue(results.contains(aHateC));

        results = asList(a.getOutEdges(convertId(graph,"friend")));
        assertEquals(results.size(), 2);
        assertTrue(results.contains(aFriendB));
        assertTrue(results.contains(aFriendC));

        results = asList(a.getOutEdges(convertId(graph,"hate")));
        assertEquals(results.size(), 1);
        assertTrue(results.contains(aHateC));

        results = asList(a.getInEdges(convertId(graph,"hate")));
        assertEquals(results.size(), 1);
        assertTrue(results.contains(cHateA));

        results = asList(a.getInEdges(convertId(graph,"friend")));
        assertEquals(results.size(), 0);

        results = asList(b.getInEdges(convertId(graph,"hate")));
        assertEquals(results.size(), 1);
        assertTrue(results.contains(cHateB));

        results = asList(b.getInEdges(convertId(graph,"friend")));
        assertEquals(results.size(), 1);
        assertTrue(results.contains(aFriendB));

        graph.shutdown();

    }

    public void testVertexQuery() {
        Graph graph = graphTest.generateGraph();
        if (!graph.getFeatures().isRDFModel) {


            Vertex a = graph.addVertex(null);
            Vertex b = graph.addVertex(null);
            Vertex c = graph.addVertex(null);
            Edge aFriendB = graph.addEdge(null, a, b, convertId(graph,"friend"));
            Edge aFriendC = graph.addEdge(null, a, c, convertId(graph,"friend"));
            Edge aHateC = graph.addEdge(null, a, c, convertId(graph,"hate"));
            Edge cHateA = graph.addEdge(null, c, a, convertId(graph,"hate"));
            Edge cHateB = graph.addEdge(null, c, b, convertId(graph,"hate"));
            aFriendB.setProperty("amount", 1.0);
            aFriendB.setProperty("date", 10);
            aFriendC.setProperty("amount", 0.5);
            aHateC.setProperty("amount", 1.0);
            cHateA.setProperty("amount", 1.0);
            cHateB.setProperty("amount", 0.4);

            // out edges

            List results = asList(a.query().direction(Query.Direction.OUT).edges());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            assertTrue(results.contains(aHateC));
            results = asList(a.query().direction(Query.Direction.OUT).vertices());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.OUT).count(), 3);


            results = asList(a.query().direction(Query.Direction.OUT).labels("hate", "friend").edges());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            assertTrue(results.contains(aHateC));
            results = asList(a.query().direction(Query.Direction.OUT).labels("hate", "friend").vertices());
            assertEquals(results.size(), 3);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.OUT).labels("hate", "friend").count(), 3);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").edges());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").vertices());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.OUT).labels("friend").count(), 2);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendB));
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(b));
            assertEquals(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0).count(), 1);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0, Query.Compare.NOT_EQUAL).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0, Query.Compare.NOT_EQUAL).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0, Query.Compare.NOT_EQUAL).count(), 1);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0, Query.Compare.LESS_THAN_EQUAL).edges());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0, Query.Compare.LESS_THAN_EQUAL).vertices());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 1.0, Query.Compare.LESS_THAN_EQUAL).count(), 2);

            results = asList(a.query().direction(Query.Direction.OUT).has("amount", 1.0, Query.Compare.LESS_THAN).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Query.Direction.OUT).has("amount", 1.0, Query.Compare.LESS_THAN).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.OUT).has("amount", 1.0, Query.Compare.LESS_THAN).count(), 1);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 0.5).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendC));
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").has("amount", 0.5).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));

            results = asList(a.query().direction(Query.Direction.IN).labels("hate", "friend").has("amount", 0.5, Query.Compare.GREATER_THAN).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(cHateA));
            results = asList(a.query().direction(Query.Direction.IN).labels("hate", "friend").has("amount", 0.5, Query.Compare.GREATER_THAN).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.IN).labels("hate", "friend").has("amount", 0.5, Query.Compare.GREATER_THAN).count(), 1);

            results = asList(a.query().direction(Query.Direction.IN).labels("hate").has("amount", 1.0, Query.Compare.GREATER_THAN).edges());
            assertEquals(results.size(), 0);
            results = asList(a.query().direction(Query.Direction.IN).labels("hate").has("amount", 1.0, Query.Compare.GREATER_THAN).vertices());
            assertEquals(results.size(), 0);
            assertEquals(a.query().direction(Query.Direction.IN).labels("hate").has("amount", 1.0, Query.Compare.GREATER_THAN).count(), 0);

            results = asList(a.query().direction(Query.Direction.IN).labels("hate").has("amount", 1.0, Query.Compare.GREATER_THAN_EQUAL).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(cHateA));
            results = asList(a.query().direction(Query.Direction.IN).labels("hate").has("amount", 1.0, Query.Compare.GREATER_THAN_EQUAL).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(c));
            assertEquals(a.query().direction(Query.Direction.IN).labels("hate").has("amount", 1.0, Query.Compare.GREATER_THAN_EQUAL).count(), 1);

            results = asList(a.query().direction(Query.Direction.OUT).interval("date", 5, 10).edges());
            assertEquals(results.size(), 0);
            results = asList(a.query().direction(Query.Direction.OUT).interval("date", 5, 10).vertices());
            assertEquals(results.size(), 0);
            assertEquals(a.query().direction(Query.Direction.OUT).interval("date", 5, 10).count(), 0);

            results = asList(a.query().direction(Query.Direction.OUT).interval("date", 5, 11).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendB));
            results = asList(a.query().direction(Query.Direction.OUT).interval("date", 5, 11).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(b));
            assertEquals(a.query().direction(Query.Direction.OUT).interval("date", 5, 11).count(), 1);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").interval("date", 5, 11).edges());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(aFriendB));
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").interval("date", 5, 11).vertices());
            assertEquals(results.size(), 1);
            assertTrue(results.contains(b));
            assertEquals(a.query().direction(Query.Direction.OUT).labels("friend").interval("date", 5, 11).count(), 1);

            results = asList(a.query().direction(Query.Direction.BOTH).labels("friend", "hate").edges());
            assertEquals(results.size(), 4);
            assertTrue(results.contains(aFriendB));
            assertTrue(results.contains(aFriendC));
            assertTrue(results.contains(aHateC));
            assertTrue(results.contains(cHateA));
            results = asList(a.query().direction(Query.Direction.BOTH).labels("friend", "hate").vertices());
            assertEquals(results.size(), 4);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertFalse(results.contains(a));
            assertEquals(a.query().direction(Query.Direction.BOTH).labels("friend", "hate").count(), 4);

            results = asList(a.query().labels("friend", "hate").limit(2).edges());
            assertEquals(results.size(), 2);
            results = asList(a.query().labels("friend", "hate").limit(2).vertices());
            assertEquals(results.size(), 2);
            assertTrue(results.contains(b));
            assertTrue(results.contains(c));
            assertFalse(results.contains(a));
            assertEquals(a.query().labels("friend", "hate").limit(2).count(), 2);

            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").limit(0).edges());
            assertEquals(results.size(), 0);
            results = asList(a.query().direction(Query.Direction.OUT).labels("friend").limit(0).vertices());
            assertEquals(results.size(), 0);
            assertEquals(a.query().direction(Query.Direction.OUT).labels("friend").limit(0).count(), 0);


        }
        graph.shutdown();

    }


    public void testVertexEdgeLabels2() {
        Graph graph = graphTest.generateGraph();
        Vertex a = graph.addVertex(null);
        Vertex b = graph.addVertex(null);
        Vertex c = graph.addVertex(null);
        Edge aFriendB = graph.addEdge(null, a, b, convertId(graph,"friend"));
        Edge aFriendC = graph.addEdge(null, a, c, convertId(graph,"friend"));
        Edge aHateC = graph.addEdge(null, a, c, convertId(graph,"hate"));
        Edge cHateA = graph.addEdge(null, c, a, convertId(graph,"hate"));
        Edge cHateB = graph.addEdge(null, c, b, convertId(graph,"hate"));


        List<Edge> results = asList(a.getOutEdges(convertId(graph,"friend"), convertId(graph,"hate")));
        assertEquals(results.size(), 3);
        assertTrue(results.contains(aFriendB));
        assertTrue(results.contains(aFriendC));
        assertTrue(results.contains(aHateC));

        results = asList(a.getInEdges(convertId(graph,"friend"), convertId(graph,"hate")));
        assertEquals(results.size(), 1);
        assertTrue(results.contains(cHateA));

        results = asList(b.getInEdges(convertId(graph,"friend"), convertId(graph,"hate")));
        assertEquals(results.size(), 2);
        assertTrue(results.contains(aFriendB));
        assertTrue(results.contains(cHateB));

        results = asList(b.getInEdges(convertId(graph,"blah"), convertId(graph,"blah2"), convertId(graph,"blah3")));
        assertEquals(results.size(), 0);

        graph.shutdown();

    }

    public void testTreeConnectivity() {
        Graph graph = graphTest.generateGraph();
        this.stopWatch();
        int branchSize = 11;
        Vertex start = graph.addVertex(null);
        for (int i = 0; i < branchSize; i++) {
            Vertex a = graph.addVertex(null);
            graph.addEdge(null, start, a, convertId(graph,"test1"));
            for (int j = 0; j < branchSize; j++) {
                Vertex b = graph.addVertex(null);
                graph.addEdge(null, a, b, convertId(graph,"test2"));
                for (int k = 0; k < branchSize; k++) {
                    Vertex c = graph.addVertex(null);
                    graph.addEdge(null, b, c, convertId(graph,"test3"));
                }
            }
        }

        assertEquals(0, count(start.getInEdges()));
        assertEquals(branchSize, count(start.getOutEdges()));
        for (Edge e : start.getOutEdges()) {
            assertEquals(convertId(graph,"test1"), e.getLabel());
            assertEquals(branchSize, count(e.getInVertex().getOutEdges()));
            assertEquals(1, count(e.getInVertex().getInEdges()));
            for (Edge f : e.getInVertex().getOutEdges()) {
                assertEquals(convertId(graph,"test2"), f.getLabel());
                assertEquals(branchSize, count(f.getInVertex().getOutEdges()));
                assertEquals(1, count(f.getInVertex().getInEdges()));
                for (Edge g : f.getInVertex().getOutEdges()) {
                    assertEquals(convertId(graph,"test3"), g.getLabel());
                    assertEquals(0, count(g.getInVertex().getOutEdges()));
                    assertEquals(1, count(g.getInVertex().getInEdges()));
                }
            }
        }

        int totalVertices = 0;
        for (int i = 0; i < 4; i++) {
            totalVertices = totalVertices + (int) Math.pow(branchSize, i);
        }
        printPerformance(graph.toString(), totalVertices, "vertices added in a tree structure", this.stopWatch());

        if (graph.getFeatures().supportsVertexIteration) {
            this.stopWatch();
            Set<Vertex> vertices = new HashSet<Vertex>();
            for (Vertex v : graph.getVertices()) {
                vertices.add(v);
            }
            assertEquals(totalVertices, vertices.size());
            printPerformance(graph.toString(), totalVertices, "vertices iterated", this.stopWatch());
        }

        if (graph.getFeatures().supportsEdgeIteration) {
            this.stopWatch();
            Set<Edge> edges = new HashSet<Edge>();
            for (Edge e : graph.getEdges()) {
                edges.add(e);
            }
            assertEquals(totalVertices - 1, edges.size());
            printPerformance(graph.toString(), totalVertices - 1, "edges iterated", this.stopWatch());
        }
        graph.shutdown();

    }

    public void testConcurrentModification() {
        Graph graph = graphTest.generateGraph();
        Vertex a = graph.addVertex(null);
        graph.addVertex(null);
        graph.addVertex(null);
        if (!graph.getFeatures().isRDFModel) {
            for (Vertex vertex : graph.getVertices()) {
                graph.addEdge(null, vertex, a, convertId(graph,"x"));
                graph.addEdge(null, vertex, a, convertId(graph,"y"));
            }
            for (Vertex vertex : graph.getVertices()) {
                assertEquals(BaseTest.count(vertex.getOutEdges()), 2);
                for (Edge edge : vertex.getOutEdges()) {
                    graph.removeEdge(edge);
                }
            }
            for (Vertex vertex : graph.getVertices()) {
                graph.removeVertex(vertex);
            }
        } else {
            for (int i = 0; i < 10; i++) {
                graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), convertId(graph,"test"));
            }
            for (Edge edge : graph.getEdges()) {
                graph.removeEdge(edge);
            }
        }
        graph.shutdown();

    }

    public void testGraphDataPersists() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().isPersistent) {

            Vertex v = graph.addVertex(null);
            Vertex u = graph.addVertex(null);
            if (!graph.getFeatures().isRDFModel) {
                v.setProperty("name", "marko");
                u.setProperty("name", "pavel");
            }
            Edge e = graph.addEdge(null, v, u, convertId(graph,"collaborator"));
            if (!graph.getFeatures().isRDFModel)
                e.setProperty("location", "internet");

            if (graph.getFeatures().supportsVertexIteration) {
                assertEquals(count(graph.getVertices()), 2);
            }
            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(count(graph.getEdges()), 1);
            }

            graph.shutdown();

            this.stopWatch();
            graph = graphTest.generateGraph();
            printPerformance(graph.toString(), 1, "graph loaded", this.stopWatch());
            if (graph.getFeatures().supportsVertexIteration) {
                assertEquals(count(graph.getVertices()), 2);
                if (!graph.getFeatures().isRDFModel) {
                    for (Vertex vertex : graph.getVertices()) {
                        assertTrue(vertex.getProperty("name").equals("marko") || vertex.getProperty("name").equals("pavel"));
                    }
                }
            }
            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(count(graph.getEdges()), 1);
                for (Edge edge : graph.getEdges()) {
                    assertEquals(edge.getLabel(), convertId(graph,"collaborator"));
                    if (!graph.getFeatures().isRDFModel)
                        assertEquals(edge.getProperty("location"), "internet");
                }
            }

        }
        graph.shutdown();
    }

    protected class MockSerializable implements Serializable {
        private String testField;

        public String getTestField() {
            return this.testField;
        }

        public void setTestField(String testField) {
            this.testField = testField;
        }

    }
}
