package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import org.neo4j.graphdb.PropertyContainer;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jAutomaticIndex<T extends Neo4jElement, S extends PropertyContainer> extends Neo4jIndex<T, S> implements AutomaticIndex<T> {

    Set<String> autoIndexKeys = null;

    public Neo4jAutomaticIndex(String name, Class<T> indexClass, Set<String> autoIndexKeys, Neo4jGraph graph) {
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