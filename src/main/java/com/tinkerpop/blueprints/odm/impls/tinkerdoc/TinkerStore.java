package com.tinkerpop.blueprints.odm.impls.tinkerdoc;

import com.tinkerpop.blueprints.odm.Store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerStore implements Store<TinkerDocument> {

    private static final String ID = "_id";
    private HashMap<String, TinkerDocument> store = new HashMap<String, TinkerDocument>();

    public TinkerDocument makeDocument(Map<String, Object> map) {
        return new TinkerDocument(map);
    }

    public TinkerDocument retrieve(String id) {
        return this.store.get(id);
    }

    public Iterable<TinkerDocument> retrieve(TinkerDocument document) {
        HashSet<TinkerDocument> returnDoc = new HashSet<TinkerDocument>();
        returnDoc.add(this.store.get((String) document.get(ID)));
        return returnDoc;
    }

    public void save(TinkerDocument document) {
        Object id = document.get(ID);
        if (null == id)
            document.put(ID, UUID.randomUUID());
        this.store.put((String) document.get(ID), document);
    }

    public void delete(TinkerDocument document) {
        this.store.remove((String) document.get(ID));
    }

    public void shutdown() {
        this.store.clear();
    }
}
