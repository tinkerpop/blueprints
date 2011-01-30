package com.tinkerpop.blueprints.pgm.impls.orientdb;

import java.util.HashSet;
import java.util.Set;

import com.orientechnologies.orient.core.db.record.ORecordTrackedList;
import com.orientechnologies.orient.core.index.OIndex;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Index;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrientAutomaticIndex<T extends OrientElement> extends OrientIndex<T> implements AutomaticIndex<T> {

	Set<String>									autoIndexKeys	= null;
	private static final String	KEYS					= "keys";

	public OrientAutomaticIndex(final OrientGraph iGraph, final String indexName, final Class<T> iIndexClass, Set<String> indexKeys) {
		super(iGraph, indexName, iIndexClass, Index.Type.AUTOMATIC);
		if (indexKeys != null)
			autoIndexKeys = new HashSet<String>(indexKeys);
		init();
		saveConfiguration();
	}

	public OrientAutomaticIndex(OrientGraph iGraph, OIndex iIndex) {
		super(iGraph, iIndex);
		init();
	}

	public Type getIndexType() {
		return Type.AUTOMATIC;
	}

	protected void autoUpdate(final String key, final Object newValue, final Object oldValue, final T element) {
		if (this.getIndexClass().isAssignableFrom(element.getClass())
				&& (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {
			if (oldValue != null)
				this.remove(key, oldValue, element);
			this.put(key, newValue, element);
		}
	}

	protected void autoRemove(final String key, final Object oldValue, final T element) {
		if (this.getIndexClass().isAssignableFrom(element.getClass())
				&& (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {
			this.remove(key, oldValue, element);
		}
	}

	public Set<String> getAutoIndexKeys() {
		return this.autoIndexKeys;
	}

	private void init() {
		ORecordTrackedList field = underlying.getConfiguration().field(KEYS);
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
