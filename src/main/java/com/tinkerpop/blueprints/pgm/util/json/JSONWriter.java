package com.tinkerpop.blueprints.pgm.util.json;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
                jsonList.add(createJSONElement((Element) item));
            } else if (item instanceof List) {
                jsonList.add(createJSONList((List) item));
            } else if (item instanceof Map) {
                jsonList.add(createJSONMap((Map) item));
            } else {
                jsonList.add(item);
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

            if (key instanceof List) {
                key = createJSONList((List) key);
            } else if (key instanceof Map) {
                key = createJSONMap((Map) key);
            } else if (key instanceof Element) {
                key = createJSONElement((Element) key);
            }
            jsonMap.put(key, value);
        }
        return jsonMap;

    }

    public static JSONObject createJSONElement(final Element element) {
        final JSONObject jsonElement = new JSONObject();
        jsonElement.put(JSONTokens._ID, element.getId());
        jsonElement.put(JSONTokens._PROPERTIES, createJSONMap(createPropertyMap(element)));
        if (element instanceof Vertex) {
            final Vertex vertex = (Vertex) element;
            jsonElement.put(JSONTokens._TYPE, JSONTokens.VERTEX);
            JSONArray jsonArrayOut = new JSONArray();
            for (Edge edge : vertex.getOutEdges()) {
                jsonArrayOut.add(edge.getId());
            }
            jsonElement.put(JSONTokens.OUT_E, jsonArrayOut);
            final JSONArray jsonArrayIn = new JSONArray();
            for (Edge edge : vertex.getInEdges()) {
                jsonArrayIn.add(edge.getId());
            }
            jsonElement.put(JSONTokens.IN_E, jsonArrayIn);
        } else if (element instanceof Edge) {
            final Edge edge = (Edge) element;
            jsonElement.put(JSONTokens._TYPE, JSONTokens.EDGE);
            jsonElement.put(JSONTokens.OUT_V, edge.getOutVertex().getId());
            jsonElement.put(JSONTokens.IN_V, edge.getInVertex().getId());

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
