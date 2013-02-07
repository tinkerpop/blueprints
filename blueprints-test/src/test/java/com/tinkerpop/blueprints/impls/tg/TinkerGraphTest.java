package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexTestSuite;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraphTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
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
        return generateGraph("graph");
    }

    @Override
    public Graph generateGraph(final String graphDirectoryName) {
        return new TinkerGraph(getDirectory() + "/" + graphDirectoryName);
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

    public void testGraphFileTypeJava() {
        testGraphFileType("test-java", TinkerGraph.FileType.JAVA);
    }

    public void testGraphFileTypeGML() {
        testGraphFileType("test-gml", TinkerGraph.FileType.GML);
    }

    public void testGraphFileTypeGraphML() {
        testGraphFileType("test-graphml", TinkerGraph.FileType.GRAPHML);
    }

    public void testGraphFileTypeGraphSON() {
        testGraphFileType("test-graphson", TinkerGraph.FileType.GRAPHSON);
    }

    private void testGraphFileType(String directory, TinkerGraph.FileType fileType) {
        String path = getDirectory() + "/" + directory;

        File file = new File(path);
        if (file.exists()) {
            try {
                delete(file);
            }
            catch (IOException e) {
                System.out.println("Cannot delete file " + file);
            }
        }

        final TinkerGraph sourceGraph = TinkerGraphFactory.createTinkerGraph();
        TinkerGraph targetGraph = new TinkerGraph(path, fileType);
        createKeyIndices(targetGraph);

        copyGraphs(sourceGraph, targetGraph);

        createManualIndices(targetGraph);
        targetGraph.shutdown();

        final TinkerGraph compareGraph = new TinkerGraph(path, fileType);
        compareGraphs(targetGraph, compareGraph);
    }

    private void delete(final File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }

        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }

    private void createKeyIndices(final TinkerGraph g) {
        g.createKeyIndex("name", Vertex.class);
        g.createKeyIndex("weight", Edge.class);
    }

    private void createManualIndices(final TinkerGraph g) {
        Index<Vertex> ageIndex = g.createIndex("age", Vertex.class);
        Vertex v1 = g.getVertex(1);
        Vertex v2 = g.getVertex(2);
        ageIndex.put("age", v1.getProperty("age"), v1);
        ageIndex.put("age", v2.getProperty("age"), v2);

        Index<Edge> weightIndex = g.createIndex("weight", Edge.class);
        Edge e7 = g.getEdge(7);
        Edge e12 = g.getEdge(12);
        weightIndex.put("weight", e7.getProperty("weight"), e7);
        weightIndex.put("weight", e12.getProperty("weight"), e12);
    }

    private void copyGraphs(TinkerGraph src, TinkerGraph dst) {
        for (Vertex v : src.getVertices()) {
            ElementHelper.copyProperties(v, dst.addVertex(v.getId()));
        }

        for (Edge e : src.getEdges()) {
            ElementHelper.copyProperties(
                    e,
                    dst.addEdge(e.getId(), e.getVertex(Direction.OUT), e.getVertex(Direction.IN), e.getLabel()));
        }
    }

    private void compareGraphs(TinkerGraph g1, TinkerGraph g2) {
        for (Vertex v : g1.getVertices()) {
            assertTrue(ElementHelper.areEqual(v, g2.getVertex(v.getId())));
        }

        for (Edge e : g1.getEdges()) {
            assertTrue(ElementHelper.areEqual(e, g2.getEdge(e.getId())));
        }

        final Index idxAge = g2.getIndex("age", Vertex.class);
        assertEquals(g2.getVertex(1), idxAge.get("age", 29).iterator().next());
        assertEquals(g2.getVertex(2), idxAge.get("age", 27).iterator().next());

        final Index idxWeight = g2.getIndex("weight", Edge.class);
        assertEquals(g2.getEdge(7), idxWeight.get("weight", 0.5f).iterator().next());
        assertEquals(g2.getEdge(12), idxWeight.get("weight", 0.2f).iterator().next());

        final Iterator namesItty = g2.getVertices("name", "marko").iterator();
        assertEquals(g2.getVertex(1), namesItty.next());
        assertFalse(namesItty.hasNext());

        final Iterator weightItty = g2.getEdges("weight", 0.5f).iterator();
        assertEquals(g2.getEdge(7), weightItty.next());
        assertFalse(weightItty.hasNext());

    }
}