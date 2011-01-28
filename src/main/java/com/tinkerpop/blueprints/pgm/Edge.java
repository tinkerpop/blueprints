package com.tinkerpop.blueprints.pgm;

/**
 * An edge links two vertices. Along with its key/value properties, an edge has both a directionality and a label.
 * The directionality determines which vertex is the tail vertex (out vertex) and which vertex is the head vertex (in vertex).
 * The edge label determines the type of relationship that exists between the two vertices.
 * Diagrammatically, outVertex ---label---> inVertex.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Edge extends Element {

    /**
     * Return the vertex on the tail of the edge.
     *
     * @return the tail vertex
     */
    public Vertex getOutVertex();

    /**
     * Return the vertex on the head of the edge.
     *
     * @return the head vertex
     */
    public Vertex getInVertex();

    /**
     * Return the label associated with the edge.
     *
     * @return the edge label
     */
    public String getLabel();

    //public T getRawEdge();

}
