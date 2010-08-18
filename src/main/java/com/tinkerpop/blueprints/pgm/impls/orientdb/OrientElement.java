package com.tinkerpop.blueprints.pgm.impls.orientdb;

import java.util.Set;

import com.orientechnologies.orient.core.db.graph.OGraphElement;
import com.orientechnologies.orient.core.id.ORID;
import com.tinkerpop.blueprints.pgm.Element;

public abstract class OrientElement implements Element {
	private static final String	LABEL	= "label";
	protected OGraphElement			raw;

	protected OrientElement(final OGraphElement iElement) {
		raw = iElement;
	}

	public void setProperty(final String key, final Object value) {
		raw.set(key, value);
		save();
	}

	public Object removeProperty(final String key) {
		final Object old = raw.remove(key);
		save();
		return old;
	}

	public Object getProperty(final String key) {
		return raw.get(key);
	}

	public Set<String> getPropertyKeys() {
		final Set<String> set = raw.propertyNames();
		set.remove(LABEL);
		return set;
	}

	/**
	 * Returns the Element Id assuring to save it if it's transient yet.
	 */
	public Object getId() {
		ORID rid = raw.getId();
		save();
		return rid;
	}

	public void delete() {
		raw.delete();
	}

	public void save() {
		raw.save();
	}

	public String getLabel() {
		return (String) raw.get(LABEL);
	}

	public void setLabel(String label) {
		raw.set(LABEL, label);
		save();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((raw == null) ? 0 : raw.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrientElement other = (OrientElement) obj;
		if (raw == null) {
			if (other.raw != null)
				return false;
		} else if (!raw.equals(other.raw))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return raw != null ? raw.toString() : "";
	}
}
