package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import org.neo4j.graphdb.PropertyContainer;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jAutomaticIndex<T extends Neo4jElement, S extends PropertyContainer> extends Neo4jIndex<T, S> implements AutomaticIndex<T> {

    Set<String> autoIndexKeys;

    public Neo4jAutomaticIndex(final String name, final Class<T> indexClass, final Neo4jGraph graph, final Set<String> keys) {
        super(name, indexClass, graph);
        if (!this.loadKeyField()) {
            if (null != keys) {
                this.autoIndexKeys = new HashSet<String>();
                this.autoIndexKeys.addAll(keys);
            }
            this.saveKeyField();
        }
    }

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }

    protected void autoUpdate(final String key, final Object newValue, final Object oldValue, final T element) {
        if (null == this.autoIndexKeys || this.autoIndexKeys.contains(key)) {
            if (oldValue != null)
                this.removeBasic(key, oldValue, element);
            this.putBasic(key, newValue, element);
        }
    }

    protected void autoRemove(final String key, final Object oldValue, final T element) {
        if (null == this.autoIndexKeys || this.autoIndexKeys.contains(key)) {
            this.removeBasic(key, oldValue, element);
        }
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }

    private void saveKeyField() {
        try {
            //this.graph.autoStartTransaction();
            String field;
            if (null != this.autoIndexKeys) {
                field = "";
                for (final String key : this.autoIndexKeys) {
                    field = field + Neo4jTokens.KEY_SEPARATOR + key;
                }
            } else {
                field = "null";
            }
            this.getIndexManager().setConfiguration(this.rawIndex, Neo4jTokens.BLUEPRINTS_AUTOKEYS, field);

            //this.graph.autoStopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        } catch (final RuntimeException e) {
            //this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw e;
        } catch (final Exception e) {
            //this.graph.autoStopTransaction(TransactionalGraph.Conclusion.FAILURE);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private boolean loadKeyField() {
        final String keysString = this.getIndexManager().getConfiguration(this.rawIndex).get(Neo4jTokens.BLUEPRINTS_AUTOKEYS);
        if (null != keysString) {
            if (keysString.equals("null")) {
                this.autoIndexKeys = null;
            } else {
                this.autoIndexKeys = new HashSet<String>();
                for (final String key : keysString.split(Neo4jTokens.KEY_SEPARATOR)) {
                    if (key.length() > 0) {
                        this.autoIndexKeys.add(key);
                    }
                }
            }
            return true; // index previously existed
        } else {
            return false; // index did not exist
        }
    }

}
