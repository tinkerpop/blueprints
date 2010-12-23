package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.record.ORecordTrackedList;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrientAutomaticIndex<T extends OrientElement> extends OrientIndex<T> implements AutomaticIndex<T> {

    Set<String> autoIndexKeys = new HashSet<String>();
    private static final String INDEX_EVERYTHING = "index_everything";
    private String indexEverything = "default_index_everything";
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
            this.indexEverything = INDEX_EVERYTHING;
        else {
            this.indexEverything = null;
            this.autoIndexKeys.add(key);
        }

        this.saveConfiguration();
    }

    public void removeElement(final T element) {
        for (String key : autoIndexKeys) {
            Object value;
            if (Edge.class.isAssignableFrom(this.getIndexClass()) && key == AutomaticIndex.LABEL)
                value = ((OrientEdge) element).getLabel();
            else
                value = element.getProperty(key);
            if (value != null) {
                this.remove(key, value, element);
            }
        }
    }

    public void addElement(final T element) {
        for (String key : autoIndexKeys) {
            Object value;
            if (Edge.class.isAssignableFrom(this.getIndexClass()) && key == AutomaticIndex.LABEL)
                value = ((OrientEdge) element).getLabel();
            else
                value = element.getProperty(key);
            if (value != null) {
                this.put(key, value, element);
            }
        }
    }

    protected void autoUpdate(final String key, final Object newValue, final Object oldValue, final T element) {
        if (this.indexEverything != null && !this.autoIndexKeys.contains(key)) {
            this.autoIndexKeys.add(key);
            this.saveConfiguration();
        }
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.autoIndexKeys.contains(key))) {
            if (oldValue != null)
                this.remove(key, oldValue, element);
            this.put(key, newValue, element);
        }
    }

    protected void autoRemove(final String key, final Object oldValue, final T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && this.autoIndexKeys.contains(key)) {
            this.remove(key, oldValue, element);
        }
    }

    public void removeAutoIndexKey(final String key) {
        this.autoIndexKeys.remove(key);
        this.saveConfiguration();
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }

    private void init() {
        ORecordTrackedList field = indexCfg.field(KEYS);
        if (null != field) {
            this.indexEverything = indexCfg.field(INDEX_EVERYTHING);
            for (Object key : field) {
                this.autoIndexKeys.add((String) key);
            }
        }
    }

    private void saveConfiguration() {
        indexCfg.field(KEYS, this.autoIndexKeys);
        indexCfg.field(INDEX_EVERYTHING, this.indexEverything);
        graph.saveIndexConfiguration();
    }
}
