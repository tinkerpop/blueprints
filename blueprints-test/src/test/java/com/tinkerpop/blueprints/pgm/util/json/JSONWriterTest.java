package com.tinkerpop.blueprints.pgm.util.json;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JSONWriterTest {
    private TinkerGraph graph = new TinkerGraph();

    @Before
    public void setUp() {
        this.graph.clear();
    }

    @Test
    public void createJSONObjectEdgeNoPropertiesNoKeysNoTypes() throws JSONException {
        Vertex v1 = this.graph.addVertex(1);
        Vertex v2 = this.graph.addVertex(2);

        Edge e = this.graph.addEdge(3, v1, v2, "test");

        JSONObject json = JSONWriter.createJSONElement(e);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(3, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has(JSONTokens._LABEL));
        Assert.assertEquals("test", json.optString(JSONTokens._LABEL));
        Assert.assertTrue(json.has(JSONTokens._TYPE));
        Assert.assertEquals("edge", json.optString(JSONTokens._TYPE));
        Assert.assertTrue(json.has(JSONTokens._IN_V));
        Assert.assertEquals(2, json.optInt(JSONTokens._IN_V));
        Assert.assertTrue(json.has(JSONTokens._OUT_V));
        Assert.assertEquals(1, json.optInt(JSONTokens._OUT_V));
    }

    @Test
    public void createJSONObjectVertexNoPropertiesNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);

        JSONObject json = JSONWriter.createJSONElement(v);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has(JSONTokens._TYPE));
        Assert.assertEquals("vertex", json.optString(JSONTokens._TYPE));
    }

    @Test
    public void createJSONObjectVertexPrimitivePropertiesNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        v.setProperty("keyString", "string");
        v.setProperty("keyLong", 1L);
        v.setProperty("keyInt", 2);
        v.setProperty("keyFloat", 3.3f);
        v.setProperty("keyExponentialDouble", 1312928167.626012);
        v.setProperty("keyDouble", 4.4);
        v.setProperty("keyBoolean", true);

        JSONObject json = JSONWriter.createJSONElement(v);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyString"));
        Assert.assertEquals("string", json.optString("keyString"));
        Assert.assertTrue(json.has("keyLong"));
        Assert.assertEquals(1L, json.optLong("keyLong"));
        Assert.assertTrue(json.has("keyInt"));
        Assert.assertEquals(2, json.optInt("keyInt"));
        Assert.assertTrue(json.has("keyFloat"));
        Assert.assertEquals(3.3f, (float) json.optDouble("keyFloat"), 0);
        Assert.assertTrue(json.has("keyExponentialDouble"));
        Assert.assertEquals(1312928167.626012, json.optDouble("keyExponentialDouble"), 0);
        Assert.assertTrue(json.has("keyDouble"));
        Assert.assertEquals(4.4, json.optDouble("keyDouble"), 0);
        Assert.assertTrue(json.has("keyBoolean"));
        Assert.assertTrue(json.optBoolean("keyBoolean"));
    }

    @Test
    public void createJSONObjectVertexMapPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        Map map = new HashMap();
        map.put("this", "some");
        map.put("that", 1);

        v.setProperty("keyMap", map);

        JSONObject json = JSONWriter.createJSONElement(v);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyMap"));

        JSONObject mapAsJSON = json.optJSONObject("keyMap");
        Assert.assertNotNull(mapAsJSON);
        Assert.assertTrue(mapAsJSON.has("this"));
        Assert.assertEquals("some", mapAsJSON.optString("this"));
        Assert.assertTrue(mapAsJSON.has("that"));
        Assert.assertEquals(1, mapAsJSON.optInt("that"));
    }

    @Test
    public void createJSONObjectVertexListPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<Object> list = new ArrayList<Object>();
        list.add("this");
        list.add("that");
        list.add("other");
        list.add(true);

        v.setProperty("keyList", list);

        JSONObject json = JSONWriter.createJSONElement(v);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyList"));

        JSONArray listAsJSON = json.optJSONArray("keyList");
        Assert.assertNotNull(listAsJSON);
        Assert.assertEquals(4, listAsJSON.length());
    }

    @Test
    public void createJSONObjectVertexStringArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        String[] stringArray = new String[]{"this", "that", "other"};

        v.setProperty("keyStringArray", stringArray);

        JSONObject json = JSONWriter.createJSONElement(v);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyStringArray"));

        JSONArray stringArrayAsJSON = json.optJSONArray("keyStringArray");
        Assert.assertNotNull(stringArrayAsJSON);
        Assert.assertEquals(3, stringArrayAsJSON.length());
    }

    @Test
    public void createJSONObjectVertexDoubleArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        double[] doubleArray = new double[]{1.0, 2.0, 3.0};

        v.setProperty("keyDoubleArray", doubleArray);

        JSONObject json = JSONWriter.createJSONElement(v);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyDoubleArray"));

        JSONArray doubleArrayAsJSON = json.optJSONArray("keyDoubleArray");
        Assert.assertNotNull(doubleArrayAsJSON);
        Assert.assertEquals(3, doubleArrayAsJSON.length());
    }

    @Test
    public void createJSONObjectVertexIntArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        int[] intArray = new int[]{1, 2, 3};

        v.setProperty("keyIntArray", intArray);

        JSONObject json = JSONWriter.createJSONElement(v);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyIntArray"));

        JSONArray intArrayAsJSON = json.optJSONArray("keyIntArray");
        Assert.assertNotNull(intArrayAsJSON);
        Assert.assertEquals(3, intArrayAsJSON.length());
    }

    @Test
    public void createJSONObjectVertexLongArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        long[] longArray = new long[]{1l, 2l, 3l};

        v.setProperty("keyLongArray", longArray);

        JSONObject json = JSONWriter.createJSONElement(v);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyLongArray"));

        JSONArray longArrayAsJSON = json.optJSONArray("keyLongArray");
        Assert.assertNotNull(longArrayAsJSON);
        Assert.assertEquals(3, longArrayAsJSON.length());
    }

    @Test
    public void createJSONObjectFloatArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        float[] floatArray = new float[]{1.0f, 2.0f, 3.0f};

        v.setProperty("keyFloatArray", floatArray);

        JSONObject json = JSONWriter.createJSONElement(v);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyFloatArray"));

        JSONArray floatArrayAsJSON = json.optJSONArray("keyFloatArray");
        Assert.assertNotNull(floatArrayAsJSON);
        Assert.assertEquals(3, floatArrayAsJSON.length());
    }

    @Test
    public void createJSONObjectBooleanArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        boolean[] booleanArray = new boolean[]{true, false, true};

        v.setProperty("keyBooleanArray", booleanArray);

        JSONObject json = JSONWriter.createJSONElement(v);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyBooleanArray"));

        JSONArray booleanArrayAsJSON = json.optJSONArray("keyBooleanArray");
        Assert.assertNotNull(booleanArrayAsJSON);
        Assert.assertEquals(3, booleanArrayAsJSON.length());
    }

    @Test
    public void createJSONObjectVertexCatPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        v.setProperty("mycat", new Cat("smithers"));

        JSONObject json = JSONWriter.createJSONElement(v);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("mycat"));
        Assert.assertEquals("smithers", json.optString("mycat"));
    }

    @Test
    public void createJSONObjectVertexCatPropertyNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        v.setProperty("mycat", new Cat("smithers"));

        JSONObject json = JSONWriter.createJSONElement(v, null, true);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("mycat"));
        JSONObject jsonObjectCat = json.optJSONObject("mycat");
        Assert.assertTrue(jsonObjectCat.has("value"));
        Assert.assertEquals("smithers", jsonObjectCat.optString("value"));
    }

    @Test
    public void createJSONObjectVertexCatArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<Cat> cats = new ArrayList<Cat>();
        cats.add(new Cat("smithers"));
        cats.add(new Cat("mcallister"));

        v.setProperty("cats", cats);

        JSONObject json = JSONWriter.createJSONElement(v);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("cats"));

        JSONArray catListAsJson = json.optJSONArray("cats");
        Assert.assertNotNull(catListAsJson);
        Assert.assertEquals(2, catListAsJson.length());
    }

    @Test
    public void createJSONObjectCrazyPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List mix = new ArrayList();
        mix.add(new Cat("smithers"));
        mix.add(true);

        List deepCats = new ArrayList();
        deepCats.add(new Cat("mcallister"));
        mix.add(deepCats);

        Map map = new HashMap();
        map.put("crazy", mix);

        int[] someInts = new int[]{1, 2, 3};
        map.put("ints", someInts);

        map.put("regular", "stuff");

        Map innerMap = new HashMap();
        innerMap.put("me", "you");

        map.put("inner", innerMap);

        v.setProperty("crazy-map", map);

        JSONObject json = JSONWriter.createJSONElement(v);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("crazy-map"));

        JSONObject mapAsJson = json.optJSONObject("crazy-map");
        Assert.assertTrue(mapAsJson.has("regular"));
        Assert.assertEquals("stuff", mapAsJson.optString("regular"));

        Assert.assertTrue(mapAsJson.has("ints"));
        JSONArray intArrayAsJson = mapAsJson.optJSONArray("ints");
        Assert.assertNotNull(intArrayAsJson);
        Assert.assertEquals(3, intArrayAsJson.length());

        Assert.assertTrue(mapAsJson.has("crazy"));
        JSONArray deepListAsJSON = mapAsJson.optJSONArray("crazy");
        Assert.assertNotNull(deepListAsJSON);
        Assert.assertEquals(3, deepListAsJSON.length());

        Assert.assertTrue(mapAsJson.has("inner"));
        JSONObject mapInMapAsJSON = mapAsJson.optJSONObject("inner");
        Assert.assertNotNull(mapInMapAsJSON);
        Assert.assertTrue(mapInMapAsJSON.has("me"));
        Assert.assertEquals("you", mapInMapAsJSON.optString("me"));

    }

    @Test
    public void createJSONObjectVertexNoPropertiesWithKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        v.setProperty("x", "X");
        v.setProperty("y", "Y");
        v.setProperty("z", "Z");

        List<String> returnKeys = new ArrayList<String>();
        returnKeys.add("y");
        JSONObject json = JSONWriter.createJSONElement(v, returnKeys, false);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has(JSONTokens._TYPE));
        Assert.assertEquals("vertex", json.optString(JSONTokens._TYPE));
        Assert.assertFalse(json.has("x"));
        Assert.assertFalse(json.has("z"));
        Assert.assertTrue(json.has("y"));
    }

    @Test
    public void createJSONObjectVertexVertexPropertiesWithKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        v.setProperty("x", "X");
        v.setProperty("y", "Y");
        v.setProperty("z", "Z");

        Vertex innerV = this.graph.addVertex(2);
        innerV.setProperty("x", "X");
        innerV.setProperty("y", "Y");
        innerV.setProperty("z", "Z");

        v.setProperty("v", innerV);

        List<String> returnKeys = new ArrayList<String>();
        returnKeys.add("y");
        returnKeys.add("v");

        JSONObject json = JSONWriter.createJSONElement(v, returnKeys, false);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has(JSONTokens._TYPE));
        Assert.assertEquals("vertex", json.optString(JSONTokens._TYPE));
        Assert.assertFalse(json.has("x"));
        Assert.assertFalse(json.has("z"));
        Assert.assertTrue(json.has("y"));
        Assert.assertTrue(json.has("v"));

        JSONObject innerJson = json.optJSONObject("v");
        Assert.assertFalse(innerJson.has("x"));
        Assert.assertFalse(innerJson.has("z"));
        Assert.assertTrue(innerJson.has("y"));
        Assert.assertFalse(innerJson.has("v"));
    }

    @Test
    public void createJSONObjectVertexPrimitivePropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        v.setProperty("keyString", "string");
        v.setProperty("keyLong", 1L);
        v.setProperty("keyInt", 2);
        v.setProperty("keyFloat", 3.3f);
        v.setProperty("keyDouble", 4.4);
        v.setProperty("keyBoolean", true);

        JSONObject json = JSONWriter.createJSONElement(v, null, true);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyString"));

        JSONObject valueAsJson = json.optJSONObject("keyString");
        Assert.assertNotNull(valueAsJson);
        Assert.assertTrue(valueAsJson.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_STRING, valueAsJson.optString(JSONTokens.TYPE));
        Assert.assertTrue(valueAsJson.has(JSONTokens.VALUE));
        Assert.assertEquals("string", valueAsJson.optString(JSONTokens.VALUE));

        valueAsJson = json.optJSONObject("keyLong");
        Assert.assertNotNull(valueAsJson);
        Assert.assertTrue(valueAsJson.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_LONG, valueAsJson.optString(JSONTokens.TYPE));
        Assert.assertTrue(valueAsJson.has(JSONTokens.VALUE));
        Assert.assertEquals(1L, valueAsJson.optLong(JSONTokens.VALUE));

        valueAsJson = json.optJSONObject("keyInt");
        Assert.assertNotNull(valueAsJson);
        Assert.assertTrue(valueAsJson.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_INTEGER, valueAsJson.optString(JSONTokens.TYPE));
        Assert.assertTrue(valueAsJson.has(JSONTokens.VALUE));
        Assert.assertEquals(2, valueAsJson.optInt(JSONTokens.VALUE));

        valueAsJson = json.optJSONObject("keyFloat");
        Assert.assertNotNull(valueAsJson);
        Assert.assertTrue(valueAsJson.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_FLOAT, valueAsJson.optString(JSONTokens.TYPE));
        Assert.assertTrue(valueAsJson.has(JSONTokens.VALUE));
        Assert.assertEquals(3.3f, (float) valueAsJson.optDouble(JSONTokens.VALUE), 0);

        valueAsJson = json.optJSONObject("keyDouble");
        Assert.assertNotNull(valueAsJson);
        Assert.assertTrue(valueAsJson.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_DOUBLE, valueAsJson.optString(JSONTokens.TYPE));
        Assert.assertTrue(valueAsJson.has(JSONTokens.VALUE));
        Assert.assertEquals(4.4, valueAsJson.optDouble(JSONTokens.VALUE), 0);

        valueAsJson = json.optJSONObject("keyBoolean");
        Assert.assertNotNull(valueAsJson);
        Assert.assertTrue(valueAsJson.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_BOOLEAN, valueAsJson.optString(JSONTokens.TYPE));
        Assert.assertTrue(valueAsJson.has(JSONTokens.VALUE));
        Assert.assertTrue(valueAsJson.optBoolean(JSONTokens.VALUE));
    }

    @Test
    public void createJSONObjectVertexListPropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<String> list = new ArrayList<String>();
        list.add("this");
        list.add("this");
        list.add("this");

        v.setProperty("keyList", list);

        JSONObject json = JSONWriter.createJSONElement(v, null, true);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyList"));

        JSONObject listWithTypeAsJson = json.optJSONObject("keyList");
        Assert.assertNotNull(listWithTypeAsJson);
        Assert.assertTrue(listWithTypeAsJson.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_LIST, listWithTypeAsJson.optString(JSONTokens.TYPE));
        Assert.assertTrue(listWithTypeAsJson.has(JSONTokens.VALUE));
        JSONArray listAsJSON = listWithTypeAsJson.optJSONArray(JSONTokens.VALUE);
        Assert.assertNotNull(listAsJSON);
        Assert.assertEquals(3, listAsJSON.length());

        for (int ix = 0; ix < listAsJSON.length(); ix++) {
            JSONObject valueAsJson = listAsJSON.optJSONObject(ix);
            Assert.assertNotNull(valueAsJson);
            Assert.assertTrue(valueAsJson.has(JSONTokens.TYPE));
            Assert.assertEquals(JSONTokens.TYPE_STRING, valueAsJson.optString(JSONTokens.TYPE));
            Assert.assertTrue(valueAsJson.has(JSONTokens.VALUE));
            Assert.assertEquals("this", valueAsJson.optString(JSONTokens.VALUE));
        }
    }

    @Test
    public void createJSONObjectVertexBooleanListPropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<Boolean> list = new ArrayList<Boolean>();
        list.add(true);
        list.add(true);
        list.add(true);

        v.setProperty("keyList", list);

        JSONObject json = JSONWriter.createJSONElement(v, null, true);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyList"));

        JSONObject listWithTypeAsJson = json.optJSONObject("keyList");
        Assert.assertNotNull(listWithTypeAsJson);
        Assert.assertTrue(listWithTypeAsJson.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_LIST, listWithTypeAsJson.optString(JSONTokens.TYPE));
        Assert.assertTrue(listWithTypeAsJson.has(JSONTokens.VALUE));
        JSONArray listAsJSON = listWithTypeAsJson.optJSONArray(JSONTokens.VALUE);
        Assert.assertNotNull(listAsJSON);
        Assert.assertEquals(3, listAsJSON.length());

        for (int ix = 0; ix < listAsJSON.length(); ix++) {
            JSONObject valueAsJson = listAsJSON.optJSONObject(ix);
            Assert.assertNotNull(valueAsJson);
            Assert.assertTrue(valueAsJson.has(JSONTokens.TYPE));
            Assert.assertEquals(JSONTokens.TYPE_BOOLEAN, valueAsJson.optString(JSONTokens.TYPE));
            Assert.assertTrue(valueAsJson.has(JSONTokens.VALUE));
            Assert.assertEquals(true, valueAsJson.optBoolean(JSONTokens.VALUE));
        }
    }

    @Test
    public void createJSONObjectVertexLongListPropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<Long> list = new ArrayList<Long>();
        list.add(1000L);
        list.add(1000L);
        list.add(1000L);

        v.setProperty("keyList", list);

        JSONObject json = JSONWriter.createJSONElement(v, null, true);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyList"));

        JSONObject listWithTypeAsJson = json.optJSONObject("keyList");
        Assert.assertNotNull(listWithTypeAsJson);
        Assert.assertTrue(listWithTypeAsJson.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_LIST, listWithTypeAsJson.optString(JSONTokens.TYPE));
        Assert.assertTrue(listWithTypeAsJson.has(JSONTokens.VALUE));
        JSONArray listAsJSON = listWithTypeAsJson.optJSONArray(JSONTokens.VALUE);
        Assert.assertNotNull(listAsJSON);
        Assert.assertEquals(3, listAsJSON.length());

        for (int ix = 0; ix < listAsJSON.length(); ix++) {
            JSONObject valueAsJson = listAsJSON.optJSONObject(ix);
            Assert.assertNotNull(valueAsJson);
            Assert.assertTrue(valueAsJson.has(JSONTokens.TYPE));
            Assert.assertEquals(JSONTokens.TYPE_LONG, valueAsJson.optString(JSONTokens.TYPE));
            Assert.assertTrue(valueAsJson.has(JSONTokens.VALUE));
            Assert.assertEquals(1000L, valueAsJson.optLong(JSONTokens.VALUE));
        }
    }

    @Test
    public void createJSONObjectVertexIntListPropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(1);
        list.add(1);

        v.setProperty("keyList", list);

        JSONObject json = JSONWriter.createJSONElement(v, null, true);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyList"));

        JSONObject listWithTypeAsJson = json.optJSONObject("keyList");
        Assert.assertNotNull(listWithTypeAsJson);
        Assert.assertTrue(listWithTypeAsJson.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_LIST, listWithTypeAsJson.optString(JSONTokens.TYPE));
        Assert.assertTrue(listWithTypeAsJson.has(JSONTokens.VALUE));
        JSONArray listAsJSON = listWithTypeAsJson.optJSONArray(JSONTokens.VALUE);
        Assert.assertNotNull(listAsJSON);
        Assert.assertEquals(3, listAsJSON.length());

        for (int ix = 0; ix < listAsJSON.length(); ix++) {
            JSONObject valueAsJson = listAsJSON.optJSONObject(ix);
            Assert.assertNotNull(valueAsJson);
            Assert.assertTrue(valueAsJson.has(JSONTokens.TYPE));
            Assert.assertEquals(JSONTokens.TYPE_INTEGER, valueAsJson.optString(JSONTokens.TYPE));
            Assert.assertTrue(valueAsJson.has(JSONTokens.VALUE));
            Assert.assertEquals(1, valueAsJson.optInt(JSONTokens.VALUE));
        }
    }

    @Test
    public void createJSONObjectVertexListOfListPropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(1);
        list.add(1);

        List<List<Integer>> listList = new ArrayList<List<Integer>>();
        listList.add(list);

        v.setProperty("keyList", listList);

        JSONObject json = JSONWriter.createJSONElement(v, null, true);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyList"));

        JSONObject listWithTypeAsJson = json.optJSONObject("keyList");
        Assert.assertNotNull(listWithTypeAsJson);
        Assert.assertTrue(listWithTypeAsJson.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_LIST, listWithTypeAsJson.optString(JSONTokens.TYPE));
        Assert.assertTrue(listWithTypeAsJson.has(JSONTokens.VALUE));
        JSONArray listAsJSON = listWithTypeAsJson.optJSONArray(JSONTokens.VALUE).optJSONObject(0).getJSONArray(JSONTokens.VALUE);
        Assert.assertNotNull(listAsJSON);
        Assert.assertEquals(3, listAsJSON.length());

        for (int ix = 0; ix < listAsJSON.length(); ix++) {
            JSONObject valueAsJson = listAsJSON.optJSONObject(ix);
            Assert.assertNotNull(valueAsJson);
            Assert.assertTrue(valueAsJson.has(JSONTokens.TYPE));
            Assert.assertEquals(JSONTokens.TYPE_INTEGER, valueAsJson.optString(JSONTokens.TYPE));
            Assert.assertTrue(valueAsJson.has(JSONTokens.VALUE));
            Assert.assertEquals(1, valueAsJson.optInt(JSONTokens.VALUE));
        }
    }


    @Test
    public void createJSONObjectVertexMapPropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);

        Map map = new HashMap();
        map.put("this", "some");
        map.put("that", 1);

        v.setProperty("keyMap", map);

        JSONObject json = JSONWriter.createJSONElement(v, null, true);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(JSONTokens._ID));
        Assert.assertEquals(1, json.optInt(JSONTokens._ID));
        Assert.assertTrue(json.has("keyMap"));

        JSONObject mapWithTypeAsJSON = json.optJSONObject("keyMap");
        Assert.assertNotNull(mapWithTypeAsJSON);
        Assert.assertTrue(mapWithTypeAsJSON.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_MAP, mapWithTypeAsJSON.optString(JSONTokens.TYPE));

        Assert.assertTrue(mapWithTypeAsJSON.has(JSONTokens.VALUE));
        JSONObject mapAsJSON = mapWithTypeAsJSON.optJSONObject(JSONTokens.VALUE);

        Assert.assertTrue(mapAsJSON.has("this"));
        JSONObject thisAsJson = mapAsJSON.optJSONObject("this");
        Assert.assertTrue(thisAsJson.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_STRING, thisAsJson.optString(JSONTokens.TYPE));
        Assert.assertTrue(thisAsJson.has(JSONTokens.VALUE));
        Assert.assertEquals("some", thisAsJson.optString(JSONTokens.VALUE));

        Assert.assertTrue(mapAsJSON.has("that"));
        JSONObject thatAsJson = mapAsJSON.optJSONObject("that");
        Assert.assertTrue(thatAsJson.has(JSONTokens.TYPE));
        Assert.assertEquals(JSONTokens.TYPE_INTEGER, thatAsJson.optString(JSONTokens.TYPE));
        Assert.assertTrue(thatAsJson.has(JSONTokens.VALUE));
        Assert.assertEquals(1, thatAsJson.optInt(JSONTokens.VALUE));


    }

    @Test
    public void createJSONObjectNullsNoKeysNoTypes() throws JSONException {
        Graph g = new TinkerGraph();
        Vertex v = g.addVertex(1);
        v.setProperty("key", null);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("innerkey", null);

        List<String> innerList = new ArrayList<String>();
        innerList.add(null);
        innerList.add("innerstring");
        map.put("list", innerList);

        v.setProperty("keyMap", map);

        List<String> list = new ArrayList<String>();
        list.add(null);
        list.add("string");
        v.setProperty("keyList", list);

        JSONObject json = JSONWriter.createJSONElement(v, null, false);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.isNull("key"));

        JSONObject jsonMap = json.optJSONObject("keyMap");
        Assert.assertNotNull(jsonMap);
        Assert.assertTrue(jsonMap.isNull("innerkey"));

        JSONArray jsonInnerArray = jsonMap.getJSONArray("list");
        Assert.assertNotNull(jsonInnerArray);
        Assert.assertTrue(jsonInnerArray.isNull(0));
        Assert.assertEquals("innerstring", jsonInnerArray.get(1));

        JSONArray jsonArray = json.getJSONArray("keyList");
        Assert.assertNotNull(jsonArray);
        Assert.assertTrue(jsonArray.isNull(0));
        Assert.assertEquals("string", jsonArray.get(1));
    }

    @Test
    public void createJSONObjectNullsNoKeysWithTypes() throws JSONException {
        Graph g = new TinkerGraph();
        Vertex v = g.addVertex(1);
        v.setProperty("key", null);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("innerkey", null);

        List<String> innerList = new ArrayList<String>();
        innerList.add(null);
        innerList.add("innerstring");
        map.put("list", innerList);

        v.setProperty("keyMap", map);

        List<String> list = new ArrayList<String>();
        list.add(null);
        list.add("string");
        v.setProperty("keyList", list);

        JSONObject json = JSONWriter.createJSONElement(v, null, true);

        Assert.assertNotNull(json);
        JSONObject jsonObjectKey = json.optJSONObject("key");
        Assert.assertTrue(jsonObjectKey.isNull(JSONTokens.VALUE));
        Assert.assertEquals(JSONTokens.TYPE_UNKNOWN, jsonObjectKey.optString(JSONTokens.TYPE));

        JSONObject jsonMap = json.optJSONObject("keyMap").optJSONObject(JSONTokens.VALUE);
        Assert.assertNotNull(jsonMap);
        JSONObject jsonObjectMap = jsonMap.optJSONObject("innerkey");
        Assert.assertTrue(jsonObjectMap.isNull(JSONTokens.VALUE));
        Assert.assertEquals(JSONTokens.TYPE_UNKNOWN, jsonObjectMap.optString(JSONTokens.TYPE));

        JSONArray jsonInnerArray = jsonMap.getJSONObject("list").getJSONArray(JSONTokens.VALUE);
        Assert.assertNotNull(jsonInnerArray);
        JSONObject jsonObjectInnerListFirst = jsonInnerArray.getJSONObject(0);
        Assert.assertTrue(jsonObjectInnerListFirst.isNull(JSONTokens.VALUE));
        Assert.assertEquals(JSONTokens.TYPE_UNKNOWN, jsonObjectInnerListFirst.optString(JSONTokens.TYPE));

        JSONArray jsonArray = json.getJSONObject("keyList").getJSONArray(JSONTokens.VALUE);
        Assert.assertNotNull(jsonArray);
        JSONObject jsonObjectListFirst = jsonArray.getJSONObject(0);
        Assert.assertTrue(jsonObjectListFirst.isNull(JSONTokens.VALUE));
        Assert.assertEquals(JSONTokens.TYPE_UNKNOWN, jsonObjectListFirst.optString(JSONTokens.TYPE));
    }

    private class Cat {
        private String name;

        public Cat(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
