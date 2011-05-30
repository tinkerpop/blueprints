package com.tinkerpop.blueprints.pgm.impls.event;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.Iterator;
import java.util.Set;

/**
 * An element with a GraphChangedListener attached.  Those listeners are notified when changes occur to
 * the properties of the element.
 */
public class EventElement implements Element {
    protected final Element element;
    protected final Iterator<GraphChangedListener> graphChangedListeners;

    public EventElement(final Element element, final Iterator<GraphChangedListener> graphChangedListeners) {
        this.element = element;
        this.graphChangedListeners = graphChangedListeners;
    }

    protected void onVertexPropertyChanged(final Vertex vertex, final String key, final Object newValue) {
        while (this.graphChangedListeners.hasNext()) {
            GraphChangedListener listener = this.graphChangedListeners.next();
            listener.vertexPropertyChanged(vertex, key, newValue);
        }
    }

    protected void onEdgePropertyChanged(final Edge edge, final String key, final Object removedValue) {
        while (this.graphChangedListeners.hasNext()) {
            GraphChangedListener listener = this.graphChangedListeners.next();
            listener.edgePropertyChanged(edge, key, removedValue);
        }
    }

    protected void onVertexPropertyRemoved(final Vertex vertex, final String key, final Object newValue) {
        while (this.graphChangedListeners.hasNext()) {
            GraphChangedListener listener = this.graphChangedListeners.next();
            listener.vertexPropertyRemoved(vertex, key, newValue);
        }
    }

    protected void onEdgePropertyRemoved(final Edge edge, final String key, final Object removedValue) {
        while (this.graphChangedListeners.hasNext()) {
            GraphChangedListener listener = this.graphChangedListeners.next();
            listener.edgePropertyRemoved(edge, key, removedValue);
        }
    }

    public Set<String> getPropertyKeys() {
        return this.element.getPropertyKeys();
    }

    public Object getId() {
        return this.element.getId();
    }

    /**
     * Raises a vertexPropertyRemoved or edgePropertyRemoved event.
     */
    public Object removeProperty(final String key)  {
        Object propertyRemoved = element.removeProperty(key);

        if (this instanceof Vertex) {
            this.onVertexPropertyRemoved((Vertex) this, key, propertyRemoved);
        } else if (this instanceof Edge) {
            this.onEdgePropertyRemoved((Edge) this, key, propertyRemoved);
        }

        return element.removeProperty(key);
    }

    public Object getProperty(final String key) {
        return this.element.getProperty(key);
    }

    /**
     * Raises a vertexPropertyRemoved or edgePropertyChanged event.
     */
    public void setProperty(final String key, final Object value)  {
        this.element.setProperty(key, value);

        if (this instanceof Vertex) {
            this.onVertexPropertyChanged((Vertex) this, key, value);
        } else if (this instanceof Edge) {
            this.onEdgePropertyChanged((Edge) this, key, value);
        }
    }

    public String toString() {
        return this.element.toString();
    }

    public int hashCode() {
        return this.element.hashCode();
    }

    public boolean equals(Object object) {
        return (object.getClass().equals(this.getClass())) && this.element.getId().equals(((EventElement) object).getId());
    }
}
