package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Element;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This is a helper class for filtering an iterable of elements by their key/value.
 * Useful for graph implementations that do no support automatic key indices and need to filter on Graph.getVertices/Edges(key,value).
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PropertyFilteredIterable<T extends Element> implements CloseableIterable<T> {

    private final String key;
    private final Object value;
    private final Iterable<T> iterable;

    public PropertyFilteredIterable(final String key, final Object value, final Iterable<T> iterable) {
        this.key = key;
        this.value = value;
        this.iterable = iterable;
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
                            final Object temp = element.getProperty(key);
                            if (null != temp) {
                                if (temp.equals(value)) {
                                    this.nextElement = element;
                                    return true;
                                }
                            } else {
                                if (value == null) {
                                    this.nextElement = element;
                                    return true;
                                }
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
                        if (element.getPropertyKeys().contains(key) && element.getProperty(key).equals(value)) {
                            return element;
                        }
                    }
                }
            }
        };
    }
}
