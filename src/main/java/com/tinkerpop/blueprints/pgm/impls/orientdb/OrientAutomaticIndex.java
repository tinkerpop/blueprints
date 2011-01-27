package com.tinkerpop.blueprints.pgm.impls.orientdb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.orientechnologies.orient.core.index.OIndex;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrientAutomaticIndex<T extends OrientElement> extends OrientIndex<T> implements AutomaticIndex<T> {

	Set<String>									autoIndexKeys		= new HashSet<String>();
	private boolean							indexEverything	= true;
	private static final String	KEYS						= "keys";

	public OrientAutomaticIndex(final OrientGraph iGraph, final String indexName, final Class<T> iIndexClass,
			final com.tinkerpop.blueprints.pgm.Index.Type type) {
		super(iGraph, indexName, iIndexClass, type);
		init();
	}

	public OrientAutomaticIndex(OrientGraph iGraph, OIndex iIndex) {
		super(iGraph, iIndex);
		init();
	}

	public Type getIndexType() {
		return Type.AUTOMATIC;
	}

	public void addAutoIndexKey(final String key) {
		if (null == key)
			this.indexEverything = true;
		else {
			this.indexEverything = false;
			this.autoIndexKeys.add(key);
		}

		this.saveConfiguration();
	}

	protected void autoUpdate(final String key, final Object newValue, final Object oldValue, final T element) {
		if (this.indexEverything && !this.autoIndexKeys.contains(key)) {
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
		if (this.indexEverything)
			return null;
		return this.autoIndexKeys;
	}

	public Set<String> getAutoIndexKeysInUse() {
		return this.autoIndexKeys;
	}

	@SuppressWarnings("unchecked")
	private void init() {
		final Object k = underlying.getConfiguration().field(KEYS);

		if (k == null || k instanceof String)
			this.indexEverything = true;
		else {
			this.indexEverything = false;
			if (null != k) {
				List<String> field = (List<String>) k;
				for (String key : field) {
					this.autoIndexKeys.add(key);
				}
			}
		}
	}

	private void saveConfiguration() {
		if (this.indexEverything)
			underlying.getConfiguration().field(KEYS, "*");
		else
			underlying.getConfiguration().field(KEYS, this.autoIndexKeys);
		graph.saveIndexConfiguration();
	}
}
