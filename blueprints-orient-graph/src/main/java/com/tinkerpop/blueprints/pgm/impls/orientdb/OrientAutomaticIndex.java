package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.index.OIndex;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Index;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrientAutomaticIndex<T extends OrientElement> extends OrientIndex<T> implements AutomaticIndex<T> {

    Set<String> autoIndexKeys = null;
    private static final String KEYS = "keys";

    public OrientAutomaticIndex(final OrientGraph graph, final String indexName, final Class<T> indexClass, Set<String> indexKeys) {
        super(graph, indexName, indexClass, Index.Type.AUTOMATIC, null);
        if (indexKeys != null)
            autoIndexKeys = new HashSet<String>(indexKeys);
        init();
        saveConfiguration();
    }

    public OrientAutomaticIndex(OrientGraph graph, OIndex rawIndex) {
        super(graph, rawIndex);
        init();
    }

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }

    protected void autoUpdate(final String key, final Object newValue, final Object oldValue, final T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {

            if (oldValue != null)
                this.removeBasic(key + SEPARATOR + oldValue, element);
            this.putBasic(key + SEPARATOR + newValue, element);
        }
    }

    protected void autoRemove(final String key, final Object oldValue, final T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {
            this.removeBasic(key + SEPARATOR + oldValue, element);
        }
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }

    private void init() {
        final Collection<Object> field = underlying.getConfiguration().field(KEYS);
        if (null != field) {
            this.autoIndexKeys = new HashSet<String>();
            for (Object key : field) {
                this.autoIndexKeys.add((String) key);
            }
        }
    }

    private void saveConfiguration() {
        underlying.getConfiguration().field(KEYS, this.autoIndexKeys);
        graph.saveIndexConfiguration();
    }
}
