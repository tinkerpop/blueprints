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

    protected void autoUpdate(String key, Object newValue, Object oldValue, T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {
            if (oldValue != null)
                this.remove(key, oldValue, element);
            this.put(key, newValue, element);
        }
    }

    protected void autoRemove(String key, Object oldValue, T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {
            this.remove(key, oldValue, element);
        }
    }

    public void removeAutoIndexKey(String key) {
        if (null != autoIndexKeys)
            this.autoIndexKeys.remove(key);
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }

    /*public void updateConfiguration() {
        String ... this.neo4jIndex.getConfiguration()
    }*/

}