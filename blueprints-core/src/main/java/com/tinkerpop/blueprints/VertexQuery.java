package com.tinkerpop.blueprints;

/**
 * A VertexQuery object defines a collection of filters and modifiers that are used to intelligently select edges from a vertex.
 *
 * @author Matthias Brocheler (http://matthiasb.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Luca Garulli (http://www.orientechnologies.com)
 * @author Daniel Kuppitz (daniel.kuppitz@shoproach.com)
 */
public interface VertexQuery extends Query {

    /**
     * The direction of the edges to retrieve.
     *
     * @param direction whether to retrieve the incoming, outgoing, or both directions
     * @return the modified query object
     */
    public VertexQuery direction(Direction direction);

    /**
     * Filter out the edge if its label is not in set of provided labels.
     *
     * @param labels the labels to check against
     * @return the modified query object
     */
    public VertexQuery labels(String... labels);

    /**
     * Execute the query and return the number of edges that are unfiltered.
     *
     * @return the number of unfiltered edges
     */
    public long count();

    /**
     * Return the raw ids of the vertices on the other end of the edges.
     *
     * @return the raw ids of the vertices on the other end of the edges
     */
    public Object vertexIds();

    @Override
    public VertexQuery has(String key);

    @Override
    public VertexQuery hasNot(String key);

    @Override
    public VertexQuery has(String key, Object value);

    @Override
    public VertexQuery hasNot(String key, Object value);

    @Override
    public VertexQuery has(String key, Predicate predicate, Object value);

    @Override
    @Deprecated
    public <T extends Comparable<T>> VertexQuery has(String key, T value, Compare compare);

    @Override
    public <T extends Comparable<?>> VertexQuery interval(String key, T startValue, T endValue);

    @Override
    public VertexQuery limit(int limit);

}
