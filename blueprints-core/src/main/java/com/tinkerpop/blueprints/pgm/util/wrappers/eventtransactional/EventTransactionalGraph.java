package com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.WrapperGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.EventGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.EventVertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.EdgeAddedEvent;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.EdgeRemovedEvent;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.Event;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.VertexAddedEvent;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.VertexRemovedEvent;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.util.EventTransactionalEdgeIterable;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.util.EventTransactionalVertexIterable;

import java.util.LinkedList;
import java.util.List;

public class EventTransactionalGraph<T extends TransactionalGraph> extends EventGraph<T> implements TransactionalGraph, WrapperGraph<T> {
    protected final ThreadLocal<List<Event>> eventBuffer = new ThreadLocal<List<Event>>(){
        protected List<Event> initialValue() {
            return new LinkedList<Event>();
        }
    };

    public EventTransactionalGraph(final T baseGraph) {
        super(baseGraph);
    }

    @Override
    public void startTransaction() {
        try {
            this.baseGraph.startTransaction();
            this.eventBuffer.get();
        } catch (RuntimeException re) {
            throw re;
        }
    }

    @Override
    public void stopTransaction(Conclusion conclusion) {
        try {
            this.baseGraph.stopTransaction(conclusion);
            if (conclusion == Conclusion.SUCCESS) {
                fireEventBuffer();
            }

            resetEventBuffer();

        } catch (RuntimeException re) {
            throw re;
        }
    }

    @Override
    public Vertex addVertex(final Object id) {
        final Vertex vertex = this.baseGraph.addVertex(id);
        if (vertex == null) {
            return null;
        } else {
            this.onVertexAdded(vertex);
            return new EventTransactionalVertex(vertex, this.graphChangedListeners, this.eventBuffer);
        }
    }

    @Override
    public Vertex getVertex(final Object id) {
        final Vertex vertex = this.baseGraph.getVertex(id);
        if (vertex == null) {
            return null;
        } else {
            return new EventTransactionalVertex(vertex, this.graphChangedListeners, this.eventBuffer);
        }
    }

    @Override
    public void removeVertex(final Vertex vertex) {
        Vertex vertexToRemove = vertex;
        if (vertex instanceof EventTransactionalVertex) {
            vertexToRemove = ((EventTransactionalVertex) vertex).getBaseVertex();
        }

        this.baseGraph.removeVertex(vertexToRemove);
        this.onVertexRemoved(vertex);
    }

    @Override
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
            return new EventTransactionalEdge(edge, this.graphChangedListeners, this.eventBuffer);
        }
    }

    @Override
    public Edge getEdge(final Object id) {
        final Edge edge = this.baseGraph.getEdge(id);
        if (edge == null) {
            return null;
        } else {
            return new EventTransactionalEdge(edge, this.graphChangedListeners, this.eventBuffer);
        }
    }

    @Override
    public void removeEdge(final Edge edge) {
        Edge edgeToRemove = edge;
        if (edge instanceof EventTransactionalEdge) {
            edgeToRemove = ((EventTransactionalEdge) edge).getBaseEdge();
        }

        this.baseGraph.removeEdge(edgeToRemove);
        this.onEdgeRemoved(edge);
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return new EventTransactionalVertexIterable(this.baseGraph.getVertices(), this.graphChangedListeners, eventBuffer);
    }

    @Override
    public CloseableIterable<Vertex> getVertices(final String key, final Object value) {
        return new EventTransactionalVertexIterable(this.baseGraph.getVertices(key, value), this.graphChangedListeners, eventBuffer);
    }

    @Override
    public Iterable<Edge> getEdges() {
        return new EventTransactionalEdgeIterable(this.baseGraph.getEdges(), this.graphChangedListeners, eventBuffer);
    }

    @Override
    public CloseableIterable<Edge> getEdges(final String key, final Object value) {
        return new EventTransactionalEdgeIterable(this.baseGraph.getEdges(key, value), this.graphChangedListeners, eventBuffer);
    }

    @Override
    public void shutdown() {
        try {
            this.baseGraph.shutdown();
            
            // TODO: hmmmmmm??
            this.fireEventBuffer();
        } catch (RuntimeException re) {
            throw re;
        }
    }

    @Override
    protected void onVertexAdded(Vertex vertex) {
        eventBuffer.get().add(new VertexAddedEvent(vertex));
    }
    
    @Override
    protected void onVertexRemoved(final Vertex vertex) {
        eventBuffer.get().add(new VertexRemovedEvent(vertex));
    }

    @Override
    protected void onEdgeAdded(Edge edge) {
        eventBuffer.get().add(new EdgeAddedEvent(edge));
    }

    @Override
    protected void onEdgeRemoved(final Edge edge) {
        eventBuffer.get().add(new EdgeRemovedEvent(edge));
    }

    private void resetEventBuffer() {
        eventBuffer.set(new LinkedList<Event>());
    }

    private void fireEventBuffer() {
        for (Event event : eventBuffer.get()) {
            event.fireEvent(getListenerIterator());
        }
    }
}
