package com.tinkerpop.blueprints.util.io.graphson;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class GraphSONUtilityTest {
    private TinkerGraph graph = new TinkerGraph();

    private final String vertexJson1 = "{\"name\":\"marko\",\"age\":29,\"_id\":1,\"_type\":\"vertex\"}";
    private final String vertexJson2 =  "{\"name\":\"vadas\",\"age\":27,\"_id\":2,\"_type\":\"vertex\"}";

    private final String edgeJsonLight = "{\"weight\":0.5,\"_outV\":1,\"_inV\":2}";
    private final String edgeJson = "{\"weight\":0.5,\"_id\":7,\"_type\":\"edge\",\"_outV\":1,\"_inV\":2,\"_label\":\"knows\"}";

    private InputStream inputStreamVertexJson1;
    private InputStream inputStreamEdgeJsonLight;

    @Before
    public void setUp() {
        this.graph.clear();

        this.inputStreamVertexJson1 = new ByteArrayInputStream(vertexJson1.getBytes());
        this.inputStreamEdgeJsonLight = new ByteArrayInputStream(edgeJsonLight.getBytes());

    }

    @Test
    public void jsonFromElementEdgeNoPropertiesNoKeysNoTypes() throws JSONException {
        Vertex v1 = this.graph.addVertex(1);
        Vertex v2 = this.graph.addVertex(2);

        Edge e = this.graph.addEdge(3, v1, v2, "test");

        JSONObject json = GraphSONUtility.jsonFromElement(e, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(3, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has(GraphSONTokens._LABEL));
        Assert.assertEquals("test", json.optString(GraphSONTokens._LABEL));
        Assert.assertTrue(json.has(GraphSONTokens._TYPE));
        Assert.assertEquals("edge", json.optString(GraphSONTokens._TYPE));
        Assert.assertTrue(json.has(GraphSONTokens._IN_V));
        Assert.assertEquals(2, json.optInt(GraphSONTokens._IN_V));
        Assert.assertTrue(json.has(GraphSONTokens._OUT_V));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._OUT_V));
    }

    @Test
    public void jsonFromElementEdgeCompactIdOnlyAsInclude() throws JSONException {
        Vertex v1 = this.graph.addVertex(1);
        Vertex v2 = this.graph.addVertex(2);

        Edge e = this.graph.addEdge(3, v1, v2, "test");

        Set<String> propertiesToInclude = new HashSet<String>() {{
            add(GraphSONTokens._ID);
        }};

        JSONObject json = GraphSONUtility.jsonFromElement(e, propertiesToInclude, GraphSONMode.COMPACT);

        Assert.assertNotNull(json);
        Assert.assertFalse(json.has(GraphSONTokens._TYPE));
        Assert.assertFalse(json.has(GraphSONTokens._LABEL));
        Assert.assertFalse(json.has(GraphSONTokens._IN_V));
        Assert.assertFalse(json.has(GraphSONTokens._OUT_V));
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertFalse(json.has("weight"));
    }

    @Test
    public void jsonFromElementEdgeCompactIdOnlyAsExclude() throws JSONException {
        ElementFactory factory = new GraphElementFactory(this.graph);
        Vertex v1 = this.graph.addVertex(1);
        Vertex v2 = this.graph.addVertex(2);

        Edge e = this.graph.addEdge(3, v1, v2, "test");

        Set<String> propertiesToExclude = new HashSet<String>() {{
            add(GraphSONTokens._TYPE);
            add(GraphSONTokens._LABEL);
            add(GraphSONTokens._IN_V);
            add(GraphSONTokens._OUT_V);
            add("weight");
        }};

        ElementPropertyConfig config = new ElementPropertyConfig(null, propertiesToExclude,
                ElementPropertyConfig.ElementPropertiesRule.INCLUDE,
                ElementPropertyConfig.ElementPropertiesRule.EXCLUDE);
        GraphSONUtility utility = new GraphSONUtility(GraphSONMode.COMPACT, factory, config);
        JSONObject json = utility.jsonFromElement(e);

        Assert.assertNotNull(json);
        Assert.assertFalse(json.has(GraphSONTokens._TYPE));
        Assert.assertFalse(json.has(GraphSONTokens._LABEL));
        Assert.assertFalse(json.has(GraphSONTokens._IN_V));
        Assert.assertFalse(json.has(GraphSONTokens._OUT_V));
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertFalse(json.has("weight"));
    }

    @Test
    public void jsonFromElementEdgeCompactAllKeys() throws JSONException {
        Vertex v1 = this.graph.addVertex(1);
        Vertex v2 = this.graph.addVertex(2);

        Edge e = this.graph.addEdge(3, v1, v2, "test");

        JSONObject json = GraphSONUtility.jsonFromElement(e, null, GraphSONMode.COMPACT);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertTrue(json.has(GraphSONTokens._TYPE));
        Assert.assertTrue(json.has(GraphSONTokens._LABEL));
        Assert.assertTrue(json.has(GraphSONTokens._IN_V));
        Assert.assertTrue(json.has(GraphSONTokens._OUT_V));
    }


    @Test
    public void jsonFromElementVertexNoPropertiesNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has(GraphSONTokens._TYPE));
        Assert.assertEquals("vertex", json.optString(GraphSONTokens._TYPE));
    }

    @Test
    public void jsonFromElementVertexCompactIdOnlyAsInclude() throws JSONException {
        Vertex v = this.graph.addVertex(1);

        Set<String> propertiesToInclude = new HashSet<String>() {{
            add(GraphSONTokens._ID);
        }};

        JSONObject json = GraphSONUtility.jsonFromElement(v, propertiesToInclude, GraphSONMode.COMPACT);

        Assert.assertNotNull(json);
        Assert.assertFalse(json.has(GraphSONTokens._TYPE));
        Assert.assertTrue(json.has(GraphSONTokens._ID));
    }

    @Test
    public void jsonFromElementVertexCompactIdOnlyAsExclude() throws JSONException {
        GraphElementFactory factory = new GraphElementFactory(this.graph);
        Vertex v = this.graph.addVertex(1);

        Set<String> propertiesToExclude = new HashSet<String>() {{
            add(GraphSONTokens._TYPE);
        }};

        ElementPropertyConfig config = new ElementPropertyConfig(propertiesToExclude, null,
                ElementPropertyConfig.ElementPropertiesRule.EXCLUDE,
                ElementPropertyConfig.ElementPropertiesRule.EXCLUDE);

        GraphSONUtility utility = new GraphSONUtility(GraphSONMode.COMPACT, factory, config);
        JSONObject json = utility.jsonFromElement(v);

        Assert.assertNotNull(json);
        Assert.assertFalse(json.has(GraphSONTokens._TYPE));
        Assert.assertTrue(json.has(GraphSONTokens._ID));
    }

    @Test
    public void jsonFromElementVertexCompactAllOnly() throws JSONException {
        Vertex v = this.graph.addVertex(1);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.COMPACT);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._TYPE));
        Assert.assertTrue(json.has(GraphSONTokens._ID));
    }

    @Test
    public void jsonFromElementVertexPrimitivePropertiesNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        v.setProperty("keyString", "string");
        v.setProperty("keyLong", 1L);
        v.setProperty("keyInt", 2);
        v.setProperty("keyFloat", 3.3f);
        v.setProperty("keyExponentialDouble", 1312928167.626012);
        v.setProperty("keyDouble", 4.4);
        v.setProperty("keyBoolean", true);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
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
    public void jsonFromElementVertexMapPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        Map map = new HashMap();
        map.put("this", "some");
        map.put("that", 1);

        v.setProperty("keyMap", map);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyMap"));

        JSONObject mapAsJSON = json.optJSONObject("keyMap");
        Assert.assertNotNull(mapAsJSON);
        Assert.assertTrue(mapAsJSON.has("this"));
        Assert.assertEquals("some", mapAsJSON.optString("this"));
        Assert.assertTrue(mapAsJSON.has("that"));
        Assert.assertEquals(1, mapAsJSON.optInt("that"));
    }

    @Test
    public void jsonFromElementVertexListPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<Object> list = new ArrayList<Object>();
        list.add("this");
        list.add("that");
        list.add("other");
        list.add(true);

        v.setProperty("keyList", list);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyList"));

        JSONArray listAsJSON = json.optJSONArray("keyList");
        Assert.assertNotNull(listAsJSON);
        Assert.assertEquals(4, listAsJSON.length());
    }

    @Test
    public void jsonFromElementVertexStringArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        String[] stringArray = new String[]{"this", "that", "other"};

        v.setProperty("keyStringArray", stringArray);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyStringArray"));

        JSONArray stringArrayAsJSON = json.optJSONArray("keyStringArray");
        Assert.assertNotNull(stringArrayAsJSON);
        Assert.assertEquals(3, stringArrayAsJSON.length());
    }

    @Test
    public void jsonFromElementVertexDoubleArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        double[] doubleArray = new double[]{1.0, 2.0, 3.0};

        v.setProperty("keyDoubleArray", doubleArray);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyDoubleArray"));

        JSONArray doubleArrayAsJSON = json.optJSONArray("keyDoubleArray");
        Assert.assertNotNull(doubleArrayAsJSON);
        Assert.assertEquals(3, doubleArrayAsJSON.length());
    }

    @Test
    public void jsonFromElementVertexIntArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        int[] intArray = new int[]{1, 2, 3};

        v.setProperty("keyIntArray", intArray);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyIntArray"));

        JSONArray intArrayAsJSON = json.optJSONArray("keyIntArray");
        Assert.assertNotNull(intArrayAsJSON);
        Assert.assertEquals(3, intArrayAsJSON.length());
    }

    @Test
    public void jsonFromElementVertexLongArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        long[] longArray = new long[]{1l, 2l, 3l};

        v.setProperty("keyLongArray", longArray);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyLongArray"));

        JSONArray longArrayAsJSON = json.optJSONArray("keyLongArray");
        Assert.assertNotNull(longArrayAsJSON);
        Assert.assertEquals(3, longArrayAsJSON.length());
    }

    @Test
    public void jsonFromElementFloatArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        float[] floatArray = new float[]{1.0f, 2.0f, 3.0f};

        v.setProperty("keyFloatArray", floatArray);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyFloatArray"));

        JSONArray floatArrayAsJSON = json.optJSONArray("keyFloatArray");
        Assert.assertNotNull(floatArrayAsJSON);
        Assert.assertEquals(3, floatArrayAsJSON.length());
    }

    @Test
    public void jsonFromElementBooleanArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        boolean[] booleanArray = new boolean[]{true, false, true};

        v.setProperty("keyBooleanArray", booleanArray);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyBooleanArray"));

        JSONArray booleanArrayAsJSON = json.optJSONArray("keyBooleanArray");
        Assert.assertNotNull(booleanArrayAsJSON);
        Assert.assertEquals(3, booleanArrayAsJSON.length());
    }

    @Test
    public void jsonFromElementVertexCatPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        v.setProperty("mycat", new Cat("smithers"));

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("mycat"));
        Assert.assertEquals("smithers", json.optString("mycat"));
    }

    @Test
    public void jsonFromElementVertexCatPropertyNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        v.setProperty("mycat", new Cat("smithers"));

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.EXTENDED);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("mycat"));
        JSONObject jsonObjectCat = json.optJSONObject("mycat");
        Assert.assertTrue(jsonObjectCat.has("value"));
        Assert.assertEquals("smithers", jsonObjectCat.optString("value"));
    }

    @Test
    public void jsonFromElementVertexCatArrayPropertyNoKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<Cat> cats = new ArrayList<Cat>();
        cats.add(new Cat("smithers"));
        cats.add(new Cat("mcallister"));

        v.setProperty("cats", cats);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("cats"));

        JSONArray catListAsJson = json.optJSONArray("cats");
        Assert.assertNotNull(catListAsJson);
        Assert.assertEquals(2, catListAsJson.length());
    }

    @Test
    public void jsonFromElementCrazyPropertyNoKeysNoTypes() throws JSONException {
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

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
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
    public void jsonFromElementVertexNoPropertiesWithKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        v.setProperty("x", "X");
        v.setProperty("y", "Y");
        v.setProperty("z", "Z");

        Set<String> propertiesToInclude = new HashSet<String>();
        propertiesToInclude.add("y");
        JSONObject json = GraphSONUtility.jsonFromElement(v, propertiesToInclude, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has(GraphSONTokens._TYPE));
        Assert.assertEquals("vertex", json.optString(GraphSONTokens._TYPE));
        Assert.assertFalse(json.has("x"));
        Assert.assertFalse(json.has("z"));
        Assert.assertTrue(json.has("y"));
    }

    @Test
    public void jsonFromElementVertexVertexPropertiesWithKeysNoTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        v.setProperty("x", "X");
        v.setProperty("y", "Y");
        v.setProperty("z", "Z");

        Vertex innerV = this.graph.addVertex(2);
        innerV.setProperty("x", "X");
        innerV.setProperty("y", "Y");
        innerV.setProperty("z", "Z");

        v.setProperty("v", innerV);

        Set<String> propertiesToInclude = new HashSet<String>();
        propertiesToInclude.add("y");
        propertiesToInclude.add("v");

        JSONObject json = GraphSONUtility.jsonFromElement(v, propertiesToInclude, GraphSONMode.NORMAL);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has(GraphSONTokens._TYPE));
        Assert.assertEquals("vertex", json.optString(GraphSONTokens._TYPE));
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
    public void jsonFromElementVertexPrimitivePropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        v.setProperty("keyString", "string");
        v.setProperty("keyLong", 1L);
        v.setProperty("keyInt", 2);
        v.setProperty("keyFloat", 3.3f);
        v.setProperty("keyDouble", 4.4);
        v.setProperty("keyBoolean", true);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.EXTENDED);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyString"));

        JSONObject valueAsJson = json.optJSONObject("keyString");
        Assert.assertNotNull(valueAsJson);
        Assert.assertTrue(valueAsJson.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_STRING, valueAsJson.optString(GraphSONTokens.TYPE));
        Assert.assertTrue(valueAsJson.has(GraphSONTokens.VALUE));
        Assert.assertEquals("string", valueAsJson.optString(GraphSONTokens.VALUE));

        valueAsJson = json.optJSONObject("keyLong");
        Assert.assertNotNull(valueAsJson);
        Assert.assertTrue(valueAsJson.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_LONG, valueAsJson.optString(GraphSONTokens.TYPE));
        Assert.assertTrue(valueAsJson.has(GraphSONTokens.VALUE));
        Assert.assertEquals(1L, valueAsJson.optLong(GraphSONTokens.VALUE));

        valueAsJson = json.optJSONObject("keyInt");
        Assert.assertNotNull(valueAsJson);
        Assert.assertTrue(valueAsJson.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_INTEGER, valueAsJson.optString(GraphSONTokens.TYPE));
        Assert.assertTrue(valueAsJson.has(GraphSONTokens.VALUE));
        Assert.assertEquals(2, valueAsJson.optInt(GraphSONTokens.VALUE));

        valueAsJson = json.optJSONObject("keyFloat");
        Assert.assertNotNull(valueAsJson);
        Assert.assertTrue(valueAsJson.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_FLOAT, valueAsJson.optString(GraphSONTokens.TYPE));
        Assert.assertTrue(valueAsJson.has(GraphSONTokens.VALUE));
        Assert.assertEquals(3.3f, (float) valueAsJson.optDouble(GraphSONTokens.VALUE), 0);

        valueAsJson = json.optJSONObject("keyDouble");
        Assert.assertNotNull(valueAsJson);
        Assert.assertTrue(valueAsJson.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_DOUBLE, valueAsJson.optString(GraphSONTokens.TYPE));
        Assert.assertTrue(valueAsJson.has(GraphSONTokens.VALUE));
        Assert.assertEquals(4.4, valueAsJson.optDouble(GraphSONTokens.VALUE), 0);

        valueAsJson = json.optJSONObject("keyBoolean");
        Assert.assertNotNull(valueAsJson);
        Assert.assertTrue(valueAsJson.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_BOOLEAN, valueAsJson.optString(GraphSONTokens.TYPE));
        Assert.assertTrue(valueAsJson.has(GraphSONTokens.VALUE));
        Assert.assertTrue(valueAsJson.optBoolean(GraphSONTokens.VALUE));
    }

    @Test
    public void jsonFromElementVertexListPropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<String> list = new ArrayList<String>();
        list.add("this");
        list.add("this");
        list.add("this");

        v.setProperty("keyList", list);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.EXTENDED);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyList"));

        JSONObject listWithTypeAsJson = json.optJSONObject("keyList");
        Assert.assertNotNull(listWithTypeAsJson);
        Assert.assertTrue(listWithTypeAsJson.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_LIST, listWithTypeAsJson.optString(GraphSONTokens.TYPE));
        Assert.assertTrue(listWithTypeAsJson.has(GraphSONTokens.VALUE));
        JSONArray listAsJSON = listWithTypeAsJson.optJSONArray(GraphSONTokens.VALUE);
        Assert.assertNotNull(listAsJSON);
        Assert.assertEquals(3, listAsJSON.length());

        for (int ix = 0; ix < listAsJSON.length(); ix++) {
            JSONObject valueAsJson = listAsJSON.optJSONObject(ix);
            Assert.assertNotNull(valueAsJson);
            Assert.assertTrue(valueAsJson.has(GraphSONTokens.TYPE));
            Assert.assertEquals(GraphSONTokens.TYPE_STRING, valueAsJson.optString(GraphSONTokens.TYPE));
            Assert.assertTrue(valueAsJson.has(GraphSONTokens.VALUE));
            Assert.assertEquals("this", valueAsJson.optString(GraphSONTokens.VALUE));
        }
    }

    @Test
    public void jsonFromElementVertexBooleanListPropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<Boolean> list = new ArrayList<Boolean>();
        list.add(true);
        list.add(true);
        list.add(true);

        v.setProperty("keyList", list);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.EXTENDED);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyList"));

        JSONObject listWithTypeAsJson = json.optJSONObject("keyList");
        Assert.assertNotNull(listWithTypeAsJson);
        Assert.assertTrue(listWithTypeAsJson.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_LIST, listWithTypeAsJson.optString(GraphSONTokens.TYPE));
        Assert.assertTrue(listWithTypeAsJson.has(GraphSONTokens.VALUE));
        JSONArray listAsJSON = listWithTypeAsJson.optJSONArray(GraphSONTokens.VALUE);
        Assert.assertNotNull(listAsJSON);
        Assert.assertEquals(3, listAsJSON.length());

        for (int ix = 0; ix < listAsJSON.length(); ix++) {
            JSONObject valueAsJson = listAsJSON.optJSONObject(ix);
            Assert.assertNotNull(valueAsJson);
            Assert.assertTrue(valueAsJson.has(GraphSONTokens.TYPE));
            Assert.assertEquals(GraphSONTokens.TYPE_BOOLEAN, valueAsJson.optString(GraphSONTokens.TYPE));
            Assert.assertTrue(valueAsJson.has(GraphSONTokens.VALUE));
            Assert.assertEquals(true, valueAsJson.optBoolean(GraphSONTokens.VALUE));
        }
    }

    @Test
    public void jsonFromElementVertexLongListPropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<Long> list = new ArrayList<Long>();
        list.add(1000L);
        list.add(1000L);
        list.add(1000L);

        v.setProperty("keyList", list);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.EXTENDED);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyList"));

        JSONObject listWithTypeAsJson = json.optJSONObject("keyList");
        Assert.assertNotNull(listWithTypeAsJson);
        Assert.assertTrue(listWithTypeAsJson.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_LIST, listWithTypeAsJson.optString(GraphSONTokens.TYPE));
        Assert.assertTrue(listWithTypeAsJson.has(GraphSONTokens.VALUE));
        JSONArray listAsJSON = listWithTypeAsJson.optJSONArray(GraphSONTokens.VALUE);
        Assert.assertNotNull(listAsJSON);
        Assert.assertEquals(3, listAsJSON.length());

        for (int ix = 0; ix < listAsJSON.length(); ix++) {
            JSONObject valueAsJson = listAsJSON.optJSONObject(ix);
            Assert.assertNotNull(valueAsJson);
            Assert.assertTrue(valueAsJson.has(GraphSONTokens.TYPE));
            Assert.assertEquals(GraphSONTokens.TYPE_LONG, valueAsJson.optString(GraphSONTokens.TYPE));
            Assert.assertTrue(valueAsJson.has(GraphSONTokens.VALUE));
            Assert.assertEquals(1000L, valueAsJson.optLong(GraphSONTokens.VALUE));
        }
    }

    @Test
    public void jsonFromElementVertexIntListPropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(1);
        list.add(1);

        v.setProperty("keyList", list);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.EXTENDED);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyList"));

        JSONObject listWithTypeAsJson = json.optJSONObject("keyList");
        Assert.assertNotNull(listWithTypeAsJson);
        Assert.assertTrue(listWithTypeAsJson.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_LIST, listWithTypeAsJson.optString(GraphSONTokens.TYPE));
        Assert.assertTrue(listWithTypeAsJson.has(GraphSONTokens.VALUE));
        JSONArray listAsJSON = listWithTypeAsJson.optJSONArray(GraphSONTokens.VALUE);
        Assert.assertNotNull(listAsJSON);
        Assert.assertEquals(3, listAsJSON.length());

        for (int ix = 0; ix < listAsJSON.length(); ix++) {
            JSONObject valueAsJson = listAsJSON.optJSONObject(ix);
            Assert.assertNotNull(valueAsJson);
            Assert.assertTrue(valueAsJson.has(GraphSONTokens.TYPE));
            Assert.assertEquals(GraphSONTokens.TYPE_INTEGER, valueAsJson.optString(GraphSONTokens.TYPE));
            Assert.assertTrue(valueAsJson.has(GraphSONTokens.VALUE));
            Assert.assertEquals(1, valueAsJson.optInt(GraphSONTokens.VALUE));
        }
    }

    @Test
    public void jsonFromElementVertexListOfListPropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(1);
        list.add(1);

        List<List<Integer>> listList = new ArrayList<List<Integer>>();
        listList.add(list);

        v.setProperty("keyList", listList);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.EXTENDED);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyList"));

        JSONObject listWithTypeAsJson = json.optJSONObject("keyList");
        Assert.assertNotNull(listWithTypeAsJson);
        Assert.assertTrue(listWithTypeAsJson.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_LIST, listWithTypeAsJson.optString(GraphSONTokens.TYPE));
        Assert.assertTrue(listWithTypeAsJson.has(GraphSONTokens.VALUE));
        JSONArray listAsJSON = listWithTypeAsJson.optJSONArray(GraphSONTokens.VALUE).optJSONObject(0).getJSONArray(GraphSONTokens.VALUE);
        Assert.assertNotNull(listAsJSON);
        Assert.assertEquals(3, listAsJSON.length());

        for (int ix = 0; ix < listAsJSON.length(); ix++) {
            JSONObject valueAsJson = listAsJSON.optJSONObject(ix);
            Assert.assertNotNull(valueAsJson);
            Assert.assertTrue(valueAsJson.has(GraphSONTokens.TYPE));
            Assert.assertEquals(GraphSONTokens.TYPE_INTEGER, valueAsJson.optString(GraphSONTokens.TYPE));
            Assert.assertTrue(valueAsJson.has(GraphSONTokens.VALUE));
            Assert.assertEquals(1, valueAsJson.optInt(GraphSONTokens.VALUE));
        }
    }


    @Test
    public void jsonFromElementVertexMapPropertiesNoKeysWithTypes() throws JSONException {
        Vertex v = this.graph.addVertex(1);

        Map map = new HashMap();
        map.put("this", "some");
        map.put("that", 1);

        v.setProperty("keyMap", map);

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.EXTENDED);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.has(GraphSONTokens._ID));
        Assert.assertEquals(1, json.optInt(GraphSONTokens._ID));
        Assert.assertTrue(json.has("keyMap"));

        JSONObject mapWithTypeAsJSON = json.optJSONObject("keyMap");
        Assert.assertNotNull(mapWithTypeAsJSON);
        Assert.assertTrue(mapWithTypeAsJSON.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_MAP, mapWithTypeAsJSON.optString(GraphSONTokens.TYPE));

        Assert.assertTrue(mapWithTypeAsJSON.has(GraphSONTokens.VALUE));
        JSONObject mapAsJSON = mapWithTypeAsJSON.optJSONObject(GraphSONTokens.VALUE);

        Assert.assertTrue(mapAsJSON.has("this"));
        JSONObject thisAsJson = mapAsJSON.optJSONObject("this");
        Assert.assertTrue(thisAsJson.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_STRING, thisAsJson.optString(GraphSONTokens.TYPE));
        Assert.assertTrue(thisAsJson.has(GraphSONTokens.VALUE));
        Assert.assertEquals("some", thisAsJson.optString(GraphSONTokens.VALUE));

        Assert.assertTrue(mapAsJSON.has("that"));
        JSONObject thatAsJson = mapAsJSON.optJSONObject("that");
        Assert.assertTrue(thatAsJson.has(GraphSONTokens.TYPE));
        Assert.assertEquals(GraphSONTokens.TYPE_INTEGER, thatAsJson.optString(GraphSONTokens.TYPE));
        Assert.assertTrue(thatAsJson.has(GraphSONTokens.VALUE));
        Assert.assertEquals(1, thatAsJson.optInt(GraphSONTokens.VALUE));


    }

    @Test
    public void jsonFromElementNullsNoKeysNoTypes() throws JSONException {
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

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.NORMAL);

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
    public void jsonFromElementNullsNoKeysWithTypes() throws JSONException {
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

        JSONObject json = GraphSONUtility.jsonFromElement(v, null, GraphSONMode.EXTENDED);

        Assert.assertNotNull(json);
        JSONObject jsonObjectKey = json.optJSONObject("key");
        Assert.assertTrue(jsonObjectKey.isNull(GraphSONTokens.VALUE));
        Assert.assertEquals(GraphSONTokens.TYPE_UNKNOWN, jsonObjectKey.optString(GraphSONTokens.TYPE));

        JSONObject jsonMap = json.optJSONObject("keyMap").optJSONObject(GraphSONTokens.VALUE);
        Assert.assertNotNull(jsonMap);
        JSONObject jsonObjectMap = jsonMap.optJSONObject("innerkey");
        Assert.assertTrue(jsonObjectMap.isNull(GraphSONTokens.VALUE));
        Assert.assertEquals(GraphSONTokens.TYPE_UNKNOWN, jsonObjectMap.optString(GraphSONTokens.TYPE));

        JSONArray jsonInnerArray = jsonMap.getJSONObject("list").getJSONArray(GraphSONTokens.VALUE);
        Assert.assertNotNull(jsonInnerArray);
        JSONObject jsonObjectInnerListFirst = jsonInnerArray.getJSONObject(0);
        Assert.assertTrue(jsonObjectInnerListFirst.isNull(GraphSONTokens.VALUE));
        Assert.assertEquals(GraphSONTokens.TYPE_UNKNOWN, jsonObjectInnerListFirst.optString(GraphSONTokens.TYPE));

        JSONArray jsonArray = json.getJSONObject("keyList").getJSONArray(GraphSONTokens.VALUE);
        Assert.assertNotNull(jsonArray);
        JSONObject jsonObjectListFirst = jsonArray.getJSONObject(0);
        Assert.assertTrue(jsonObjectListFirst.isNull(GraphSONTokens.VALUE));
        Assert.assertEquals(GraphSONTokens.TYPE_UNKNOWN, jsonObjectListFirst.optString(GraphSONTokens.TYPE));
    }

    @Test
    public void vertexFromJsonValid() throws IOException, JSONException {
        Graph g = new TinkerGraph();
        ElementFactory factory = new GraphElementFactory(g);

        Vertex v = GraphSONUtility.vertexFromJson(new JSONObject(new JSONTokener(vertexJson1)), factory, GraphSONMode.NORMAL, null);

        Assert.assertSame(v, g.getVertex(1));

        // tinkergraph converts id to string
        Assert.assertEquals("1", v.getId());
        Assert.assertEquals("marko", v.getProperty("name"));
        Assert.assertEquals(29, v.getProperty("age"));
    }

    @Test
    public void vertexFromJsonStringValid() throws IOException, JSONException {
        Graph g = new TinkerGraph();
        ElementFactory factory = new GraphElementFactory(g);

        Vertex v = GraphSONUtility.vertexFromJson(vertexJson1, factory, GraphSONMode.NORMAL, null);

        Assert.assertSame(v, g.getVertex(1));

        // tinkergraph converts id to string
        Assert.assertEquals("1", v.getId());
        Assert.assertEquals("marko", v.getProperty("name"));
        Assert.assertEquals(29, v.getProperty("age"));
    }

    @Test
    public void vertexFromJsonInputStreamValid() throws IOException, JSONException {
        Graph g = new TinkerGraph();
        ElementFactory factory = new GraphElementFactory(g);

        Vertex v = GraphSONUtility.vertexFromJson(inputStreamVertexJson1, factory, GraphSONMode.NORMAL, null);

        Assert.assertSame(v, g.getVertex(1));

        // tinkergraph converts id to string
        Assert.assertEquals("1", v.getId());
        Assert.assertEquals("marko", v.getProperty("name"));
        Assert.assertEquals(29, v.getProperty("age"));
    }

    @Test
    public void vertexFromJsonIgnoreKeyValid() throws IOException, JSONException {
        Graph g = new TinkerGraph();
        ElementFactory factory = new GraphElementFactory(g);

        Set<String> ignoreAge = new HashSet<String>();
        ignoreAge.add("age");
        ElementPropertyConfig config = ElementPropertyConfig.ExcludeProperties(ignoreAge, null);
        GraphSONUtility utility = new GraphSONUtility(GraphSONMode.NORMAL, factory, config);
        Vertex v = utility.vertexFromJson(new JSONObject(new JSONTokener(vertexJson1)));

        Assert.assertSame(v, g.getVertex(1));

        // tinkergraph converts id to string
        Assert.assertEquals("1", v.getId());
        Assert.assertEquals("marko", v.getProperty("name"));
        Assert.assertNull(v.getProperty("age"));
    }

    @Test
    public void edgeFromJsonValid()  throws IOException, JSONException {
        Graph g = new TinkerGraph();
        ElementFactory factory = new GraphElementFactory(g);

        Vertex v1 = GraphSONUtility.vertexFromJson(new JSONObject(new JSONTokener(vertexJson1)), factory, GraphSONMode.NORMAL, null);
        Vertex v2 = GraphSONUtility.vertexFromJson(new JSONObject(new JSONTokener(vertexJson2)), factory, GraphSONMode.NORMAL, null);
        Edge e = GraphSONUtility.edgeFromJson(new JSONObject(new JSONTokener(edgeJson)), v1, v2, factory, GraphSONMode.NORMAL, null);

        Assert.assertSame(v1, g.getVertex(1));
        Assert.assertSame(v2, g.getVertex(2));
        Assert.assertSame(e, g.getEdge(7));

        // tinkergraph converts id to string
        Assert.assertEquals("7", e.getId());
        Assert.assertEquals(0.5d, e.getProperty("weight"));
        Assert.assertEquals("knows", e.getLabel());
        Assert.assertEquals(v1, e.getVertex(Direction.OUT));
        Assert.assertEquals(v2, e.getVertex(Direction.IN));
    }

    @Test
    public void edgeFromJsonStringValid()  throws IOException, JSONException {
        Graph g = new TinkerGraph();
        ElementFactory factory = new GraphElementFactory(g);

        Vertex v1 = GraphSONUtility.vertexFromJson(vertexJson1, factory, GraphSONMode.NORMAL, null);
        Vertex v2 = GraphSONUtility.vertexFromJson(vertexJson2, factory, GraphSONMode.NORMAL, null);
        Edge e = GraphSONUtility.edgeFromJson(edgeJson, v1, v2, factory, GraphSONMode.NORMAL, null);

        Assert.assertSame(v1, g.getVertex(1));
        Assert.assertSame(v2, g.getVertex(2));
        Assert.assertSame(e, g.getEdge(7));

        // tinkergraph converts id to string
        Assert.assertEquals("7", e.getId());
        Assert.assertEquals(0.5d, e.getProperty("weight"));
        Assert.assertEquals("knows", e.getLabel());
        Assert.assertEquals(v1, e.getVertex(Direction.OUT));
        Assert.assertEquals(v2, e.getVertex(Direction.IN));
    }

    @Test
    public void edgeFromJsonIgnoreWeightValid()  throws IOException, JSONException {
        Graph g = new TinkerGraph();
        ElementFactory factory = new GraphElementFactory(g);

        Vertex v1 = GraphSONUtility.vertexFromJson(new JSONObject(new JSONTokener(vertexJson1)), factory, GraphSONMode.NORMAL, null);
        Vertex v2 = GraphSONUtility.vertexFromJson(new JSONObject(new JSONTokener(vertexJson2)), factory, GraphSONMode.NORMAL, null);

        Set<String> ignoreWeight = new HashSet<String>();
        ignoreWeight.add("weight");
        ElementPropertyConfig config = ElementPropertyConfig.ExcludeProperties(null, ignoreWeight);
        GraphSONUtility utility = new GraphSONUtility(GraphSONMode.NORMAL, factory, config);
        Edge e = utility.edgeFromJson(new JSONObject(new JSONTokener(edgeJson)), v1, v2);

        Assert.assertSame(v1, g.getVertex(1));
        Assert.assertSame(v2, g.getVertex(2));
        Assert.assertSame(e, g.getEdge(7));

        // tinkergraph converts id to string
        Assert.assertEquals("7", e.getId());
        Assert.assertNull(e.getProperty("weight"));
        Assert.assertEquals("knows", e.getLabel());
        Assert.assertEquals(v1, e.getVertex(Direction.OUT));
        Assert.assertEquals(v2, e.getVertex(Direction.IN));
    }

    @Test
    public void edgeFromJsonNormalLabelOrIdOnEdge()  throws IOException, JSONException {
        Graph g = new TinkerGraph();
        ElementFactory factory = new GraphElementFactory(g);

        Vertex v1 = GraphSONUtility.vertexFromJson(new JSONObject(new JSONTokener(vertexJson1)), factory, GraphSONMode.NORMAL, null);
        Vertex v2 = GraphSONUtility.vertexFromJson(new JSONObject(new JSONTokener(vertexJson2)), factory, GraphSONMode.NORMAL, null);
        Edge e = GraphSONUtility.edgeFromJson(new JSONObject(new JSONTokener(edgeJsonLight)), v1, v2, factory, GraphSONMode.NORMAL, null);

        Assert.assertSame(v1, g.getVertex(1));
        Assert.assertSame(v2, g.getVertex(2));
        Assert.assertSame(e, g.getEdge(0));
    }

    @Test
    public void edgeFromJsonInputStreamCompactLabelOrIdOnEdge()  throws IOException, JSONException {
        Graph g = new TinkerGraph();
        ElementFactory factory = new GraphElementFactory(g);

        Vertex v1 = GraphSONUtility.vertexFromJson(new JSONObject(new JSONTokener(vertexJson1)), factory, GraphSONMode.COMPACT, null);
        Vertex v2 = GraphSONUtility.vertexFromJson(new JSONObject(new JSONTokener(vertexJson2)), factory, GraphSONMode.COMPACT, null);
        Edge e = GraphSONUtility.edgeFromJson(inputStreamEdgeJsonLight, v1, v2, factory, GraphSONMode.COMPACT, null);

        Assert.assertSame(v1, g.getVertex(1));
        Assert.assertSame(v2, g.getVertex(2));
        Assert.assertSame(e, g.getEdge(0));
    }

    @Test
    public void edgeFromJsonInputStreamCompactNoIdOnEdge()  throws IOException, JSONException {
        Graph g = new TinkerGraph();
        ElementFactory factory = new GraphElementFactory(g);

        Set<String> vertexKeys = new HashSet<String>() {{
            add(GraphSONTokens._ID);
        }};

        Set<String> edgeKeys = new HashSet<String>() {{
            add(GraphSONTokens._IN_V);
        }};

        GraphSONUtility graphson = new GraphSONUtility(GraphSONMode.COMPACT, factory, vertexKeys, edgeKeys);

        Vertex v1 = graphson.vertexFromJson(new JSONObject(new JSONTokener(vertexJson1)));
        Vertex v2 = graphson.vertexFromJson(new JSONObject(new JSONTokener(vertexJson2)));
        Edge e = graphson.edgeFromJson(inputStreamEdgeJsonLight, v1, v2);

        Assert.assertSame(v1, g.getVertex(1));
        Assert.assertSame(v2, g.getVertex(2));
        Assert.assertSame(e, g.getEdge(0));
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
