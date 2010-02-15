package com.tinkerpop.blueprints.odm.impls.mongodb;

import com.mongodb.*;
import com.tinkerpop.blueprints.odm.Store;
import com.tinkerpop.blueprints.odm.Document;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MongoStore implements Store<MongoDocument> {

    private Mongo mongo;
    private DB database;
    private DBCollection collection;

    private static final String ID = "_id";

    public MongoStore(final String host, final int port, final String database, final String collection) throws UnknownHostException {
        this.mongo = new Mongo(host, port);
        this.database = this.mongo.getDB(database);
        this.collection = this.database.getCollection(collection);
    }

    public MongoDocument retrieve(final String id) {
        DBObject queryObject = new BasicDBObject();
        queryObject.put(ID, id);
        return new MongoDocument(this.collection.findOne(queryObject));
    }

    public Iterable<MongoDocument> retrieve(final MongoDocument document) {
        DBCursor cursor = this.collection.find(document.getRawObject());
        return new MongoIterable(cursor);
    }

    public void save(final MongoDocument document) {
        this.collection.insert(document.getRawObject());
    }

    public void delete(final MongoDocument document) {
        this.collection.remove(document.getRawObject());
    }

    public MongoDocument makeDocument(Map map) {
        return new MongoDocument(new BasicDBObject(map));
    }

    public void shutdown() {
        // TODO: what is needed to shutdown a connection in MongoDB?
    }

    private class MongoIterable implements Iterable<MongoDocument> {

        private final DBCursor cursor;

        public MongoIterable(DBCursor cursor) {
            this.cursor = cursor;
        }

        public Iterator<MongoDocument> iterator() {
            return new MongoIterator(this.cursor);
        }
    }

    private class MongoIterator implements Iterator<MongoDocument> {

        private final DBCursor cursor;

        public MongoIterator(DBCursor cursor) {
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
    }
}
