package com.tinkerpop.blueprints.impls.sail;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.GraphTest;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SailGraphSpecificTestSuite extends TestSuite {

    public SailGraphSpecificTestSuite() {
    }

    public SailGraphSpecificTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testTypeConversion() {
        assertEquals(SailVertex.castLiteral(new LiteralImpl("marko", new URIImpl("http://www.w3.org/2001/XMLSchema#string"))).getClass(), String.class);
        assertEquals(SailVertex.castLiteral(new LiteralImpl("marko")).getClass(), String.class);
        assertEquals(SailVertex.castLiteral(new LiteralImpl("27", new URIImpl("http://www.w3.org/2001/XMLSchema#int"))).getClass(), Integer.class);
        assertEquals(SailVertex.castLiteral(new LiteralImpl("27", new URIImpl("http://www.w3.org/2001/XMLSchema#float"))).getClass(), Float.class);
        assertEquals(SailVertex.castLiteral(new LiteralImpl("27.0134", new URIImpl("http://www.w3.org/2001/XMLSchema#double"))).getClass(), Double.class);
        assertEquals(SailVertex.castLiteral(new LiteralImpl("hello", "en")), "hello");
    }

    public void testNamespaceConversion() throws Exception {
        SailGraph graph = (SailGraph) graphTest.generateGraph();
        graph.addNamespace("tg", "http://tinkerpop.com#");
        graph.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        assertEquals(graph.expandPrefix("tg:name"), "http://tinkerpop.com#name");
        assertEquals(graph.expandPrefix("rdf:label"), "http://www.w3.org/1999/02/22-rdf-syntax-ns#label");
        assertEquals(graph.prefixNamespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#label"), "rdf:label");
        assertEquals(graph.prefixNamespace("http://tinkerpop.com#name"), "tg:name");
        graph.shutdown();

    }

    public void testURIs() {
        assertFalse(SailHelper.isURI("_:1234"));
        assertFalse(SailHelper.isURI("_:abcdefghijklmnopqrstuvwxyz"));
        assertTrue(SailHelper.isURI("http://marko"));
        assertTrue(SailHelper.isURI("http://www.w3.org/2001/XMLSchema#string"));
    }

    public void testBNodes() {
        assertTrue(SailHelper.isBNode("_:1234"));
        assertTrue(SailHelper.isBNode("_:abcdefghijklmnopqrstuvwxyz"));
        assertFalse(SailHelper.isBNode("_:"));
        assertFalse(SailHelper.isBNode("http://marko"));
        assertFalse(SailHelper.isBNode("http://www.w3.org/2001/XMLSchema#string"));
    }

    public void testLiterals() {
        assertTrue(SailHelper.isLiteral("\"java\"^^<http://www.w3.org/2001/XMLSchema#string>"));
        assertFalse(SailHelper.isLiteral("http://www.w3.org/2001/XMLSchema#string"));
        assertFalse(SailHelper.isLiteral("^^<http://www.w3.org/2001/XMLSchema#string>"));
        assertTrue(SailHelper.isLiteral("\"\"^^<http://www.w3.org/2001/XMLSchema#string>"));
        assertTrue(SailHelper.isLiteral("\"\""));
        assertTrue(SailHelper.isLiteral("\"marko\""));
        assertFalse(SailHelper.isLiteral("\"marko\"marko"));
        assertFalse(SailHelper.isLiteral("\""));
        // TODO: make this true assertFalse(SesameGraph.isLiteral("\"marko\"marko\""));


        Matcher matcher = SailHelper.literalPattern.matcher("\"java\"^^<http://www.w3.org/2001/XMLSchema#string>");
        matcher.matches();
        assertNull(matcher.group(6));
        assertEquals(matcher.group(1), "java");
        assertEquals(matcher.group(4), "http://www.w3.org/2001/XMLSchema#string");

        matcher = SailHelper.literalPattern.matcher("\"java\"@en");
        matcher.matches();
        assertNull(matcher.group(4));
        assertEquals(matcher.group(1), "java");
        assertEquals(matcher.group(6), "en");
    }

    public void testLiteralProperties() {
        SailGraph graph = (SailGraph) graphTest.generateGraph();
        Vertex v = graph.getVertex("\"java\"^^<http://www.w3.org/2001/XMLSchema#string>");
        assertEquals(v.getProperty(SailTokens.VALUE), "java");
        assertEquals(v.getProperty(SailTokens.DATATYPE), "http://www.w3.org/2001/XMLSchema#string");
        assertNull(v.getProperty(SailTokens.LANGUAGE));
        assertEquals(v.getProperty(SailTokens.KIND), "literal");

        v = graph.getVertex("\"10\"^^<http://www.w3.org/2001/XMLSchema#int>");
        assertEquals(v.getProperty(SailTokens.VALUE), 10);
        assertEquals(v.getProperty(SailTokens.DATATYPE), "http://www.w3.org/2001/XMLSchema#int");
        assertNull(v.getProperty(SailTokens.LANGUAGE));
        assertEquals(v.getProperty(SailTokens.KIND), "literal");

        v = graph.getVertex("\"goodbye\"@en");
        assertEquals(v.getProperty(SailTokens.VALUE), "goodbye");
        assertEquals(v.getProperty(SailTokens.LANGUAGE), "en");
        assertNull(v.getProperty(SailTokens.DATATYPE));
        assertEquals(v.getProperty(SailTokens.KIND), "literal");

    }

    public void testValueKinds() {
        SailGraph graph = (SailGraph) graphTest.generateGraph();
        Vertex v = graph.getVertex("\"java\"^^<http://www.w3.org/2001/XMLSchema#string>");
        assertEquals(v.getProperty(SailTokens.KIND), "literal");

        v = graph.getVertex("http://markorodriguez.com");
        assertEquals(v.getProperty(SailTokens.KIND), "uri");

        v = graph.getVertex("_:123");
        assertEquals(v.getProperty(SailTokens.KIND), "bnode");
    }

    public void testSparql() {
        SailGraph graph = (SailGraph) graphTest.generateGraph();
        SailGraphFactory.createTinkerGraph(graph);

        String query = "SELECT ?x ?y WHERE { ?x tg:knows ?y }";
        this.stopWatch();

        List<Map<String, Vertex>> results = graph.executeSparql(query);
        assertEquals(results.size(), 2);
        for (Map<String, Vertex> map : results) {
            assertEquals(map.get("x"), graph.getVertex("tg:1"));
            assertTrue(map.get("y").equals(graph.getVertex("tg:2")) || map.get("y").equals(graph.getVertex("tg:4")));
        }
        graph.shutdown();
    }

    public void testNamedGraphs() {
        SailGraph graph = (SailGraph) graphTest.generateGraph();
        SailGraphFactory.createTinkerGraph(graph);
        int counter = 0;
        for (Edge edge : graph.getEdges()) {
            counter++;
            SailEdge se = (SailEdge) edge;
            assertNull(se.getNamedGraph());
            assertNull(se.getProperty(SailTokens.NAMED_GRAPH));
            assertFalse(se.hasNamedGraph());

        }
        assertEquals(counter, 6);

        for (Edge edge : graph.getEdges()) {
            SailEdge se = (SailEdge) edge;
            se.setNamedGraph("http://agraph");
            assertEquals(se.getNamedGraph(), "http://agraph");
            assertEquals(se.getNamedGraph(), se.getProperty(SailTokens.NAMED_GRAPH));
            assertTrue(se.hasNamedGraph());
        }
        assertEquals(count(graph.getEdges()), 6);
    }

    public void testAddManyVertexProperties() {
        SailGraph graph = (SailGraph) graphTest.generateGraph();
        Set<Vertex> vertices = new HashSet<Vertex>();
        this.stopWatch();
        for (int i = 0; i < 50; i++) {
            Vertex vertex = graph.addVertex("\"" + UUID.randomUUID().toString() + "\"");
            for (int j = 0; j < 15; j++) {
                vertex.setProperty(SailTokens.DATATYPE, "http://www.w3.org/2001/XMLSchema#anyURI");
            }
            vertices.add(vertex);
        }
        printPerformance(graph.toString(), 15 * 50, "vertex properties added (with vertices being added too)", this.stopWatch());
        if (graph.getFeatures().supportsVertexIteration)
            assertEquals(count(graph.getVertices()), 50);
        assertEquals(vertices.size(), 50);
        for (Vertex vertex : vertices) {
            assertEquals(3, vertex.getPropertyKeys().size());
            assertTrue(vertex.getPropertyKeys().contains(SailTokens.DATATYPE));
            assertEquals("http://www.w3.org/2001/XMLSchema#anyURI", vertex.getProperty(SailTokens.DATATYPE));
            assertTrue(vertex.getPropertyKeys().contains(SailTokens.VALUE));
            assertEquals("literal", vertex.getProperty(SailTokens.KIND));

        }
        graph.shutdown();
    }

    public void testBasicAddVertex() {
        SailGraph graph = (SailGraph) graphTest.generateGraph();
        Vertex v1 = graph.addVertex("http://tinkerpop.com#marko");
        assertEquals("http://tinkerpop.com#marko", v1.getId());
        Vertex v2 = graph.addVertex("\"1\"^^<datatype:int>");
        assertEquals("\"1\"^^<datatype:int>", v2.getId());
        Vertex v3 = graph.addVertex("_:ABLANKNODE");
        assertEquals(v3.getId(), "_:ABLANKNODE");
        Vertex v4 = graph.addVertex("\"2.24\"^^<http://www.w3.org/2001/XMLSchema#double>");
        assertEquals("\"2.24\"^^<http://www.w3.org/2001/XMLSchema#double>", v4.getId());
        graph.shutdown();
    }

    public void testAddVertexProperties() {
        SailGraph graph = (SailGraph) graphTest.generateGraph();
        Vertex v1 = graph.addVertex("\"1\"^^<http://www.w3.org/2001/XMLSchema#int>");
        assertEquals("http://www.w3.org/2001/XMLSchema#int", v1.getProperty(SailTokens.DATATYPE));
        assertEquals(1, v1.getProperty(SailTokens.VALUE));
        assertNull(v1.getProperty(SailTokens.LANGUAGE));
        assertNull(v1.getProperty("random something"));

        Vertex v2 = graph.addVertex("\"hello\"@en");
        assertEquals("en", v2.getProperty(SailTokens.LANGUAGE));
        assertEquals("hello", v2.getProperty(SailTokens.VALUE));
        assertNull(v2.getProperty(SailTokens.DATATYPE));
        assertNull(v2.getProperty("random something"));
        graph.shutdown();
    }

    public void testRemoveVertexProperties() {
        SailGraph graph = (SailGraph) graphTest.generateGraph();
        Vertex v1 = graph.addVertex("\"1\"^^<http://www.w3.org/2001/XMLSchema#int>");
        assertEquals("http://www.w3.org/2001/XMLSchema#int", v1.removeProperty("type"));
        assertEquals("1", v1.getProperty("value"));
        assertNull(v1.getProperty("lang"));
        assertNull(v1.getProperty("random something"));

        Vertex v2 = graph.addVertex("\"hello\"@en");
        assertEquals("en", v2.removeProperty("lang"));
        assertEquals("hello", v2.getProperty("value"));
        assertNull(v2.getProperty("type"));
        assertNull(v2.getProperty("random something"));

        graph.shutdown();
    }

    public void testNavigateThroughLiteralVertex() {
        SailGraph graph = (SailGraph) graphTest.generateGraph();
        SailGraphFactory.createTinkerGraph(graph);

        Vertex v1 = graph.getVertex("tg:1");
        SailVertex vx = new SailVertex(new LiteralImpl("Marko"), graph);
        graph.addEdge(null, v1, vx, "tg:name");

        Vertex v = v1.getEdges(Direction.OUT, "tg:name").iterator().next().getVertex(Direction.IN);
        assertEquals("Marko", v.getProperty(SailTokens.VALUE));
        v1 = v.getEdges(Direction.IN, "tg:name").iterator().next().getVertex(Direction.OUT);
        assertEquals("http://tinkerpop.com#1", v1.getId());
    }
}