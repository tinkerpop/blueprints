package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.Element;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class RexsterElement implements Element {

    protected final JSONObject rawElement;
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
        return rawElement.get(key);
    }

    public void setProperty(String key, Object value) {

    }

    public Object removeProperty(String key) {
        return null;
    }
}
