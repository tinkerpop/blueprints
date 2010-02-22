package com.tinkerpop.blueprints.odm.impls.mongodb;

import com.mongodb.*;
import com.tinkerpop.blueprints.odm.Store;

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MongoStore implements Store<MongoDocument> {

    private DBCollection collection;

    private static final String ID = "_id";

    public MongoStore(final String host, final int port, final String databaseName, final String collectionName) throws UnknownHostException {
        Mongo mongo = new Mongo(host, port);
        DB database = mongo.getDB(databaseName);
        this.collection = database.getCollection(collectionName);
    }

    public MongoDocument retrieve(final String id) {
        DBObject queryObject = new BasicDBObject();
        queryObject.put(ID, id);
        DBObject returnObject = this.collection.findOne(queryObject);
        if(null == returnObject)
            return null;
        else
            return new MongoDocument(returnObject);
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

    public MongoDocument makeDocument(Map<String,Object> map) {
        return new MongoDocument(new BasicDBObject(map));
    }

    public String toString() {
        return "mongostore[" + this.collection.getFullName() + "]";
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
