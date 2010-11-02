package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.parser.GraphMLReaderTestSuite;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Test suite for OrientDB graph implementation.
 *
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraphTest extends BaseTest {

    private static final SuiteConfiguration config = new SuiteConfiguration();

    static {
        config.allowsDuplicateEdges = true;
        config.allowsSelfLoops = true;
        config.requiresRDFIds = false;
        config.isRDFModel = false;
        config.supportsVertexIteration = true;
        config.supportsEdgeIteration = true;
        config.supportsVertexIndex = true;
        config.supportsEdgeIndex = true;
        config.ignoresSuppliedIds = true;
        config.supportsTransactions = true;
    }

    public void testVertexTestSuite() throws Exception {
        this.stopWatch();
        doSuiteTest(new VertexTestSuite(config));
        printTestPerformance("VertexTestSuite", this.stopWatch());
    }

    public void testEdgeTestSuite() throws Exception {
        this.stopWatch();
        doSuiteTest(new EdgeTestSuite(config));
        printTestPerformance("EdgeTestSuite", this.stopWatch());
    }

    public void testGraphTestSuite() throws Exception {
        this.stopWatch();
        doSuiteTest(new GraphTestSuite(config));
        printTestPerformance("GraphTestSuite", this.stopWatch());
    }

    public void testIndexableGraphTestSuite() throws Exception {
        this.stopWatch();
        doSuiteTest(new IndexableGraphTestSuite(config));
        printTestPerformance("IndexableGraphTestSuite", this.stopWatch());
    }

    /*public void testIndexTestSuite() throws Exception {
        this.stopWatch();
        doSuiteTest(new IndexTestSuite(config));
        printTestPerformance("IndexTestSuite", this.stopWatch());
    }*/

    public void testAutomaticIndexTestSuite() throws Exception {
        this.stopWatch();
        doSuiteTest(new AutomaticIndexTestSuite(config));
        printTestPerformance("AutomaticIndexTestSuite", this.stopWatch());
    }

    public void testTransactionalGraphTestSuite() throws Exception {
        this.stopWatch();
        doSuiteTest(new TransactionalGraphTestSuite(config));
        printTestPerformance("TransactionGraphTestSuite", this.stopWatch());
    }

    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doSuiteTest(new GraphMLReaderTestSuite(config));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }

    private void doSuiteTest(final ModelTestSuite suite) throws Exception {
        String doTest = System.getProperty("testOrientGraph");
        if (doTest == null || doTest.equals("true")) {
            String url = System.getProperty("orientGraphDirectory");
            if (url == null)
                url = "/tmp/blueprints_test";

            final File directory = new File(url);
            if (!directory.exists())
                directory.mkdirs();

            for (Method method : suite.getClass().getDeclaredMethods()) {
                if (method.getName().startsWith("test")) {
                    System.out.println("Testing " + method.getName() + "...");
                    OrientGraph graph = new OrientGraph("local:" + url + "/graph");
                    method.invoke(suite, graph);
                    graph.shutdown();
                    deleteDirectory(new File(url));
                }
            }
        }
    }
}
