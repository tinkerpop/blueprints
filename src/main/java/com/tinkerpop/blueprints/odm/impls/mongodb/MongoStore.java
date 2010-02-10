package com.tinkerpop.blueprints.odm.impls.mongodb;

import com.mongodb.Mongo;
import com.mongodb.DB;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.tinkerpop.blueprints.odm.Store;
import com.tinkerpop.blueprints.odm.Document;

import java.net.UnknownHostException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MongoStore implements Store {

    private Mongo mongo;
    private DB database;

    public MongoStore(String host, int port, String database) throws UnknownHostException {
        this.mongo = new Mongo(host, port);
        this.database = this.mongo.getDB(database);
    }

    public Document getDocument(String collection, String id) {
        DBObject idObject = new BasicDBObject("_id", id);
        DBObject returnObject = this.database.getCollection(collection).findOne(idObject);
        return null;
    }

    public void putDocument(Document document) {

    }

    public void removeDocument(Document document) {

    }

    public void shutdown() {
        this.mongo = null;
    }
}
