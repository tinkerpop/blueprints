package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import java.util.Map;

/**
 * Interface for a listener to EventGraph change events.
 *
 * Implementations of this interface should be added to the list of listeners on the addListener method on
 * the EventGraph.
 *
 * @author Stephen Mallette
 */
public interface GraphChangedListener {

    /**
     * Raised when a new Vertex is added.
     *
     * @param vertex the vertex that was added
     */
    public void vertexAdded(final Vertex vertex);

    /**
     * Raised after the property of a vertex changed.
     *
     * @param vertex   the vertex that changed
     * @param key      the key of the property that changed
     * @param setValue the new value of the property
     */
    public void vertexPropertyChanged(final Vertex vertex, final String key, final Object oldValue, final Object setValue);

    /**
     * Raised after a vertex property was removed.
     *
     * @param vertex       the vertex that changed
     * @param key          the key that was removed
     * @param removedValue the value of the property that was removed
     */
    public void vertexPropertyRemoved(final Vertex vertex, final String key, final Object removedValue);

    /**
     * Raised after a vertex was removed from the graph.
     *
     * @param vertex the vertex that was removed
     * @param props  the properties of the removed vertex
     */
    public void vertexRemoved(final Vertex vertex, Map<String, Object> props);

    /**
     * Raised after a new edge is added.
     *
     * @param edge the edge that was added
     */
    public void edgeAdded(final Edge edge);

    /**
     * Raised after the property of a edge changed.
     *
     * @param edge     the edge that changed
     * @param key      the key of the property that changed
     * @param setValue the new value of the property
     */
    public void edgePropertyChanged(final Edge edge, final String key, final Object oldValue, final Object setValue);

    /**
     * Raised after an edge property was removed.
     *
     * @param edge         the edge that changed
     * @param key          the key that was removed
     * @param removedValue the value of the property that was removed
     */
    public void edgePropertyRemoved(final Edge edge, final String key, final Object removedValue);

    /**
     * Raised after an edge was removed from the graph.
     *
     * @param edge  the edge that was removed.
     * @param props the properties of the removed vertex
     */
    public void edgeRemoved(final Edge edge, Map<String, Object> props);
}
