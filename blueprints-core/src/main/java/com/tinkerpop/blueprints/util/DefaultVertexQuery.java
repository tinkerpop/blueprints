package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * For those graph engines that do not support the low-level querying of the edges of a vertex, then DefaultVertexQuery can be used.
 * DefaultVertexQuery assumes, at minimum, that Vertex.getOutEdges() and Vertex.getInEdges() is implemented by the respective Vertex.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class DefaultVertexQuery extends DefaultQuery implements VertexQuery {

    protected final Vertex vertex;

    public DefaultVertexQuery(final Vertex vertex) {
        this.vertex = vertex;
    }

    public VertexQuery has(final String key) {
        super.has(key);
        return this;
    }

    public VertexQuery hasNot(final String key) {
        super.hasNot(key);
        return this;
    }

    public VertexQuery has(final String key, final Object value) {
        super.has(key, value);
        return this;
    }

    public VertexQuery hasNot(final String key, final Object value) {
        super.hasNot(key, value);
        return this;
    }

    public VertexQuery has(final String key, final Predicate predicate, final Object value) {
        super.has(key, predicate, value);
        return this;
    }

    public <T extends Comparable<T>> VertexQuery has(final String key, final T value, final Compare compare) {
        super.has(key, compare, value);
        return this;
    }

    public <T extends Comparable<?>> VertexQuery interval(final String key, final T startValue, final T endValue) {
        super.interval(key, startValue, endValue);
        return this;
    }

    public VertexQuery limit(final int limit) {
        super.limit(limit);
        return this;
    }

    public VertexQuery direction(final Direction direction) {
        this.direction = direction;
        return this;
    }

    public VertexQuery labels(final String... labels) {
        this.labels = labels;
        return this;
    }

    public Iterable<Edge> edges() {
        return new DefaultVertexQueryIterable<Edge>(false);
    }

    public Iterable<Vertex> vertices() {
        return new DefaultVertexQueryIterable<Vertex>(true);
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

    private class DefaultVertexQueryIterable<T extends Element> implements Iterable<T> {

        private Iterable<Edge> iterable;
        private boolean forVertex;

        public DefaultVertexQueryIterable(final boolean forVertex) {
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
                    if (this.count > limit) return false;

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
                            if (++this.count <= limit) {
                                this.nextEdge = edge;
                                return true;
                            }
                        }
                    }
                    return false;
                }
            };
        }
    }
}
