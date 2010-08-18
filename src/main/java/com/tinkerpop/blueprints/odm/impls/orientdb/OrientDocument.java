package com.tinkerpop.blueprints.odm.impls.orientdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.odm.Document;

public class OrientDocument implements Document<ODocument> {
	private ODocument	raw;

	public OrientDocument() {
		raw = new ODocument();
	}

	public OrientDocument(final OrientStore iStore) {
		raw = new ODocument(iStore.getDatabase());
	}

	public OrientDocument(final ODocument iRawDocument) {
		raw = iRawDocument;
	}

	public Object put(final String key, final Object value) {
		return raw.field(key, value);
	}

	public Object get(final String key) {
		return raw.field(key);
	}

	public Iterable<String> keys() {
		final ArrayList<String> list = new ArrayList<String>();
		for (String f : raw.fieldNames())
			list.add(f);
		return list;
	}

	public Map<String, Object> asMap() {
		final HashMap<String, Object> map = new HashMap<String, Object>();

		for (String f : raw.fieldNames())
			map.put(f, raw.field(f));

		return map;
	}

	public ODocument getRawObject() {
		return raw;
	}

}
