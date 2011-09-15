package com.tinkerpop.blueprints.pgm.impls.readonly;

import com.tinkerpop.blueprints.pgm.Element;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyElement implements Element {

    protected final Element rawElement;

    public ReadOnlyElement(final Element rawElement) {
        this.rawElement = rawElement;
    }

    public Set<String> getPropertyKeys() {
        return this.rawElement.getPropertyKeys();
    }

    public Object getId() {
        return this.rawElement.getId();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Object removeProperty(final String key) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public Object getProperty(final String key) {
        return this.rawElement.getProperty(key);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void setProperty(final String key, final Object value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public String toString() {
        return this.rawElement.toString();
    }

    public int hashCode() {
        return this.rawElement.hashCode();
    }

    public boolean equals(Object object) {
        return (object.getClass().equals(this.getClass())) && this.rawElement.getId().equals(((ReadOnlyElement) object).getId());
    }
}
