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

    protected JSONObject rawElement;
    protected final RexsterGraph graph;

    public RexsterElement(JSONObject rawElement, RexsterGraph graph) {
        this.rawElement = rawElement;
        this.graph = graph;
    }

    public Object getId() {
        return rawElement.get(RexsterTokens._ID);
    }

    public Set<String> getPropertyKeys() {
        Set<String> keys = new HashSet<String>();
        keys.addAll(this.rawElement.keySet());
        keys.remove(RexsterTokens._TYPE);
        keys.remove(RexsterTokens._LABEL);
        keys.remove(RexsterTokens._ID);
        keys.remove(RexsterTokens._OUTE);
        keys.remove(RexsterTokens._INE);
        keys.remove(RexsterTokens._OUTV);
        keys.remove(RexsterTokens._INV);
        return keys;
    }

    public Object getProperty(String key) {
        if (this instanceof Vertex)
            this.rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId());
        else
            this.rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + this.getId());
        return this.rawElement.get(key);
    }

    public void setProperty(String key, Object value) {
        if (key.startsWith(RexsterTokens.UNDERSCORE))
            throw new RuntimeException("RexsterGraph does not support property keys that start with underscore");

        if (this instanceof Vertex) {
            RestHelper.postResultObjectForm(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + this.getId(), key + RexsterTokens.EQUALS + value);
        } else {
            RestHelper.postResultObjectForm(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + this.getId(), key + RexsterTokens.EQUALS + value);
        }
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public Object removeProperty(String key) {
        throw new UnsupportedOperationException("Removing properties is currently unsupported");
    }
}
