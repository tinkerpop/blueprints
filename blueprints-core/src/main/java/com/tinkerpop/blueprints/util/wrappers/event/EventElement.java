package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.wrappers.event.listener.EdgePropertyChangedEvent;
import com.tinkerpop.blueprints.util.wrappers.event.listener.EdgePropertyRemovedEvent;
import com.tinkerpop.blueprints.util.wrappers.event.listener.VertexPropertyChangedEvent;
import com.tinkerpop.blueprints.util.wrappers.event.listener.VertexPropertyRemovedEvent;

import java.util.Set;

/**
 * An element with a GraphChangedListener attached.  Those listeners are notified when changes occur to
 * the properties of the element.
 *
 * @author Stephen Mallette
 */
public abstract class EventElement implements Element {
    protected final EventGraph eventGraph;

    protected final Element baseElement;

    protected EventElement(final Element baseElement, final EventGraph eventGraph) {
        this.baseElement = baseElement;
        this.eventGraph = eventGraph;
    }

    protected void onVertexPropertyChanged(final Vertex vertex, final String key, final Object oldValue, final Object newValue) {
        this.eventGraph.getTrigger().addEvent(new VertexPropertyChangedEvent(vertex, key, oldValue, newValue));
    }

    protected void onEdgePropertyChanged(final Edge edge, final String key, final Object oldValue, final Object newValue) {
        this.eventGraph.getTrigger().addEvent(new EdgePropertyChangedEvent(edge, key, oldValue, newValue));
    }

    protected void onVertexPropertyRemoved(final Vertex vertex, final String key, final Object removedValue) {
        this.eventGraph.getTrigger().addEvent(new VertexPropertyRemovedEvent(vertex, key, removedValue));
    }

    protected void onEdgePropertyRemoved(final Edge edge, final String key, final Object removedValue) {
        this.eventGraph.getTrigger().addEvent(new EdgePropertyRemovedEvent(edge, key, removedValue));
    }

    public Set<String> getPropertyKeys() {
        return this.baseElement.getPropertyKeys();
    }

    public Object getId() {
        return this.baseElement.getId();
    }

    /**
     * Raises a vertexPropertyRemoved or edgePropertyRemoved event.
     */
    public <T> T removeProperty(final String key) {
        final Object propertyRemoved = baseElement.removeProperty(key);

        if (this instanceof Vertex) {
            this.onVertexPropertyRemoved((Vertex) this, key, propertyRemoved);
        } else if (this instanceof Edge) {
            this.onEdgePropertyRemoved((Edge) this, key, propertyRemoved);
        }

        return (T) propertyRemoved;
    }

    public <T> T getProperty(final String key) {
        return this.baseElement.getProperty(key);
    }

    /**
     * Raises a vertexPropertyRemoved or edgePropertyChanged event.
     */
    public void setProperty(final String key, final Object value) {
        final Object oldValue = this.baseElement.getProperty(key);
        this.baseElement.setProperty(key, value);

        if (this instanceof Vertex) {
            this.onVertexPropertyChanged((Vertex) this, key, oldValue, value);
        } else if (this instanceof Edge) {
            this.onEdgePropertyChanged((Edge) this, key, oldValue, value);
        }
    }

    public String toString() {
        return this.baseElement.toString();
    }

    public int hashCode() {
        return this.baseElement.hashCode();
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    public Element getBaseElement() {
        return this.baseElement;
    }

    public void remove() {
        if (this instanceof Vertex)
            this.eventGraph.removeVertex((Vertex) this);
        else
            this.eventGraph.removeEdge((Edge) this);
    }
}
