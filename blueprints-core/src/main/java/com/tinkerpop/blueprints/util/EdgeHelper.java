package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class EdgeHelper {

    /**
     * An edge is relabeled by creating a new edge with the same properties, but new label.
     * Note that an edge is deleted and an edge is added.
     *
     * @param graph    the graph to add the new edge to
     * @param oldEdge  the existing edge to "relabel"
     * @param newId    the id of the new edge
     * @param newLabel the label of the new edge
     * @return the newly created edge
     */
    public static Edge relabelEdge(final Graph graph, final Edge oldEdge, final Object newId, final String newLabel) {
        final Vertex outVertex = oldEdge.getVertex(Direction.OUT);
        final Vertex inVertex = oldEdge.getVertex(Direction.IN);
        final Edge newEdge = graph.addEdge(newId, outVertex, inVertex, newLabel);
        ElementHelper.copyProperties(oldEdge, newEdge);
        graph.removeEdge(oldEdge);
        return newEdge;
    }

    /**
     * Edges are relabeled by creating new edges with the same properties, but new label.
     * Note that for each edge is deleted and an edge is added.
     *
     * @param graph    the graph to add the new edge to
     * @param oldEdges the existing edges to "relabel"
     * @param newLabel the label of the new edge
     */
    public static void relabelEdges(final Graph graph, final Iterable<Edge> oldEdges, final String newLabel) {
        for (final Edge oldEdge : oldEdges) {
            final Vertex outVertex = oldEdge.getVertex(Direction.OUT);
            final Vertex inVertex = oldEdge.getVertex(Direction.IN);
            final Edge newEdge = graph.addEdge(null, outVertex, inVertex, newLabel);
            ElementHelper.copyProperties(oldEdge, newEdge);
            graph.removeEdge(oldEdge);
        }
    }

    public static Vertex getOther(final Edge edge, final Vertex vertex) {
        final Vertex temp = edge.getVertex(Direction.IN);
        if (temp.equals(vertex))
            return edge.getVertex(Direction.OUT);
        else
            return temp;
    }
}
