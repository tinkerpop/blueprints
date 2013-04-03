package com.tinkerpop.blueprints.impls.orient;

import com.tinkerpop.blueprints.Graph;

/**
 * Test suite for OrientDB graph implementation that uses Lightweight edges.
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraphLightweightEdgesTest extends OrientGraphTest {

  public Graph generateGraph(final String graphDirectoryName) {
    String directory = getWorkingDirectory();
    this.currentGraph = new OrientGraph("local:" + directory + "/" + graphDirectoryName);
    this.currentGraph.setUseLightweightEdges(true);
    return this.currentGraph;
  }

}