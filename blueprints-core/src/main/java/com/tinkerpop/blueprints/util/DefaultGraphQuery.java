package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class DefaultGraphQuery extends DefaultQuery implements GraphQuery {

    private final Graph graph;

    public DefaultGraphQuery(final Graph graph) {
        this.graph = graph;
    }

    public GraphQuery has(final String key, final Object value) {
        this.hasContainers.add(new HasContainer(key, value, Compare.EQUAL));
        return this;
    }

    public <T extends Comparable<T>> GraphQuery has(final String key, final T value, final Compare compare) {
        this.hasContainers.add(new HasContainer(key, value, compare));
        return this;
    }

    public <T extends Comparable<T>> GraphQuery interval(final String key, final T startValue, final T endValue) {
        this.hasContainers.add(new HasContainer(key, startValue, Compare.GREATER_THAN_EQUAL));
        this.hasContainers.add(new HasContainer(key, endValue, Compare.LESS_THAN));
        return this;
    }

    public GraphQuery limit(final long max) {
        this.limit = max;
        return this;
    }

    public Iterable<Edge> edges() {
        return new DefaultGraphQueryIterable<Edge>(false);
    }

    public Iterable<Vertex> vertices() {
        return new DefaultGraphQueryIterable<Vertex>(true);
    }

    private class DefaultGraphQueryIterable<T extends Element> implements Iterable<T> {

        private Iterable<T> iterable = null;

        public DefaultGraphQueryIterable(final boolean forVertex) {
            for (final HasContainer hasContainer : hasContainers) {
                if (hasContainer.compare.equals(Compare.EQUAL)) {
                    if (forVertex)
                        this.iterable = (Iterable<T>) graph.getVertices(hasContainer.key, hasContainer.value);
                    else
                        this.iterable = (Iterable<T>) graph.getEdges(hasContainer.key, hasContainer.value);
                }
            }
            if (null == this.iterable) {
                if (forVertex)
                    this.iterable = (Iterable<T>) graph.getVertices();
                else
                    this.iterable = (Iterable<T>) graph.getEdges();
            }
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
                    if (count >= limit) return false;
                    while (this.itty.hasNext()) {
                        final T element = this.itty.next();
                        boolean filter = false;

                        /*if (!forVertex) {
                            filter = !containsLabel(((Edge) element).getLabel(), labels);
                        }*/

                        //if (!filter) {
                        for (final HasContainer hasContainer : hasContainers) {
                            if (!hasContainer.isLegal(element)) {
                                filter = true;
                                break;
                            }
                        }
                        //}
                        if (!filter) {
                            this.nextElement = element;
                            this.count++;
                            return true;
                        }
                    }
                    return false;
                }
            };
        }

        /*private boolean containsLabel(final String label, final String[] labels) {
            if (labels.length == 0)
                return true;

            for (final String temp : labels) {
                if (temp.equals(label))
                    return true;
            }
            return false;
        }*/
    }

}
