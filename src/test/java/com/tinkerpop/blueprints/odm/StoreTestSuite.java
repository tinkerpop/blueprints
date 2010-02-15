package com.tinkerpop.blueprints.odm;

import java.util.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class StoreTestSuite extends ModelTestSuite {

    public StoreTestSuite() {
    }

    public StoreTestSuite(final SuiteConfiguration config) {
        super(config);
    }

    public void testAddingSimpleDocuments(final Store<Document> store) {
        int total = 20;
        List<String> ids = new ArrayList<String>();
        for (int i = 0; i < total; i++) {
            String id = UUID.randomUUID().toString();
            ids.add(id);
            Map document = new HashMap();
            document.put(config.id, id);
            document.put("name", "marko");
            document.put("age", 30);
            document.put("index", i);
            store.save(store.makeDocument(document));
        }
        assertEquals(ids.size(), total);
        for (int i = 0; i < total; i++) {
            Map map = new HashMap();
            map.put(config.id, ids.get(i));
            Document document = store.retrieve(store.makeDocument(map)).iterator().next();
            assertEquals(document.get(config.id), ids.get(i));
            assertEquals(document.get("index"), i);
            assertEquals(document.get("name"), "marko");
            assertEquals(document.get("age"), 30);
        }
    }

}
