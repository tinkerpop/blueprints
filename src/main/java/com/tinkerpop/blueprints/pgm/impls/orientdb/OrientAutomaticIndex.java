package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.record.ORecordTrackedList;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrientAutomaticIndex<T extends OrientElement> extends OrientIndex<T> implements AutomaticIndex<T> {

    Set<String> autoIndexKeys;
    private static final String KEYS = "keys";

    public OrientAutomaticIndex(final String name, final Class<T> indexClass, Set<String> indexKeys, final OrientGraph graph, final ODocument indexCfg) {
        super(name, indexClass, graph, indexCfg);
        if (null == indexKeys)
            this.autoIndexKeys = null;
        else {
            this.autoIndexKeys = new HashSet<String>();
            this.autoIndexKeys.addAll(indexKeys);
        }
        this.init();
        this.saveConfiguration();

    }

    public OrientAutomaticIndex(final String name, final OrientGraph graph, final ODocument indexCfg) {
        super(name, null, graph, indexCfg);
        this.init();

    }

    public Type getIndexType() {
        return Type.AUTOMATIC;
    }


    protected void autoUpdate(final String key, final Object newValue, final Object oldValue, final T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {
            if (oldValue != null)
                this.remove(key, oldValue, element);
            this.put(key, newValue, element);
        }
    }

    protected void autoRemove(final String key, final Object oldValue, final T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {
            this.remove(key, oldValue, element);
        }
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }

    private void init() {
        ORecordTrackedList field = indexCfg.field(KEYS);
        if (null != field) {
            this.autoIndexKeys = new HashSet<String>();
            for (Object key : field) {
                this.autoIndexKeys.add((String) key);
            }
        }
    }

    private void saveConfiguration() {
        indexCfg.field(KEYS, this.autoIndexKeys);
        graph.saveIndexConfiguration();
    }
}
