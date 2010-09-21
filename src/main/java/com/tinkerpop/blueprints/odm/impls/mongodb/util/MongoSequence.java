package com.tinkerpop.blueprints.odm.impls.mongodb.util;

import com.mongodb.DBCursor;
import com.tinkerpop.blueprints.odm.impls.mongodb.MongoDocument;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MongoSequence implements Iterator<MongoDocument>, Iterable<MongoDocument> {

    private final DBCursor cursor;

    public MongoSequence(DBCursor cursor) {
        this.cursor = cursor;
    }

    public boolean hasNext() {
        return this.cursor.hasNext();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public MongoDocument next() {
        return new MongoDocument(this.cursor.next());
    }

    public Iterator<MongoDocument> iterator() {
        return this;
    }
}