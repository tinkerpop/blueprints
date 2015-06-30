package com.tinkerpop.blueprints.util.io.graphson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * GraphSONReader reads the data from a TinkerPop JSON stream to a graph.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GraphSONReader {
    private static final JsonFactory jsonFactory = new MappingJsonFactory();
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
     * @param filename name of a file of JSON data
     * @throws IOException thrown when the JSON data is not correctly formatted
     */
    public void inputGraph(final String filename) throws IOException {
        GraphSONReader.inputGraph(this.graph, filename, 1000);
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
     * @param filename   name of a file of JSON data
     * @param bufferSize the amount of elements to hold in memory before committing a transactions (only valid for TransactionalGraphs)
     * @throws IOException thrown when the JSON data is not correctly formatted
     */
    public void inputGraph(final String filename, int bufferSize) throws IOException {
        GraphSONReader.inputGraph(this.graph, filename, bufferSize);
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
        inputGraph(graph, jsonInputStream, 1000);
    }

    /**
     * Input the JSON stream data into the graph.
     * In practice, usually the provided graph is empty.
     *
     * @param graph    the graph to populate with the JSON data
     * @param filename name of a file of JSON data
     * @throws IOException thrown when the JSON data is not correctly formatted
     */
    public static void inputGraph(final Graph graph, final String filename) throws IOException {
        inputGraph(graph, filename, 1000);
    }

    public static void inputGraph(final Graph inputGraph, final InputStream jsonInputStream, int bufferSize) throws IOException {
        inputGraph(inputGraph, jsonInputStream, bufferSize, null, null);
    }

    public static void inputGraph(final Graph inputGraph, final String filename, int bufferSize) throws IOException {
        inputGraph(inputGraph, filename, bufferSize, null, null);
    }

    /**
     * Input the JSON stream data into the graph.
     * More control over how data is streamed is provided by this method.
     *
     * @param inputGraph the graph to populate with the JSON data
     * @param filename   name of a file of JSON data
     * @param bufferSize the amount of elements to hold in memory before committing a transactions (only valid for TransactionalGraphs)
     * @throws IOException thrown when the JSON data is not correctly formatted
     */
    public static void inputGraph(final Graph inputGraph, final String filename, int bufferSize,
                                  final Set<String> edgePropertyKeys, final Set<String> vertexPropertyKeys) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        GraphSONReader.inputGraph(inputGraph, fis, bufferSize, edgePropertyKeys, vertexPropertyKeys);
        fis.close();
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
    public static void inputGraph(final Graph inputGraph, final InputStream jsonInputStream, int bufferSize,
                                  final Set<String> edgePropertyKeys, final Set<String> vertexPropertyKeys) throws IOException {

        if (jsonInputStream == null) {
            throw new IllegalArgumentException("InputStream must not be null");
        }

        final JsonParser jp = jsonFactory.createJsonParser(jsonInputStream);

        // if this is a transactional graph then we're buffering
        final BatchGraph graph = BatchGraph.wrap(inputGraph, bufferSize);

        final ElementFactory elementFactory = new GraphElementFactory(graph);
        GraphSONUtility graphson = new GraphSONUtility(GraphSONMode.NORMAL, elementFactory,
                vertexPropertyKeys, edgePropertyKeys);

        while (jp.nextToken() != JsonToken.END_OBJECT) {
            final String fieldname = jp.getCurrentName() == null ? "" : jp.getCurrentName();
            if (fieldname.equals(GraphSONTokens.MODE)) {
                jp.nextToken();
                final GraphSONMode mode = GraphSONMode.valueOf(jp.getText());
                graphson = new GraphSONUtility(mode, elementFactory, vertexPropertyKeys, edgePropertyKeys);
            } else if (fieldname.equals(GraphSONTokens.VERTICES)) {
                jp.nextToken();
                while (jp.nextToken() != JsonToken.END_ARRAY) {
                    final JsonNode node = jp.readValueAsTree();
                    graphson.vertexFromJson(node);
                }
            } else if (fieldname.equals(GraphSONTokens.EDGES)) {
                jp.nextToken();
                while (jp.nextToken() != JsonToken.END_ARRAY) {
                    final JsonNode node = jp.readValueAsTree();
                    final Vertex inV = graph.getVertex(GraphSONUtility.getTypedValueFromJsonNode(node.get(GraphSONTokens._IN_V)));
                    final Vertex outV = graph.getVertex(GraphSONUtility.getTypedValueFromJsonNode(node.get(GraphSONTokens._OUT_V)));
                    graphson.edgeFromJson(node, outV, inV);
                }
            }
        }

        jp.close();

        graph.commit();
        ;
    }


}
