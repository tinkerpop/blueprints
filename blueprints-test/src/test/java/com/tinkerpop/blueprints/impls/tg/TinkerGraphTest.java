package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQueryTestSuite;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexTestSuite;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraphTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQueryTestSuite;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONWriterTestSuite;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;
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

    public void testVertexQueryTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new VertexQueryTestSuite(this));
        printTestPerformance("VertexQueryTestSuite", this.stopWatch());
    }

    public void testGraphQueryTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphQueryTestSuite(this));
        printTestPerformance("GraphQueryTestSuite", this.stopWatch());
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

    public void testGraphSONWriterTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphSONWriterTestSuite(this));
        printTestPerformance("GraphSONWriterTestSuite", this.stopWatch());
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
        testGraphFileType("graph-test-java", TinkerGraph.FileType.JAVA);
    }

    public void testGraphFileTypeGML() {
        testGraphFileType("graph-test-gml", TinkerGraph.FileType.GML);
    }

    public void testGraphFileTypeGraphML() {
        testGraphFileType("graph-test-graphml", TinkerGraph.FileType.GRAPHML);
    }

    public void testGraphFileTypeGraphSON() {
        testGraphFileType("graph-test-graphson", TinkerGraph.FileType.GRAPHSON);
    }

    private void testGraphFileType(final String directory, final TinkerGraph.FileType fileType) {
        final String path = getDirectory() + "/" + directory;
        deleteDirectory(new File(path));

        final TinkerGraph sourceGraph = TinkerGraphFactory.createTinkerGraph();
        final TinkerGraph targetGraph = new TinkerGraph(path, fileType);
        createKeyIndices(targetGraph);

        copyGraphs(sourceGraph, targetGraph);

        createManualIndices(targetGraph);

        this.stopWatch();
        targetGraph.shutdown();
        printTestPerformance("save graph: " + fileType.toString(), this.stopWatch());

        this.stopWatch();
        final TinkerGraph compareGraph = new TinkerGraph(path, fileType);
        printTestPerformance("load graph: " + fileType.toString(), this.stopWatch());

        compareGraphs(targetGraph, compareGraph, fileType);
    }

    private void createKeyIndices(final TinkerGraph g) {
        g.createKeyIndex("name", Vertex.class);
        g.createKeyIndex("weight", Edge.class);
    }

    private void createManualIndices(final TinkerGraph g) {
        final Index<Vertex> ageIndex = g.createIndex("age", Vertex.class);
        final Vertex v1 = g.getVertex(1);
        final Vertex v2 = g.getVertex(2);
        ageIndex.put("age", v1.getProperty("age"), v1);
        ageIndex.put("age", v2.getProperty("age"), v2);

        final Index<Edge> weightIndex = g.createIndex("weight", Edge.class);
        final Edge e7 = g.getEdge(7);
        final Edge e12 = g.getEdge(12);
        weightIndex.put("weight", e7.getProperty("weight"), e7);
        weightIndex.put("weight", e12.getProperty("weight"), e12);
    }

    private void copyGraphs(final TinkerGraph src, final TinkerGraph dst) {
        for (Vertex v : src.getVertices()) {
            ElementHelper.copyProperties(v, dst.addVertex(v.getId()));
        }

        for (Edge e : src.getEdges()) {
            ElementHelper.copyProperties(
                    e,
                    dst.addEdge(e.getId(), dst.getVertex(e.getVertex(Direction.OUT).getId()), dst.getVertex(e.getVertex(Direction.IN).getId()), e.getLabel()));
        }
    }

    private void compareGraphs(final TinkerGraph g1, final TinkerGraph g2, final TinkerGraph.FileType fileType) {
        for (Vertex v1 : g1.getVertices()) {
            final Vertex v2 = g2.getVertex(v1.getId());

            compareEdgeCounts(v1, v2, Direction.IN);
            compareEdgeCounts(v1, v2, Direction.OUT);
            compareEdgeCounts(v1, v2, Direction.BOTH);

            assertTrue(ElementHelper.haveEqualProperties(v1, v2));
            assertTrue(ElementHelper.areEqual(v1, v2));
        }

        for (Edge e1 : g1.getEdges()) {
            final Edge e2 = g2.getEdge(e1.getId());

            compareVertices(e1, e2, Direction.IN);
            compareVertices(e2, e2, Direction.OUT);

            if (fileType == TinkerGraph.FileType.GML) {
                // For GML we need to iterate the properties manually to catch the
                // case where the property returned from GML is an integer
                // while the target graph property is a float.
                for (String p : e1.getPropertyKeys()) {
                    final Object v1 = e1.getProperty(p);
                    final Object v2 = e2.getProperty(p);

                    if (!v1.getClass().equals(v2.getClass())) {
                        if ((v1 instanceof Float) && (v2 instanceof Integer)) {
                            assertEquals(v1, ((Integer) v2).floatValue());
                        } else if ((v1 instanceof Integer) && (v2 instanceof Float)) {
                            assertEquals(((Integer) v1).floatValue(), v2);
                        }
                    } else {
                        assertEquals(v1, v2);
                    }
                }
            } else {
                assertTrue(ElementHelper.haveEqualProperties(e1, e2));
            }

            assertTrue(ElementHelper.areEqual(e1, e2));
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

    private void compareEdgeCounts(Vertex v1, Vertex v2, Direction direction) {
        int c1 = 0;
        final Iterator it1 = v1.getEdges(direction).iterator();
        while (it1.hasNext()) {
            it1.next();
            c1++;
        }

        int c2 = 0;
        final Iterator it2 = v2.getEdges(direction).iterator();
        while (it2.hasNext()) {
            it2.next();
            c2++;
        }

        assertEquals(c1, c2);
    }

    private void compareVertices(Edge e1, Edge e2, Direction direction) {
        final Vertex v1 = e1.getVertex(direction);
        final Vertex v2 = e2.getVertex(direction);

        assertEquals(v1.getId(), v2.getId());
    }
}
