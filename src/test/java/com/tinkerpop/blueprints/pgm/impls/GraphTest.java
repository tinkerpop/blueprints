package com.tinkerpop.blueprints.pgm.impls;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.TestSuite;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class GraphTest extends BaseTest {

    /**
     * Does the graph framework allow for two edges with the same vertices and edge label to exist?
     */
    public boolean allowsDuplicateEdges;
    /**
     * Does the graph framework allow an edge to have the same out/tail and in/head vertex?
     */
    public boolean allowsSelfLoops;
    /**
     * Does the graph framework ignored user provided ids in graph.addVertex(Object id)?
     */
    public boolean ignoresSuppliedIds;
    /**
     * Does the graph framework persist the graph to disk after shutdown?
     */
    public boolean isPersistent;
    /**
     * Is the graph framework an RDF framework?
     */
    public boolean isRDFModel;
    /**
     * Does the graph framework support the indexing of edges by their properties?
     */
    public boolean supportsEdgeIndex;
    /**
     * Does the graph framework support graph.getEdges()?
     */
    public boolean supportsEdgeIteration;
    /**
     * Does the graph framework support the indexing of vertices by their properties?
     */
    public boolean supportsVertexIndex;
    /**
     * Does the graph framework support graph.getVertices()?
     */
    public boolean supportsVertexIteration;
    /**
     * Does the graph implement TransactionalGraph?
     */
    public boolean supportsTransactions;

    public Graph createGraphDatabase() {
      Graph graph = loadGraphDatabase();
      graph.clear();
      return graph;
    }

    public abstract Graph loadGraphDatabase();
    
    public abstract void doTestSuite(final TestSuite testSuite) throws Exception;

}
