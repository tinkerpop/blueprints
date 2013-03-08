package com.tinkerpop.blueprints;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public interface Query {

    public enum Compare {
        EQUAL, NOT_EQUAL, GREATER_THAN, GREATER_THAN_EQUAL, LESS_THAN, LESS_THAN_EQUAL;

        public Compare opposite() {
            if (this.equals(EQUAL))
                return NOT_EQUAL;
            else if (this.equals(NOT_EQUAL))
                return EQUAL;
            else if (this.equals(GREATER_THAN))
                return LESS_THAN_EQUAL;
            else if (this.equals(GREATER_THAN_EQUAL))
                return LESS_THAN;
            else if (this.equals(LESS_THAN))
                return GREATER_THAN_EQUAL;
            else
                return GREATER_THAN;
        }

    }

    /**
     * Filter out the edge if it does not have a property with the specified value.
     *
     * @param key   the key of the property
     * @param value the value to check against
     * @return the modified query object
     */
    public Query has(final String key, final Object value);

    /**
     * Filter out the edge if it does not have a property with a comparable value.
     *
     * @param key     the key of the property
     * @param value   the value to check against
     * @param compare the comparator to use for comparison
     * @return the modified query object
     */
    public <T extends Comparable<T>> Query has(final String key, final T value, final Compare compare);

    /**
     * Filter out the edge of its property value is not within the provided interval.
     *
     * @param key        the key of the property
     * @param startValue the inclusive start value of the interval
     * @param endValue   the exclusive end value of the interval
     * @return the modified query object
     */
    public <T extends Comparable<T>> Query interval(final String key, final T startValue, final T endValue);


    /**
     * Execute the query and return the matching edges.
     *
     * @return the unfiltered edges
     */
    public Iterable<Edge> edges();

    /**
     * Execute the query and return the vertices on the other end of the matching edges.
     *
     * @return the unfiltered edge's vertices
     */
    public Iterable<Vertex> vertices();


    /**
     * Filter out the edge if the max number of edges to retrieve has already been reached.
     *
     * @param max the max number of edges to return
     * @return the modified query object
     */
    public Query limit(final long max);


}
