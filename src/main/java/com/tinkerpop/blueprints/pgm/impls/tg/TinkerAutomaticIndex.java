package com.tinkerpop.blueprints.pgm.impls.tg;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerAutomaticIndex<T extends TinkerElement> extends TinkerIndex<T> implements AutomaticIndex<T> {

    Set<String> autoIndexKeys = null;

    public TinkerAutomaticIndex(String name, Class<T> indexClass, Set<String> autoIndexKeys) {
        super(name, indexClass);
        if (null != autoIndexKeys) {
            this.autoIndexKeys = new HashSet<String>();
            this.autoIndexKeys.addAll(autoIndexKeys);
        }
    }

    public void addAutoIndexKey(String key) {
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

    public boolean doAutoIndex(String key) {
        return this.autoIndexKeys == null || this.autoIndexKeys.contains(key);
    }

    public void removeAutoIndexKey(String key) {
        if (null != autoIndexKeys)
            this.autoIndexKeys.remove(key);
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }
}
