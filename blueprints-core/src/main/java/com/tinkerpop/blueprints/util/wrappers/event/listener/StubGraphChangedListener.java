package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An event listener that acts as a counter for changes to the graph.
 *
 * @author Stephen Mallette
 */
public class StubGraphChangedListener implements GraphChangedListener {
    private AtomicLong addEdgeEvent = new AtomicLong(0);
    private AtomicLong addVertexEvent = new AtomicLong(0);
    private AtomicLong vertexPropertyChangedEvent = new AtomicLong(0);
    private AtomicLong vertexPropertyRemovedEvent = new AtomicLong(0);
    private AtomicLong vertexRemovedEvent = new AtomicLong(0);
    private AtomicLong edgePropertyChangedEvent = new AtomicLong(0);
    private AtomicLong edgePropertyRemovedEvent = new AtomicLong(0);
    private AtomicLong edgeRemovedEvent = new AtomicLong(0);

    private final List<String> order = new ArrayList<String>();

    public void reset() {
        addEdgeEvent = new AtomicLong(0);
        addVertexEvent = new AtomicLong(0);
        vertexPropertyChangedEvent = new AtomicLong(0);
        vertexPropertyRemovedEvent = new AtomicLong(0);
        vertexRemovedEvent = new AtomicLong(0);
        edgePropertyChangedEvent = new AtomicLong(0);
        edgePropertyRemovedEvent = new AtomicLong(0);
        edgeRemovedEvent = new AtomicLong(0);

        order.clear();
    }

    public List<String> getOrder() {
        return this.order;
    }

    public void vertexAdded(final Vertex vertex) {
        addVertexEvent.incrementAndGet();
        order.add("v-added-" + vertex.getId());
    }

    public void vertexPropertyChanged(final Vertex vertex, final String s, final Object o, final Object n) {
        vertexPropertyChangedEvent.incrementAndGet();;
        order.add("v-property-changed-" + vertex.getId() + "-" + s + ":" + o + "->" + n);
    }

    public void vertexPropertyRemoved(final Vertex vertex, final String s, final Object o) {
        vertexPropertyRemovedEvent.incrementAndGet();;
        order.add("v-property-removed-" + vertex.getId() + "-" + s + ":" + o);
    }

    public void vertexRemoved(final Vertex vertex, final Map<String, Object> props) {
        vertexRemovedEvent.incrementAndGet();;
        order.add("v-removed-" + vertex.getId());
    }

    public void edgeAdded(final Edge edge) {
        addEdgeEvent.incrementAndGet();;
        order.add("e-added-" + edge.getId());
    }

    public void edgePropertyChanged(final Edge edge, final String s, final Object o, final Object n) {
        edgePropertyChangedEvent.incrementAndGet();;
        order.add("e-property-changed-" + edge.getId() + "-" + s + ":" + o + "->" + n);
    }

    public void edgePropertyRemoved(final Edge edge, final String s, final Object o) {
        edgePropertyRemovedEvent.incrementAndGet();;
        order.add("e-property-removed-" + edge.getId() + "-" + s + ":" + o);
    }

    public void edgeRemoved(final Edge edge) {
        edgeRemovedEvent.incrementAndGet();;
        order.add("e-removed-" + edge.getId());
    }

    public long addEdgeEventRecorded() {
        return addEdgeEvent.get();
    }

    public long addVertexEventRecorded() {
        return addVertexEvent.get();
    }

    public long vertexPropertyChangedEventRecorded() {
        return vertexPropertyChangedEvent.get();
    }

    public long vertexPropertyRemovedEventRecorded() {
        return vertexPropertyRemovedEvent.get();
    }

    public long vertexRemovedEventRecorded() {
        return vertexRemovedEvent.get();
    }

    public long edgePropertyChangedEventRecorded() {
        return edgePropertyChangedEvent.get();
    }

    public long edgePropertyRemovedEventRecorded() {
        return edgePropertyRemovedEvent.get();
    }

    public long edgeRemovedEventRecorded() {
        return edgeRemovedEvent.get();
    }
}
