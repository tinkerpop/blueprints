package com.tinkerpop.blueprints.pgm;

/**
 * A vertex maintains pointers to both a set of incoming and outgoing edges.
 * The outgoing edges are those edges for which the vertex is the tail.
 * The incoming edges are those edges for which the vertex is the head.
 * Diagrammatically, ---inEdges---> vertex ---outEdges--->.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Vertex extends Element {

    public static final String TYPE_ERROR_MESSAGE = "The provided Object[] can only be of type String of Filter";

    /**
     * The edges emanating from, or leaving, the vertex.
     * The provided filter objects can either be String or Filter.
     * If String, then the edge label must equal the provided String.
     * If Filter, then the edge must meet the Filter criteria.
     *
     * @param filters the filters of the edges to return
     * @return the edges for which the vertex is the tail
     */
    public Iterable<Edge> getOutEdges(Object... filters);

    /**
     * The edges incoming to, or arriving at, the vertex.
     * The provided filter objects can either be String or Filter.
     * If String, then the edge label must equal the provided String.
     * If Filter, then the edge must meet the Filter criteria.
     *
     * @param filters the labels of the edges to return
     * @return the edges for which the vertex is the head
     */
    public Iterable<Edge> getInEdges(Object... filters);

    //public T getRawVertex();
}
