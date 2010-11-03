package com.tinkerpop.blueprints.pgm.impls.orientdb;

import java.util.HashSet;
import java.util.Set;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrientAutomaticIndex<T extends OrientElement> extends OrientIndex<T> implements AutomaticIndex<T> {

    Set<String> autoIndexKeys = null;

    public OrientAutomaticIndex(String name, Class<T> indexClass, Set<String> autoIndexKeys, OrientGraph graph, ODocument indexCfg) {
        super(name, indexClass, graph, indexCfg);
        init(autoIndexKeys);
    }

    public OrientAutomaticIndex(String name, Set<String> autoIndexKeys, OrientGraph graph, ODocument indexCfg) {
        super(name, null, graph, indexCfg);
        init(autoIndexKeys);
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
        
        saveConfiguration();
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
        if (null != this.autoIndexKeys)
            this.autoIndexKeys.remove(key);

        saveConfiguration();
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }

		private void init(Set<String> autoIndexKeys) {
			if (null != autoIndexKeys) {
			    this.autoIndexKeys = new HashSet<String>();
			    this.autoIndexKeys.addAll(autoIndexKeys);
			}
      indexCfg.field("keys", autoIndexKeys );
		}

		private void saveConfiguration() {
			indexCfg.field("keys", autoIndexKeys );
			graph.saveIndexConfiguration();
		}
}