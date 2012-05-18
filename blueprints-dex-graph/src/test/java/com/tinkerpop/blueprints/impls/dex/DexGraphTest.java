package com.tinkerpop.blueprints.impls.dex;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphTestSuite;
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

    /*public void testKeyIndexableGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new KeyIndexableGraphTestSuite(this));
        printTestPerformance("KeyIndexableGraphTestSuite", this.stopWatch());
    }*/

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

    public void testDexVertexLabel() throws Exception {
        Graph graph = generateGraph();

        this.stopWatch();
        assertTrue(graph.addVertex(null).getProperty(StringFactory.LABEL).equals(DexGraph.DEFAULT_DEX_VERTEX_LABEL));
        assertTrue(graph.addVertex("people").getProperty(StringFactory.LABEL).equals("people"));
        assertTrue(graph.addVertex("thing").getProperty(StringFactory.LABEL).equals("thing"));
        BaseTest.printPerformance(graph.toString(), 3, "vertices with user labels added", this.stopWatch());

        this.stopWatch();
        Vertex v1 = graph.addVertex("mylabel");
        boolean excep = false;
        try {
            v1.setProperty(StringFactory.LABEL, "otherlabel");
        } catch (IllegalArgumentException e) {
            excep = true;
        } finally {
            assertTrue(excep);
        }
        BaseTest.printPerformance(graph.toString(), 1, "validate label is protected", this.stopWatch());
        graph.shutdown();
    }

    public Graph generateGraph() {
        String db = System.getProperty("dexGraphFile");
        if (db == null)
            db = "/tmp/blueprints_test.dex";
        return new DexGraph(db);
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        String doTest = System.getProperty("testDexGraph");
        if (doTest == null || doTest.equals("true")) {
            String db = System.getProperty("dexGraphFile");
            if (db == null)
                db = "/tmp/blueprints_test.dex";
            File fDB = new File(db);
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
}
