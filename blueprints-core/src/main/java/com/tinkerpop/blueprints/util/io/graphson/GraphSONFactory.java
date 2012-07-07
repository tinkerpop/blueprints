package com.tinkerpop.blueprints.util.io.graphson;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
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
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public final class GraphSONFactory {

    private static final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
    private static final JsonFactory jsonFactory = new MappingJsonFactory();

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Reads an individual Vertex from JSON.  The vertex must match the accepted GraphSON format.
     *
     * @param json a single vertex in GraphSON format
     * @param factory the factory responsible for constructing graph elements
     * @param hasEmbeddedTypes the GraphSON has embedded types
     */
    public static Vertex vertexFromJson(final JSONObject json, final ElementFactory factory, final boolean hasEmbeddedTypes) throws IOException{
        final JsonParser jp = jsonFactory.createJsonParser(json.toString());
        final JsonNode node = jp.readValueAsTree();
        return vertexFromJson(node, factory, hasEmbeddedTypes);
    }

    /**
     * Reads an individual Vertex from JSON.  The vertex must match the accepted GraphSON format.
     *
     * @param node a single vertex in GraphSON format
     * @param factory the factory responsible for constructing graph elements
     * @param hasEmbeddedTypes the GraphSON has embedded types
     */
    public static Vertex vertexFromJson(final JsonNode node, final ElementFactory factory, final boolean hasEmbeddedTypes) throws IOException{

        final Map<String, Object> props = readProperties(node, true, hasEmbeddedTypes);

        final String vertexId = node.get(GraphSONTokens._ID).getValueAsText();
        final Vertex v = factory.createVertex(vertexId);

        for (Map.Entry<String, Object> entry : props.entrySet()) {
            v.setProperty(entry.getKey(), entry.getValue());
        }

        return v;
    }

    /**
     * Reads an individual Edge from JSON.  The edge must match the accepted GraphSON format.
     *
     * @param json a single edge in GraphSON format
     * @param factory the factory responsible for constructing graph elements
     * @param hasEmbeddedTypes the GraphSON has embedded types
     */
    public static Edge edgeFromJSON(final JSONObject json, final Vertex out, final Vertex in,
                                final ElementFactory factory, final boolean hasEmbeddedTypes)  throws IOException {
        final JsonParser jp = jsonFactory.createJsonParser(json.toString());
        final JsonNode node = jp.readValueAsTree();
        return edgeFromJSON(node, out, in, factory, hasEmbeddedTypes);
    }

    /**
     * Reads an individual Edge from JSON.  The edge must match the accepted GraphSON format.
     *
     * @param node a single edge in GraphSON format
     * @param factory the factory responsible for constructing graph elements
     * @param hasEmbeddedTypes the GraphSON has embedded types
     */
    public static Edge edgeFromJSON(final JsonNode node, final Vertex out, final Vertex in,
                                final ElementFactory factory, final boolean hasEmbeddedTypes)  throws IOException {

        final Map<String, Object> props = GraphSONFactory.readProperties(node, true, hasEmbeddedTypes);

        final String edgeId = node.get(GraphSONTokens._ID).getValueAsText();
        final String label = node.get(GraphSONTokens._LABEL).getValueAsText();

        final Edge e = factory.createEdge(edgeId, out, in, label);

        for (Map.Entry<String, Object> entry : props.entrySet()) {
            e.setProperty(entry.getKey(), entry.getValue());
        }

        return e;
    }

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
        putObject(jsonElement, GraphSONTokens._ID, element.getId());

        if (element instanceof Vertex) {
            jsonElement.put(GraphSONTokens._TYPE, GraphSONTokens.VERTEX);
        } else if (element instanceof Edge) {
            final Edge edge = (Edge) element;
            jsonElement.put(GraphSONTokens._TYPE, GraphSONTokens.EDGE);
            putObject(jsonElement, GraphSONTokens._OUT_V, edge.getVertex(Direction.OUT).getId());
            putObject(jsonElement, GraphSONTokens._IN_V, edge.getVertex(Direction.IN).getId());
            jsonElement.put(GraphSONTokens._LABEL, edge.getLabel());
        }

        return jsonElement;
    }

    static Map<String, Object> readProperties(final JsonNode node, final boolean ignoreReservedKeys, final boolean hasEmbeddedTypes) {
        final Map<String, Object> map = new HashMap<String, Object>();

        final Iterator<Map.Entry<String, JsonNode>> iterator = node.getFields();
        while (iterator.hasNext()) {
            final Map.Entry<String, JsonNode> entry = iterator.next();

            if (!ignoreReservedKeys || (ignoreReservedKeys && !isReservedKey(entry.getKey()))) {
                map.put(entry.getKey(), readProperty(entry.getValue(), hasEmbeddedTypes));
            }
        }

        return map;
    }

    private static boolean isReservedKey(final String key) {
        return key.equals(GraphSONTokens._ID) || key.equals(GraphSONTokens._TYPE) || key.equals(GraphSONTokens._LABEL)
                || key.equals(GraphSONTokens._OUT_V) || key.equals(GraphSONTokens._IN_V);
    }

    private static Object readProperty(final JsonNode node, final boolean hasEmbeddedTypes) {
        final Object propertyValue;

        if (hasEmbeddedTypes) {
            if (node.get(GraphSONTokens.TYPE).getValueAsText().equals(GraphSONTokens.TYPE_UNKNOWN)) {
                propertyValue = null;
            } else if (node.get(GraphSONTokens.TYPE).getValueAsText().equals(GraphSONTokens.TYPE_BOOLEAN)) {
                propertyValue = node.get(GraphSONTokens.VALUE).getBooleanValue();
            } else if (node.get(GraphSONTokens.TYPE).getValueAsText().equals(GraphSONTokens.TYPE_FLOAT)) {
                propertyValue = Float.parseFloat(node.get(GraphSONTokens.VALUE).getValueAsText());
            } else if (node.get(GraphSONTokens.TYPE).getValueAsText().equals(GraphSONTokens.TYPE_DOUBLE)) {
                propertyValue = node.get(GraphSONTokens.VALUE).getDoubleValue();
            } else if (node.get(GraphSONTokens.TYPE).getValueAsText().equals(GraphSONTokens.TYPE_INTEGER)) {
                propertyValue = node.get(GraphSONTokens.VALUE).getIntValue();
            } else if (node.get(GraphSONTokens.TYPE).getValueAsText().equals(GraphSONTokens.TYPE_LONG)) {
                propertyValue = node.get(GraphSONTokens.VALUE).getLongValue();
            } else if (node.get(GraphSONTokens.TYPE).getValueAsText().equals(GraphSONTokens.TYPE_STRING)) {
                propertyValue = node.get(GraphSONTokens.VALUE).getTextValue();
            } else if (node.get(GraphSONTokens.TYPE).getValueAsText().equals(GraphSONTokens.TYPE_LIST)) {
                propertyValue = readProperties(node.get(GraphSONTokens.VALUE).getElements(), hasEmbeddedTypes);
            } else if (node.get(GraphSONTokens.TYPE).getValueAsText().equals(GraphSONTokens.TYPE_MAP)) {
                propertyValue = readProperties(node.get(GraphSONTokens.VALUE), false, hasEmbeddedTypes);
            } else {
                propertyValue = node.getValueAsText();
            }
        } else {
            if (node.isNull()) {
                propertyValue = null;
            } else if (node.isBoolean()) {
                propertyValue = node.getBooleanValue();
            } else if (node.isDouble()) {
                propertyValue = node.getDoubleValue();
            } else if (node.isInt()) {
                propertyValue = node.getIntValue();
            } else if (node.isLong()) {
                propertyValue = node.getLongValue();
            } else if (node.isTextual()) {
                propertyValue = node.getTextValue();
            } else if (node.isArray()) {
                propertyValue = readProperties(node.getElements(), hasEmbeddedTypes);
            } else if (node.isObject()) {
                propertyValue = readProperties(node, false, hasEmbeddedTypes);
            } else {
                propertyValue = node.getValueAsText();
            }
        }

        return propertyValue;
    }

    private static List readProperties(final Iterator<JsonNode> listOfNodes, final boolean hasEmbeddedTypes) {
        final List array = new ArrayList();

        while (listOfNodes.hasNext()) {
            array.add(readProperty(listOfNodes.next(), hasEmbeddedTypes));
        }

        return array;
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
            valueAndType.put(GraphSONTokens.TYPE, type);

            if (type.equals(GraphSONTokens.TYPE_LIST)) {

                // values of lists must be accumulated as ObjectNode objects under the value key.
                // will return as a ArrayNode. called recursively to traverse the entire
                // object graph of each item in the array.
                ArrayNode list = (ArrayNode) value;

                // there is a set of values that must be accumulated as an array under a key
                ArrayNode valueArray = valueAndType.putArray(GraphSONTokens.VALUE);
                for (int ix = 0; ix < list.size(); ix++) {
                    // the value of each item in the array is a node object from an ArrayNode...must
                    // get the value of it.
                    addObject(valueArray, getValue(getTypedValueFromJsonNode(list.get(ix)), includeType));
                }

            } else if (type.equals(GraphSONTokens.TYPE_MAP)) {

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

                valueAndType.put(GraphSONTokens.VALUE, convertedMap);
            } else {

                // this must be a primitive value or a complex object.  if a complex
                // object it will be handled by a call to toString and stored as a
                // string value
                putObject(valueAndType, GraphSONTokens.VALUE, value);
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
        String type = GraphSONTokens.TYPE_STRING;
        if (value == null) {
            type = "unknown";
        } else if (value instanceof Double) {
            type = GraphSONTokens.TYPE_DOUBLE;
        } else if (value instanceof Float) {
            type = GraphSONTokens.TYPE_FLOAT;
        } else if (value instanceof Integer) {
            type = GraphSONTokens.TYPE_INTEGER;
        } else if (value instanceof Long) {
            type = GraphSONTokens.TYPE_LONG;
        } else if (value instanceof Boolean) {
            type = GraphSONTokens.TYPE_BOOLEAN;
        } else if (value instanceof ArrayNode) {
            type = GraphSONTokens.TYPE_LIST;
        } else if (value instanceof ObjectNode) {
            type = GraphSONTokens.TYPE_MAP;
        }

        return type;
    }
}
