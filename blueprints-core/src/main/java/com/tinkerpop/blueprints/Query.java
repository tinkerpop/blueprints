package com.tinkerpop.blueprints;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
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
     * Filter out the elements that do not have a property with specified key.
     *
     * @param key the key that the element must have
     * @return the modified query object
     */
    public Query has(final String key);

    /**
     * Filter out the elements that do have a property with specified key.
     *
     * @param key the key that the element must not have
     * @return the modified query object
     */
    public Query hasNot(final String key);

    /**
     * Filter out the element if it does not have a property with the specified value.
     * If multiple values are provided then at least one has to match the element value.
     *
     * @param key    the key of the property
     * @param values the values to check against
     * @return the modified query object
     */
    public Query has(final String key, final Object... values);

    /**
     * Filter out the element if it does not have a property with a comparable value.
     *
     * @param key     the key of the property
     * @param value   the value to check against
     * @param compare the comparator to use for comparison
     * @return the modified query object
     */
    @Deprecated
    public <T extends Comparable<T>> Query has(final String key, final T value, final Compare compare);

    /**
     * Filter out the element if it does not have a property with a comparable value.
     *
     * @param key     the key of the property
     * @param compare the comparator to use for comparison
     * @param values  the values to check against
     * @return the modified query object
     */
    public <T extends Comparable<T>> Query has(final String key, final Compare compare, final T... values);

    /**
     * Filter out the element of its property value is not within the provided interval.
     *
     * @param key        the key of the property
     * @param startValue the inclusive start value of the interval
     * @param endValue   the exclusive end value of the interval
     * @return the modified query object
     */
    public <T extends Comparable<T>> Query interval(final String key, final T startValue, final T endValue);

    /**
     * Filter out the element if the total number of incident/adjacent elements to retrieve has already been reached.
     *
     * @param total the total number of elements to return
     * @return the modified query object
     */
    public Query limit(final long total);

    /**
     * Filter out elements not within the skip/limit range specified.
     *
     * @param skip  the number of elements to skip since the first element
     * @param total the number of elements to return after the skip has been reached
     * @return the modified query object
     */
    public Query limit(final long skip, final long total);

    /**
     * Execute the query and return the matching edges.
     *
     * @return the unfiltered incident edges
     */
    public Iterable<Edge> edges();

    /**
     * Execute the query and return the vertices on the other end of the matching edges.
     *
     * @return the unfiltered adjacent vertices
     */
    public Iterable<Vertex> vertices();


}
