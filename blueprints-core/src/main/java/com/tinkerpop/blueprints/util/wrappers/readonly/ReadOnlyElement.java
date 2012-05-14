package com.tinkerpop.blueprints.util.wrappers.readonly;

import com.tinkerpop.blueprints.Element;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyElement implements Element {

    protected final Element baseElement;

    public ReadOnlyElement(final Element baseElement) {
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

    public Object getProperty(final String key) {
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

    public boolean equals(Object object) {
        return null != object && (object.getClass().equals(this.getClass())) && this.baseElement.getId().equals(((ReadOnlyElement) object).getId());
    }
}
