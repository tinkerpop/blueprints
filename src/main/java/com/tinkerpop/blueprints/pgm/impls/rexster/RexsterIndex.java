package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterVertexSequence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterIndex<T extends Element> implements Index<T> {

    private final String indexName;
    private final Class<T> indexClass;
    private final RexsterGraph graph;

    public RexsterIndex(RexsterGraph graph, String indexName, Class<T> indexClass) {
        this.graph = graph;
        this.indexName = indexName;
        this.indexClass = indexClass;
    }

    public void remove(String key, Object value, T element) {
        throw new UnsupportedOperationException();
    }

    public void put(String key, Object value, T element) {
        throw new UnsupportedOperationException();
    }

    public String getIndexName() {
        return this.indexName;
    }

    public Type getIndexType() {
        return Type.MANUAL;
    }

    public Class<T> getIndexClass() {
        return this.indexClass;
    }

    public Iterable<T> get(String key, Object value) {
        if (Vertex.class.isAssignableFrom(this.indexClass))
            return (Iterable<T>) new RexsterVertexSequence(graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + indexName + RexsterTokens.QUESTION + key + RexsterTokens.EQUALS + RestHelper.uriCast(value), this.graph);
        else
            return (Iterable<T>) new RexsterEdgeSequence(graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + indexName + RexsterTokens.QUESTION + key + RexsterTokens.EQUALS + RestHelper.uriCast(value), this.graph);

    }

}
