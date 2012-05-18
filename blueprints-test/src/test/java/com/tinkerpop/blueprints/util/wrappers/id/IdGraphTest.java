package com.tinkerpop.blueprints.util.wrappers.id;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraphTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdGraphTest extends GraphTest {

    public void testElementClasses() throws Exception {
        Graph graph = this.generateGraph();
        Vertex v1 = graph.addVertex(null);
        Vertex v2 = graph.addVertex(null);
        Edge e = graph.addEdge(null, v1, v2, "knows");

        assertTrue(v1 instanceof IdVertex);
        assertTrue(e instanceof IdEdge);

        Iterator<Edge> outE = v1.getEdges(Direction.OUT).iterator();
        assertTrue(outE.hasNext());
        e = outE.next();
        assertTrue(e instanceof IdEdge);
        assertTrue(e.getVertex(Direction.IN) instanceof IdVertex);
        assertTrue(e.getVertex(Direction.OUT) instanceof IdVertex);

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
        graph.shutdown();
    }


    public void testIdIndicesExist() throws Exception {
        KeyIndexableGraph graph = (KeyIndexableGraph) generateGraph();
        graph = (KeyIndexableGraph) ((WrapperGraph) graph).getBaseGraph();
        assertTrue(graph.getIndexedKeys(Vertex.class).contains(IdGraph.ID));
        assertTrue(graph.getIndexedKeys(Edge.class).contains(IdGraph.ID));
        graph.shutdown();
    }

    public void testDefaultIdFactory() throws Exception {
        Graph graph = this.generateGraph();
        Vertex v = graph.addVertex(null);
        String id = (String) v.getId();

        assertEquals(36, id.length());
        assertEquals(5, id.split("-").length);

        Vertex v2 = graph.addVertex(null);
        Edge e = graph.addEdge(null, v, v2, "knows");

        id = (String) e.getId();
        assertEquals(36, id.length());
        assertEquals(5, id.split("-").length);
        graph.shutdown();
    }


    public void testAddVertexWithSpecifiedId() throws Exception {
        Graph graph = this.generateGraph();
        Vertex v = graph.addVertex("forty-two");
        assertEquals("forty-two", v.getId());
        graph.shutdown();
    }

    public void testProperties() throws Exception {
        Graph graph = this.generateGraph();
        Vertex v = graph.addVertex(null);
        v.setProperty("name", "Zaphod");
        v.setProperty("profession", "ex-president of the Galaxy");

        Set<String> keys = v.getPropertyKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("name"));
        assertTrue(keys.contains("profession"));
        assertEquals("Zaphod", v.getProperty("name"));
        graph.shutdown();
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

    public void testKeyIndexableGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new KeyIndexableGraphTestSuite(this));
        printTestPerformance("KeyIndexableGraphTestSuite", this.stopWatch());
    }

    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }

    /*public void testGMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GMLReaderTestSuite(this));
        printTestPerformance("GMLReaderTestSuite", this.stopWatch());
    }*/

    public void testGraphSONReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphSONReaderTestSuite(this));
        printTestPerformance("GraphSONReaderTestSuite", this.stopWatch());
    }

    public Graph generateGraph() {
        final TinkerGraph baseGraph = new TinkerGraph();
        baseGraph.getFeatures().isPersistent = false;
        return new IdGraph<TinkerGraph>(baseGraph);
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                method.invoke(testSuite);
            }
        }
    }

}
