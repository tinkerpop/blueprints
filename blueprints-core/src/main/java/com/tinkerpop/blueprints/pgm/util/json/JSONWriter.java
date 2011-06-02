package com.tinkerpop.blueprints.pgm.util.json;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class JSONWriter {

    private static JSONArray createJSONList(final List list) {
        final JSONArray jsonList = new JSONArray();
        for (Object item : list) {
            if (item instanceof Element) {
                jsonList.put(createJSONElement((Element) item));
            } else if (item instanceof List) {
                jsonList.put(createJSONList((List) item));
            } else if (item instanceof Map) {
                jsonList.put(createJSONMap((Map) item));
            } else {
                jsonList.put(item);
            }
        }
        return jsonList;
    }

    private static JSONObject createJSONMap(final Map map) {
        final JSONObject jsonMap = new JSONObject();
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof List) {
                value = createJSONList((List) value);
            } else if (value instanceof Map) {
                value = createJSONMap((Map) value);
            } else if (value instanceof Element) {
                value = createJSONElement((Element) value);
            }

            try {
                jsonMap.put(key.toString(), value);
            } catch (JSONException jsone) {
                // this key isn't going to be null or there's a big problem anyway
                // since a null pointer will fire
            }
        }
        return jsonMap;

    }

    public static JSONObject createJSONElement(final Element element) {
        final JSONObject jsonElement = new JSONObject();

        try {
            jsonElement.put(JSONTokens._ID, element.getId());
            jsonElement.put(JSONTokens._PROPERTIES, createJSONMap(createPropertyMap(element)));
            if (element instanceof Vertex) {
                final Vertex vertex = (Vertex) element;
                jsonElement.put(JSONTokens._TYPE, JSONTokens.VERTEX);
                JSONArray jsonArrayOut = new JSONArray();
                for (Edge edge : vertex.getOutEdges()) {
                    jsonArrayOut.put(edge.getId());
                }
                jsonElement.put(JSONTokens.OUT_E, jsonArrayOut);
                final JSONArray jsonArrayIn = new JSONArray();
                for (Edge edge : vertex.getInEdges()) {
                    jsonArrayIn.put(edge.getId());
                }
                jsonElement.put(JSONTokens.IN_E, jsonArrayIn);
            } else if (element instanceof Edge) {
                final Edge edge = (Edge) element;
                jsonElement.put(JSONTokens._TYPE, JSONTokens.EDGE);
                jsonElement.put(JSONTokens.OUT_V, edge.getOutVertex().getId());
                jsonElement.put(JSONTokens.IN_V, edge.getInVertex().getId());

            }
        } catch (JSONException jsone) {
            // the keys are all constants...this really can't happen
        }
        return jsonElement;
    }

    private static Map createPropertyMap(final Element element) {
        final Map map = new HashMap<String, Object>();
        for (String key : element.getPropertyKeys()) {
            map.put(key, element.getProperty(key));
        }
        return map;
    }
}
