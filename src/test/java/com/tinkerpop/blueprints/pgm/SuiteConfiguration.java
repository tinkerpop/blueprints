package com.tinkerpop.blueprints.pgm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SuiteConfiguration {

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
     * Is the graph framework an RDF framework?
     */
    public boolean isRDFModel;
    /**
     * Is the graph framework's id address space that of URIs, blank nodes, and literals?
     */
    public boolean requiresRDFIds;
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

}
