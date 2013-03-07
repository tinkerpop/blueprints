package com.tinkerpop.blueprints;

/**
 * A VertexQuery object defines a collection of filters and modifiers that are used to intelligently select edges from a vertex.
 *
 * @author Matthias Brocheler (http://matthiasb.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface VertexQuery extends Query {


    /**
     * The direction of the edges to retrieve.
     *
     * @param direction whether to retrieve the incoming, outgoing, or both directions
     * @return the modified query object
     */
    public VertexQuery direction(final Direction direction);

    /**
     * Filter out the edge if its label is not in set of provided labels.
     *
     * @param labels the labels to check against
     * @return the modified query object
     */
    public VertexQuery labels(final String... labels);

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
    public VertexQuery has(final String key, final Object value);

    @Override
    public <T extends Comparable<T>> VertexQuery has(final String key, final T value, final Compare compare);

    @Override
    public <T extends Comparable<T>> VertexQuery interval(final String key, final T startValue, final T endValue);

    @Override
    public VertexQuery limit(final long max);


}
