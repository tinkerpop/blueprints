package com.tinkerpop.blueprints.odm.impls.mongodb;

import com.tinkerpop.blueprints.odm.Document;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MongoDocument implements Document<DBObject> {

    DBObject object;

    public MongoDocument(DBObject object) {
        this.object = object;    
    }

    public MongoDocument() {
        this.object = new BasicDBObject();
    }

    public Object get(String key) {
        return object.get(key);
    }

    public Object put(String key, Object value) {
        return this.object.put(key, value);
    }

    public Iterable<String> keys() {
        return this.object.keySet();
    }

    public DBObject getRawObject() {
        return this.object;
    }

}
