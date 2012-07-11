package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * For those graph engines that do not support the low-level querying of the edges of a vertex, then DefaultQuery can be used.
 * DefaultQuery assumes, at minimum, that Vertex.getOutEdges() and Vertex.getInEdges() is implemented by the respective Graph.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class DefaultQuery implements Query {

    private static final String[] EMPTY_LABELS = new String[]{};

    private final Vertex vertex;
    public Direction direction = Direction.BOTH;
    public String[] labels = EMPTY_LABELS;
    public long limit = Long.MAX_VALUE;
    public List<HasContainer> hasContainers = new ArrayList<HasContainer>();

    public DefaultQuery(final Vertex vertex) {
        this.vertex = vertex;
    }

    public Query has(final String key, final Object value) {
        this.hasContainers.add(new HasContainer(key, value, Compare.EQUAL));
        return this;
    }

    public <T extends Comparable<T>> Query has(final String key, final T value, final Compare compare) {
        this.hasContainers.add(new HasContainer(key, value, compare));
        return this;
    }

    public <T extends Comparable<T>> Query interval(final String key, final T startValue, final T endValue) {
        this.hasContainers.add(new HasContainer(key, startValue, Compare.GREATER_THAN_EQUAL));
        this.hasContainers.add(new HasContainer(key, endValue, Compare.LESS_THAN));
        return this;
    }

    public Query direction(final Direction direction) {
        this.direction = direction;
        return this;
    }

    public Query labels(final String... labels) {
        this.labels = labels;
        return this;
    }

    public Query limit(final long max) {
        this.limit = max;
        return this;
    }

    public Iterable<Edge> edges() {
        return new DefaultQueryIterable<Edge>(false);
    }

    public Iterable<Vertex> vertices() {
        return new DefaultQueryIterable<Vertex>(true);
    }

    public long count() {
        long count = 0;
        for (final Edge edge : this.edges()) {
            count++;
        }
        return count;
    }

    public Object vertexIds() {
        final List<Object> list = new ArrayList<Object>();
        for (final Vertex vertex : this.vertices()) {
            list.add(vertex.getId());
        }
        return list;
    }

    private class HasContainer {
        public String key;
        public Object value;
        public Compare compare;

        public HasContainer(final String key, final Object value, final Compare compare) {
            this.key = key;
            this.value = value;
            this.compare = compare;
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

    private class DefaultQueryIterable<T extends Element> implements Iterable<T> {

        private Iterable<Edge> iterable;
        private boolean forVertex;

        public DefaultQueryIterable(final boolean forVertex) {
            this.forVertex = forVertex;
            this.iterable = vertex.getEdges(direction, labels);
        }

        public Iterator<T> iterator() {
            return new Iterator<T>() {
                Edge nextEdge = null;
                final Iterator<Edge> itty = iterable.iterator();
                long count = 0;

                public boolean hasNext() {
                    if (null != this.nextEdge) {
                        return true;
                    } else {
                        return this.loadNext();
                    }
                }

                public T next() {
                    while (true) {
                        if (this.nextEdge != null) {
                            final Edge temp = this.nextEdge;
                            this.nextEdge = null;
                            if (forVertex) {
                                if (direction == Direction.OUT)
                                    return (T) temp.getVertex(Direction.IN);
                                else if (direction == Direction.IN)
                                    return (T) temp.getVertex(Direction.OUT);
                                else {
                                    if (temp.getVertex(Direction.OUT).equals(vertex)) {
                                        return (T) temp.getVertex(Direction.IN);
                                    } else {
                                        return (T) temp.getVertex(Direction.OUT);
                                    }
                                }

                            } else {
                                return (T) temp;
                            }
                        }

                        if (!this.loadNext())
                            throw new NoSuchElementException();
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                private boolean loadNext() {
                    this.nextEdge = null;
                    if (count >= limit) return false;
                    while (this.itty.hasNext()) {
                        final Edge edge = this.itty.next();
                        boolean filter = false;
                        for (final HasContainer hasContainer : hasContainers) {
                            if (!hasContainer.isLegal(edge)) {
                                filter = true;
                                break;
                            }
                        }
                        if (!filter) {
                            this.nextEdge = edge;
                            this.count++;
                            return true;
                        }
                    }
                    return false;
                }
            };
        }
    }
}
