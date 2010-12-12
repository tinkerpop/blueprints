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
public class RexsterIndex<T extends RexsterElement> implements Index<T> {

    protected final String indexName;
    protected final Class<T> indexClass;
    protected final RexsterGraph graph;

    public RexsterIndex(RexsterGraph graph, String indexName, Class<T> indexClass) {
        this.graph = graph;
        this.indexName = indexName;
        this.indexClass = indexClass;
    }

    public void remove(String key, Object value, T element) {
        String type;
        if (element instanceof Vertex)
            type = "vertex";
        else
            type = "edge";
        RestHelper.delete(this.graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + this.indexName + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value) + RexsterTokens.AND + RexsterTokens.TYPE_EQUALS + type + RexsterTokens.AND + RexsterTokens.ID_EQUALS + element.getId());

    }

    public void put(String key, Object value, T element) {
        String type;
        if (element instanceof Vertex)
            type = "vertex";
        else
            type = "edge";
        RestHelper.post(this.graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + this.indexName + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value) + RexsterTokens.AND + RexsterTokens.TYPE_EQUALS + type + RexsterTokens.AND + RexsterTokens.ID_EQUALS + element.getId());
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
            return (Iterable<T>) new RexsterVertexSequence(graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + indexName + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value), this.graph);
        else
            return (Iterable<T>) new RexsterEdgeSequence(graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + indexName + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value), this.graph);
    }

    public boolean equals(Object object) {
        if (object.getClass().equals(this.getClass())) {
            Index other = (Index) object;
            return other.getIndexClass().equals(this.getIndexClass()) && other.getIndexName().equals(this.getIndexName()) && other.getIndexType().equals(this.getIndexType());
        } else {
            return false;
        }
    }

}
