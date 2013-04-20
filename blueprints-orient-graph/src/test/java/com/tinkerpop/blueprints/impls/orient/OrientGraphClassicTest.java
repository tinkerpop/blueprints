package com.tinkerpop.blueprints.impls.orient;

import com.tinkerpop.blueprints.Graph;

/**
 * Test suite for OrientDB graph implementation compatible with the classic one before OrientDB 1.4.
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraphClassicTest extends OrientGraphTest {

  public Graph generateGraph(final String graphDirectoryName) {
    this.currentGraph = (OrientGraph) super.generateGraph(graphDirectoryName);
    this.currentGraph.setUseLightweightEdges(false);
    this.currentGraph.setUseClassForEdgeLabel(false);
    this.currentGraph.setUseClassForVertexLabel(false);
    this.currentGraph.setUseVertexFieldsForEdgeLabels(false);
    return this.currentGraph;
  }

}