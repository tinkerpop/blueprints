package com.tinkerpop.blueprints.pgm.util.wrappers.event;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.GraphChangedListener;

import java.util.LinkedList;

public class StubGraphChangedListener implements GraphChangedListener {
    private int addEdgeEvent = 0;
    private int addVertexEvent = 0;
    private int vertexPropertyChangedEvent = 0;
    private int vertexPropertyRemovedEvent = 0;
    private int vertexRemovedEvent = 0;
    private int edgePropertyChangedEvent = 0;
    private int edgePropertyRemovedEvent = 0;
    private int edgeRemovedEvent = 0;

    private final LinkedList<String> order = new LinkedList<String>();

    public void reset() {
        addEdgeEvent = 0;
        addVertexEvent = 0;
        vertexPropertyChangedEvent = 0;
        vertexPropertyRemovedEvent = 0;
        vertexRemovedEvent = 0;
        edgePropertyChangedEvent = 0;
        edgePropertyRemovedEvent = 0;
        edgeRemovedEvent = 0;

        order.clear();
    }

    public LinkedList<String> getOrder() {
        return this.order;
    }

    public void vertexAdded(Vertex vertex) {
        addVertexEvent++;
        order.add("v-added-" + vertex.getId());
    }

    public void vertexPropertyChanged(Vertex vertex, String s, Object o) {
        vertexPropertyChangedEvent++;
        order.add("v-property-changed-" + vertex.getId() + "-" + s + ":" + o);
    }

    public void vertexPropertyRemoved(Vertex vertex, String s, Object o) {
        vertexPropertyRemovedEvent++;
        order.add("v-property-removed-" + vertex.getId() + "-" + s + ":" + o);
    }

    public void vertexRemoved(Vertex vertex) {
        vertexRemovedEvent++;
        order.add("v-removed-" + vertex.getId());
    }

    public void edgeAdded(Edge edge) {
        addEdgeEvent++;
        order.add("e-added-" + edge.getId());
    }

    public void edgePropertyChanged(Edge edge, String s, Object o) {
        edgePropertyChangedEvent++;
        order.add("e-property-changed-" + edge.getId() + "-" + s + ":" + o);
    }

    public void edgePropertyRemoved(Edge edge, String s, Object o) {
        edgePropertyRemovedEvent++;
        order.add("e-property-removed-" + edge.getId() + "-" + s + ":" + o);
    }

    public void edgeRemoved(Edge edge) {
        edgeRemovedEvent++;
        order.add("e-removed-" + edge.getId());
    }

    public int addEdgeEventRecorded() {
        return addEdgeEvent;
    }

    public int addVertexEventRecorded() {
        return addVertexEvent;
    }

    public int vertexPropertyChangedEventRecorded() {
        return vertexPropertyChangedEvent;
    }

    public int vertexPropertyRemovedEventRecorded() {
        return vertexPropertyRemovedEvent;
    }

    public int vertexRemovedEventRecorded() {
        return vertexRemovedEvent;
    }

    public int edgePropertyChangedEventRecorded() {
        return edgePropertyChangedEvent;
    }

    public int edgePropertyRemovedEventRecorded() {
        return edgePropertyRemovedEvent;
    }

    public int edgeRemovedEventRecorded() {
        return edgeRemovedEvent;
    }
}
