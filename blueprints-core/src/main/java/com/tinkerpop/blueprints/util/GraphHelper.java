package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

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
}
