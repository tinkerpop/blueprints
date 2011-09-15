package com.tinkerpop.blueprints.pgm.impls;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;


/**
 * A collection of helpful methods for creating standard toString() representations of Graph-related objects.
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

    public static final String ID = "id";
    public static final String LABEL = "label";

    public static final String PROPERTY_EXCEPTION_MESSAGE = " is a reserved property key";

    public static String vertexString(final Vertex vertex) {
        return V + L_BRACKET + vertex.getId() + R_BRACKET;
    }

    public static String edgeString(final Edge edge) {
        return E + L_BRACKET + edge.getId() + R_BRACKET + L_BRACKET + edge.getOutVertex().getId() + DASH + edge.getLabel() + ARROW + edge.getInVertex().getId() + R_BRACKET;
    }

    public static String graphString(final Graph graph, final String internalString) {
        return graph.getClass().getSimpleName().toLowerCase() + "[" + internalString + "]";
    }

    public static String indexString(final Index index) {
        String returnString = index.getIndexType() + "[" + index.getIndexName() + ":" + index.getIndexClass().getSimpleName() + "]";
        if (index instanceof AutomaticIndex) {
            returnString = returnString + "[autoIndexKeys:" + ((AutomaticIndex) index).getAutoIndexKeys() + "]";
        }
        return returnString;
    }
}
