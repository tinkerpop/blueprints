package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrientAutomaticIndex<T extends OrientElement> extends OrientIndex<T> implements AutomaticIndex<T> {

    Set<String> autoIndexKeys = null;

    public OrientAutomaticIndex(String name, Class<T> indexClass, Set<String> autoIndexKeys, OrientGraph graph) {
        super(name, indexClass, graph);
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

    public boolean doAutoIndex(String key, Class classToIndex) {
        return this.getIndexClass().isAssignableFrom(classToIndex) && (this.autoIndexKeys == null || this.autoIndexKeys.contains(key));
    }

    public void removeAutoIndexKey(String key) {
        if (null != autoIndexKeys)
            this.autoIndexKeys.remove(key);
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }
}