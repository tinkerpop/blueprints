package com.tinkerpop.blueprints.pgm.impls.rexster;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterAutomaticIndex<T extends Element> extends RexsterIndex<T> implements AutomaticIndex<T> {

    Set<String> autoIndexKeys = null;

    public RexsterAutomaticIndex(RexsterGraph graph, String name, Class<T> indexClass) {
        super(graph, name, indexClass);
    }

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }

    public void addAutoIndexKey(final String key) {
        if (null == key)
            this.autoIndexKeys = null;
        else {
            if (autoIndexKeys == null) {
                this.autoIndexKeys = new HashSet<String>();
                this.autoIndexKeys.add(key);
            } else {
                this.autoIndexKeys.add(key);
            }
        }
    }

    public void removeAutoIndexKey(final String key) {
        if (null != autoIndexKeys)
            this.autoIndexKeys.remove(key);
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }
}
