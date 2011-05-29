package com.tinkerpop.blueprints.pgm.impls.event;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.impls.event.util.EventEdgeSequence;

import java.util.Iterator;

public class EventVertex extends EventElement implements Vertex {
    public EventVertex(final Vertex vertex, final Iterator<GraphChangedListener> graphChangedListeners) {
        super(vertex, graphChangedListeners);
    }

    public Iterable<Edge> getInEdges() {
        return new EventEdgeSequence(((Vertex) this.element).getInEdges().iterator(), this.graphChangedListeners);
    }

    public Iterable<Edge> getOutEdges() {
        return new EventEdgeSequence(((Vertex) this.element).getOutEdges().iterator(), this.graphChangedListeners);
    }

    public Iterable<Edge> getInEdges(final String label) {
        return new EventEdgeSequence(((Vertex) this.element).getInEdges(label).iterator(), this.graphChangedListeners);
    }

    public Iterable<Edge> getOutEdges(final String label) {
        return new EventEdgeSequence(((Vertex) this.element).getOutEdges(label).iterator(), this.graphChangedListeners);
    }

    public Vertex getRawVertex() {
        return (Vertex) this.element;
    }
}
