package com.tinkerpop.blueprints.util.wrappers.readonly;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.util.ElementHelper;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
abstract class ReadOnlyElement implements Element {

    protected final Element baseElement;

    protected ReadOnlyElement(final Element baseElement) {
        this.baseElement = baseElement;
    }

    public Set<String> getPropertyKeys() {
        return this.baseElement.getPropertyKeys();
    }

    public Object getId() {
        return this.baseElement.getId();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Object removeProperty(final String key) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public <T> T getProperty(final String key) {
        return this.baseElement.getProperty(key);
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void setProperty(final String key, final Object value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public String toString() {
        return this.baseElement.toString();
    }

    public int hashCode() {
        return this.baseElement.hashCode();
    }

    public void remove() {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }
}
