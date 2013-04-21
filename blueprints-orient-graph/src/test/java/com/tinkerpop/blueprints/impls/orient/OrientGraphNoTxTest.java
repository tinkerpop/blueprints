package com.tinkerpop.blueprints.impls.orient;

import java.io.File;
import java.lang.reflect.Method;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.IndexableGraphTestSuite;
import com.tinkerpop.blueprints.KeyIndexableGraphTestSuite;
import com.tinkerpop.blueprints.QueryTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrientGraphNoTxTest extends GraphTest {

  private OrientGraphNoTx currentGraph;

  // testing only those suites that are read-only

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

  public void testKeyIndexableGraphTestSuite() throws Exception {
    this.stopWatch();
    doTestSuite(new KeyIndexableGraphTestSuite(this));
    printTestPerformance("KeyIndexableGraphTestSuite", this.stopWatch());
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
    final String directory = getWorkingDirectory();
    this.currentGraph = new OrientGraphNoTx("local:" + directory + "/" + graphDirectoryName);
    return this.currentGraph;
  }

  public void doTestSuite(final TestSuite testSuite) throws Exception {
    String directory = getWorkingDirectory();
    deleteDirectory(new File(directory));
    for (Method method : testSuite.getClass().getDeclaredMethods()) {
      if (method.getName().startsWith("test")) {
        System.out.println("Testing " + method.getName() + "...");
        method.invoke(testSuite);
        try {
          if (this.currentGraph != null)
            this.currentGraph.shutdown();
        } catch (Exception e) {
        }
        OGraphDatabase g = new OGraphDatabase("local:" + directory + "/graph");
        if (g.exists())
          g.open("admin", "admin").drop();
      }
    }
  }

  private String getWorkingDirectory() {
    return this.computeTestDataRoot().getAbsolutePath();
  }
}