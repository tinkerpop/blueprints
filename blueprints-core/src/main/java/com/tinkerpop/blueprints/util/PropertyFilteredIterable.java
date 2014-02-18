package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Compare;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Predicate;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This is a helper class for filtering an iterable of elements by their key/value.
 * Useful for graph implementations that do no support automatic key indices and need to filter on Graph.getVertices/Edges(key,value).
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PropertyFilteredIterable<T extends Element> implements CloseableIterable<T> {

    private final Iterable<T> iterable;
    private final HasContainer hasContainer;

    public PropertyFilteredIterable(final String key, final Object value, final Iterable<T> iterable) {
        this.iterable = iterable;
        this.hasContainer = new HasContainer(key, Compare.EQUAL, value);
    }

    public void close() {
        if (this.iterable instanceof CloseableIterable) {
            ((CloseableIterable) this.iterable).close();
        }
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Iterator<T> itty = iterable.iterator();
            private T nextElement = null;

            public void remove() {
                this.itty.remove();
            }

            public boolean hasNext() {
                if (null != nextElement)
                    return true;
                else {
                    try {
                        while (true) {
                            final T element = this.itty.next();
                            if (hasContainer.isLegal(element)) {
                                this.nextElement = element;
                                return true;
                            }
                        }
                    } catch (NoSuchElementException e) {
                        this.nextElement = null;
                        return false;
                    }
                }
            }

            public T next() {
                if (null != this.nextElement) {
                    final T temp = this.nextElement;
                    this.nextElement = null;
                    return temp;
                } else {
                    while (true) {
                        final T element = this.itty.next();
                        if (hasContainer.isLegal(element))
                            return element;

                    }
                }
            }
        };
    }

    protected class HasContainer {
        public String key;
        public Object value;
        public Predicate predicate;

        public HasContainer(final String key, final Predicate predicate, final Object value) {
            this.key = key;
            this.value = value;
            this.predicate = predicate;
        }

        public boolean isLegal(final Element element) {
            if (this.key.equals(StringFactory.ID)) {
                return this.predicate.evaluate(element.getId(), this.value);
            } else if (this.key.equals(StringFactory.LABEL) && element instanceof Edge) {
                return this.predicate.evaluate(((Edge) element).getLabel(), this.value);
            } else {
                return this.predicate.evaluate(element.getProperty(this.key), this.value);
            }
        }
    }
}
