package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class IndexableGraphHelper {

    /**
     * Add a vertex to a graph only if no other vertex in the provided Index is indexed by the property key/value pair.
     * If a vertex already exists with that key/value pair, return the pre-existing vertex.
     *
     * @param graph       the graph to add the vertex to
     * @param id          the id of the vertex to create (can be null)
     * @param index       the index to determine if another vertex with the same key/value exists
     * @param uniqueKey   the key to check on for uniqueness of the vertex
     * @param uniqueValue the value to check on for uniqueness of the vertex
     * @return the newly created vertex or the vertex that satisfies the uniqueness criteria
     */
    public static Vertex addUniqueVertex(final IndexableGraph graph, final Object id, final Index<Vertex> index, final String uniqueKey, final Object uniqueValue) {
        final Iterator<Vertex> results = index.get(uniqueKey, uniqueValue).iterator();
        if (results.hasNext()) {
            return results.next();
        } else {
            final Vertex vertex = graph.addVertex(id);
            vertex.setProperty(uniqueKey, uniqueValue);
            return vertex;
        }
    }
}
