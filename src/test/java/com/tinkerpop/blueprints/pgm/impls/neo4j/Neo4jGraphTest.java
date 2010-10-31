package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.parser.GraphMLReaderTestSuite;

import java.io.File;
import java.lang.reflect.Method;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jGraphTest extends BaseTest {

    private static final SuiteConfiguration config = new SuiteConfiguration();

    static {
        config.allowsDuplicateEdges = true;
        config.allowsSelfLoops = false;
        config.requiresRDFIds = false;
        config.isRDFModel = false;
        config.supportsVertexIteration = true;
        config.supportsEdgeIteration = true;
        config.supportsVertexIndex = true;
        config.supportsEdgeIndex = true;
        config.ignoresSuppliedIds = true;
        config.supportsTransactions = true;
    }

    public void testVertexSuite() throws Exception {
        doSuiteTest(new VertexTestSuite(config));
    }

    public void testEdgeSuite() throws Exception {
        doSuiteTest(new EdgeTestSuite(config));
    }

    public void testGraphSuite() throws Exception {
        doSuiteTest(new GraphTestSuite(config));
    }

    public void testGraphMLReaderSuite() throws Exception {
        doSuiteTest(new GraphMLReaderTestSuite(config));
    }

    public void testAutomaticIndexTestSuite() throws Exception {
        doSuiteTest(new AutomaticIndexTestSuite(config));
    }

    public void testIndexTestSuite() throws Exception {
        doSuiteTest(new IndexTestSuite(config));
    }

    public void testIndexableGraphSuite() throws Exception {
        doSuiteTest(new IndexableGraphTestSuite(config));
    }

    public void testTransactionalGraphSuite() throws Exception {
        doSuiteTest(new TransactionalGraphTestSuite(config));
    }

    private void doSuiteTest(final ModelTestSuite suite) throws Exception {
        String doTest = System.getProperty("testNeo4j");
        if (doTest == null || doTest.equals("true")) {
            String directory = System.getProperty("neo4jDirectory");
            if (directory == null) directory = "/tmp/blueprints_test";
            deleteDirectory(new File(directory));
            for (Method method : suite.getClass().getDeclaredMethods()) {
                if (method.getName().startsWith("test")) {
                    Graph graph = new Neo4jGraph(directory);
                    // removes the "reference node" in Neo4j
                    graph.removeVertex(graph.getVertex(0));
                    System.out.println("Testing " + method.getName() + "...");
                    method.invoke(suite, graph);
                    graph.shutdown();
                    deleteDirectory(new File(directory));
                }
            }
        }
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
}
