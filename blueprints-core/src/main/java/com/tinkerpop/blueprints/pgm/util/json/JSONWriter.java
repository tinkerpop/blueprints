package com.tinkerpop.blueprints.pgm.util.json;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class JSONWriter {

    private static JSONArray createJSONList(final List list, final List<String> propertyKeys, final boolean showTypes) throws JSONException {
        final JSONArray jsonList = new JSONArray();
        for (Object item : list) {
            if (item instanceof Element) {
                jsonList.put(createJSONElement((Element) item, propertyKeys, showTypes));
            } else if (item instanceof List) {
                jsonList.put(createJSONList((List) item, propertyKeys, showTypes));
            } else if (item instanceof Map) {
                jsonList.put(createJSONMap((Map) item, propertyKeys, showTypes));
            } else if (item.getClass().isArray()) {
                jsonList.put(createJSONList(convertArrayToList(item), propertyKeys, showTypes));
            } else {
                jsonList.put(item);
            }
        }
        return jsonList;
    }

    private static JSONObject createJSONMap(final Map map, final List<String> propertyKeys, final boolean showTypes) throws JSONException {
        final JSONObject jsonMap = new JSONObject();
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof List) {
                value = createJSONList((List) value, propertyKeys, showTypes);
            } else if (value instanceof Map) {
                value = createJSONMap((Map) value, propertyKeys, showTypes);
            } else if (value instanceof Element) {
                value = createJSONElement((Element) value, propertyKeys, showTypes);
            } else if (value.getClass().isArray()) {
                value = createJSONList(convertArrayToList(value), propertyKeys, showTypes);
            }

            jsonMap.put(key.toString(), getValue(value, showTypes));
        }
        return jsonMap;

    }


    public static JSONObject createJSONElement(final Element element) {
        return createJSONElement(element, null, false);
    }

    public static JSONObject createJSONElement(final Element element, final List<String> propertyKeys, final boolean showTypes) {

        JSONObject jsonElement = new JSONObject();

        try {
            jsonElement = createJSONMap(createPropertyMap(element, propertyKeys), propertyKeys, showTypes);
            jsonElement.put(JSONTokens._ID, element.getId());

            if (element instanceof Vertex) {
                jsonElement.put(JSONTokens._TYPE, JSONTokens.VERTEX);
            } else if (element instanceof Edge) {
                final Edge edge = (Edge) element;
                jsonElement.put(JSONTokens._TYPE, JSONTokens.EDGE);
                jsonElement.put(JSONTokens._OUT_V, edge.getOutVertex().getId());
                jsonElement.put(JSONTokens._IN_V, edge.getInVertex().getId());
                jsonElement.put(JSONTokens._LABEL, edge.getLabel());

            }
        } catch (JSONException jsone) {
            // the keys are all constants...this really can't happen
        }

        return jsonElement;
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

    private static Object getValue(Object value, final boolean includeType) throws JSONException {

        Object returnValue = value;

        // type will be one of: map, list, string, long, int, double, float.
        // in the event of a complex object it will call a toString and store as a
        // string
        String type = determineType(value);

        // if the includeType is set to true then show the data types of the properties
        if (includeType) {
            JSONObject valueAndType = new JSONObject();
            valueAndType.put(JSONTokens.TYPE, type);

            if (type.equals(JSONTokens.TYPE_LIST)) {

                // values of lists must be accumulated as JSONObjects under the value key.
                // will return as a JSONArray. called recursively to traverse the entire
                // object graph of each item in the array.
                JSONArray list = (JSONArray) value;
                for (int ix = 0; ix < list.length(); ix++) {
                    valueAndType.accumulate(JSONTokens.VALUE, getValue(list.get(ix), includeType));
                }
            } else if (type.equals(JSONTokens.TYPE_MAP)) {

                // maps are converted to a JSONObject.  called recursively to traverse
                // the entire object graph within the map.
                JSONObject convertedMap = new JSONObject();
                JSONObject jsonObject = (JSONObject) value;
                Iterator keyIterator = jsonObject.keys();
                while (keyIterator.hasNext()) {
                    Object key = keyIterator.next();

                    // no need to getValue() here as this is already a JSONObject and should have type info
                    convertedMap.put(key.toString(), jsonObject.get(key.toString()));
                }

                valueAndType.put(JSONTokens.VALUE, convertedMap);
            } else {

                // this must be a primitive value or a complex object.  if a complex
                // object it will be handled by a call to toString and stored as a
                // string value
                valueAndType.put(JSONTokens.VALUE, value);
            }

            // this goes back as a JSONObject with data type and value
            returnValue = valueAndType;
        }

        return returnValue;
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
        if (value instanceof Double) {
            type = JSONTokens.TYPE_DOUBLE;
        } else if (value instanceof Float) {
            type = JSONTokens.TYPE_FLOAT;
        } else if (value instanceof Integer) {
            type = JSONTokens.TYPE_INTEGER;
        } else if (value instanceof Long) {
            type = JSONTokens.TYPE_LONG;
        } else if (value instanceof Boolean) {
            type = JSONTokens.TYPE_BOOLEAN;
        } else if (value instanceof JSONArray) {
            type = JSONTokens.TYPE_LIST;
        } else if (value instanceof JSONObject) {
            type = JSONTokens.TYPE_MAP;
        }

        return type;
    }
}
