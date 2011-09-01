package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterVertexSequence;
import org.codehaus.jettison.json.JSONObject;

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
        RestHelper.delete(this.graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + RestHelper.encode(this.indexName) + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value) + RexsterTokens.AND + RexsterTokens.CLASS_EQUALS + clazz + RexsterTokens.AND + RexsterTokens.ID_EQUALS + RestHelper.encode(element.getId()));

    }

    public void put(final String key, final Object value, final T element) {
        String clazz;
        if (element instanceof Vertex)
            clazz = RexsterTokens.VERTEX;
        else if (element instanceof Edge)
            clazz = RexsterTokens.EDGE;
        else
            throw new RuntimeException("The provided element is not a legal vertex or edge: " + element);
        RestHelper.post(this.graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + RestHelper.encode(this.indexName) + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value) + RexsterTokens.AND + RexsterTokens.CLASS_EQUALS + clazz + RexsterTokens.AND + RexsterTokens.ID_EQUALS + RestHelper.encode(element.getId()));
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

    public CloseableSequence<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.indexClass))
            return (CloseableSequence<T>) new RexsterVertexSequence(this.graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + RestHelper.encode(this.indexName) + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value), this.graph);
        else
            return (CloseableSequence<T>) new RexsterEdgeSequence(this.graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + RestHelper.encode(this.indexName) + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value), this.graph);
    }

    public boolean equals(final Object object) {
        if (object.getClass().equals(this.getClass())) {
            Index other = (Index) object;
            return other.getIndexClass().equals(this.getIndexClass()) && other.getIndexName().equals(this.getIndexName()) && other.getIndexType().equals(this.getIndexType());
        } else {
            return false;
        }
    }

    public String toString() {
        return StringFactory.indexString(this);
    }

    public long count(final String key, final Object value) {
        JSONObject countJson = RestHelper.get(this.graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + RestHelper.encode(this.indexName) + RexsterTokens.SLASH_COUNT + RexsterTokens.QUESTION + RexsterTokens.KEY_EQUALS + key + RexsterTokens.AND + RexsterTokens.VALUE_EQUALS + RestHelper.uriCast(value));
        long theSize = countJson.optLong("totalSize");

        return theSize;
    }
}
