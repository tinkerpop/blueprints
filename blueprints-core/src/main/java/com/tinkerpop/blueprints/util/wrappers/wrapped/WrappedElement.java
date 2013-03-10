package com.tinkerpop.blueprints.util.wrappers.wrapped;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.util.ElementHelper;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class WrappedElement implements Element {

    protected Element baseElement;

    protected WrappedElement(final Element baseElement) {
        this.baseElement = baseElement;
    }

    public void setProperty(final String key, final Object value) {
        this.baseElement.setProperty(key, value);
    }

    public <T> T getProperty(final String key) {
        return this.baseElement.getProperty(key);
    }

    public <T> T removeProperty(final String key) {
        return this.baseElement.removeProperty(key);
    }

    public Set<String> getPropertyKeys() {
        return this.baseElement.getPropertyKeys();
    }

    public Object getId() {
        return this.baseElement.getId();
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    public int hashCode() {
        return this.baseElement.hashCode();
    }

    public Element getBaseElement() {
        return this.baseElement;
    }

    public void remove() {
        this.baseElement.remove();
    }

    public String toString() {
        return this.baseElement.toString();
    }
}
