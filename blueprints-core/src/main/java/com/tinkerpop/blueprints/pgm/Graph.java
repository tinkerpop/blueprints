package com.tinkerpop.blueprints.pgm;

/**
 * A graph is a container object for a collection of vertices and a collection edges.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Graph {

    /**
     * Create a new vertex, add it to the graph, and return the newly created vertex.
     * The provided object identifier is a recommendation for the identifier to use.
     * It is not required that the implementation use this identifier.
     * If the object identifier is already being used by the graph to reference a vertex,
     * then that reference vertex is returned and no vertex is created.
     * If the identifier is a vertex (perhaps from another graph),
     * then the vertex is duplicated for this graph. Thus, a vertex can not be an identifier.
     *
     * @param id the recommended object identifier
     * @return the newly created vertex or the vertex already referenced by the provided identifier.
     */
    public Vertex addVertex(Object id);

    /**
     * Return the vertex referenced by the provided object identifier.
     * If no vertex is referenced by that identifier, then return null.
     *
     * @param id the identifier of the vertex to retrieved from the graph
     * @return the vertex referenced by the provided identifier or null when no such vertex exists
     */
    public Vertex getVertex(Object id);

    /**
     * Remove the provided vertex from the graph.
     * Upon removing the vertex, all the edges by which the vertex is connected will be removed as well.
     *
     * @param vertex the vertex to remove from the graph
     */
    public void removeVertex(Vertex vertex);

    /**
     * Return an iterable reference to all the vertices in the graph. Thus, what is returned can be subjected to the foreach construct.
     * If this is not possible for the implementation, then an UnsupportedOperationException can be thrown.
     *
     * @return an iterable reference to all vertices in the graph
     */
    public Iterable<Vertex> getVertices();

    /**
     * Add an edge to the graph. The added edges requires a recommended identifier, a tail vertex, an head vertex, and a label.
     * Like adding a vertex, the provided object identifier is can be ignored by the implementation.
     *
     * @param id        the recommended object identifier
     * @param outVertex the vertex on the tail of the edge
     * @param inVertex  the vertex on the head of the edge
     * @param label     the label associated with the edge
     * @return the newly created edge
     */
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label);

    /**
     * Return the edge referenced by the provided object identifier.
     * If no edge is referenced by that identifier, then return null.
     *
     * @param id the identifier of the edge to retrieved from the graph
     * @return the edge referenced by the provided identifier or null when no such edge exists
     */
    public Edge getEdge(Object id);

    /**
     * Remove the provided edge from the graph.
     *
     * @param edge the edge to remove from the graph
     */
    public void removeEdge(Edge edge);

    /**
     * Return an iterable reference to all the edges in the graph. Thus, what is returned can be subjected to the foreach construct.
     * If this is not possible for the implementation, then an UnsupportedOperationException can be thrown.
     *
     * @return an iterable reference to all edges in the graph
     */
    public Iterable<Edge> getEdges();

    /**
     * Remove all the edges and vertices from the graph.
     */
    public void clear();

    /**
     * A shutdown function is required to properly close the graph.
     * This is important for implementations that utilize disk-based serializations.
     */
    public void shutdown();

    //public Object getRawGraph();
}
