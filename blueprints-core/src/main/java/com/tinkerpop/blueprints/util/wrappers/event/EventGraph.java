package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.WrappedGraphQuery;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;
import com.tinkerpop.blueprints.util.wrappers.event.listener.EdgeAddedEvent;
import com.tinkerpop.blueprints.util.wrappers.event.listener.EdgeRemovedEvent;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.util.wrappers.event.listener.VertexAddedEvent;
import com.tinkerpop.blueprints.util.wrappers.event.listener.VertexRemovedEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An EventGraph is a wrapper to existing Graph implementations and provides for graph events to be raised
 * to one or more listeners on changes to the Graph.  Notifications to the listeners occur for the
 * following events: new vertex/edge, vertex/edge property changed, vertex/edge property removed,
 * vertex/edge removed.
 *
 * The limiting factor to events being raised is related to out-of-process functions changing graph elements.
 *
 * To gather events from EventGraph, simply provide an implementation of the {@link GraphChangedListener} to
 * the EventGraph by utilizing the addListener method.  EventGraph allows the addition of multiple GraphChangedListener
 * implementations.  Each listener will be notified in the order that it was added.
 *
 * @author Stephen Mallette
 */
public class EventGraph<T extends Graph> implements Graph, WrapperGraph<T> {

    protected EventTrigger trigger;

    protected final T baseGraph;

    protected final List<GraphChangedListener> graphChangedListeners = new ArrayList<GraphChangedListener>();

    private final Features features;

    public EventGraph(final T baseGraph) {
        this.baseGraph = baseGraph;
        this.features = this.baseGraph.getFeatures().copyFeatures();
        this.features.isWrapper = true;

        this.trigger = new EventTrigger(this, false);
    }

    public void removeAllListeners() {
        this.graphChangedListeners.clear();
    }

    public void addListener(final GraphChangedListener listener) {
        this.graphChangedListeners.add(listener);
    }

    public Iterator<GraphChangedListener> getListenerIterator() {
        return this.graphChangedListeners.iterator();
    }

    public EventTrigger getTrigger() {
        return this.trigger;
    }

    public void removeListener(final GraphChangedListener listener) {
        this.graphChangedListeners.remove(listener);
    }

    protected void onVertexAdded(Vertex vertex) {
        this.trigger.addEvent(new VertexAddedEvent(vertex));
    }

    protected void onVertexRemoved(final Vertex vertex, Map<String, Object> props) {
        this.trigger.addEvent(new VertexRemovedEvent(vertex, props));
    }

    protected void onEdgeAdded(Edge edge) {
        this.trigger.addEvent(new EdgeAddedEvent(edge));
    }

    protected void onEdgeRemoved(final Edge edge, Map<String, Object> props) {
        this.trigger.addEvent(new EdgeRemovedEvent(edge, props));
    }

    /**
     * Raises a vertexAdded event.
     */
    public Vertex addVertex(final Object id) {
        final Vertex vertex = this.baseGraph.addVertex(id);
        if (vertex == null) {
            return null;
        } else {
            this.onVertexAdded(vertex);
            return new EventVertex(vertex, this);
        }
    }

    public Vertex getVertex(final Object id) {
        final Vertex vertex = this.baseGraph.getVertex(id);
        if (vertex == null) {
            return null;
        } else {
            return new EventVertex(vertex, this);
        }
    }

    /**
     * Raises a vertexRemoved event.
     */
    public void removeVertex(final Vertex vertex) {
        Vertex vertexToRemove = vertex;
        if (vertex instanceof EventVertex) {
            vertexToRemove = ((EventVertex) vertex).getBaseVertex();
        }

        Map<String, Object> props = ElementHelper.getProperties(vertex);
        this.baseGraph.removeVertex(vertexToRemove);
        this.onVertexRemoved(vertex, props);
    }

    public Iterable<Vertex> getVertices() {
        return new EventVertexIterable(this.baseGraph.getVertices(), this);
    }

    public Iterable<Vertex> getVertices(final String key, final Object value) {
        return new EventVertexIterable(this.baseGraph.getVertices(key, value), this);
    }

    /**
     * Raises an edgeAdded event.
     */
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        Vertex outVertexToSet = outVertex;
        if (outVertex instanceof EventVertex) {
            outVertexToSet = ((EventVertex) outVertex).getBaseVertex();
        }

        Vertex inVertexToSet = inVertex;
        if (inVertex instanceof EventVertex) {
            inVertexToSet = ((EventVertex) inVertex).getBaseVertex();
        }

        final Edge edge = this.baseGraph.addEdge(id, outVertexToSet, inVertexToSet, label);
        if (edge == null) {
            return null;
        } else {
            this.onEdgeAdded(edge);
            return new EventEdge(edge, this);
        }
    }

    public Edge getEdge(final Object id) {
        final Edge edge = this.baseGraph.getEdge(id);
        if (edge == null) {
            return null;
        } else {
            return new EventEdge(edge, this);
        }
    }

    /**
     * Raises an edgeRemoved event.
     */
    public void removeEdge(final Edge edge) {
        Edge edgeToRemove = edge;
        if (edge instanceof EventEdge) {
            edgeToRemove = ((EventEdge) edge).getBaseEdge();
        }

        Map<String, Object> props = ElementHelper.getProperties(edge);
        this.baseGraph.removeEdge(edgeToRemove);
        this.onEdgeRemoved(edge, props);
    }

    public Iterable<Edge> getEdges() {
        return new EventEdgeIterable(this.baseGraph.getEdges(), this);
    }

    public Iterable<Edge> getEdges(final String key, final Object value) {
        return new EventEdgeIterable(this.baseGraph.getEdges(key, value), this);
    }

    public GraphQuery query() {
        final EventGraph eventGraph = this;
        return new WrappedGraphQuery(this.baseGraph.query()) {
            @Override
            public Iterable<Edge> edges() {
                return new EventEdgeIterable(this.query.edges(), eventGraph);
            }

            @Override
            public Iterable<Vertex> vertices() {
                return new EventVertexIterable(this.query.vertices(), eventGraph);
            }
        };
    }

    public void shutdown() {
        try {
            this.baseGraph.shutdown();

            // TODO: hmmmmmm??
            this.trigger.fireEventQueue();
            this.trigger.resetEventQueue();
        } catch (Exception re) {

        }
    }

    public String toString() {
        return StringFactory.graphString(this, this.baseGraph.toString());
    }

    @Override
    public T getBaseGraph() {
        return this.baseGraph;
    }

    public Features getFeatures() {
        return this.features;
    }
}
