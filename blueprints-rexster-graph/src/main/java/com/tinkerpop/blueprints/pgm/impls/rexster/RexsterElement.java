package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RexsterObjectSequence; //PDW

import org.json.simple.JSONObject;
import org.json.simple.JSONArray; //PDW

import java.util.HashSet;
import java.util.Set;
import java.net.URLEncoder; //PDW

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class RexsterElement implements Element {

    // protected final Object id;
    protected final RexsterGraph graph;
    protected JSONObject rawElement; //PDW

    public RexsterElement(final JSONObject rawElement, final RexsterGraph graph) {
        // this.id = rawElement.get(RexsterTokens._ID);
        this.graph = graph;
        this.rawElement = rawElement; //PDW
    }

    public Object getId() {
        return rawElement.get(RexsterTokens._ID);
        // return this.id;
    }

    public Set<String> getPropertyKeys() {
        JSONObject rawElement = this.rawElement; //PDW
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
        return this.rawElement.get(key);
        // JSONObject rawElement;
        // if (this instanceof Vertex)
        //     rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId() + RexsterTokens.QUESTION + RexsterTokens.REXSTER_SHOW_TYPES_EQUALS_TRUE);
        // else
        //     rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + this.getId() + RexsterTokens.QUESTION + RexsterTokens.REXSTER_SHOW_TYPES_EQUALS_TRUE);
        // 
        // JSONObject typedProperty = (JSONObject) rawElement.get(key);
        // if (null != typedProperty)
        //     return RestHelper.typeCast((String) typedProperty.get(RexsterTokens.TYPE), typedProperty.get(RexsterTokens.VALUE));
        // else
        //     return null;        
    }

    public void setProperty(final String key, final Object value) {
        if (key.equals(StringFactory.ID) || (key.equals(StringFactory.LABEL) && this instanceof Edge))
            throw new RuntimeException(key + StringFactory.PROPERTY_EXCEPTION_MESSAGE);

        if (key.startsWith(RexsterTokens.UNDERSCORE))
            throw new RuntimeException("RexsterGraph does not support property keys that start with underscore");

        if (this instanceof Vertex) {
            RestHelper.postResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId() + RexsterTokens.QUESTION + key + RexsterTokens.EQUALS + RestHelper.uriCast(value));
        } else {
            RestHelper.postResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + this.getId() + RexsterTokens.QUESTION + key + RexsterTokens.EQUALS + RestHelper.uriCast(value));
        }
        this.rawElement.put(key, value); //PDW
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public Object removeProperty(final String key) {
        Object object = this.rawElement.remove(key); //PDW
        // Object object = this.getProperty(key);

        if (this instanceof Vertex)
            RestHelper.delete(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId() + RexsterTokens.QUESTION + key);
        else
            RestHelper.delete(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + this.getId() + RexsterTokens.QUESTION + key);

        return object;
    }

    public boolean equals(final Object object) {
        return (this.getClass().equals(object.getClass()) && this.getId().equals(((Element) object).getId()));
    }
    
    //PDW v.runGremlin(script) and e.runGremlin(script)
	public Iterable<Object> runGremlin(final String script) {
		Iterable<Object> results = null;

        try {
    	    if (this instanceof Vertex)
    	        results = new RexsterObjectSequence(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId() + RexsterTokens.GREMLIN_EXTENSION + RexsterTokens.QUESTION + RexsterTokens.SCRIPT_EQUALS + URLEncoder.encode(script), this.graph);
    	    else
    	        results = new RexsterObjectSequence(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + this.getId() + RexsterTokens.GREMLIN_EXTENSION + RexsterTokens.QUESTION + RexsterTokens.SCRIPT_EQUALS + URLEncoder.encode(script), this.graph);
        } catch (Exception e) {
            throw new RuntimeException("Could not run Gremlin script: " + script);
        }

		return results;
	}
	
}
