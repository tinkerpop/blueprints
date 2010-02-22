package com.tinkerpop.blueprints.odm.impls.tinkerdoc;

import com.tinkerpop.blueprints.odm.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerDocument implements Document<Map<String, Object>> {

    private Map<String, Object> map = new HashMap<String, Object>();

    public TinkerDocument(Map<String, Object> map) {
        this.map = map;
    }

    public Object put(String key, Object value) {
        return this.map.put(key, value);
    }

    public Object get(String key) {
        Object nestedObject = this.map.get(key);
        if (nestedObject instanceof Map) {
            return new TinkerDocument((Map) nestedObject);
        } else {
            return nestedObject;
        }
    }

    public Iterable<String> keys() {
        return this.map.keySet();
    }

    public Map<String, Object> asMap() {
        return this.map;
    }

    public Map<String, Object> getRawObject() {
        return this.map;
    }


}
