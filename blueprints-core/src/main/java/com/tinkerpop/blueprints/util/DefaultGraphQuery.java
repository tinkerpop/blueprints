package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * For those graph engines that do not support the low-level querying of the vertices or edges, then DefaultGraphQuery can be used.
 * DefaultGraphQuery assumes, at minimum, that Graph.getVertices() and Graph.getEdges() is implemented by the respective Graph.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class DefaultGraphQuery extends DefaultQuery implements GraphQuery {

    protected final Graph graph;

    public DefaultGraphQuery(final Graph graph) {
        this.graph = graph;
    }

    public GraphQuery has(final String key) {
        super.has(key);
        return this;
    }

    public GraphQuery hasNot(final String key) {
        super.hasNot(key);
        return this;
    }

    public GraphQuery has(final String key, final Object value) {
        super.has(key, value);
        return this;
    }

    public GraphQuery hasNot(final String key, final Object value) {
        super.hasNot(key, value);
        return this;
    }

    public GraphQuery has(final String key, final Predicate predicate, final Object value) {
        super.has(key, predicate, value);
        return this;
    }

    public <T extends Comparable<T>> GraphQuery has(final String key, final T value, final Compare compare) {
        super.has(key, compare, value);
        return this;
    }

    public <T extends Comparable<?>> GraphQuery interval(final String key, final T startValue, final T endValue) {
        super.interval(key, startValue, endValue);
        return this;
    }

    public GraphQuery limit(final int limit) {
        super.limit(limit);
        return this;
    }

    public Iterable<Edge> edges() {
        return new DefaultGraphQueryIterable<Edge>() {
            protected Set<String> getIndexedKeys() {
                return ((KeyIndexableGraph) graph).getIndexedKeys(Edge.class);
            }

            protected Iterable<Edge> getAllElements() {
                return graph.getEdges();
            }

            protected Iterable<Edge> getElementsFromContainer(HasContainer container) {
                return graph.getEdges(container.key, container.value);
            }
        };
    }

    public Iterable<Vertex> vertices() {
        return new DefaultGraphQueryIterable<Vertex>() {
            protected Set<String> getIndexedKeys() {
                return ((KeyIndexableGraph) graph).getIndexedKeys(Vertex.class);
            }

            protected Iterable<Vertex> getAllElements() {
                return graph.getVertices();
            }

            protected Iterable<Vertex> getElementsFromContainer(HasContainer container) {
                return graph.getVertices(container.key, container.value);
            }
        };
    }

    protected abstract class DefaultGraphQueryIterable<T extends Element> implements Iterable<T> {

        private Iterable<T> iterable = null;

        public DefaultGraphQueryIterable() {
            this.iterable = getElementIterable();
        }

        public Iterator<T> iterator() {
            return new Iterator<T>() {
                T nextElement = null;
                final Iterator<T> itty = iterable.iterator();
                long count = 0;

                public boolean hasNext() {
                    if (null != this.nextElement) {
                        return true;
                    } else {
                        return this.loadNext();
                    }
                }

                public T next() {
                    while (true) {
                        if (this.nextElement != null) {
                            final T temp = this.nextElement;
                            this.nextElement = null;
                            return temp;
                        }

                        if (!this.loadNext())
                            throw new NoSuchElementException();
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                private boolean loadNext() {
                    this.nextElement = null;
                    if (this.count > limit) return false;
                    while (this.itty.hasNext()) {
                        final T element = this.itty.next();
                        boolean filter = false;

                        for (final HasContainer hasContainer : hasContainers) {
                            if (!hasContainer.isLegal(element)) {
                                filter = true;
                                break;
                            }
                        }

                        if (!filter) {
                            if (++this.count <= limit) {
                                this.nextElement = element;
                                return true;
                            }
                        }

                    }
                    return false;
                }
            };
        }

        private Iterable<T> getElementIterable() {
            if (graph instanceof KeyIndexableGraph) {
                final Set<String> keys = getIndexedKeys();
                HasContainer container = null;
                for (final HasContainer hasContainer : hasContainers) {
                    if (hasContainer.predicate.equals(com.tinkerpop.blueprints.Compare.EQUAL) && keys.contains(hasContainer.key)) {
                        container = hasContainer;
                        break;
                    }
                }
                if (container != null) {
                    return getElementsFromContainer(container);
                }
            }

            return getAllElements();
        }

        protected abstract Set<String> getIndexedKeys();

        protected abstract Iterable<T> getAllElements();

        protected abstract Iterable<T> getElementsFromContainer(HasContainer container);
    }
}
