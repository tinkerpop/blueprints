package com.tinkerpop.blueprints.util.io.graphson;


import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.MappingJsonFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * GraphSONWriter writes a Graph to a TinkerPop JSON OutputStream.
 *
 * @author Stephen Mallette
 */
public class GraphSONWriter {
    private static final JsonFactory jsonFactory = new MappingJsonFactory();
    private final Graph graph;

    /**
     * @param graph the Graph to pull the data from
     */
    public GraphSONWriter(final Graph graph) {
        this.graph = graph;
    }

    /**
     * Write the data in a Graph to a JSON OutputStream.
     *
     * @param jsonOutputStream   the JSON OutputStream to write the Graph data to
     * @param edgePropertyKeys   the keys of the edge elements to write to JSON
     * @param vertexPropertyKeys the keys of the vertex elements to write to JSON
     * @param mode               determines the format of the GraphSON
     * @throws IOException thrown if there is an error generating the JSON data
     */
    public void outputGraph(final OutputStream jsonOutputStream, final List<String> edgePropertyKeys,
                            final List<String> vertexPropertyKeys, final GraphSONMode mode) throws IOException {
        final JsonGenerator jg = jsonFactory.createJsonGenerator(jsonOutputStream);

        jg.writeStartObject();

        jg.writeStringField(GraphSONTokens.MODE, mode.toString());

        jg.writeArrayFieldStart(GraphSONTokens.VERTICES);
        for (Vertex v : this.graph.getVertices()) {
            jg.writeTree(GraphSONUtility.objectNodeFromElement(v, vertexPropertyKeys, mode));
        }

        jg.writeEndArray();

        jg.writeArrayFieldStart(GraphSONTokens.EDGES);
        for (Edge e : this.graph.getEdges()) {
            jg.writeTree(GraphSONUtility.objectNodeFromElement(e, edgePropertyKeys, mode));
        }
        jg.writeEndArray();

        jg.writeEndObject();

        jg.flush();
        jg.close();
    }

    /**
     * Write the data in a Graph to a JSON OutputStream. All keys are written to JSON. Utilizing
     * GraphSONMode.NORMAL.
     *
     * @param graph            the graph to serialize to JSON
     * @param jsonOutputStream the JSON OutputStream to write the Graph data to
     * @throws IOException thrown if there is an error generating the JSON data
     */
    public static void outputGraph(final Graph graph, final OutputStream jsonOutputStream) throws IOException {
        final GraphSONWriter writer = new GraphSONWriter(graph);
        writer.outputGraph(jsonOutputStream, null, null, GraphSONMode.NORMAL);
    }

    /**
     * Write the data in a Graph to a JSON OutputStream. All keys are written to JSON.
     *
     * @param graph            the graph to serialize to JSON
     * @param jsonOutputStream the JSON OutputStream to write the Graph data to
     * @param mode             determines the format of the GraphSON
     * @throws IOException thrown if there is an error generating the JSON data
     */
    public static void outputGraph(final Graph graph, final OutputStream jsonOutputStream,
                                   final GraphSONMode mode) throws IOException {
        final GraphSONWriter writer = new GraphSONWriter(graph);
        writer.outputGraph(jsonOutputStream, null, null, mode);
    }

    /**
     * Write the data in a Graph to a JSON OutputStream.
     *
     * @param graph              the graph to serialize to JSON
     * @param jsonOutputStream   the JSON OutputStream to write the Graph data to
     * @param edgePropertyKeys   the keys of the edge elements to write to JSON
     * @param vertexPropertyKeys the keys of the vertex elements to write to JSON
     * @param mode               determines the format of the GraphSON
     * @throws IOException thrown if there is an error generating the JSON data
     */
    public static void outputGraph(final Graph graph, final OutputStream jsonOutputStream,
                                   final List<String> edgePropertyKeys, final List<String> vertexPropertyKeys,
                                   final GraphSONMode mode) throws IOException {
        final GraphSONWriter writer = new GraphSONWriter(graph);
        writer.outputGraph(jsonOutputStream, edgePropertyKeys, vertexPropertyKeys, mode);
    }

}
