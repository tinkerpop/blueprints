package com.tinkerpop.blueprints.pgm.impls;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Filter;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FilteredEdgeSequence implements Iterable<Edge>, Iterator<Edge> {

    private final Iterator<Edge> edgeIterator;
    private final Object[] filters;
    private Edge nextEdge;
    private boolean stringFilters = false;
    private boolean filterFilters = false;

    public FilteredEdgeSequence(final Iterator<Edge> edgeIterator, final Object... filters) {
        this.edgeIterator = edgeIterator;
        this.filters = filters;

        for (final Object filter : filters) {
            if (filter instanceof String) {
                this.stringFilters = true;
            } else if (filter instanceof Filter) {
                this.filterFilters = true;
            }
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return this.nextEdge != null || this.loadNext();
    }

    public Edge next() {
        if (this.nextEdge != null) {
            final Edge temp = this.nextEdge;
            this.nextEdge = null;
            return temp;
        } else {
            if (this.loadNext()) {
                final Edge temp = this.nextEdge;
                this.nextEdge = null;
                return temp;
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    public Iterator<Edge> iterator() {
        return this;
    }

    private boolean loadNext() {
        while (this.edgeIterator.hasNext()) {
            final Edge edge = this.edgeIterator.next();

            boolean keep = false;
            if (this.stringFilters) {
                boolean hasLabelString = false;
                final String label = edge.getLabel();
                for (final Object filter : this.filters) {
                    if (filter instanceof String) {
                        hasLabelString = true;
                        if (label.equals(filter))
                            keep = true;
                    }
                }
                if (hasLabelString && !keep) {
                    continue;
                }
            }

            keep = true;
            if (this.filterFilters) {
                for (final Object filter : this.filters) {
                    if (filter instanceof Filter) {
                        final Filter f = (Filter) filter;
                        final Object value = edge.getProperty(f.key);
                        keep = this.keepElement(f, value);
                        if (!keep)
                            break;
                    }
                }
            }

            if (keep) {
                this.nextEdge = edge;
                return true;
            }
        }
        return false;
    }

    protected boolean keepElement(final Filter f, final Object value) {
        switch (f.compare) {
            case EQUAL:
                if (null == value)
                    return f.value == null;
                return value.equals(f.value);
            case NOT_EQUAL:
                if (null == value)
                    return f.value != null;
                return !value.equals(f.value);
            case GREATER_THAN:
                if (null == value || f.value == null)
                    return false;
                return ((Comparable) value).compareTo(f.value) == 1;
            case LESS_THAN:
                if (null == value || f.value == null)
                    return false;
                return ((Comparable) value).compareTo(f.value) == -1;
            case GREATER_THAN_EQUAL:
                if (null == value || f.value == null)
                    return false;
                return ((Comparable) value).compareTo(f.value) >= 0;
            case LESS_THAN_EQUAL:
                if (null == value || f.value == null)
                    return false;
                return ((Comparable) value).compareTo(f.value) <= 0;
            default:
                throw new IllegalArgumentException("Invalid state as no valid filter was provided");
        }
    }
}

    
