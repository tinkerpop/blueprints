package com.tinkerpop.blueprints.util.io.graphson;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;
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
 * GraphSONReader reads the data from a TinkerPop JSON stream to a graph.
 *
 * @author Stephen Mallette
 */
public class GraphSONReader {
    private final Graph graph;

    /**
     * @param graph the graph to populate with the JSON data
     */
    public GraphSONReader(final Graph graph) {
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
        GraphSONReader.inputGraph(this.graph, jsonInputStream, 1000);
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
        GraphSONReader.inputGraph(this.graph, jsonInputStream, bufferSize);
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
        GraphSONReader.inputGraph(graph, jsonInputStream, 1000);
    }

    /**
     * Input the JSON stream data into the graph.
     * More control over how data is streamed is provided by this method.
     *
     * @param inputGraph      the graph to populate with the JSON data
     * @param jsonInputStream an InputStream of JSON data
     * @param bufferSize      the amount of elements to hold in memory before committing a transactions (only valid for TransactionalGraphs)
     * @throws IOException thrown when the JSON data is not correctly formatted
     */
    public static void inputGraph(final Graph inputGraph, final InputStream jsonInputStream, int bufferSize) throws IOException {
        boolean hasEmbeddedTypes = false;
        final JsonFactory jsonFactory = new MappingJsonFactory();
        final JsonParser jp = jsonFactory.createJsonParser(jsonInputStream);

        // if this is a transactional graph then we're buffering
        final BatchGraph graph = BatchGraph.wrap(inputGraph, bufferSize);
        //final Map<String, Object> vertexIdMap = new HashMap<String, Object>();

        while (jp.nextToken() != JsonToken.END_OBJECT) {
            final String fieldname = jp.getCurrentName() == null ? "" : jp.getCurrentName();
            if (fieldname.equals(GraphSONTokens.EMBEDDED_TYPES)) {
                jp.nextToken();
                hasEmbeddedTypes = jp.getBooleanValue();
            } else if (fieldname.equals(GraphSONTokens.VERTICES)) {
                jp.nextToken();
                while (jp.nextToken() != JsonToken.END_ARRAY) {
                    final JsonNode node = jp.readValueAsTree();
                    final Map<String, Object> props = readProperties(node, true, hasEmbeddedTypes);

                    final String vertexId = node.get(GraphSONTokens._ID).getValueAsText();
                    final Vertex v = graph.addVertex(vertexId);

                    for (Map.Entry<String, Object> entry : props.entrySet()) {
                        v.setProperty(entry.getKey(), entry.getValue());
                    }
                }
            } else if (fieldname.equals(GraphSONTokens.EDGES)) {
                jp.nextToken();
                while (jp.nextToken() != JsonToken.END_ARRAY) {
                    final JsonNode node = jp.readValueAsTree();
                    final Map<String, Object> props = readProperties(node, true, hasEmbeddedTypes);

                    final String edgeId = node.get(GraphSONTokens._ID).getValueAsText();
                    final String label = node.get(GraphSONTokens._LABEL).getValueAsText();

                    final Vertex inV = graph.getVertex(node.get(GraphSONTokens._IN_V).getValueAsText());
                    final Vertex outV = graph.getVertex(node.get(GraphSONTokens._OUT_V).getValueAsText());

                    final Edge e = graph.addEdge(edgeId, outV, inV, label);

                    for (Map.Entry<String, Object> entry : props.entrySet()) {
                        e.setProperty(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        jp.close();

        graph.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
    }

    private static Map<String, Object> readProperties(final JsonNode node, final boolean ignoreReservedKeys, final boolean hasEmbeddedTypes) {
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
}
