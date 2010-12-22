package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.Edge;
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

    protected final String indexName;
    protected final Class<T> indexClass;
    protected final RexsterGraph graph;

    public RexsterIndex(final RexsterGraph graph, final String indexName, final Class<T> indexClass) {
        this.graph = graph;
        this.indexName = indexName;
        this.indexClass = indexClass;
    }

    public void remove(final String key, final Object value, final T element) {
        String clazz;
        if (element instanceof Vertex)
            clazz = RexsterTokens.VERTEX;
        else if (element instanceof Edge)
            clazz = RexsterTokens.EDGE;
        else
            throw new RuntimeException("The provided element is not a legal vertex or edge: " + element);
        RestHelper.delete(this.graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + this.indexName + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value) + RexsterTokens.AND + RexsterTokens.CLASS_EQUALS + clazz + RexsterTokens.AND + RexsterTokens.ID_EQUALS + element.getId());

    }

    public void removeElement(final T element) {
        throw new UnsupportedOperationException();
    }

    public void put(final String key, final Object value, final T element) {
        String clazz;
        if (element instanceof Vertex)
            clazz = RexsterTokens.VERTEX;
        else if (element instanceof Edge)
            clazz = RexsterTokens.EDGE;
        else
            throw new RuntimeException("The provided element is not a legal vertex or edge: " + element);
        RestHelper.post(this.graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + this.indexName + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value) + RexsterTokens.AND + RexsterTokens.CLASS_EQUALS + clazz + RexsterTokens.AND + RexsterTokens.ID_EQUALS + element.getId());
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

    public Iterable<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.indexClass))
            return (Iterable<T>) new RexsterVertexSequence(graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + indexName + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value), this.graph);
        else
            return (Iterable<T>) new RexsterEdgeSequence(graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + indexName + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value), this.graph);
    }

    public boolean equals(final Object object) {
        if (object.getClass().equals(this.getClass())) {
            Index other = (Index) object;
            return other.getIndexClass().equals(this.getIndexClass()) && other.getIndexName().equals(this.getIndexName()) && other.getIndexType().equals(this.getIndexType());
        } else {
            return false;
        }
    }

}
