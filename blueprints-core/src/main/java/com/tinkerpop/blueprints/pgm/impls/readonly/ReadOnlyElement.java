package com.tinkerpop.blueprints.pgm.impls.readonly;

import com.tinkerpop.blueprints.pgm.Element;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyElement implements Element {

    protected final Element element;

    public ReadOnlyElement(final Element element) {
        this.element = element;
    }

    public Set<String> getPropertyKeys() {
        return this.element.getPropertyKeys();
    }

    public Object getId() {
        return this.element.getId();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Object removeProperty(final String key) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public Object getProperty(final String key) {
        return this.element.getProperty(key);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void setProperty(final String key, final Object value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public String toString() {
        return this.element.toString();
    }

    public int hashCode() {
        return this.element.hashCode();
    }

    public boolean equals(Object object) {
        return this.element.equals(object);
    }
}
