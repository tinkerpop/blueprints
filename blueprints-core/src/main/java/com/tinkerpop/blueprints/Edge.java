package com.tinkerpop.blueprints;

/**
 * An Edge links two vertices. Along with its key/value properties, an edge has both a directionality and a label.
 * The directionality determines which vertex is the tail vertex (out vertex) and which vertex is the head vertex (in vertex).
 * The edge label determines the type of relationship that exists between the two vertices.
 * Diagrammatically, outVertex ---label---> inVertex.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Edge extends Element {

    /**
     * Return the tail/in or head/out vertex.
     *
     * @param direction whether to return the tail/in or head/out vertex
     * @return the tail/in or head/out vertex
     * @throws IllegalArgumentException if a direction of both is provided, throw an IllegalArgumentException
     */
    public Vertex getVertex(Direction direction) throws IllegalArgumentException;

    /**
     * Return the label associated with the edge.
     *
     * @return the edge label
     */
    public String getLabel();
}
