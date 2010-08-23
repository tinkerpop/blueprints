package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.parser.GraphMLReaderTestSuite;

import java.io.File;
import java.lang.reflect.Method;

/**
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
        config.supportsVertexIndex = false;
        config.supportsEdgeIndex = false;
        config.ignoresSuppliedIds = true;
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

    public void testIndexSuite() throws Exception {
        doSuiteTest(new IndexTestSuite(config));
    }

    public void testGraphMLReaderSuite() throws Exception {
        doSuiteTest(new GraphMLReaderTestSuite(config));
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

                    OrientGraph graph = new OrientGraph("local:" + url + "/graph");
                    if (graph.exists()) {
                        graph.open("admin", "admin");
                        graph.clear();
                    } else
                        graph.create();

                    System.out.println("Testing " + method.getName() + "...");
                    method.invoke(suite, graph);

                    graph.shutdown();
                }
            }

            deleteGraphDirectory(new File(url));
        }
    }

    protected static void deleteGraphDirectory(final File directory) {
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    deleteGraphDirectory(file);
                } else {
                    file.delete();
                }
            }
            directory.delete();
        }
    }

}
