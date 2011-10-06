package com.tinkerpop.blueprints.pgm.util.json;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Helps write graph elements to TinkerPop JSON format. Contains methods to support both Jackson and Jettison
 * for JSON processing.
 *
 * @author Stephen Mallette
 */
public final class JSONWriter {

    private static JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a Jettison JSONObject from a graph element. All property keys are serialized and types are not shown.
     *
     * @param element The graph element to convert to JSON.
     */
    public static JSONObject createJSONElement(final Element element) throws JSONException {
        return createJSONElement(element, null, false);
    }

    /**
     * Creates a Jettison JSONObject from a graph element.
     *
     * @param element      the graph element to convert to JSON.
     * @param propertyKeys The property keys at the root of the element to serialize.  If null, then all keys are serialized.
     * @param showTypes    Data types are written to the JSON explicitly if true.
     */
    public static JSONObject createJSONElement(final Element element, final List<String> propertyKeys, final boolean showTypes) throws JSONException {
        ObjectNode objectNode = createJSONElementAsObjectNode(element, propertyKeys, showTypes);

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(new JSONTokener(mapper.writeValueAsString(objectNode)));
        } catch (IOException ioe) {
            // repackage this as a JSONException...seems sensible as the caller will only know about
            // the jettison object not being created
            throw new JSONException(ioe);
        }

        return jsonObject;
    }

    /**
     * Creates a Jackson ObjectNode from a graph element. All property keys are serialized and types are not shown.
     *
     * @param element The graph element to convert to JSON.
     */
    public static ObjectNode createJSONElementAsObjectNode(final Element element) {
        return createJSONElementAsObjectNode(element, null, false);
    }

    /**
     * Creates a Jackson ObjectNode from a graph element.
     *
     * @param element      the graph element to convert to JSON.
     * @param propertyKeys The property keys at the root of the element to serialize.  If null, then all keys are serialized.
     * @param showTypes    Data types are written to the JSON explicitly if true.
     */
    public static ObjectNode createJSONElementAsObjectNode(final Element element, final List<String> propertyKeys, final boolean showTypes) {

        ObjectNode jsonElement = createJSONMap(createPropertyMap(element, propertyKeys), propertyKeys, showTypes);
        putObject(jsonElement, JSONTokens._ID, element.getId());

        if (element instanceof Vertex) {
            jsonElement.put(JSONTokens._TYPE, JSONTokens.VERTEX);
        } else if (element instanceof Edge) {
            final Edge edge = (Edge) element;
            jsonElement.put(JSONTokens._TYPE, JSONTokens.EDGE);
            putObject(jsonElement, JSONTokens._OUT_V, edge.getOutVertex().getId());
            putObject(jsonElement, JSONTokens._IN_V, edge.getInVertex().getId());
            jsonElement.put(JSONTokens._LABEL, edge.getLabel());
        }

        return jsonElement;
    }

    private static ArrayNode createJSONList(final List list, final List<String> propertyKeys, final boolean showTypes) {
        final ArrayNode jsonList = jsonNodeFactory.arrayNode();
        for (Object item : list) {
            if (item instanceof Element) {
                jsonList.add(createJSONElementAsObjectNode((Element) item, propertyKeys, showTypes));
            } else if (item instanceof List) {
                jsonList.add(createJSONList((List) item, propertyKeys, showTypes));
            } else if (item instanceof Map) {
                jsonList.add(createJSONMap((Map) item, propertyKeys, showTypes));
            } else if (item != null && item.getClass().isArray()) {
                jsonList.add(createJSONList(convertArrayToList(item), propertyKeys, showTypes));
            } else {
                addObject(jsonList, item);
            }
        }
        return jsonList;
    }

    private static ObjectNode createJSONMap(final Map map, final List<String> propertyKeys, final boolean showTypes) {
        final ObjectNode jsonMap = jsonNodeFactory.objectNode();
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            if (value != null) {
                if (value instanceof List) {
                    value = createJSONList((List) value, propertyKeys, showTypes);
                } else if (value instanceof Map) {
                    value = createJSONMap((Map) value, propertyKeys, showTypes);
                } else if (value instanceof Element) {
                    value = createJSONElementAsObjectNode((Element) value, propertyKeys, showTypes);
                } else if (value.getClass().isArray()) {
                    value = createJSONList(convertArrayToList(value), propertyKeys, showTypes);
                }
            }

            putObject(jsonMap, key.toString(), getValue(value, showTypes));
        }
        return jsonMap;

    }

    private static void addObject(final ArrayNode jsonList, final Object value) {
        if (value == null) {
            jsonList.add((JsonNode) null);
        } else if (value instanceof Boolean) {
            jsonList.add((Boolean) value);
        } else if (value instanceof Long) {
            jsonList.add((Long) value);
        } else if (value instanceof Integer) {
            jsonList.add((Integer) value);
        } else if (value instanceof Float) {
            jsonList.add((Float) value);
        } else if (value instanceof Double) {
            jsonList.add((Double) value);
        } else if (value instanceof String) {
            jsonList.add((String) value);
        } else if (value instanceof ObjectNode) {
            jsonList.add((ObjectNode) value);
        } else if (value instanceof ArrayNode) {
            jsonList.add((ArrayNode) value);
        } else {
            jsonList.add(value.toString());
        }
    }

    private static void putObject(final ObjectNode jsonMap, final String key, final Object value) {
        if (value == null) {
            jsonMap.put(key, (JsonNode) null);
        } else if (value instanceof Boolean) {
            jsonMap.put(key, (Boolean) value);
        } else if (value instanceof Long) {
            jsonMap.put(key, (Long) value);
        } else if (value instanceof Integer) {
            jsonMap.put(key, (Integer) value);
        } else if (value instanceof Float) {
            jsonMap.put(key, (Float) value);
        } else if (value instanceof Double) {
            jsonMap.put(key, (Double) value);
        } else if (value instanceof String) {
            jsonMap.put(key, (String) value);
        } else if (value instanceof ObjectNode) {
            jsonMap.put(key, (ObjectNode) value);
        } else if (value instanceof ArrayNode) {
            jsonMap.put(key, (ArrayNode) value);
        } else {
            jsonMap.put(key, value.toString());
        }
    }

    private static Map createPropertyMap(final Element element, final List<String> propertyKeys) {
        final Map map = new HashMap<String, Object>();

        if (propertyKeys == null) {
            for (String key : element.getPropertyKeys()) {
                map.put(key, element.getProperty(key));
            }
        } else {
            for (String key : propertyKeys) {
                Object valToPutInMap = element.getProperty(key);
                if (valToPutInMap != null) {
                    map.put(key, valToPutInMap);
                }
            }
        }

        return map;
    }

    private static Object getValue(Object value, final boolean includeType) {

        Object returnValue = value;

        // if the includeType is set to true then show the data types of the properties
        if (includeType) {

            // type will be one of: map, list, string, long, int, double, float.
            // in the event of a complex object it will call a toString and store as a
            // string
            String type = determineType(value);

            ObjectNode valueAndType = jsonNodeFactory.objectNode();
            valueAndType.put(JSONTokens.TYPE, type);

            if (type.equals(JSONTokens.TYPE_LIST)) {

                // values of lists must be accumulated as ObjectNode objects under the value key.
                // will return as a ArrayNode. called recursively to traverse the entire
                // object graph of each item in the array.
                ArrayNode list = (ArrayNode) value;

                // there is a set of values that must be accumulated as an array under a key
                ArrayNode valueArray = valueAndType.putArray(JSONTokens.VALUE);
                for (int ix = 0; ix < list.size(); ix++) {
                    // the value of each item in the array is a node object from an ArrayNode...must
                    // get the value of it.
                    addObject(valueArray, getValue(getTypedValueFromJsonNode(list.get(ix)), includeType));
                }

            } else if (type.equals(JSONTokens.TYPE_MAP)) {

                // maps are converted to a ObjectNode.  called recursively to traverse
                // the entire object graph within the map.
                ObjectNode convertedMap = jsonNodeFactory.objectNode();
                ObjectNode jsonObject = (ObjectNode) value;
                Iterator keyIterator = jsonObject.getFieldNames();
                while (keyIterator.hasNext()) {
                    Object key = keyIterator.next();

                    // no need to getValue() here as this is already a ObjectNode and should have type info
                    convertedMap.put(key.toString(), jsonObject.get(key.toString()));
                }

                valueAndType.put(JSONTokens.VALUE, convertedMap);
            } else {

                // this must be a primitive value or a complex object.  if a complex
                // object it will be handled by a call to toString and stored as a
                // string value
                putObject(valueAndType, JSONTokens.VALUE, value);
            }

            // this goes back as a JSONObject with data type and value
            returnValue = valueAndType;
        }

        return returnValue;
    }

    private static Object getTypedValueFromJsonNode(JsonNode node) {
        Object theValue = null;

        if (node != null && !node.isNull()) {
            if (node.isBoolean()) {
                theValue = node.getBooleanValue();
            } else if (node.isDouble()) {
                theValue = node.getDoubleValue();
            } else if (node.isInt()) {
                theValue = node.getIntValue();
            } else if (node.isLong()) {
                theValue = node.getLongValue();
            } else if (node.isTextual()) {
                theValue = node.getTextValue();
            } else if (node.isArray()) {
                // this is an array so just send it back so that it can be
                // reprocessed to its primitive components
                theValue = node;
            } else {
                theValue = node.getValueAsText();
            }
        }

        return theValue;
    }

    private static List convertArrayToList(final Object value) {

        // is there seriously no better way to do this...bah!
        List list = new ArrayList();
        if (value instanceof int[]) {
            int[] arr = (int[]) value;
            for (int ix = 0; ix < arr.length; ix++) {
                list.add(arr[ix]);
            }
        } else if (value instanceof double[]) {
            double[] arr = (double[]) value;
            for (int ix = 0; ix < arr.length; ix++) {
                list.add(arr[ix]);
            }
        } else if (value instanceof float[]) {
            float[] arr = (float[]) value;
            for (int ix = 0; ix < arr.length; ix++) {
                list.add(arr[ix]);
            }
        } else if (value instanceof long[]) {
            long[] arr = (long[]) value;
            for (int ix = 0; ix < arr.length; ix++) {
                list.add(arr[ix]);
            }
        } else if (value instanceof boolean[]) {
            boolean[] arr = (boolean[]) value;
            for (int ix = 0; ix < arr.length; ix++) {
                list.add(arr[ix]);
            }
        } else {
            list = Arrays.asList((Object[]) value);
        }

        return list;
    }

    private static String determineType(final Object value) {
        String type = JSONTokens.TYPE_STRING;
        if (value == null) {
            type = "unknown";
        } else if (value instanceof Double) {
            type = JSONTokens.TYPE_DOUBLE;
        } else if (value instanceof Float) {
            type = JSONTokens.TYPE_FLOAT;
        } else if (value instanceof Integer) {
            type = JSONTokens.TYPE_INTEGER;
        } else if (value instanceof Long) {
            type = JSONTokens.TYPE_LONG;
        } else if (value instanceof Boolean) {
            type = JSONTokens.TYPE_BOOLEAN;
        } else if (value instanceof ArrayNode) {
            type = JSONTokens.TYPE_LIST;
        } else if (value instanceof ObjectNode) {
            type = JSONTokens.TYPE_MAP;
        }

        return type;
    }
}
