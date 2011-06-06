package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.impls.rexster.util.RestHelper;
import org.codehaus.jettison.json.JSONArray;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterAutomaticIndex<T extends Element> extends RexsterIndex<T> implements AutomaticIndex<T> {

    public RexsterAutomaticIndex(final RexsterGraph graph, final String name, final Class<T> indexClass) {
        super(graph, name, indexClass);
    }

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }

    public Set<String> getAutoIndexKeys() {
        Set<String> keys = new HashSet<String>();
        JSONArray array = RestHelper.getResultArray(this.graph.getGraphURI() + RexsterTokens.SLASH_INDICES_SLASH + RestHelper.encode(this.indexName) + RexsterTokens.SLASH_KEYS);

        for (int ix = 0; ix < array.length(); ix++) {
            keys.add(array.optString(ix));
        }

        if (keys.size() == 1 && null == keys.iterator().next())
            return null;
        else
            return keys;
    }

}
