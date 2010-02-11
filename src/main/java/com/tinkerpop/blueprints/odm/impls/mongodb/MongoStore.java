package com.tinkerpop.blueprints.odm.impls.mongodb;

import com.mongodb.*;
import com.tinkerpop.blueprints.odm.Store;

import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MongoStore implements Store {

    private Mongo mongo;
    private DB database;
    private DBCollection collection;

    public MongoStore(String host, int port, String database, String collection) throws UnknownHostException {
        this.mongo = new Mongo(host, port);
        this.database = this.mongo.getDB(database);
        this.collection = this.database.getCollection(collection);
    }

    public Map get(Map document) {
        DBObject idObject = new BasicDBObject(document);
        DBObject returnObject = this.collection.findOne(idObject);
        return returnObject.toMap();
    }

    public void put(Map document) {
        DBObject dbObject = new BasicDBObject(document);
        this.collection.insert(dbObject);

    }

    public void remove(Map document) {
        DBObject dbObject = new BasicDBObject(document);
        this.collection.remove(dbObject);
    }

    public void shutdown() {
        // TODO: what is needed to shutdown a connection in MongoDB?
    }
}
