package com.tinkerpop.blueprints.pgm.impls;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Filter;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FilteredEdgeIterable implements Iterable<Edge> {

    private final Iterable<Edge> edgeIterable;
    private final List<String> labels;
    private Filter filter = null;

    public FilteredEdgeIterable(final Iterable<Edge> edgeIterable, final Object... filters) {
        this.edgeIterable = edgeIterable;
        this.labels = new LinkedList<String>();
        for (final Object filter : filters) {
            if (filter instanceof String) {
                labels.add((String) filter);
            } else if (filter instanceof Filter) {
                this.filter = (Filter) filter;
            } else {
                throw new IllegalArgumentException(Vertex.TYPE_ERROR_MESSAGE);

            }
        }
    }

    public FilteredEdgeIterable(final Iterable<Edge> edgeIterable, final List<String> labels, final Filter filter) {
        this.edgeIterable = edgeIterable;
        this.labels = labels;
        this.filter = filter;
    }

    public FilteredEdgeIterable(final Iterable<Edge> edgeIterable, final Filter filter) {
        this.edgeIterable = edgeIterable;
        this.labels = Collections.emptyList();
        this.filter = filter;
    }

    public FilteredEdgeIterable(final Iterable<Edge> edgeIterable, final List<String> labels) {
        this.edgeIterable = edgeIterable;
        this.labels = labels;
        this.filter = null;
    }

    public Iterator<Edge> iterator() {
        return new FilteredEdgeIterator(this.edgeIterable.iterator());
    }

    public static Filter getFilter(final Object... filters) {
        for (final Object filter : filters) {
            if (filter instanceof Filter) {
                return (Filter) filter;
            } else if (!(filter instanceof String))
                throw new IllegalArgumentException(Vertex.TYPE_ERROR_MESSAGE);
        }
        return null;
    }

    public static List<String> getLabels(final Object... filters) {
        List<String> list = new LinkedList<String>();
        for (final Object filter : filters) {
            if (filter instanceof String) {
                list.add((String) filter);
            } else if (!(filter instanceof Filter))
                throw new IllegalArgumentException(Vertex.TYPE_ERROR_MESSAGE);
        }
        return list;
    }

    private final class FilteredEdgeIterator implements Iterator<Edge> {

        private final Iterator<Edge> edgeIterator;
        private Edge nextEdge;


        public FilteredEdgeIterator(final Iterator<Edge> edgeIterator) {
            this.edgeIterator = edgeIterator;
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

        private boolean loadNext() {
            while (this.edgeIterator.hasNext()) {
                final Edge edge = this.edgeIterator.next();

                boolean keep = false;
                if (!labels.isEmpty()) {
                    final String edgeLabel = edge.getLabel();
                    for (final String label : labels) {
                        if (edgeLabel.equals(label)) {
                            keep = true;
                            break;
                        }
                    }
                } else {
                    keep = true;
                }

                if (!keep)
                    continue;


                if (null != filter)
                    keep = filter.isLegal(edge);
                else
                    keep = true;

                if (keep) {
                    this.nextEdge = edge;
                    return true;
                }
            }

            return false;
        }


    }


}

    
