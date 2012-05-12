package com.tinkerpop.blueprints.pgm.util;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;


/**
 * A collection of helpful methods for creating standard toString() representations of graph-related objects.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class StringFactory {

    public static final String V = "v";
    public static final String E = "e";
    public static final String L_BRACKET = "[";
    public static final String R_BRACKET = "]";
    public static final String DASH = "-";
    public static final String ARROW = "->";
    public static final String COLON = ":";

    public static final String ID = "id";
    public static final String LABEL = "label";

    public static String vertexString(final Vertex vertex) {
        return V + L_BRACKET + vertex.getId() + R_BRACKET;
    }

    public static String edgeString(final Edge edge) {
        return E + L_BRACKET + edge.getId() + R_BRACKET + L_BRACKET + edge.getOutVertex().getId() + DASH + edge.getLabel() + ARROW + edge.getInVertex().getId() + R_BRACKET;
    }

    public static String graphString(final Graph graph, final String internalString) {
        return graph.getClass().getSimpleName().toLowerCase() + L_BRACKET + internalString + R_BRACKET;
    }

    public static String indexString(final Index index) {
        return "index" + L_BRACKET + index.getIndexName() + COLON + index.getIndexClass().getSimpleName() + R_BRACKET;
    }
}
