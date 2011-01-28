package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;

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
        Vertex vertex = graph.addVertex(id);
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
        Edge edge = graph.addEdge(id, outVertex, inVertex, label);
        for (int i = 0; i < properties.length; i = i + 2) {
            edge.setProperty((String) properties[i], properties[i + 1]);
        }
        return edge;
    }
}
