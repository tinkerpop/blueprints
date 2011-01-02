package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IndexHelper {

    public static void reIndexVertices(IndexableGraph graph) {
        for (Vertex vertex : graph.getVertices()) {
            for (String key : vertex.getPropertyKeys()) {
                vertex.setProperty(key, vertex.getProperty(key));
            }
        }
    }

    public static void reIndexEdges(IndexableGraph graph) {
        for (Edge edge : graph.getEdges()) {
            for (String key : edge.getPropertyKeys()) {
                edge.setProperty(key, edge.getProperty(key));
            }
        }
    }
}
