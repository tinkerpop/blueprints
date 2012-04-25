package com.tinkerpop.blueprints.pgm;

import java.util.LinkedList;
import java.util.List;

/**
 * A Filter is used to constrain what adjacent edges of a vertex are retrieved from the underlying graph storage engine.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Filter {

    public enum Compare {EQUAL, NOT_EQUAL, GREATER_THAN, GREATER_THAN_EQUAL, LESS_THAN, LESS_THAN_EQUAL}

    public List<Filter> filters = new LinkedList<Filter>();

    /**
     * Add a property filter to the filter chain.
     *
     * @param key   the key of the property the check
     * @param value the value that should be equal to the retrieved property value
     * @return the filter chain with new property filter concatenated onto it
     */
    public Filter property(final String key, final Object value) {
        filters.add(new PropertyFilter(key, value));
        return this;
    }

    /**
     * Add a property filter to the filter chain.
     *
     * @param key     the key of the property the check
     * @param value   the value that should be compared to the retrieved property value
     * @param compare the comparator used to determine filtering
     * @return the filter chain with new property filter concatenated onto it
     */
    public Filter property(final String key, final Object value, final Compare compare) {
        filters.add(new PropertyFilter(key, value, compare));
        return this;
    }

    /**
     * Add a range filter to the filter chain (which is decomposed into two property filters)
     * The range is startValue >= value < endValue.
     *
     * @param key        the key of the property to check
     * @param startValue the low end of the range
     * @param endValue   the high end of the range
     * @return the filter chain with new range filter concatenated onto it
     */
    public Filter range(final String key, final Object startValue, final Object endValue) {
        filters.add(new PropertyFilter(key, startValue, Compare.GREATER_THAN_EQUAL));
        filters.add(new PropertyFilter(key, endValue, Compare.LESS_THAN));
        return this;
    }

    /**
     * A label filter is used to determine if a provided edge has a label contained in the provided labels.
     *
     * @param labels the labels to check against
     * @return the filter chain with new label filter concatenated onto it
     */
    public Filter label(final String... labels) {
        filters.add(new LabelFilter(labels));
        return this;
    }

    /**
     * A label filter is used to determine if a provided edge has a label contained (or not contained) in the provided labels.
     *
     * @param compare whether the check is inclusive or exclusive of the provided labels
     * @param labels  the labels to check against
     * @return the filter chain with new label filter concatenated onto it
     */
    public Filter label(final Compare compare, final String... labels) {
        filters.add(new LabelFilter(compare, labels));
        return this;
    }

    /**
     * Useful for when the underlying graph engine does not support filtering natively.
     * This method can be used to filter the element according to the constructed filters.
     *
     * @param element the element to be checked.
     * @return whether the element should exist or not.
     */
    public boolean isLegal(final Element element) {
        for (final Filter filter : this.filters) {
            if (!filter.isLegal(element))
                return false;
        }
        return true;
    }


    public class LabelFilter extends Filter {

        public String[] labels;
        public Compare compare;

        public LabelFilter(final Compare compare, final String... labels) {
            if (compare != Compare.EQUAL && compare != Compare.NOT_EQUAL) {
                throw new IllegalArgumentException("Only comparators of EQUAL and NOT_EQUAL are allowed: " + compare);
            }
            this.compare = compare;
            this.labels = labels;
        }

        public LabelFilter(final String... labels) {
            this(Compare.EQUAL, labels);
        }

        public boolean isLegal(final Element edge) {
            final String edgeLabel = ((Edge) edge).getLabel();
            if (compare == Compare.EQUAL) {
                for (final String label : this.labels) {
                    if (label.equals(edgeLabel))
                        return true;
                }
                return false;
            } else if (compare == Compare.NOT_EQUAL) {
                for (final String label : this.labels) {
                    if (label.equals(edgeLabel))
                        return false;
                }
                return true;
            } else {
                throw new IllegalArgumentException("Invalid state as no valid filter was provided");
            }
        }
    }

    public class PropertyFilter extends Filter {

        public final String key;
        public final Object value;
        public final Compare compare;

        public PropertyFilter(final String key, final Object value, final Compare compare) {
            this.key = key;
            this.value = value;
            this.compare = compare;
        }

        public PropertyFilter(final String key, final Object value) {
            this(key, value, Compare.EQUAL);
        }

        public boolean isLegal(final Element element) {
            final Object elementValue = element.getProperty(key);

            switch (compare) {
                case EQUAL:
                    if (null == elementValue)
                        return value == null;
                    return elementValue.equals(value);
                case NOT_EQUAL:
                    if (null == elementValue)
                        return value != null;
                    return !elementValue.equals(value);
                case GREATER_THAN:
                    if (null == elementValue || value == null)
                        return false;
                    return ((Comparable) elementValue).compareTo(value) >= 1;
                case LESS_THAN:
                    if (null == elementValue || value == null)
                        return false;
                    return ((Comparable) elementValue).compareTo(value) <= -1;
                case GREATER_THAN_EQUAL:
                    if (null == elementValue || value == null)
                        return false;
                    return ((Comparable) elementValue).compareTo(value) >= 0;
                case LESS_THAN_EQUAL:
                    if (null == elementValue || value == null)
                        return false;
                    return ((Comparable) elementValue).compareTo(value) <= 0;
                default:
                    throw new IllegalArgumentException("Invalid state as no valid filter was provided");
            }
        }

    }
}
