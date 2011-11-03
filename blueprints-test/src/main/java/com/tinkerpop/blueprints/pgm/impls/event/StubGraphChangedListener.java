package com.tinkerpop.blueprints.pgm.impls.event;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

public class StubGraphChangedListener implements GraphChangedListener {
    private boolean addEdgeEvent = false;
    private boolean addVertexEvent = false;
    private boolean vertexPropertyChangedEvent = false;
    private boolean vertexPropertyRemovedEvent = false;
    private boolean vertexRemovedEvent = false;
    private boolean edgePropertyChangedEvent = false;
    private boolean edgePropertyRemovedEvent = false;
    private boolean edgeRemovedEvent = false;
    private boolean graphClearedEvent = false;

    public void reset() {
        addEdgeEvent = false;
        addVertexEvent = false;
        vertexPropertyChangedEvent = false;
        vertexPropertyRemovedEvent = false;
        vertexRemovedEvent = false;
        edgePropertyChangedEvent = false;
        edgePropertyRemovedEvent = false;
        edgeRemovedEvent = false;
        graphClearedEvent = false;
    }

    public void vertexAdded(Vertex vertex) {
        addVertexEvent = true;
    }

    public void vertexPropertyChanged(Vertex vertex, String s, Object o) {
        vertexPropertyChangedEvent = true;
    }

    public void vertexPropertyRemoved(Vertex vertex, String s, Object o) {
        vertexPropertyRemovedEvent = true;
    }

    public void vertexRemoved(Vertex vertex) {
        vertexRemovedEvent = true;
    }

    public void edgeAdded(Edge edge) {
        addEdgeEvent = true;
    }

    public void edgePropertyChanged(Edge edge, String s, Object o) {
        edgePropertyChangedEvent = true;
    }

    public void edgePropertyRemoved(Edge edge, String s, Object o) {
        edgePropertyRemovedEvent = true;
    }

    public void edgeRemoved(Edge edge) {
        edgeRemovedEvent = true;
    }

    public void graphCleared() {
        graphClearedEvent = true;
    }

    public boolean addEdgeEventRecorded() {
        return addEdgeEvent;
    }

    public boolean addVertexEventRecorded() {
        return addVertexEvent;
    }

    public boolean vertexPropertyChangedEventRecorded() {
        return vertexPropertyChangedEvent;
    }

    public boolean vertexPropertyRemovedEventRecorded() {
        return vertexPropertyRemovedEvent;
    }

    public boolean vertexRemovedEventRecorded() {
        return vertexRemovedEvent;
    }

    public boolean edgePropertyChangedEventRecorded() {
        return edgePropertyChangedEvent;
    }

    public boolean edgePropertyRemovedEventRecorded() {
        return edgePropertyRemovedEvent;
    }

    public boolean edgeRemovedEventRecorded() {
        return edgeRemovedEvent;
    }

    public boolean graphClearedEventRecorded() {
        return graphClearedEvent;
    }
}
