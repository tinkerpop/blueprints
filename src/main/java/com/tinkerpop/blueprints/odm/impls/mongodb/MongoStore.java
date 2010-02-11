package com.tinkerpop.blueprints.odm.impls.mongodb;

import com.mongodb.*;
import com.tinkerpop.blueprints.odm.Store;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MongoStore implements Store {

    private Mongo mongo;
    private DB database;
    private DBCollection collection;

    private static final String ID = "_id";

    public MongoStore(final String host, final int port, final String database, final String collection) throws UnknownHostException {
        this.mongo = new Mongo(host, port);
        this.database = this.mongo.getDB(database);
        this.collection = this.database.getCollection(collection);
    }

    public Map get(final Map document) {
        DBObject idObject = new BasicDBObject(document);
        DBObject returnObject = this.collection.findOne(idObject);
        return returnObject.toMap();
    }

    public Map get(final String id) {
        Map map = new HashMap();
        map.put(ID, id);
        return this.get(map);
    }

    public Iterable<Map> getAll(final Map document) {
        DBCursor cursor = this.collection.find(new BasicDBObject(document));
        return new MongoIterable(cursor);
    }

    public void put(final Map document) {
        DBObject dbObject = new BasicDBObject(document);
        this.collection.insert(dbObject);

    }

    public void remove(final Map document) {
        DBObject dbObject = new BasicDBObject(document);
        this.collection.remove(dbObject);
    }

    public void shutdown() {
        // TODO: what is needed to shutdown a connection in MongoDB?
    }

    private class MongoIterable implements Iterable<Map> {

        private final DBCursor cursor;

        public MongoIterable(DBCursor cursor) {
            this.cursor = cursor;
        }

        public Iterator<Map> iterator() {
            return new MongoIterator(this.cursor);
        }
    }

    private class MongoIterator implements Iterator<Map> {

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

        public Map next() {
            return this.cursor.next().toMap();
        }
    }
}
