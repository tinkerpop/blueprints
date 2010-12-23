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

    Set<String> autoIndexKeys = null;
    private static final String KEYS = "keys";

    public OrientAutomaticIndex(final String name, final Class<T> indexClass, final OrientGraph graph, final ODocument indexCfg) {
        super(name, indexClass, graph, indexCfg);
        init();
    }

    public OrientAutomaticIndex(final String name, final OrientGraph graph, final ODocument indexCfg) {
        super(name, null, graph, indexCfg);
        init();

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

        this.saveConfiguration();
    }

    public void removeElement(final T element) {
        if (autoIndexKeys == null) return;
        for (String key : autoIndexKeys) {
            Object value;
            if (key == AutomaticIndex.LABEL)
                value = ((OrientEdge) element).getLabel();
            else
                value = element.getProperty(key);
            if (value != null) {
                this.remove(key, value, element);
            }
        }
    }

    public void addElement(final T element) {
        if (autoIndexKeys == null) return;
        for (String key : autoIndexKeys) {
            Object value;
            if (key == AutomaticIndex.LABEL)
                value = ((OrientEdge) element).getLabel();
            else
                value = element.getProperty(key);
            if (value != null) {
                this.put(key, value, element);
            }
        }
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

    public void removeAutoIndexKey(final String key) {
        if (null != this.autoIndexKeys)
            this.autoIndexKeys.remove(key);

        this.saveConfiguration();
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }

    private void init() {
        ORecordTrackedList field = indexCfg.field(KEYS);
        if (null == field)
            this.autoIndexKeys = null;
        else {
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
