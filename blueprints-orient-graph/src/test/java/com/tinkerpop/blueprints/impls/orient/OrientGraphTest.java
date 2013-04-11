package com.tinkerpop.blueprints.impls.orient;

import java.io.File;
import java.lang.reflect.Method;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.IndexTestSuite;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraphTestSuite;
import com.tinkerpop.blueprints.QueryTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.TransactionalGraphTestSuite;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;

/**
 * Test suite for OrientDB graph implementation.
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public abstract class OrientGraphTest extends GraphTest {

  protected OrientGraph currentGraph;

  // public void testOrientBenchmarkTestSuite() throws Exception {
  // this.stopWatch();
  // doTestSuite(new OrientBenchmarkTestSuite(this));
  // printTestPerformance("OrientBenchmarkTestSuite", this.stopWatch());
  // }

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

  public void testKeyIndexableGraphTestSuite() throws Exception {
    this.stopWatch();
    doTestSuite(new KeyIndexableGraphTestSuite(this));
    printTestPerformance("KeyIndexableGraphTestSuite", this.stopWatch());
  }

  public void testTransactionalGraphTestSuite() throws Exception {
    this.stopWatch();
    doTestSuite(new TransactionalGraphTestSuite(this));
    printTestPerformance("TransactionGraphTestSuite", this.stopWatch());
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
    return generateGraph("graph");
  }

  public Graph generateGraph(final String graphDirectoryName) {
    final String dbPath = getWorkingDirectory() + "/" + graphDirectoryName;
    this.currentGraph = new OrientGraph("local:" + dbPath);
    return currentGraph;
  }

  public void doTestSuite(final TestSuite testSuite) throws Exception {
    String directory = getWorkingDirectory();
    deleteDirectory(new File(directory));
    for (Method method : testSuite.getClass().getDeclaredMethods()) {
      if (method.getName().startsWith("test")) {
        System.out.println("Testing " + method.getName() + "...");
        method.invoke(testSuite);
        dropGraph(directory + "/graph");
      }
    }
  }

  @Override
  public void dropGraph(final String graphDirectoryName) {
    // this is necessary on windows systems: deleting the directory is not enough because it takes a
    // while to unlock files
    try {
      if (this.currentGraph != null)
        this.currentGraph.shutdown();
    } catch (Exception e) {
    }

    final ODatabaseDocumentTx g = new ODatabaseDocumentTx("local:" + graphDirectoryName);
    if (g.exists())
      g.open("admin", "admin").drop();

    deleteDirectory(new File(graphDirectoryName));
  }

  protected String getWorkingDirectory() {
    return this.computeTestDataRoot().getAbsolutePath();
  }
}