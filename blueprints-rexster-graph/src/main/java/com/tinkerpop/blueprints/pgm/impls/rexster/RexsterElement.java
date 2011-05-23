package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
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
    protected JSONObject rawElement;
    private Boolean isTypedElement = false;

    public RexsterElement(final JSONObject rawElement, final RexsterGraph graph) {
        this.id = rawElement.get(RexsterTokens._ID);
        this.graph = graph;
        this.rawElement = rawElement; // cache vertex/edge raw JSON
    }

    public Object getId() {
        return this.id;
    }

    protected JSONObject getRawElement() {
        return this.rawElement;
    }

    public Set<String> getPropertyKeys() {
        JSONObject rawElement = this.rawElement;
        // JSONObject rawElement;
        // 
        // if (this instanceof Vertex)
        //     rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId());
        // else
        //     rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + this.getId());

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
        Object value = this.rawElement.get(key);
        if (value instanceof String || this.isTypedElement) // already typed
            return value;
        // code necessary to distinguish Integer-Long and Float-Double
        JSONObject typedElement;
        if (this instanceof Vertex)
            typedElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RexsterTokens.REXSTER_SHOW_TYPES_EQUALS_TRUE);
        else
            typedElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RexsterTokens.REXSTER_SHOW_TYPES_EQUALS_TRUE);
        Set<String> typedKeys = new HashSet<String>();
        typedKeys.addAll(typedElement.keySet());
        typedKeys.remove(RexsterTokens._TYPE); // _type and _label are never typed
        typedKeys.remove(RexsterTokens._LABEL);
        for (String typedKey : typedKeys) {
            JSONObject typedProperty = (JSONObject) typedElement.get(typedKey);
            if (null != typedProperty) {
                Object typedValue = RestHelper.typeCast((String) typedProperty.get(RexsterTokens.TYPE), typedProperty.get(RexsterTokens.VALUE));
                this.rawElement.put(typedKey, typedValue); // save typed value
            }
        }    
        this.isTypedElement = true; // flag as typed
        return this.rawElement.get(key);
    }

    public void setProperty(final String key, final Object value) {
        if (key.equals(StringFactory.ID) || (key.equals(StringFactory.LABEL) && this instanceof Edge))
            throw new RuntimeException(key + StringFactory.PROPERTY_EXCEPTION_MESSAGE);

        if (key.startsWith(RexsterTokens.UNDERSCORE))
            throw new RuntimeException("RexsterGraph does not support property keys that start with underscore");

        if (this instanceof Vertex) {
            RestHelper.postResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RestHelper.encode(key) + RexsterTokens.EQUALS + RestHelper.uriCast(value));
        } else {
            RestHelper.postResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RestHelper.encode(key) + RexsterTokens.EQUALS + RestHelper.uriCast(value));
        }
        this.rawElement.put(key, value);
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public Object removeProperty(final String key) {
        Object object = this.rawElement.remove(key);
        // Object object = this.getProperty(key);

        if (this instanceof Vertex)
            RestHelper.delete(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RestHelper.encode(key));
        else
            RestHelper.delete(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RestHelper.encode(key));

        return object;
    }

    public boolean equals(final Object object) {
        return (this.getClass().equals(object.getClass()) && this.getId().equals(((Element) object).getId()));
    }
    
}
