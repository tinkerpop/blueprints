package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import org.json.simple.JSONArray;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterAutomaticIndex<T extends Element> extends RexsterIndex<T> implements AutomaticIndex<T> {

    private Set<String> indexKeys;
    
    public RexsterAutomaticIndex(final RexsterGraph graph, final String name, final Class<T> indexClass) {
        super(graph, name, indexClass);
        this.indexKeys = this.setAutoIndexKeys();
    }

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }

    public Set<String> getAutoIndexKeys() {
        return this.indexKeys; //PDW cached indexKeys
    }

    private Set<String> setAutoIndexKeys() {
        Set<String> keys = new HashSet<String>();
        JSONArray array = RestHelper.getResultArray(this.graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + this.indexName + RexsterTokens.SLASH_KEYS);
        for (Object key : array) {
            keys.add((String) key);
        }
        if (keys.size() == 1 && null == keys.iterator().next())
            return null;
        else
            return keys;
    }

}
