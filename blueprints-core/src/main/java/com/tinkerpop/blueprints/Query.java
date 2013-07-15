package com.tinkerpop.blueprints;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Luca Garulli (http://www.orientechnologies.com)
 * @author Daniel Kuppitz (daniel.kuppitz@shoproach.com)
 */

public interface Query {

    /**
     * Filter out elements that do not have a property with provided key.
     *
     * @param key the key of the property
     * @return the modified query object
     */
    public Query has(String key);

    /**
     * Filter out elements that have a property with provided key.
     *
     * @param key the key of the property
     * @return the modified query object
     */
    public Query hasNot(String key);

    /**
     * Filter out elements that do not have a property value equal to provided value.
     *
     * @param key   the key of the property
     * @param value the value to check against
     * @return the modified query object
     */
    public Query has(String key, Object value);

    /**
     * Filter out elements that have a property value equal to provided value.
     *
     * @param key   the key of the property
     * @param value the value to check against
     * @return the modified query object
     */
    public Query hasNot(String key, Object value);

    /**
     * Filter out the element if it does not have a property with a comparable value.
     *
     * @param key     the key of the property
     * @param predicate the comparator to use for comparison
     * @param value  the value to check against
     * @return the modified query object
     */
    public Query has(String key, Predicate predicate, Object value);

    /**
     * Filter out the element if it does not have a property with a comparable value.
     *
     * @param key     the key of the property
     * @param value   the value to check against
     * @param compare the comparator to use for comparison
     * @return the modified query object
     */
    @Deprecated
    public <T extends Comparable<T>> Query has(String key, T value, Compare compare);

    /**
     * Filter out the element of its property value is not within the provided interval.
     *
     * @param key        the key of the property
     * @param startValue the inclusive start value of the interval
     * @param endValue   the exclusive end value of the interval
     * @return the modified query object
     */
    public <T extends Comparable<?>> Query interval(String key, T startValue, T endValue);

    /**
     * Filter out the element if the take number of incident/adjacent elements to retrieve has already been reached.
     *
     * @param limit the take number of elements to return
     * @return the modified query object
     */
    public Query limit(int limit);

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

    //////////////////////////////////////////////////////////////////////////////////////

    @Deprecated
    public enum Compare implements Predicate {
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
            else if (this.equals(LESS_THAN_EQUAL))
                return GREATER_THAN;
            else
                throw new RuntimeException("Comparator does not have an opposite.");
        }

        public boolean evaluate(final Object first, final Object second) {
            switch (this) {
                case EQUAL:
                    if (null == first)
                        return second == null;
                    return first.equals(second);
                case NOT_EQUAL:
                    if (null == first)
                        return second != null;
                    return !first.equals(second);
                case GREATER_THAN:
                    if (null == first || second == null)
                        return false;
                    return ((Comparable) first).compareTo(second) >= 1;
                case LESS_THAN:
                    if (null == first || second == null)
                        return false;
                    return ((Comparable) first).compareTo(second) <= -1;
                case GREATER_THAN_EQUAL:
                    if (null == first || second == null)
                        return false;
                    return ((Comparable) first).compareTo(second) >= 0;
                case LESS_THAN_EQUAL:
                    if (null == first || second == null)
                        return false;
                    return ((Comparable) first).compareTo(second) <= 0;
                default:
                    throw new IllegalArgumentException("Invalid state as no valid filter was provided");
            }
        }
    }
}
