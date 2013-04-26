package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.io.MockSerializable;

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

    public void testStringRepresentationOfVertexId() {
        final Graph graph = graphTest.generateGraph();
        final Vertex a = graph.addVertex(null);
        final Object id = a.getId();
        final Vertex b = graph.getVertex(id);
        final Vertex c = graph.getVertex(id.toString());
        assertEquals(a, b);
        assertEquals(b, c);
        assertEquals(c, a);
        graph.shutdown();
    }

    public void testSemanticallyCorrectIterables() {
        Graph graph = graphTest.generateGraph();
        for (int i = 0; i < 15; i++) {
            graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), convertId(graph, "knows"));
        }
        if (graph.getFeatures().supportsVertexIteration) {
            Iterable<Vertex> vertices = graph.getVertices();
            assertEquals(count(vertices), 30);
            assertEquals(count(vertices), 30);
            Iterator<Vertex> itty = vertices.iterator();
            int counter = 0;
            while (itty.hasNext()) {
                assertTrue(itty.hasNext());
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
                assertTrue(itty.hasNext());
                itty.next();
                counter++;
            }
            assertEquals(counter, 15);
        }

        graph.shutdown();
    }


    public void testGettingVerticesAndEdgesWithKeyValue() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties) {
            Vertex v1 = graph.addVertex(null);
            v1.setProperty("name", "marko");
            v1.setProperty("location", "everywhere");
            Vertex v2 = graph.addVertex(null);
            v2.setProperty("name", "stephen");
            v2.setProperty("location", "everywhere");

            if (graph.getFeatures().supportsVertexIteration) {
                assertEquals(count(graph.getVertices("location", "everywhere")), 2);
                assertEquals(count(graph.getVertices("name", "marko")), 1);
                assertEquals(count(graph.getVertices("name", "stephen")), 1);
                assertEquals(getOnlyElement(graph.getVertices("name", "marko")), v1);
                assertEquals(getOnlyElement(graph.getVertices("name", "stephen")), v2);
            }
        }

        if (graph.getFeatures().supportsEdgeProperties) {
            Edge e1 = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), convertId(graph, "knows"));
            e1.setProperty("name", "marko");
            e1.setProperty("location", "everywhere");
            Edge e2 = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), convertId(graph, "knows"));
            e2.setProperty("name", "stephen");
            e2.setProperty("location", "everywhere");

            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(count(graph.getEdges("location", "everywhere")), 2);
                assertEquals(count(graph.getEdges("name", "marko")), 1);
                assertEquals(count(graph.getEdges("name", "stephen")), 1);
                assertEquals(graph.getEdges("name", "marko").iterator().next(), e1);
                assertEquals(graph.getEdges("name", "stephen").iterator().next(), e2);
            }
        }
        graph.shutdown();
    }

    public void testAddingVerticesAndEdges() {
        Graph graph = graphTest.generateGraph();
        Vertex a = graph.addVertex(null);
        Vertex b = graph.addVertex(null);
        Edge edge = graph.addEdge(null, a, b, convertId(graph, "knows"));
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
//TODO: doesn't work with wrapper graphs            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }

        if (graph.getFeatures().supportsEdgeIteration) {
            assertEquals(0, count(graph.getEdges()));
        }
        if (graph.getFeatures().supportsVertexIteration) {
            assertEquals(1, count(graph.getVertices()));
        }

        graph.shutdown();
    }

    public void testSettingProperties() {
        Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsEdgeProperties) {
            Vertex a = graph.addVertex(null);
            Vertex b = graph.addVertex(null);
            graph.addEdge(null, a, b, convertId(graph, "knows"));
            graph.addEdge(null, a, b, convertId(graph, "knows"));
            for (Edge edge : b.getEdges(Direction.IN)) {
                edge.setProperty("key", "value");
            }
        }
        graph.shutdown();
    }

    public void testDataTypeValidationOnProperties() {
        final Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsElementProperties() && !graph.getFeatures().isWrapper) {
            final Vertex vertexA = graph.addVertex(null);
            final Vertex vertexB = graph.addVertex(null);
            final Edge edge = graph.addEdge(null, vertexA, vertexB, convertId(graph, "knows"));

            trySetProperty(vertexA, "keyString", "value", graph.getFeatures().supportsStringProperty);
            trySetProperty(edge, "keyString", "value", graph.getFeatures().supportsStringProperty);

            trySetProperty(vertexA, "keyInteger", 100, graph.getFeatures().supportsIntegerProperty);
            trySetProperty(edge, "keyInteger", 100, graph.getFeatures().supportsIntegerProperty);

            trySetProperty(vertexA, "keyLong", 10000L, graph.getFeatures().supportsLongProperty);
            trySetProperty(edge, "keyLong", 10000L, graph.getFeatures().supportsLongProperty);

            trySetProperty(vertexA, "keyDouble", 100.321d, graph.getFeatures().supportsDoubleProperty);
            trySetProperty(edge, "keyDouble", 100.321d, graph.getFeatures().supportsDoubleProperty);

            trySetProperty(vertexA, "keyFloat", 100.321f, graph.getFeatures().supportsFloatProperty);
            trySetProperty(edge, "keyFloat", 100.321f, graph.getFeatures().supportsFloatProperty);

            trySetProperty(vertexA, "keyBoolean", true, graph.getFeatures().supportsBooleanProperty);
            trySetProperty(edge, "keyBoolean", true, graph.getFeatures().supportsBooleanProperty);

            trySetProperty(vertexA, "keyDate", new Date(), graph.getFeatures().supportsSerializableObjectProperty);
            trySetProperty(edge, "keyDate", new Date(), graph.getFeatures().supportsSerializableObjectProperty);

            final ArrayList<String> listA = new ArrayList<String>();
            listA.add("try1");
            listA.add("try2");

            trySetProperty(vertexA, "keyListString", listA, graph.getFeatures().supportsUniformListProperty);
            trySetProperty(edge, "keyListString", listA, graph.getFeatures().supportsUniformListProperty);


            tryGetProperty(vertexA, "keyListString", listA, graph.getFeatures().supportsUniformListProperty);
            tryGetProperty(edge, "keyListString", listA, graph.getFeatures().supportsUniformListProperty);


            final ArrayList listB = new ArrayList();
            listB.add("try1");
            listB.add(2);

            trySetProperty(vertexA, "keyListMixed", listB, graph.getFeatures().supportsMixedListProperty);
            trySetProperty(edge, "keyListMixed", listB, graph.getFeatures().supportsMixedListProperty);

            tryGetProperty(vertexA, "keyListString", listA, graph.getFeatures().supportsMixedListProperty);
            tryGetProperty(edge, "keyListString", listA, graph.getFeatures().supportsMixedListProperty);



            trySetProperty(vertexA, "keyArrayString", new String[]{"try1", "try2"}, graph.getFeatures().supportsPrimitiveArrayProperty);
            trySetProperty(edge, "keyArrayString", new String[]{"try1", "try2"}, graph.getFeatures().supportsPrimitiveArrayProperty);

            trySetProperty(vertexA, "keyArrayInteger", new int[]{1, 2}, graph.getFeatures().supportsPrimitiveArrayProperty);
            trySetProperty(edge, "keyArrayInteger", new int[]{1, 2}, graph.getFeatures().supportsPrimitiveArrayProperty);

            trySetProperty(vertexA, "keyArrayLong", new long[]{1000l, 2000l}, graph.getFeatures().supportsPrimitiveArrayProperty);
            trySetProperty(edge, "keyArrayLong", new long[]{1000l, 2000l}, graph.getFeatures().supportsPrimitiveArrayProperty);

            trySetProperty(vertexA, "keyArrayFloat", new float[]{1000.321f, 2000.321f}, graph.getFeatures().supportsPrimitiveArrayProperty);
            trySetProperty(edge, "keyArrayFloat", new float[]{1000.321f, 2000.321f}, graph.getFeatures().supportsPrimitiveArrayProperty);

            trySetProperty(vertexA, "keyArrayDouble", new double[]{1000.321d, 2000.321d}, graph.getFeatures().supportsPrimitiveArrayProperty);
            trySetProperty(edge, "keyArrayDouble", new double[]{1000.321d, 2000.321d}, graph.getFeatures().supportsPrimitiveArrayProperty);

            trySetProperty(vertexA, "keyArrayBoolean", new boolean[]{false, true}, graph.getFeatures().supportsPrimitiveArrayProperty);
            trySetProperty(edge, "keyArrayBoolean", new boolean[]{false, true}, graph.getFeatures().supportsPrimitiveArrayProperty);

            trySetProperty(vertexA, "keyArrayEmpty", new int[0], graph.getFeatures().supportsPrimitiveArrayProperty);
            trySetProperty(edge, "keyArrayEmpty", new int[0], graph.getFeatures().supportsPrimitiveArrayProperty);

            final Map map = new HashMap();
            map.put("testString", "try");
            map.put("testInteger", "string");

            trySetProperty(vertexA, "keyMap", map, graph.getFeatures().supportsMapProperty);
            trySetProperty(edge, "keyMap", map, graph.getFeatures().supportsMapProperty);

            final MockSerializable mockSerializable = new MockSerializable();
            mockSerializable.setTestField("test");
            trySetProperty(vertexA, "keySerializable", mockSerializable, graph.getFeatures().supportsSerializableObjectProperty);
            trySetProperty(edge, "keySerializable", mockSerializable, graph.getFeatures().supportsSerializableObjectProperty);

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

    private void tryGetProperty(final Element element, final String key, final Object value, final boolean allowDataType) {

    	if (allowDataType) {
	    assertEquals(element.getProperty(key), value);
	}
    }


    public void testSimpleRemovingVerticesEdges() {
        Graph graph = graphTest.generateGraph();

        Vertex v = graph.addVertex(null);
        Vertex u = graph.addVertex(null);
        Edge e = graph.addEdge(null, v, u, convertId(graph, "knows"));

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 2);
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 1);

        assertEquals(v.getEdges(Direction.OUT).iterator().next().getVertex(Direction.IN), u);
        assertEquals(u.getEdges(Direction.IN).iterator().next().getVertex(Direction.OUT), v);
        assertEquals(v.getEdges(Direction.OUT).iterator().next(), e);
        assertEquals(u.getEdges(Direction.IN).iterator().next(), e);
        graph.removeVertex(v);
//TODO: DEX
//assertFalse(v.getEdges(Direction.OUT).iterator().hasNext());

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 1);
        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(count(graph.getEdges()), 0);

        graph.shutdown();
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
                edges.add(graph.addEdge(null, a, b, convertId(graph, "a" + UUID.randomUUID())));
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
            edges.add(graph.addEdge(null, a, b, convertId(graph, "a" + UUID.randomUUID())));

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

        Vertex a = graph.addVertex(convertId(graph, "1"));
        Vertex b = graph.addVertex(convertId(graph, "2"));
        Vertex c = graph.addVertex(convertId(graph, "3"));
        Vertex d = graph.addVertex(convertId(graph, "4"));

        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(4, count(graph.getVertices()));

        Edge e = graph.addEdge(null, a, b, convertId(graph, "knows"));
        Edge f = graph.addEdge(null, b, c, convertId(graph, "knows"));
        Edge g = graph.addEdge(null, c, d, convertId(graph, "knows"));
        Edge h = graph.addEdge(null, d, a, convertId(graph, "knows"));

        if (graph.getFeatures().supportsEdgeIteration)
            assertEquals(4, count(graph.getEdges()));

        if (graph.getFeatures().supportsVertexIteration) {
            for (Vertex v : graph.getVertices()) {
                assertEquals(1, count(v.getEdges(Direction.OUT)));
                assertEquals(1, count(v.getEdges(Direction.IN)));
            }
        }

        if (graph.getFeatures().supportsEdgeIteration) {
            for (Edge x : graph.getEdges()) {
                assertEquals(convertId(graph, "knows"), x.getLabel());
            }
        }
        if (!graph.getFeatures().ignoresSuppliedIds) {
            a = graph.getVertex(convertId(graph, "1"));
            b = graph.getVertex(convertId(graph, "2"));
            c = graph.getVertex(convertId(graph, "3"));
            d = graph.getVertex(convertId(graph, "4"));

            assertEquals(1, count(a.getEdges(Direction.IN)));
            assertEquals(1, count(a.getEdges(Direction.OUT)));
            assertEquals(1, count(b.getEdges(Direction.IN)));
            assertEquals(1, count(b.getEdges(Direction.OUT)));
            assertEquals(1, count(c.getEdges(Direction.IN)));
            assertEquals(1, count(c.getEdges(Direction.OUT)));
            assertEquals(1, count(d.getEdges(Direction.IN)));
            assertEquals(1, count(d.getEdges(Direction.OUT)));

            Edge i = graph.addEdge(null, a, b, convertId(graph, "hates"));

            assertEquals(1, count(a.getEdges(Direction.IN)));
            assertEquals(2, count(a.getEdges(Direction.OUT)));
            assertEquals(2, count(b.getEdges(Direction.IN)));
            assertEquals(1, count(b.getEdges(Direction.OUT)));
            assertEquals(1, count(c.getEdges(Direction.IN)));
            assertEquals(1, count(c.getEdges(Direction.OUT)));
            assertEquals(1, count(d.getEdges(Direction.IN)));
            assertEquals(1, count(d.getEdges(Direction.OUT)));

            assertEquals(1, count(a.getEdges(Direction.IN)));
            assertEquals(2, count(a.getEdges(Direction.OUT)));
            for (Edge x : a.getEdges(Direction.OUT)) {
                assertTrue(x.getLabel().equals(convertId(graph, "knows")) || x.getLabel().equals(convertId(graph, "hates")));
            }
            assertEquals(convertId(graph, "hates"), i.getLabel());
            assertEquals(i.getVertex(Direction.IN).getId().toString(), convertId(graph, "2"));
            assertEquals(i.getVertex(Direction.OUT).getId().toString(), convertId(graph, "1"));
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
        Edge aFriendB = graph.addEdge(null, a, b, convertId(graph, "friend"));
        Edge aFriendC = graph.addEdge(null, a, c, convertId(graph, "friend"));
        Edge aHateC = graph.addEdge(null, a, c, convertId(graph, "hate"));
        Edge cHateA = graph.addEdge(null, c, a, convertId(graph, "hate"));
        Edge cHateB = graph.addEdge(null, c, b, convertId(graph, "hate"));

        List<Edge> results = asList(a.getEdges(Direction.OUT));
        assertEquals(results.size(), 3);
        assertTrue(results.contains(aFriendB));
        assertTrue(results.contains(aFriendC));
        assertTrue(results.contains(aHateC));

        results = asList(a.getEdges(Direction.OUT, convertId(graph, "friend")));
        assertEquals(results.size(), 2);
        assertTrue(results.contains(aFriendB));
        assertTrue(results.contains(aFriendC));

        results = asList(a.getEdges(Direction.OUT, convertId(graph, "hate")));
        assertEquals(results.size(), 1);
        assertTrue(results.contains(aHateC));

        results = asList(a.getEdges(Direction.IN, convertId(graph, "hate")));
        assertEquals(results.size(), 1);
        assertTrue(results.contains(cHateA));

        results = asList(a.getEdges(Direction.IN, convertId(graph, "friend")));
        assertEquals(results.size(), 0);

        results = asList(b.getEdges(Direction.IN, convertId(graph, "hate")));
        assertEquals(results.size(), 1);
        assertTrue(results.contains(cHateB));

        results = asList(b.getEdges(Direction.IN, convertId(graph, "friend")));
        assertEquals(results.size(), 1);
        assertTrue(results.contains(aFriendB));

        graph.shutdown();

    }

    public void testVertexEdgeLabels2() {
        Graph graph = graphTest.generateGraph();
        Vertex a = graph.addVertex(null);
        Vertex b = graph.addVertex(null);
        Vertex c = graph.addVertex(null);
        Edge aFriendB = graph.addEdge(null, a, b, convertId(graph, "friend"));
        Edge aFriendC = graph.addEdge(null, a, c, convertId(graph, "friend"));
        Edge aHateC = graph.addEdge(null, a, c, convertId(graph, "hate"));
        Edge cHateA = graph.addEdge(null, c, a, convertId(graph, "hate"));
        Edge cHateB = graph.addEdge(null, c, b, convertId(graph, "hate"));


        List<Edge> results = asList(a.getEdges(Direction.OUT, convertId(graph, "friend"), convertId(graph, "hate")));
        assertEquals(results.size(), 3);
        assertTrue(results.contains(aFriendB));
        assertTrue(results.contains(aFriendC));
        assertTrue(results.contains(aHateC));

        results = asList(a.getEdges(Direction.IN, convertId(graph, "friend"), convertId(graph, "hate")));
        assertEquals(results.size(), 1);
        assertTrue(results.contains(cHateA));

        results = asList(b.getEdges(Direction.IN, convertId(graph, "friend"), convertId(graph, "hate")));
        assertEquals(results.size(), 2);
        assertTrue(results.contains(aFriendB));
        assertTrue(results.contains(cHateB));

        results = asList(b.getEdges(Direction.IN, convertId(graph, "blah"), convertId(graph, "blah2"), convertId(graph, "blah3")));
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
            graph.addEdge(null, start, a, convertId(graph, "test1"));
            for (int j = 0; j < branchSize; j++) {
                Vertex b = graph.addVertex(null);
                graph.addEdge(null, a, b, convertId(graph, "test2"));
                for (int k = 0; k < branchSize; k++) {
                    Vertex c = graph.addVertex(null);
                    graph.addEdge(null, b, c, convertId(graph, "test3"));
                }
            }
        }

        assertEquals(0, count(start.getEdges(Direction.IN)));
        assertEquals(branchSize, count(start.getEdges(Direction.OUT)));
        for (Edge e : start.getEdges(Direction.OUT)) {
            assertEquals(convertId(graph, "test1"), e.getLabel());
            assertEquals(branchSize, count(e.getVertex(Direction.IN).getEdges(Direction.OUT)));
            assertEquals(1, count(e.getVertex(Direction.IN).getEdges(Direction.IN)));
            for (Edge f : e.getVertex(Direction.IN).getEdges(Direction.OUT)) {
                assertEquals(convertId(graph, "test2"), f.getLabel());
                assertEquals(branchSize, count(f.getVertex(Direction.IN).getEdges(Direction.OUT)));
                assertEquals(1, count(f.getVertex(Direction.IN).getEdges(Direction.IN)));
                for (Edge g : f.getVertex(Direction.IN).getEdges(Direction.OUT)) {
                    assertEquals(convertId(graph, "test3"), g.getLabel());
                    assertEquals(0, count(g.getVertex(Direction.IN).getEdges(Direction.OUT)));
                    assertEquals(1, count(g.getVertex(Direction.IN).getEdges(Direction.IN)));
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
        if (graph.getFeatures().supportsVertexIteration) {
            for (Vertex vertex : graph.getVertices()) {
                graph.addEdge(null, vertex, a, convertId(graph, "x"));
                graph.addEdge(null, vertex, a, convertId(graph, "y"));
            }
            for (Vertex vertex : graph.getVertices()) {
                assertEquals(BaseTest.count(vertex.getEdges(Direction.OUT)), 2);
                for (Edge edge : vertex.getEdges(Direction.OUT)) {
                    graph.removeEdge(edge);
                }
            }
            for (Vertex vertex : graph.getVertices()) {
                graph.removeVertex(vertex);
            }
        } else if (graph.getFeatures().supportsEdgeIteration) {
            for (int i = 0; i < 10; i++) {
                graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), convertId(graph, "test"));
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
            if (graph.getFeatures().supportsVertexProperties) {
                v.setProperty("name", "marko");
                u.setProperty("name", "pavel");
            }
            Edge e = graph.addEdge(null, v, u, convertId(graph, "collaborator"));
            if (graph.getFeatures().supportsEdgeProperties)
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
                if (graph.getFeatures().supportsVertexProperties) {
                    for (Vertex vertex : graph.getVertices()) {
                        assertTrue(vertex.getProperty("name").equals("marko") || vertex.getProperty("name").equals("pavel"));
                    }
                }
            }
            if (graph.getFeatures().supportsEdgeIteration) {
                assertEquals(count(graph.getEdges()), 1);
                for (Edge edge : graph.getEdges()) {
                    assertEquals(edge.getLabel(), convertId(graph, "collaborator"));
                    if (graph.getFeatures().supportsEdgeProperties)
                        assertEquals(edge.getProperty("location"), "internet");
                }
            }

        }
        graph.shutdown();
    }

    public void testAutotypingOfProperties() {
        final Graph graph = graphTest.generateGraph();
        if (graph.getFeatures().supportsVertexProperties) {
            Vertex v = graph.addVertex(null);
            v.setProperty(convertId(graph,"string"), "marko");
            v.setProperty(convertId(graph,"integer"), 33);
            v.setProperty(convertId(graph,"boolean"), true);

            String name = v.getProperty(convertId(graph,"string"));
            assertEquals(name, "marko");
            Integer age = v.getProperty(convertId(graph,"integer"));
            assertEquals(age, Integer.valueOf(33));
            Boolean best = v.getProperty(convertId(graph,"boolean"));
            assertTrue(best);

            name = v.removeProperty(convertId(graph,"string"));
            assertEquals(name, "marko");
            age = v.removeProperty(convertId(graph,"integer"));
            assertEquals(age, Integer.valueOf(33));
            best = v.removeProperty(convertId(graph,"boolean"));
            assertTrue(best);
        }

        if (graph.getFeatures().supportsEdgeProperties) {
            Edge e = graph.addEdge(null, graph.addVertex(null), graph.addVertex(null), "knows");
            e.setProperty(convertId(graph,"string"), "friend");
            e.setProperty(convertId(graph,"double"), 1.0d);

            String type = e.getProperty(convertId(graph,"string"));
            assertEquals(type, "friend");
            Double weight = e.getProperty(convertId(graph,"double"));
            assertEquals(weight, 1.0d);

            type = e.removeProperty(convertId(graph,"string"));
            assertEquals(type, "friend");
            weight = e.removeProperty(convertId(graph,"double"));
            assertEquals(weight, 1.0d);
        }
        graph.shutdown();
    }
}
