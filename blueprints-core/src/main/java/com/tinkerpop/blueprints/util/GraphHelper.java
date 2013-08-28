package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GraphHelper {

    /**
     * Add a vertex to the graph with specified id and provided properties.
     *
     * @param graph      the graph to create a vertex in
     * @param id         the id of the vertex to create
     * @param properties the properties of the vertex to add (must be String,Object,String,Object,...)
     * @return the vertex created in the graph with the provided properties set
     */
    public static Vertex addVertex(final Graph graph, final Object id, final Object... properties) {
        if ((properties.length % 2) != 0)
            throw new RuntimeException("There must be an equal number of keys and values");
        final Vertex vertex = graph.addVertex(id);
        for (int i = 0; i < properties.length; i = i + 2) {
            vertex.setProperty((String) properties[i], properties[i + 1]);
        }
        return vertex;
    }

    /**
     * Add an edge to the graph with specified id and provided properties.
     *
     * @param graph      the graph to create the edge in
     * @param id         the id of the edge to create
     * @param outVertex  the outgoing/tail vertex of the edge
     * @param inVertex   the incoming/head vertex of the edge
     * @param label      the label of the edge
     * @param properties the properties of the edge to add (must be String,Object,String,Object,...)
     * @return the edge created in the graph with the provided properties set
     */
    public static Edge addEdge(final Graph graph, final Object id, final Vertex outVertex, final Vertex inVertex, final String label, final Object... properties) {
        if ((properties.length % 2) != 0)
            throw new RuntimeException("There must be an equal number of keys and values");
        final Edge edge = graph.addEdge(id, outVertex, inVertex, label);
        for (int i = 0; i < properties.length; i = i + 2) {
            edge.setProperty((String) properties[i], properties[i + 1]);
        }
        return edge;
    }

    /**
     * Copy the vertex/edges of one graph over to another graph.
     * The id of the elements in the from graph are attempted to be used in the to graph.
     * This method only works for graphs where the user can control the element ids.
     *
     * @param from the graph to copy from
     * @param to   the graph to copy to
     */
    public static void copyGraph(final Graph from, final Graph to) {
        for (final Vertex fromVertex : from.getVertices()) {
            final Vertex toVertex = to.addVertex(fromVertex.getId());
            ElementHelper.copyProperties(fromVertex, toVertex);
        }
        for (final Edge fromEdge : from.getEdges()) {
            final Vertex outVertex = to.getVertex(fromEdge.getVertex(Direction.OUT).getId());
            final Vertex inVertex = to.getVertex(fromEdge.getVertex(Direction.IN).getId());
            final Edge toEdge = to.addEdge(fromEdge.getId(), outVertex, inVertex, fromEdge.getLabel());
            ElementHelper.copyProperties(fromEdge, toEdge);
        }
    }


    /**
     * Add a vertex to a graph only if no other vertex in the provided Index is indexed by the property key/value pair.
     * If a vertex already exists with that key/value pair, return the pre-existing vertex.
     *
     * @param graph       the graph to add the vertex to
     * @param id          the id of the vertex to create (can be null)
     * @param uniqueKey   the key to check on for uniqueness of the vertex
     * @param uniqueValue the value to check on for uniqueness of the vertex
     * @return the newly created vertex or the vertex that satisfies the uniqueness criteria
     */
    public static Vertex addUniqueVertex(final Graph graph, final Object id, final String uniqueKey, final Object uniqueValue) {
        final Iterator<Vertex> results = graph.getVertices(uniqueKey, uniqueValue).iterator();
        if (results.hasNext()) {
            return results.next();
        } else {
            final Vertex vertex = graph.addVertex(id);
            vertex.setProperty(uniqueKey, uniqueValue);
            return vertex;
        }
    }

    /**
     * For those graphs that do no support automatic reindexing of elements when a key is provided for indexing, this method can be used to simulate that behavior.
     * The elements in the graph are iterated and their properties (for the provided keys) are removed and then added.
     * Be sure that the key indices have been created prior to calling this method so that they can pick up the property mutations calls.
     * Finally, if the graph is a TransactionalGraph, then a 1000 mutation buffer is used for each commit.
     *
     * @param graph    the graph containing the provided elements
     * @param elements the elements to index into the key indices
     * @param keys     the keys of the key indices
     * @return the number of element properties that were indexed
     */
    public static long reIndexElements(final Graph graph, final Iterable<? extends Element> elements, final Set<String> keys) {
        final boolean isTransactional = graph.getFeatures().supportsTransactions;
        long counter = 0;
        for (final Element element : elements) {
            for (final String key : keys) {
                final Object value = element.removeProperty(key);
                if (null != value) {
                    counter++;
                    element.setProperty(key, value);

                    if (isTransactional && (counter % 1000 == 0)) {
                        graph.commit();
                    }
                }
            }
        }
        return counter;
    }
}
