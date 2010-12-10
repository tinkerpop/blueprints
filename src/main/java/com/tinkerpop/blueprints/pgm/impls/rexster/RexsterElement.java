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
            return typeCast((String) typedProperty.get("type"), typedProperty.get("value"));
        else
            return null;

    }

    public void setProperty(final String key, final Object value) {
        if (key.startsWith(RexsterTokens.UNDERSCORE))
            throw new RuntimeException("RexsterGraph does not support property keys that start with underscore");

        if (this instanceof Vertex) {
            RestHelper.postResultObjectForm(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId(), key + RexsterTokens.EQUALS + uriCast(value));
        } else {
            RestHelper.postResultObjectForm(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + this.getId(), key + RexsterTokens.EQUALS + uriCast(value));
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

    private Object typeCast(final String type, final Object value) {
        if (type.equals(RexsterTokens.STRING))
            return value;
        else if (type.equals(RexsterTokens.INTEGER))
            return Integer.valueOf(value.toString());
        else if (type.equals(RexsterTokens.LONG))
            return Long.valueOf(value.toString());
        else if (type.equals(RexsterTokens.DOUBLE))
            return Double.valueOf(value.toString());
        else if (type.equals(RexsterTokens.FLOAT))
            return Float.valueOf(value.toString());
        else
            return value;
    }

    private String uriCast(final Object value) {
        if (value instanceof String)
            return value.toString();
        else if (value instanceof Integer)
            return "(" + RexsterTokens.INTEGER + "," + value + ")";
        else if (value instanceof Long)
            return "(" + RexsterTokens.LONG + "," + value + ")";
        else if (value instanceof Double)
            return "(" + RexsterTokens.DOUBLE + "," + value + ")";
        else if (value instanceof Float)
            return "(" + RexsterTokens.FLOAT + "," + value + ")";
        else
            return value.toString();

    }
}
