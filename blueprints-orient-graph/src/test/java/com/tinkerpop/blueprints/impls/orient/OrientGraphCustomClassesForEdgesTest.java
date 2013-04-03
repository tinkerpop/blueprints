package com.tinkerpop.blueprints.impls.orient;

import com.tinkerpop.blueprints.Graph;

/**
 * Test suite for OrientDB graph implementation that store edges using custom classes derived by labels.
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraphCustomClassesForEdgesTest extends OrientGraphTest {

  public Graph generateGraph(final String graphDirectoryName) {
    String directory = getWorkingDirectory();
    this.currentGraph = new OrientGraph("local:" + directory + "/" + graphDirectoryName);
    this.currentGraph.setUseCustomClassesForEdges(true);
    return this.currentGraph;
  }

}