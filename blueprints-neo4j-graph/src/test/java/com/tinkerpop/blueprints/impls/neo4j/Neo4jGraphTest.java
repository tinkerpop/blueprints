package com.tinkerpop.blueprints.impls.neo4j;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexTestSuite;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraphTestSuite;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.QueryTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.TransactionalGraphTestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;
import org.neo4j.index.impl.lucene.LowerCaseKeywordAnalyzer;
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jGraphTest extends GraphTest {

    /*public void testNeo4jBenchmarkTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new Neo4jBenchmarkTestSuite(this));
        printTestPerformance("Neo4jBenchmarkTestSuite", this.stopWatch());
    }*/

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

    public void testQueryTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new QueryTestSuite(this));
        printTestPerformance("QueryTestSuite", this.stopWatch());
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

    public void testTransactionalGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new TransactionalGraphTestSuite(this));
        printTestPerformance("TransactionalGraphTestSuite", this.stopWatch());
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

    public Graph generateGraph() {
        String directory = this.getWorkingDirectory();
        Neo4jGraph graph = new Neo4jGraph(directory);
        graph.setCheckElementsInTransaction(true);
        return graph;
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        String directory = this.getWorkingDirectory();
        deleteDirectory(new File(directory));
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                method.invoke(testSuite);
                deleteDirectory(new File(directory));
            }
        }
    }

    private String getWorkingDirectory() {
        return this.computeTestDataRoot().getAbsolutePath();
    }

    public void testLongIdConversions() {
        String id1 = "100";  // good  100
        String id2 = "100.0"; // good 100
        String id3 = "100.1"; // good 100
        String id4 = "one"; // bad

        try {
            Double.valueOf(id1).longValue();
            assertTrue(true);
        } catch (NumberFormatException e) {
            assertFalse(true);
        }
        try {
            Double.valueOf(id2).longValue();
            assertTrue(true);
        } catch (NumberFormatException e) {
            assertFalse(true);
        }
        try {
            Double.valueOf(id3).longValue();
            assertTrue(true);
        } catch (NumberFormatException e) {
            assertFalse(true);
        }
        try {
            Double.valueOf(id4).longValue();
            assertTrue(false);
        } catch (NumberFormatException e) {
            assertFalse(false);
        }
    }

    public void testQueryIndex() throws Exception {
        String directory = this.getWorkingDirectory();
        deleteDirectory(new File(directory));

        Neo4jGraph graph = new Neo4jGraph(directory);
        Index<Vertex> vertexIndex = graph.createIndex("vertices", Vertex.class);
        Index<Edge> edgeIndex = graph.createIndex("edges", Edge.class);

        Vertex a = graph.addVertex(null);
        a.setProperty("name", "marko");
        vertexIndex.put("name", "marko", a);

        Iterator itty = graph.getIndex("vertices", Vertex.class).query("name", "*rko").iterator();
        int counter = 0;
        while (itty.hasNext()) {
            counter++;
            assertEquals(itty.next(), a);
        }
        assertEquals(counter, 1);

        Vertex b = graph.addVertex(null);
        Edge edge = graph.addEdge(null, a, b, "knows");
        edge.setProperty("weight", 0.75);
        edgeIndex.put("weight", 0.75, edge);

        itty = graph.getIndex("edges", Edge.class).query("label", "k?ows").iterator();
        counter = 0;
        while (itty.hasNext()) {
            counter++;
            assertEquals(itty.next(), edge);
        }
        assertEquals(counter, 0);

        itty = graph.getIndex("edges", Edge.class).query("weight", "[0.5 TO 1.0]").iterator();
        counter = 0;
        while (itty.hasNext()) {
            counter++;
            assertEquals(itty.next(), edge);
        }
        assertEquals(counter, 1);
        assertEquals(count(graph.getIndex("edges", Edge.class).query("weight", "[0.1 TO 0.5]")), 0);


        graph.shutdown();
        deleteDirectory(new File(directory));
    }

    public void testIndexParameters() throws Exception {
        String directory = this.getWorkingDirectory();
        deleteDirectory(new File(directory));

        IndexableGraph graph = new Neo4jGraph(directory);
        Index<Vertex> index = graph.createIndex("luceneIdx", Vertex.class, new Parameter<String, String>("analyzer", LowerCaseKeywordAnalyzer.class.getName()));
        Vertex a = graph.addVertex(null);
        a.setProperty("name", "marko");
        index.put("name", "marko", a);
        Iterator itty = index.query("name", "*rko").iterator();
        int counter = 0;
        while (itty.hasNext()) {
            counter++;
            assertEquals(itty.next(), a);
        }
        assertEquals(counter, 1);

        itty = index.query("name", "MaRkO").iterator();
        counter = 0;
        while (itty.hasNext()) {
            counter++;
            assertEquals(itty.next(), a);
        }
        assertEquals(counter, 1);

        graph.shutdown();
        deleteDirectory(new File(this.getWorkingDirectory()));
    }

    public void testArrayProperty() throws Exception {
        String directory = this.getWorkingDirectory();
        deleteDirectory(new File(directory));

        Neo4jGraph graph = new Neo4jGraph(directory);

        // Test non empty native array
        int[] v0 = new int[2];
        v0[0] = 1;
        v0[1] = 2;
        Vertex a0 = graph.addVertex(null);
        a0.setProperty("array_property", v0);
        int[] r0 = (int[]) a0.getProperty("array_property");
        assertEquals(r0.length, 2);
        assertEquals(r0[0], 1);
        assertEquals(r0[1], 2);

        // Test empty native array
        int[] v1 = new int[0];
        Vertex a1 = graph.addVertex(null);
        a1.setProperty("array_property", v1);
        int[] r1 = (int[]) a1.getProperty("array_property");
        assertEquals(r1.length, 0);

        // Test non empty, uniform array list
        ArrayList v2 = new ArrayList();
        v2.add(1);
        v2.add(2);
        Vertex a2 = graph.addVertex(null);
        a2.setProperty("array_property", v2);
        int[] r2 = (int[]) a2.getProperty("array_property");
        assertEquals(r2.length, 2);
        assertEquals(r2[0], 1);
        assertEquals(r2[1], 2);

        // Test non empty, non-uniform array list - neo4j does not support this
        ArrayList v3 = new ArrayList();
        v3.add(1);
        v3.add("2");
        Vertex a3 = graph.addVertex(null);
        try {
          a3.setProperty("array_property", v3);
          assertTrue(false);
        } catch (java.lang.IllegalArgumentException e) {
          assertTrue(true);
        }

        // Test empty array list
        ArrayList v4 = new ArrayList();
        Vertex a4 = graph.addVertex(null);
        a4.setProperty("array_property", v4);
        int[] r4 = (int[]) a4.getProperty("array_property");
        assertEquals(r4.length, 0);

        graph.shutdown();
        deleteDirectory(new File(this.getWorkingDirectory()));
    }

    public void testReadOnlyGraph() throws Exception {
        String directory = this.getWorkingDirectory();
        deleteDirectory(new File(directory));

        Neo4jGraph graph = new Neo4jGraph(directory);
        assertEquals(graph.getIndexedKeys(Vertex.class).size(), 0);
        assertFalse(graph.getIndexedKeys(Vertex.class).contains("name"));
        graph.createKeyIndex("name", Vertex.class);
        graph.addVertex(null).setProperty("name", "marko");
        graph.addVertex(null).setProperty("name", "matthias");
        assertEquals(graph.getIndexedKeys(Vertex.class).size(), 1);
        assertTrue(graph.getIndexedKeys(Vertex.class).contains("name"));
        graph.shutdown();

        graph = new Neo4jGraph(new EmbeddedReadOnlyGraphDatabase(directory));
        assertEquals(count(graph.getVertices()), 2);
        assertEquals(count(graph.getVertices("name", "marko")), 1);
        assertEquals(count(graph.getVertices("name", "matthias")), 1);
        assertEquals(graph.getIndexedKeys(Vertex.class).size(), 1);
        assertTrue(graph.getIndexedKeys(Vertex.class).contains("name"));
        graph.shutdown();
        deleteDirectory(new File(directory));
    }
}
