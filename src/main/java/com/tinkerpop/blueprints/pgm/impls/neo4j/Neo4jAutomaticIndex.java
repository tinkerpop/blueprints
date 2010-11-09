package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import org.neo4j.graphdb.PropertyContainer;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jAutomaticIndex<T extends Neo4jElement, S extends PropertyContainer> extends Neo4jIndex<T, S> implements AutomaticIndex<T> {

    Set<String> autoIndexKeys = null;

    public Neo4jAutomaticIndex(String name, Class<T> indexClass, Neo4jGraph graph) {
        super(name, indexClass, graph);
        this.loadKeyField();
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
        this.saveKeyField();
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
        this.saveKeyField();
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }

    private void saveKeyField() {
        this.graph.autoStartTransaction();
        if (this.autoIndexKeys == null)
            this.getIndexManager().removeConfiguration(this.neo4jIndex, Neo4jTokens.BLUEPRINTS_AUTOKEYS);
        else {
            String field = "";
            for (String key : autoIndexKeys) {
                field = field + Neo4jTokens.KEY_SEPARATOR + key;
            }
            this.getIndexManager().setConfiguration(this.neo4jIndex, Neo4jTokens.BLUEPRINTS_AUTOKEYS, field);
        }
        this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
    }

    private void loadKeyField() {
        String keysString = this.getIndexManager().getConfiguration(this.neo4jIndex).get(Neo4jTokens.BLUEPRINTS_AUTOKEYS);
        if (null == keysString)
            this.autoIndexKeys = null;
        else {
            this.autoIndexKeys = new HashSet<String>();
            String[] keys = keysString.split(Neo4jTokens.KEY_SEPARATOR);
            for (String key : keys) {
                if (key.length() > 0) {
                    this.autoIndexKeys.add(key);
                }
            }
            if (this.autoIndexKeys.size() == 0)
                this.autoIndexKeys = null;
        }
    }


}