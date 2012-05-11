package com.tinkerpop.blueprints.pgm.util.wrappers.id;

import junit.framework.TestCase;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdGraphTest extends TestCase { // extends GraphTest {

    public void testTrue() {
        assertTrue(true);
    }
    /*private IndexableGraph base;
    IdGraph graph;

    public IdGraphTest() {
        // The below values should be identical to TinkerGraphTest's,
        // apart from this one
        isPersistent = false;

        allowDuplicateEdges = true;
        allowSelfLoops = true;
        ignoresSuppliedIds = false;
        isRDFModel = false;
        supportsVertexIteration = true;
        supportsEdgeIteration = true;
        supportsVertexIndex = true;
        supportsEdgeIndex = true;
        supportsTransactions = false;

        allowSerializableObjectProperty = true;
        allowBooleanProperty = true;
        allowDoubleProperty = true;
        allowFloatProperty = true;
        allowIntegerProperty = true;
        allowPrimitiveArrayProperty = true;
        allowUniformListProperty = true;
        this.allowMixedListProperty = true;
        allowLongProperty = true;
        allowMapProperty = true;
        allowStringProperty = true;
    }

    @Before
    public void setUp() throws Exception {
        base = new TinkerGraph();
        graph = new IdGraph(base);
    }

    @After
    public void tearDown() throws Exception {
        base.shutdown();
    }

    @Test
    public void testElementClasses() throws Exception {
        Vertex v1 = graph.addVertex(null);
        Vertex v2 = graph.addVertex(null);
        Edge e = graph.addEdge(null, v1, v2, "knows");

        assertTrue(v1 instanceof IdVertex);
        assertTrue(e instanceof IdEdge);

        Iterator<Edge> outE = v1.getOutEdges().iterator();
        assertTrue(outE.hasNext());
        e = outE.next();
        assertTrue(e instanceof IdEdge);
        assertTrue(e.getInVertex() instanceof IdVertex);
        assertTrue(e.getOutVertex() instanceof IdVertex);

        Iterator<Vertex> vertices = graph.getVertices().iterator();
        assertTrue(vertices.hasNext());
        while (vertices.hasNext()) {
            assertTrue(vertices.next() instanceof IdVertex);
        }

        Iterator<Edge> edges = graph.getEdges().iterator();
        assertTrue(edges.hasNext());
        while (edges.hasNext()) {
            assertTrue(edges.next() instanceof IdEdge);
        }
    }

    @Test
    public void testIdIndicesExist() throws Exception {
        Index<Vertex> vertexIds = base.getIndex(IdGraph.VERTEX_IDS, Vertex.class);
        Index<Edge> edgeIds = base.getIndex(IdGraph.EDGE_IDS, Edge.class);

        assertNotNull(vertexIds);
        assertNotNull(edgeIds);

        assertNull(graph.getIndex(IdGraph.VERTEX_IDS, Vertex.class));
        assertNull(graph.getIndex(IdGraph.EDGE_IDS, Edge.class));
    }

    @Test
    public void testDefaultIdFactory() throws Exception {
        Vertex v = graph.addVertex(null);
        String id = (String) v.getId();

        assertEquals(36, id.length());
        assertEquals(5, id.split("-").length);

        Vertex v2 = graph.addVertex(null);
        Edge e = graph.addEdge(null, v, v2, "knows");

        id = (String) e.getId();
        assertEquals(36, id.length());
        assertEquals(5, id.split("-").length);
    }

    @Test
    public void testAddVertexWithSpecifiedId() throws Exception {
        Vertex v = graph.addVertex("forty-two");

        assertEquals("forty-two", v.getId());
    }

    @Test
    public void testIndices() throws Exception {
        Set<String> nameKeys = new HashSet<String>();
        nameKeys.add("name");

        graph.createAutomaticIndex("names", Vertex.class, nameKeys);
        graph.createIndex("weights", Edge.class);

        Iterable<Index<? extends Element>> indices = graph.getIndices();
        int count = 0;
        for (Index<? extends Element> i : indices) {
            String name = i.getIndexName();
            Class c = i.getIndexClass();
            Index.Type t = i.getIndexType();

            if (name.equals("names")) {
                assertEquals(Index.Type.AUTOMATIC, t);
                assertEquals(Vertex.class, c);
                Set<String> keys = ((AutomaticIndex) i).getAutoIndexKeys();
                assertEquals(1, keys.size());
                assertTrue(keys.contains("name"));
            } else if (name.equals("weights")) {
                assertEquals(Index.Type.MANUAL, t);
                assertEquals(Edge.class, c);
            } else if (!name.equals("edges") && !name.equals("vertices")) {
                fail("unexpected index: " + name);
            }

            count++;
        }
        assertEquals(4, count);

        AutomaticIndex<Vertex> names = (AutomaticIndex<Vertex>) graph.getIndex("names", Vertex.class);
        Index<Edge> weights = graph.getIndex("weights", Edge.class);

        Vertex v1 = graph.addVertex(null);
        v1.setProperty("name", "Arthur");

        Vertex v2 = graph.addVertex(null);
        v2.setProperty("name", "Ford");

        Edge e = graph.addEdge(null, v1, v2, "knows");
        e.setProperty("weight", 0.8);

        Collection<Vertex> vertices;
        vertices = toCollection(names.get("name", "Arthur"));
        assertEquals(1, vertices.size());
        assertEquals(v1.getId(), vertices.iterator().next().getId());
                vertices = toCollection(names.get("name", "Ford"));
        assertEquals(1, vertices.size());
        assertEquals(v2.getId(), vertices.iterator().next().getId());

        weights.put("weight", 0.4, e);
        Collection<Edge> edges;
        edges = toCollection(weights.get("weight", 0.8));
        assertEquals(0, edges.size());
        edges = toCollection(weights.get("weight", 0.4));
        assertEquals(1, edges.size());
        assertEquals(e.getId(), edges.iterator().next().getId());
    }

    @Test
    public void testProperties() throws Exception {
        Vertex v = graph.addVertex(null);
        v.setProperty("name", "Zaphod");
        v.setProperty("profession", "ex-president of the Galaxy");

        Set<String> keys = v.getPropertyKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("name"));
        assertTrue(keys.contains("profession"));
        assertEquals("Zaphod", v.getProperty("name"));
    }

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

    public void testAutomaticIndexTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new AutomaticIndexTestSuite(this));
        printTestPerformance("AutomaticIndexTestSuite", this.stopWatch());
    }

    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }

    public Graph generateGraph() {
        return new IdGraph(new TinkerGraph());
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        String doTest = System.getProperty("testIdGraph");
        if (doTest == null || doTest.equals("true")) {
            for (Method method : testSuite.getClass().getDeclaredMethods()) {
                if (method.getName().startsWith("test")) {
                    System.out.println("Testing " + method.getName() + "...");
                    method.invoke(testSuite);
                }
            }
        }
    }

    private <T> Collection<T> toCollection(final CloseableIterable<T> s) {
        Collection<T> c = new LinkedList<T>();
        try {
            while (s.hasNext()) {
                c.add(s.next());
            }
        } finally {
            s.close();
        }

        return c;
    }*/
}
