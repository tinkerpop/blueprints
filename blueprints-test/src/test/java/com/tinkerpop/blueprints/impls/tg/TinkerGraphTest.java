package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.IndexTestSuite;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraphTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;

import java.io.File;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerGraphTest extends GraphTest {

    /*public void testTinkerBenchmarkTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new TinkerBenchmarkTestSuite(this));
        printTestPerformance("TinkerBenchmarkTestSuite", this.stopWatch());
    }*/

    public void testGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphTestSuite(this));
        printTestPerformance("GraphTestSuite", this.stopWatch());
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

    public void testKeyIndexableGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new KeyIndexableGraphTestSuite(this));
        printTestPerformance("KeyIndexableGraphTestSuite", this.stopWatch());
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

    public void testGraphSONReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphSONReaderTestSuite(this));
        printTestPerformance("GraphSONReaderTestSuite", this.stopWatch());
    }

    public void testGMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GMLReaderTestSuite(this));
        printTestPerformance("GMLReaderTestSuite", this.stopWatch());
    }

    @Override
    public Graph generateGraph() {
        return new TinkerGraph(getDirectory());
    }

    protected String getDirectory() {
        String directory = System.getProperty("tinkerGraphDirectory");
        if (directory == null) {
            directory = this.getWorkingDirectory();
        }
        return directory;
    }

    private String getWorkingDirectory() {
        return this.computeTestDataRoot().getAbsolutePath();
    }

    @Override
    public void doTestSuite(final TestSuite testSuite) throws Exception {
        String directory = getDirectory();
        deleteDirectory(new File(directory));
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                method.invoke(testSuite);
                deleteDirectory(new File(directory));
            }
        }
    }

    public void testClear() {
        deleteDirectory(new File(getDirectory()));
        TinkerGraph graph = (TinkerGraph) this.generateGraph();
        this.stopWatch();
        for (int i = 0; i < 25; i++) {
            Vertex a = graph.addVertex(null);
            Vertex b = graph.addVertex(null);
            graph.addEdge(null, a, b, "knows");
        }
        printPerformance(graph.toString(), 75, "elements added", this.stopWatch());

        assertEquals(50, count(graph.getVertices()));
        assertEquals(25, count(graph.getEdges()));

        this.stopWatch();
        graph.clear();
        printPerformance(graph.toString(), 75, "elements deleted", this.stopWatch());

        assertEquals(0, count(graph.getVertices()));
        assertEquals(0, count(graph.getEdges()));

        graph.shutdown();
    }

    public void testShutdownStartManyTimes() {
        deleteDirectory(new File(getDirectory()));
        TinkerGraph graph = (TinkerGraph) this.generateGraph();
        for (int i = 0; i < 25; i++) {
            Vertex a = graph.addVertex(null);
            a.setProperty("name", "a" + UUID.randomUUID());
            Vertex b = graph.addVertex(null);
            b.setProperty("name", "b" + UUID.randomUUID());
            graph.addEdge(null, a, b, "knows").setProperty("weight", 1);
        }
        graph.shutdown();
        this.stopWatch();
        int iterations = 150;
        for (int i = 0; i < iterations; i++) {
            graph = (TinkerGraph) this.generateGraph();
            assertEquals(50, count(graph.getVertices()));
            for (final Vertex v : graph.getVertices()) {
                assertTrue(v.getProperty("name").toString().startsWith("a") || v.getProperty("name").toString().startsWith("b"));
            }
            assertEquals(25, count(graph.getEdges()));
            for (final Edge e : graph.getEdges()) {
                assertEquals(e.getProperty("weight"), 1);
            }

            graph.shutdown();
        }
        printPerformance(graph.toString(), iterations, "iterations of shutdown and restart", this.stopWatch());
    }
}
