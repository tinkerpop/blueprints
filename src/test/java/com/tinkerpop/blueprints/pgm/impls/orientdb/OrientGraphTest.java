package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
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

	private static final SuiteConfiguration	config	= new SuiteConfiguration();

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

	public void testTransactionalGraphTestSuite() throws Exception {
		doSuiteTest(new TransactionalGraphTestSuite(config));
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

					OGlobalConfiguration.STORAGE_KEEP_OPEN.setValue(true);

					OrientGraph graph = new OrientGraph("local:" + url + "/graph");
					graph.clear();

					method.invoke(suite, graph);

					graph.shutdown();
					deleteDirectory(new File(url));
				}
			}
		}
	}
}
