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
     * Graph allows any serializable object to be used as a property value for a graph element.
     */
    public boolean allowSerializableObjectProperty;
    /**
     * Graph allows boolean to be used as a property value for a graph element.
     */
    public boolean allowBooleanProperty;
    /**
     * Graph allows double to be used as a property value for a graph element.
     */
    public boolean allowDoubleProperty;
    /**
     * Graph allows float to be used as a property value for a graph element.
     */
    public boolean allowFloatProperty;
    /**
     * Graph allows integer to be used as a property value for a graph element.
     */
    public boolean allowIntegerProperty;
    /**
     * Graph allows a primitive array to be used as a property value for a graph element.
     */
    public boolean allowPrimitiveArrayProperty;
    /**
     * Graph allows list (all objects with the list have the same data types) to be used as a property
     * value for a graph element.
     */
    public boolean allowUniformListProperty;
    /**
     * Graph allows a mixed list (different data types within the same list) to be used as a
     * property value for a graph element.
     */
    public boolean allowMixedListProperty;
    /**
     * Graph allows long to be used as a property value for a graph element.
     */
    public boolean allowLongProperty;
    /**
     * Graph allows map to be used as a property value for a graph element.
     */
    public boolean allowMapProperty;
    /**
     * Graph allows string to be used as a property value for a graph element.
     */
    public boolean allowStringProperty;
    /**
     * Does the graph ignore user provided ids in graph.addVertex(Object id)?
     */
    public boolean ignoresSuppliedIds;
    /**
     * Does the graph persist the graph to disk after shutdown?
     */
    public boolean isPersistent;
    /**
     * Is the graph an RDF framework?
     */
    public boolean isRDFModel;
    /**
     * Is this graph a wrapper (ie. ReadOnlyGraph) for another graph implementation? If it is a wrapper then
     * property data type tests will be ignored as they are tested under the specific graph implementations.
     */
    public boolean isWrapper;
    /**
     * Does the graph support developer generated indices (beyond automatic key indices)?
     */
    public boolean supportsManualIndices;
    /**
     * Does the graph support the indexing of edges by their properties?
     */
    public boolean supportsEdgeIndex;
    /**
     * Does the graph support graph.getEdges()?
     */
    public boolean supportsEdgeIteration;
    /**
     * Does the graph support the indexing of vertices by their properties?
     */
    public boolean supportsVertexIndex;
    /**
     * Does the graph support graph.getVertices()?
     */
    public boolean supportsVertexIteration;
    /**
     * Does the graph implement TransactionalGraph?
     */
    public boolean supportsTransactions;

    public abstract Graph getGraphInstance();

    public abstract void doTestSuite(final TestSuite testSuite) throws Exception;

}
