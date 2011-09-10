package com.tinkerpop.blueprints.pgm.impls.event;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.impls.event.util.EventEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.event.util.EventVertexSequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An EventGraph is a wrapper to existing Graph implementations and provides for graph events to be raised
 * to one or more listeners on changes to the Graph.  Notifications to the listeners occur for the the
 * following events: new vertex/edge, vertex/edge property changed, vertex/edge property removed,
 * vertex/edge removed.
 * <p/>
 * The limiting factor to events being raised is related to out-of-process functions changing graph elements.
 */
public class EventGraph implements Graph {
    protected final Graph graph;

    protected final List<GraphChangedListener> graphChangedListeners = new ArrayList<GraphChangedListener>();

    public EventGraph(final Graph graph) {
        this.graph = graph;
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

    public void removeListener(final GraphChangedListener listener) {
        this.graphChangedListeners.remove(listener);
    }

    protected void onVertexAdded(final Vertex vertex) {
        for (GraphChangedListener listener : this.graphChangedListeners) {
            listener.vertexAdded(vertex);
        }
    }

    protected void onVertexRemoved(final Vertex vertex) {
        for (GraphChangedListener listener : this.graphChangedListeners) {
            listener.vertexRemoved(vertex);
        }
    }

    protected void onEdgeAdded(final Edge edge) {
        for (GraphChangedListener listener : this.graphChangedListeners) {
            listener.edgeAdded(edge);
        }
    }

    protected void onEdgeRemoved(final Edge edge) {
        for (GraphChangedListener listener : this.graphChangedListeners) {
            listener.edgeRemoved(edge);
        }
    }

    protected void onGraphCleared() {
        for (GraphChangedListener listener : this.graphChangedListeners) {
            listener.graphCleared();
        }
    }

    /**
     * Raises a vertexAdded event.
     */
    public Vertex addVertex(final Object id) {
        final Vertex vertex = this.graph.addVertex(id);
        if (vertex == null) {
            return null;
        } else {
            this.onVertexAdded(vertex);
            return new EventVertex(vertex, this.graphChangedListeners);
        }
    }

    public Vertex getVertex(final Object id) {
        final Vertex vertex = this.graph.getVertex(id);
        if (vertex == null) {
            return null;
        } else {
            return new EventVertex(vertex, this.graphChangedListeners);
        }
    }

    /**
     * Raises a vertexRemoved event.
     */
    public void removeVertex(final Vertex vertex) {
        Vertex vertexToRemove = vertex;
        if (vertex instanceof EventVertex) {
            vertexToRemove = ((EventVertex) vertex).getRawVertex();
        }

        this.graph.removeVertex(vertexToRemove);
        this.onVertexRemoved(vertex);
    }

    public Iterable<Vertex> getVertices() {
        return new EventVertexSequence(this.graph.getVertices().iterator(), this.graphChangedListeners);
    }

    /**
     * Raises an edgeAdded event.
     */
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        Vertex outVertexToSet = outVertex;
        if(outVertex instanceof EventVertex) {
            outVertexToSet = ((EventVertex) outVertex).getRawVertex();
        }
        
        Vertex inVertexToSet = inVertex;
        if(inVertex instanceof EventVertex) {
            inVertexToSet = ((EventVertex) inVertex).getRawVertex();
        }
        
        final Edge edge = this.graph.addEdge(id, outVertexToSet, inVertexToSet, label);
        if (edge == null) {
            return null;
        } else {
            return new EventEdge(edge, this.graphChangedListeners);
        }
    }

    public Edge getEdge(final Object id) {
        final Edge edge = this.graph.getEdge(id);
        if (edge == null) {
            return null;
        } else {
            this.onEdgeAdded(edge);
            return new EventEdge(edge, this.graphChangedListeners);
        }
    }

    /**
     * Raises an edgeRemoved event.
     */
    public void removeEdge(final Edge edge) {
        Edge edgeToRemove = edge;
        if (edge instanceof EventEdge) {
            edgeToRemove = ((EventEdge) edge).getRawEdge();
        }

        this.graph.removeEdge(edgeToRemove);
        this.onEdgeRemoved(edge);
    }

    public Iterable<Edge> getEdges() {
        return new EventEdgeSequence(this.graph.getEdges().iterator(), this.graphChangedListeners);
    }

    public void clear() {
        this.graph.clear();
        this.onGraphCleared();
    }

    public void shutdown() {
        this.graph.shutdown();
    }

    public String toString() {
        return "(event)" + this.graph.toString();
    }

    public Graph getRawGraph() {
        return this.graph;
    }
}
