package com.tinkerpop.blueprints.pgm.util.json;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * GraphJSONReader reads the data from a TinkerPop JSON stream to a graph.
 */
public class GraphJSONReader {
    private final Graph graph;

    /**
     * @param graph the graph to populate with the JSON data
     */
    public GraphJSONReader(final Graph graph) {
        this.graph = graph;
    }

    /**
     * Input the JSON stream data into the graph.
     * In practice, usually the provided graph is empty.
     *
     * @param jsonInputStream an InputStream of JSON data
     * @throws IOException thrown when the JSON data is not correctly formatted
     */
    public void inputGraph(final InputStream jsonInputStream) throws IOException {
        GraphJSONReader.inputGraph(this.graph, jsonInputStream, 1000);
    }

    /**
     * Input the JSON stream data into the graph.
     * In practice, usually the provided graph is empty.
     *
     * @param jsonInputStream an InputStream of JSON data
     * @param bufferSize      the amount of elements to hold in memory before committing a transactions (only valid for TransactionalGraphs)
     * @throws IOException thrown when the JSON data is not correctly formatted
     */
    public void inputGraph(final InputStream jsonInputStream, int bufferSize) throws IOException {
        GraphJSONReader.inputGraph(this.graph, jsonInputStream, bufferSize);
    }

    /**
     * Input the JSON stream data into the graph.
     * In practice, usually the provided graph is empty.
     *
     * @param graph           the graph to populate with the JSON data
     * @param jsonInputStream an InputStream of JSON data
     * @throws IOException thrown when the JSON data is not correctly formatted
     */
    public static void inputGraph(final Graph graph, final InputStream jsonInputStream) throws IOException {
        GraphJSONReader.inputGraph(graph, jsonInputStream, 1000);
    }

    /**
     * Input the JSON stream data into the graph.
     * More control over how data is streamed is provided by this method.
     *
     * @param graph           the graph to populate with the JSON data
     * @param jsonInputStream an InputStream of JSON data
     * @param bufferSize      the amount of elements to hold in memory before committing a transactions (only valid for TransactionalGraphs)
     * @throws IOException thrown when the JSON data is not correctly formatted
     */
    public static void inputGraph(final Graph graph, final InputStream jsonInputStream, int bufferSize) throws IOException {
        boolean hasEmbeddedTypes = false;
        JsonFactory jsonFactory = new MappingJsonFactory();
        JsonParser jp = jsonFactory.createJsonParser(jsonInputStream);

        Map<String, Object> vertexIdMap = new HashMap<String, Object>();

        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jp.getCurrentName() == null ? "" : jp.getCurrentName();
            if (fieldname.equals(JSONTokens.EMBEDDED_TYPES)) {
                jp.nextToken();
                hasEmbeddedTypes = jp.getBooleanValue();
            } else if (fieldname.equals(JSONTokens.VERTICES)) {
                jp.nextToken();
                while (jp.nextToken() != JsonToken.END_ARRAY) {
                    JsonNode node = jp.readValueAsTree();
                    Map<String, Object> props = readProperties(node, true, hasEmbeddedTypes);

                    String vertexId = node.get(JSONTokens._ID).getValueAsText();
                    Vertex v = graph.addVertex(vertexId);
                    vertexIdMap.put(vertexId, v.getId());

                    for (Map.Entry<String, Object> entry : props.entrySet()) {
                        v.setProperty(entry.getKey(), entry.getValue());
                    }
                }
            } else if (fieldname.equals(JSONTokens.EDGES)) {
                jp.nextToken();
                while (jp.nextToken() != JsonToken.END_ARRAY) {
                    JsonNode node = jp.readValueAsTree();
                    Map<String, Object> props = readProperties(node, true, hasEmbeddedTypes);

                    String edgeId = node.get(JSONTokens._ID).getValueAsText();
                    Object inVertexKey = vertexIdMap.get(node.get(JSONTokens._IN_V).getValueAsText());
                    Object outVertexKey = vertexIdMap.get(node.get(JSONTokens._OUT_V).getValueAsText());
                    String label = node.get(JSONTokens._LABEL).getValueAsText();

                    Vertex inV = graph.getVertex(inVertexKey);
                    Vertex outV = graph.getVertex(outVertexKey);

                    Edge e = graph.addEdge(edgeId, outV, inV, label);

                    for (Map.Entry<String, Object> entry : props.entrySet()) {
                        e.setProperty(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        jp.close();
    }

    private static Map<String, Object> readProperties(final JsonNode node, final boolean ignoreReservedKeys, final boolean hasEmbeddedTypes) {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<Map.Entry<String, JsonNode>> iterator = node.getFields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();

            if (!ignoreReservedKeys || (ignoreReservedKeys && !isReservedKey(entry.getKey()))) {
                map.put(entry.getKey(), readProperty(entry.getValue(), hasEmbeddedTypes));
            }
        }

        return map;
    }

    private static boolean isReservedKey(final String key) {
        return key.equals(JSONTokens._ID) || key.equals(JSONTokens._TYPE) || key.equals(JSONTokens._LABEL)
                || key.equals(JSONTokens._OUT_V) || key.equals(JSONTokens._IN_V);
    }

    private static Object readProperty(final JsonNode node, final boolean hasEmbeddedTypes) {
        Object propertyValue;

        if (hasEmbeddedTypes) {
            if (node.get(JSONTokens.TYPE).getValueAsText().equals(JSONTokens.TYPE_UNKNOWN)) {
                propertyValue = null;
            } else if (node.get(JSONTokens.TYPE).getValueAsText().equals(JSONTokens.TYPE_BOOLEAN)) {
                propertyValue = node.get(JSONTokens.VALUE).getBooleanValue();
            } else if (node.get(JSONTokens.TYPE).getValueAsText().equals(JSONTokens.TYPE_FLOAT)) {
                propertyValue = Float.parseFloat(node.get(JSONTokens.VALUE).getValueAsText());
            } else if (node.get(JSONTokens.TYPE).getValueAsText().equals(JSONTokens.TYPE_DOUBLE)) {
                propertyValue = node.get(JSONTokens.VALUE).getDoubleValue();
            } else if (node.get(JSONTokens.TYPE).getValueAsText().equals(JSONTokens.TYPE_INTEGER)) {
                propertyValue = node.get(JSONTokens.VALUE).getIntValue();
            } else if (node.get(JSONTokens.TYPE).getValueAsText().equals(JSONTokens.TYPE_LONG)) {
                propertyValue = node.get(JSONTokens.VALUE).getLongValue();
            } else if (node.get(JSONTokens.TYPE).getValueAsText().equals(JSONTokens.TYPE_STRING)) {
                propertyValue = node.get(JSONTokens.VALUE).getTextValue();
            } else if (node.get(JSONTokens.TYPE).getValueAsText().equals(JSONTokens.TYPE_LIST)) {
                propertyValue = readProperties(node.get(JSONTokens.VALUE).getElements(), hasEmbeddedTypes);
            } else if (node.get(JSONTokens.TYPE).getValueAsText().equals(JSONTokens.TYPE_MAP)) {
                propertyValue = readProperties(node.get(JSONTokens.VALUE), false, hasEmbeddedTypes);
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
        List array = new ArrayList();

        while (listOfNodes.hasNext()) {
            array.add(readProperty(listOfNodes.next(), hasEmbeddedTypes));
        }

        return array;
    }
}
