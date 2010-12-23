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

    public Neo4jAutomaticIndex(final String name, final Class<T> indexClass, final Neo4jGraph graph) {
        super(name, indexClass, graph);
        this.loadKeyField();
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
        this.saveKeyField();
    }

    public void removeElement(final T element) {
        if (autoIndexKeys == null) return;
        for (String key : autoIndexKeys) {
            Object value;
            if (key == AutomaticIndex.LABEL)
                value = ((Neo4jEdge) element).getLabel();
            else
                value = element.getProperty(key);
            if (value != null) {
                this.removeBasic(key, value, element);
            }
        }
    }

    public void addElement(final T element) {
        if (autoIndexKeys == null) return;
        for (String key: autoIndexKeys) {
            Object value;
            if (key == AutomaticIndex.LABEL)
                value = ((Neo4jEdge) element).getLabel();
            else
                value = element.getProperty(key);
            if (value != null) {
                this.putBasic(key, value, element);
            }
        }
    }

    protected void autoUpdate(final String key, final Object newValue, final Object oldValue, final T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {
            if (oldValue != null)
                this.removeBasic(key, oldValue, element);
            this.putBasic(key, newValue, element);
        }
    }

    protected void autoRemove(final String key, final Object oldValue, final T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {
            this.removeBasic(key, oldValue, element);
        }
    }

    public void removeAutoIndexKey(final String key) {
        if (null != autoIndexKeys)
            this.autoIndexKeys.remove(key);
        this.saveKeyField();
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }

    private void saveKeyField() {
        try {
            this.graph.autoStartTransaction();
            if (this.autoIndexKeys == null)
                this.getIndexManager().removeConfiguration(this.rawIndex, Neo4jTokens.BLUEPRINTS_AUTOKEYS);
            else {
                String field = "";
                for (String key : autoIndexKeys) {
                    field = field + Neo4jTokens.KEY_SEPARATOR + key;
                }
                this.getIndexManager().setConfiguration(this.rawIndex, Neo4jTokens.BLUEPRINTS_AUTOKEYS, field);
            }
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        } catch (RuntimeException e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (Exception e) {
            this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void loadKeyField() {
        String keysString = this.getIndexManager().getConfiguration(this.rawIndex).get(Neo4jTokens.BLUEPRINTS_AUTOKEYS);
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
