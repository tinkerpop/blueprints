package com.tinkerpop.blueprints.odm;

import com.tinkerpop.blueprints.BaseTest;

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

    public void testAddingOneLayerDocuments(final Store<Document> store) {
        int total = 2000;
        this.stopWatch();
        List<String> ids = new ArrayList<String>();
        for (int i = 0; i < total; i++) {
            String id = UUID.randomUUID().toString();
            ids.add(id);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(config.id, id);
            map.put("name", "marko");
            map.put("age", 30);
            map.put("index", i);
            store.save(store.makeDocument(map));
        }
        BaseTest.printPerformance(store.toString(), total, "1-layer documents saved", this.stopWatch());
        this.stopWatch();
        assertEquals(ids.size(), total);
        for (int i = 0; i < total; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(config.id, ids.get(i));
            Document document = store.retrieve(store.makeDocument(map)).iterator().next();
            assertEquals(document.get(config.id), ids.get(i));
            assertEquals(document.get("index"), i);
            assertEquals(document.get("name"), "marko");
            assertEquals(document.get("age"), 30);
        }
        BaseTest.printPerformance(store.toString(), total, "1-layer documents read", this.stopWatch());
    }

    public void testAddingTwoLayerDocuments(final Store<Document> store) {
        int total = 2000;
        this.stopWatch();
        List<String> ids = new ArrayList<String>();
        for (int i = 0; i < total; i++) {
            String id = UUID.randomUUID().toString();
            ids.add(id);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(config.id, id);
            map.put("name", "marko");
            map.put("age", 30);
            map.put("index", i);
            Map<String, Object> map2 = new HashMap<String, Object>();
            map2.put("country", "United States");
            map2.put("state", "New Mexico");
            map2.put("city", "Santa Fe");
            map2.put("zipcode", 87501);
            map.put("location", map2);

            store.save(store.makeDocument(map));
        }
        BaseTest.printPerformance(store.toString(), total, "2-layer documents saved", this.stopWatch());
        this.stopWatch();
        assertEquals(ids.size(), total);
        for (int i = 0; i < total; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(config.id, ids.get(i));
            Document document = store.retrieve(store.makeDocument(map)).iterator().next();
            assertEquals(document.get(config.id), ids.get(i));
            assertEquals(document.get("index"), i);
            assertEquals(document.get("name"), "marko");
            assertEquals(document.get("age"), 30);
            Document document2 = (Document)document.get("location");
            assertEquals(document2.get("state"), "New Mexico");
            assertEquals(document2.get("zipcode"), 87501);
        }
        BaseTest.printPerformance(store.toString(), total, "2-layer documents read", this.stopWatch());
    }

    public void testRemovingOneLayerDocuments(Store<Document> store) {
        int total = 2000;
        this.stopWatch();
        List<String> ids = new ArrayList<String>();
        for (int i = 0; i < total; i++) {
            String id = UUID.randomUUID().toString();
            ids.add(id);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(config.id, id);
            map.put("name", "marko");
            map.put("age", 30);
            map.put("index", i);
            store.save(store.makeDocument(map));
        }
        BaseTest.printPerformance(store.toString(), total, "1-layer documents saved", this.stopWatch());
        assertEquals(ids.size(), total);

        this.stopWatch();
        for (int i = 0; i < total; i++) {
            assertEquals(store.retrieve(ids.get(i)).get(config.id), ids.get(i));
        }
        BaseTest.printPerformance(store.toString(), total, "1-layer documents read", this.stopWatch());

        this.stopWatch();
        for (int i = 0; i < total; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(config.id, ids.get(i));
            store.delete(store.makeDocument(map));
        }
        BaseTest.printPerformance(store.toString(), total, "1-layer documents deleted", this.stopWatch());

        this.stopWatch();
        for (int i = 0; i < total; i++) {
            assertNull(store.retrieve(ids.get(i)));
        }
        BaseTest.printPerformance(store.toString(), total, "1-layer deleted documents checked", this.stopWatch());


    }

}
