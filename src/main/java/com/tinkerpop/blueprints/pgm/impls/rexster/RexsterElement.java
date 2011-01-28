package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class RexsterElement implements Element {

    protected final Object id;
    protected final RexsterGraph graph;

    public RexsterElement(final JSONObject rawElement, final RexsterGraph graph) {
        this.id = rawElement.get(RexsterTokens._ID);
        this.graph = graph;
    }

    public Object getId() {
        return this.id;
    }

    public Set<String> getPropertyKeys() {
        JSONObject rawElement;

        if (this instanceof Vertex)
            rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId());
        else
            rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + this.getId());

        Set<String> keys = new HashSet<String>();
        keys.addAll(rawElement.keySet());
        keys.remove(RexsterTokens._TYPE);
        keys.remove(RexsterTokens._LABEL);
        keys.remove(RexsterTokens._ID);
        keys.remove(RexsterTokens._OUTE);
        keys.remove(RexsterTokens._INE);
        keys.remove(RexsterTokens._OUTV);
        keys.remove(RexsterTokens._INV);
        return keys;
    }

    public Object getProperty(final String key) {
        JSONObject rawElement;
        if (this instanceof Vertex)
            rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId() + RexsterTokens.QUESTION + RexsterTokens.REXSTER_SHOW_TYPES_EQUALS_TRUE);
        else
            rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + this.getId() + RexsterTokens.QUESTION + RexsterTokens.REXSTER_SHOW_TYPES_EQUALS_TRUE);

        JSONObject typedProperty = (JSONObject) rawElement.get(key);
        if (null != typedProperty)
            return RestHelper.typeCast((String) typedProperty.get(RexsterTokens.TYPE), typedProperty.get(RexsterTokens.VALUE));
        else
            return null;

    }

    public void setProperty(final String key, final Object value) {
        if (key.startsWith(RexsterTokens.UNDERSCORE))
            throw new RuntimeException("RexsterGraph does not support property keys that start with underscore");

        if (this instanceof Vertex) {
            RestHelper.postResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId() + RexsterTokens.QUESTION + key + RexsterTokens.EQUALS + RestHelper.uriCast(value));
        } else {
            RestHelper.postResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + this.getId() + RexsterTokens.QUESTION + key + RexsterTokens.EQUALS + RestHelper.uriCast(value));
        }
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public Object removeProperty(final String key) {
        Object object = this.getProperty(key);

        if (this instanceof Vertex)
            RestHelper.delete(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId() + RexsterTokens.QUESTION + key);
        else
            RestHelper.delete(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + this.getId() + RexsterTokens.QUESTION + key);

        return object;
    }

    public boolean equals(final Object object) {
        return (this.getClass().equals(object.getClass()) && this.getId().equals(((Element) object).getId()));
    }

}
