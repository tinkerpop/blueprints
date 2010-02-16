package com.tinkerpop.blueprints.odm.impls.mongodb;

import com.tinkerpop.blueprints.odm.Document;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;

import java.util.Map;

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
        Object nestedObject = object.get(key);
        if(nestedObject instanceof DBObject) {
            return new MongoDocument((DBObject)nestedObject);
        } else {
            return nestedObject;
        }
    }

    public Object put(String key, Object value) {
        return this.object.put(key, value);
    }

    public Iterable<String> keys() {
        return this.object.keySet();
    }

    public Map asMap() {
        return this.object.toMap();
    }

    public DBObject getRawObject() {
        return this.object;
    }

}
