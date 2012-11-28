package com.tinkerpop.blueprints.oupls.sail;


import static org.junit.Assert.assertTrue;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class OrientGraphSailTest extends GraphSailTest {

  public void testTrue() {
    assertTrue(true);
  }

  public KeyIndexableGraph createGraph() {
    String directory = getWorkingDirectory();

    OGraphDatabase db = new OGraphDatabase("local:" + directory + "/graph");
    
    if( db.exists())
      db.open("admin", "admin").drop();

    OrientGraph g = new OrientGraph("local:" + directory + "/graph");
    return g;
  }

  private String getWorkingDirectory() {
    return this.computeTestDataRoot().getAbsolutePath();
  }
}
