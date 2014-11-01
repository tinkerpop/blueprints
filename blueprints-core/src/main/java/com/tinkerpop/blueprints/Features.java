package com.tinkerpop.blueprints;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Features provides a listing of the features/qualities/quirks associated with any Graph implementation.
 * This feature listing can be used to dynamically adjust code to the features of the graph implementation.
 * For example, this feature listing is used extensively throughout the Blueprints TestSuite to validate behavior of the implementation.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette
 */
public class Features {
    /**
     * Does the graph allow for two edges with the same vertices and edge label to exist?
     */
    public Boolean supportsDuplicateEdges = null;
    /**
     * Does the graph allow an edge to have the same out/tail and in/head vertex?
     */
    public Boolean supportsSelfLoops = null;
    /**
     * Does the graph allow any serializable object to be used as a property value for a graph element?
     */
    public Boolean supportsSerializableObjectProperty = null;
    /**
     * Does the graph allows boolean to be used as a property value for a graph element?
     */
    public Boolean supportsBooleanProperty = null;
    /**
     * Does the graph allows double to be used as a property value for a graph element?
     */
    public Boolean supportsDoubleProperty = null;
    /**
     * Does the graph allows float to be used as a property value for a graph element?
     */
    public Boolean supportsFloatProperty = null;
    /**
     * Does the graph allows integer to be used as a property value for a graph element?
     */
    public Boolean supportsIntegerProperty = null;
    /**
     * Does the graph allows a primitive array to be used as a property value for a graph element?
     */
    public Boolean supportsPrimitiveArrayProperty = null;
    /**
     * Does the graph allows list (all objects with the list have the same data types) to be used as a property
     * value for a graph element?
     */
    public Boolean supportsUniformListProperty = null;
    /**
     * Does the graph allows a mixed list (different data types within the same list) to be used as a
     * property value for a graph element?
     */
    public Boolean supportsMixedListProperty = null;
    /**
     * Does the graph allows long to be used as a property value for a graph element?
     */
    public Boolean supportsLongProperty = null;
    /**
     * Does the graph allows map to be used as a property value for a graph element?
     */
    public Boolean supportsMapProperty = null;
    /**
     * Graph allows string to be used as a property value for a graph element.
     */
    public Boolean supportsStringProperty = null;
    /**
     * Does the graph return elements not explicitly created with addVertex or addEdge?
     */
    public Boolean hasImplicitElements = false;
    /**
     * Does the graph ignore user provided ids in graph.addVertex(Object id)?
     */
    public Boolean ignoresSuppliedIds = null;
    /**
     * Does the graph persist the graph to disk after shutdown?
     */
    public Boolean isPersistent = null;
    /**
     * Does the graph implement WrapperGraph?
     */
    public Boolean isWrapper = null;
    /**
     * Does the graph implement IndexableGraph?
     */
    public Boolean supportsIndices = null;
    /**
     * Does the graph support the indexing of vertices by their properties?
     */
    public Boolean supportsVertexIndex = null;
    /**
     * Does the graph support the indexing of edges by their properties?
     */
    public Boolean supportsEdgeIndex = null;
    /**
     * Does the graph implement KeyIndexableGraph?
     */
    public Boolean supportsKeyIndices = null;
    /**
     * Does the graph support key indexing on vertices?
     */
    public Boolean supportsVertexKeyIndex = null;
    /**
     * Does the graph support key indexing on edges?
     */
    public Boolean supportsEdgeKeyIndex = null;
    /**
     * Does the graph support graph.getEdges()?
     */
    public Boolean supportsEdgeIteration = null;
    /**
     * Does the graph support graph.getVertices()?
     */
    public Boolean supportsVertexIteration = null;
    /**
     * Does the graph support retrieving edges by id, i.e. graph.getEdge(Object id)?
     */
    public Boolean supportsEdgeRetrieval = null;
    /**
     * Does the graph support setting and retrieving properties on vertices?
     */
    public Boolean supportsVertexProperties = null;
    /**
     * Does the graph support setting and retrieving properties on edges?
     */
    public Boolean supportsEdgeProperties = null;
    /**
     * Does the graph implement TransactionalGraph?
     */
    public Boolean supportsTransactions = null;
    /**
     * Does the graph implement ThreadedTransactionalGraph?
     */
    public Boolean supportsThreadedTransactions = null;
    /**
     * Does the graph support transactions managed such that multiple threads operating on the same graph instance
     * can have isolated transactions?
     */
    public Boolean supportsThreadIsolatedTransactions = null;

    /**
     * Checks whether the graph supports both vertex and edge properties
     *
     * @return whether the graph supports both vertex and edge properties
     */
    public boolean supportsElementProperties() {
        return supportsVertexProperties && supportsEdgeProperties;
    }

    public String toString() {
        try {
            final StringBuilder string = new StringBuilder();
            for (final Field field : this.getClass().getFields()) {
                string.append(field.getName() + ": " + field.get(this).toString() + "\n");
            }
            return string.toString().substring(0, string.length() - 1);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    public Map toMap() {
        try {
            final Map<String, Object> map = new HashMap<String, Object>();
            for (final Field field : this.getClass().getFields()) {
                map.put(field.getName(), field.get(this));
            }
            return Collections.unmodifiableMap(map);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * This method determines whether the full gamut of features have been set by the Graph implementation.
     * This is useful for implementers to ensure that they did not miss specifying a feature.
     *
     * @throws IllegalStateException thrown if a feature was not set
     */
    public void checkCompliance() throws IllegalStateException {
        try {
            for (final Field field : this.getClass().getFields()) {
                if (field.get(this) == null)
                    throw new IllegalStateException("The feature " + field.getName() + " was not specified");
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * This method copies the features in this features object to another feature object.
     *
     * @return a feature object with a clone of the features in the prior.
     */
    public Features copyFeatures() {
        try {
            final Features features = new Features();
            for (final Field field : this.getClass().getFields()) {
                field.set(features, field.get(this));
            }
            return features;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
