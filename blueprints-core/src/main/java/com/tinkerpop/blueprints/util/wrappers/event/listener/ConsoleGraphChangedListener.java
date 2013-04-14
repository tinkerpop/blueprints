package com.tinkerpop.blueprints.util.wrappers.event.listener;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import java.util.Map;

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

    public void vertexPropertyChanged(final Vertex vertex, final String key, final Object oldValue, final Object newValue) {
        System.out.println("Vertex [" + vertex.toString() + "] property [" + key + "] change value from [" + oldValue + "] to [" + newValue + "] in graph [" + graph.toString() + "]");
    }

    public void vertexPropertyRemoved(final Vertex vertex, final String key, final Object removedValue) {
        System.out.println("Vertex [" + vertex.toString() + "] property [" + key + "] with value of [" + removedValue + "] removed in graph [" + graph.toString() + "]");
    }

    public void vertexRemoved(final Vertex vertex, Map<String, Object> props) {
        System.out.println("Vertex [" + vertex.toString() + "] removed from graph [" + graph.toString() + "]");
    }

    public void edgeAdded(final Edge edge) {
        System.out.println("Edge [" + edge.toString() + "] added to graph [" + graph.toString() + "]");
    }

    public void edgePropertyChanged(final Edge edge, final String key, final Object oldValue, final Object newValue) {
        System.out.println("Edge [" + edge.toString() + "] property [" + key + "] change value from [" + oldValue + "] to [" + newValue + "] in graph [" + graph.toString() + "]");
    }

    public void edgePropertyRemoved(final Edge edge, final String key, final Object removedValue) {
        System.out.println("Edge [" + edge.toString() + "] property [" + key + "] with value of [" + removedValue + "] removed in graph [" + graph.toString() + "]");
    }

    public void edgeRemoved(final Edge edge, Map<String, Object> props) {
        System.out.println("Edge [" + edge.toString() + "] removed from graph [" + graph.toString() + "]");
    }
}
