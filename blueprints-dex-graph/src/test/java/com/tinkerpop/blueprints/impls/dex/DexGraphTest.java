package com.tinkerpop.blueprints.impls.dex;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.QueryTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class DexGraphTest extends GraphTest {

    /*public void testDexBenchmarkTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new DexBenchmarkTestSuite(this));
        printTestPerformance("DexBenchmarkTestSuite", this.stopWatch());
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

    /*
    This test does not work because Dex properties are restricted to
    the scope of a node/edge type. Thus, when using the KeyIndexableGraph
    APIs it is required to previously set the label where the key property
    is defined, as it is shown in the testKeyIndex below.
    
    public void testKeyIndexableGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new KeyIndexableGraphTestSuite(this));
        printTestPerformance("KeyIndexableGraphTestSuite", this.stopWatch());
    }
    //*/

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

    /*
    the GML Reader won't work with Dex because of our test approach.  the test uses the toy
    tinkergraph which has a mix of data types for the "weight" property on the edge...dex does
    not allow an attribute with the same name to have values with different data types so it
    blows up the test.
    public void testGMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GMLReaderTestSuite(this));
        printTestPerformance("GMLReaderTestSuite", this.stopWatch());
    }
    */

    //
    // specific dex tests
    //

    public void testDexVertexLabel() throws Exception {
        Graph graph = generateGraph(true);
        this.stopWatch();

        assertTrue(graph.addVertex(null).getProperty(StringFactory.LABEL).equals(DexGraph.DEFAULT_DEX_VERTEX_LABEL));
        ((DexGraph) graph).label.set("people");
        assertTrue(graph.addVertex(null).getProperty(StringFactory.LABEL).equals("people"));
        ((DexGraph) graph).label.set("thing");
        assertTrue(graph.addVertex(null).getProperty(StringFactory.LABEL).equals("thing"));
        assertTrue(graph.addVertex("whatever").getProperty(StringFactory.LABEL).equals("thing"));
        ((DexGraph) graph).label.set(null);
        assertTrue(graph.addVertex(null).getProperty(StringFactory.LABEL).equals(DexGraph.DEFAULT_DEX_VERTEX_LABEL));

        ((DexGraph) graph).label.set("mylabel");
        Vertex v1 = graph.addVertex("mylabel");
        boolean excep = false;
        try {
            v1.setProperty(StringFactory.LABEL, "otherlabel");
        } catch (IllegalArgumentException e) {
            excep = true;
        } finally {
            assertTrue(excep);
        }

        printTestPerformance("Dex specific #testDexVertexLabel", this.stopWatch());
        graph.shutdown();
    }

    public void testKeyIndex() {
        KeyIndexableGraph graph = (KeyIndexableGraph) generateGraph(true);
        this.stopWatch();

        ((DexGraph) graph).label.set("people");
        graph.createKeyIndex("name", Vertex.class);

        ((DexGraph) graph).label.set("thing");
        graph.createKeyIndex("name", Vertex.class);

        assertTrue(graph.getIndexedKeys(Edge.class).isEmpty());
        assertTrue(graph.getIndexedKeys(Vertex.class).size() == 1);
        assertTrue(graph.getIndexedKeys(Vertex.class).contains("name"));

        ((DexGraph) graph).label.set("people");
        Vertex v1 = graph.addVertex(null);
        v1.setProperty("name", "foo");
        Vertex v2 = graph.addVertex(null);
        v2.setProperty("name", "boo");

        ((DexGraph) graph).label.set("thing");
        Vertex v10 = graph.addVertex(null);
        v10.setProperty("name", "foo");
        Vertex v20 = graph.addVertex(null);
        v20.setProperty("name", "boo");

        ((DexGraph) graph).label.set("people");
        assertTrue(graph.getVertices("name", "foo").iterator().next().equals(v1));
        ((DexGraph) graph).label.set("thing");
        assertTrue(graph.getVertices("name", "foo").iterator().next().equals(v10));

        ArrayList<Vertex> result = new ArrayList<Vertex>(Arrays.asList(v1, v10));
        ((DexGraph) graph).label.set(null); // all types!
        for (Vertex current : graph.getVertices("name", "foo")) {
            assertTrue(result.contains(current));
            result.remove(current);
        }
        assertTrue(result.size() == 0);

        result = new ArrayList<Vertex>(Arrays.asList(v1, v2));
        for (Vertex current : graph.getVertices(StringFactory.LABEL, "people")) {
            assertTrue(result.contains(current));
            result.remove(current);
        }
        assertTrue(result.size() == 0);

        // table scan
        v1.setProperty("age", 99);
        ((DexGraph) graph).label.set("people");
        assertTrue(graph.getVertices("age", 99).iterator().next().equals(v1));

        printTestPerformance("Dex specific #testKeyIndex", this.stopWatch());
        graph.shutdown();
    }

    public Graph generateGraph() {
        return generateGraph(false);
    }

    public Graph generateGraph(boolean create) {
        String db = this.computeTestDataRoot() + "/blueprints_test.dex";

        if (create) {
            File f = new File(db);
            if (f.exists()) f.delete();
        }
        return new DexGraph(db);
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        File fDB = new File(this.computeTestDataRoot() + "/blueprints_test.dex");
        fDB.delete();
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                method.invoke(testSuite);
                fDB.delete();
            }
        }
    }
}
