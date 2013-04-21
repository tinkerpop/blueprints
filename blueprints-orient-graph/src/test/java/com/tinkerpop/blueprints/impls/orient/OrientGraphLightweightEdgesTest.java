package com.tinkerpop.blueprints.impls.orient;

import com.tinkerpop.blueprints.Graph;

/**
 * Test suite for OrientDB graph implementation that uses Lightweight edges.
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraphLightweightEdgesTest extends OrientGraphTest {

  public Graph generateGraph(final String graphDirectoryName) {
    this.currentGraph = (OrientGraph) super.generateGraph(graphDirectoryName);
    this.currentGraph.setUseClassForEdgeLabel(false);
    this.currentGraph.setUseClassForVertexLabel(false);
    return this.currentGraph;
  }

}