package com.tinkerpop.blueprints.impls.orient;

import com.tinkerpop.blueprints.Graph;

/**
 * Test suite for OrientDB graph implementation that store edges using custom classes derived by labels.
 * 
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientGraphCustomEdgesTest extends OrientGraphTest {

  public Graph generateGraph(final String graphDirectoryName) {
    this.currentGraph = (OrientGraph) super.generateGraph(graphDirectoryName);
    this.currentGraph.setUseClassForEdgeLabel(true);

    if (currentGraph.getEdgeType("friend") == null)
      currentGraph.createEdgeType("friend");
    if (currentGraph.getEdgeType("test") == null)
      currentGraph.createEdgeType("test");
    if (currentGraph.getEdgeType("knows") == null)
      currentGraph.createEdgeType("knows");
    if (currentGraph.getEdgeType("created") == null)
      currentGraph.createEdgeType("created");
    if (currentGraph.getEdgeType("collaborator") == null)
      currentGraph.createEdgeType("collaborator");
    if (currentGraph.getEdgeType("hate") == null)
      currentGraph.createEdgeType("hate");
    if (currentGraph.getEdgeType("hates") == null)
      currentGraph.createEdgeType("hates");
    if (currentGraph.getEdgeType("test-edge") == null)
      currentGraph.createEdgeType("test-edge");
    if (currentGraph.getEdgeType("self") == null)
      currentGraph.createEdgeType("self");

    return this.currentGraph;
  }

}