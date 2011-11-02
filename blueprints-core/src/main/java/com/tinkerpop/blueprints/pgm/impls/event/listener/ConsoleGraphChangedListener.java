package com.tinkerpop.blueprints.pgm.impls.event.listener;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * An example listener that writes a message to the console for each event that fires from the graph.
 *
 * @author Stephen Mallette
 */
public class ConsoleGraphChangedListener implements GraphChangedListener {

    private final Graph graph;

    public ConsoleGraphChangedListener(final Graph graph) {
        this.graph = graph;
    }

    public void vertexAdded(final Vertex vertex) {
        System.out.println("Vertex [" + vertex.toString() + "] added to graph [" + graph.toString() + "]");
    }

    public void vertexPropertyChanged(final Vertex vertex, final String key, final Object setValue) {
        System.out.println("Vertex [" + vertex.toString() + "] property [" + key + "] set to value of [" + setValue + "] in graph [" + graph.toString() + "]");
    }

    public void vertexPropertyRemoved(final Vertex vertex, final String key, final Object removedValue) {
        System.out.println("Vertex [" + vertex.toString() + "] property [" + key + "] with value of [" + removedValue + "] removed in graph [" + graph.toString() + "]");
    }

    public void vertexRemoved(final Vertex vertex) {
        System.out.println("Vertex [" + vertex.toString() + "] removed from graph [" + graph.toString() + "]");
    }

    public void edgeAdded(final Edge edge) {
        System.out.println("Edge [" + edge.toString() + "] added to graph [" + graph.toString() + "]");
    }

    public void edgePropertyChanged(final Edge edge, final String key, final Object setValue) {
        System.out.println("Edge [" + edge.toString() + "] property [" + key + "] set to value of [" + setValue + "] in graph [" + graph.toString() + "]");
    }

    public void edgePropertyRemoved(final Edge edge, final String key, final Object removedValue) {
        System.out.println("Edge [" + edge.toString() + "] property [" + key + "] with value of [" + removedValue + "] removed in graph [" + graph.toString() + "]");
    }

    public void edgeRemoved(final Edge edge) {
        System.out.println("Edge [" + edge.toString() + "] removed from graph [" + graph.toString() + "]");
    }

    public void graphCleared() {
        System.out.println("Graph [" + graph.toString() + "] cleared.");
    }
}
