package com.tinkerpop.blueprints.odm.impls.orientdb;

import java.util.HashSet;
import java.util.Map;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORecordId;
import com.tinkerpop.blueprints.odm.Store;

public class OrientStore implements Store<OrientDocument> {
	private ODatabaseDocumentTx	database;

	public OrientStore(final String iURL) {
		database = new ODatabaseDocumentTx(iURL);
	}

	public OrientDocument makeDocument(final Map<String, Object> map) {
		return new OrientDocument(this);
	}

	public OrientDocument retrieve(final String id) {
		return new OrientDocument(database.load(new ORecordId(id)));
	}

	public Iterable<OrientDocument> retrieve(final OrientDocument document) {
		document.getRawObject().load();

		final HashSet<OrientDocument> returnDoc = new HashSet<OrientDocument>();
		returnDoc.add(document);
		return returnDoc;
	}

	public void save(final OrientDocument document) {
		database.save(document.getRawObject());
	}

	public void delete(final OrientDocument document) {
		database.delete(document.getRawObject());
	}

	public boolean exists() {
		return database.exists();
	}

	public void create() {
		database.create();
	}

	public void open(final String iUserName, final String iUserPassword) {
		database.open(iUserName, iUserPassword);
	}

	public void shutdown() {
		database.close();
	}

	public ODatabaseDocumentTx getDatabase() {
		return database;
	}
}
