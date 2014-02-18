package com.tinkerpop.blueprints.util.io.graphson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.io.LexicographicalElementComparator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
     * @param filename           the JSON file to write the Graph data to
     * @param vertexPropertyKeys the keys of the vertex elements to write to JSON
     * @param edgePropertyKeys   the keys of the edge elements to write to JSON
     * @param mode               determines the format of the GraphSON
     * @throws IOException thrown if there is an error generating the JSON data
     */
    public void outputGraph(final String filename, final Set<String> vertexPropertyKeys,
                            final Set<String> edgePropertyKeys, final GraphSONMode mode) throws IOException {
        final FileOutputStream fos = new FileOutputStream(filename);
        outputGraph(fos, vertexPropertyKeys, edgePropertyKeys, mode);
        fos.close();
    }

    /**
     * Write the data in a Graph to a JSON OutputStream.
     *
     * @param jsonOutputStream   the JSON OutputStream to write the Graph data to
     * @param vertexPropertyKeys the keys of the vertex elements to write to JSON
     * @param edgePropertyKeys   the keys of the edge elements to write to JSON
     * @param mode               determines the format of the GraphSON
     * @throws IOException thrown if there is an error generating the JSON data
     */
    public void outputGraph(final OutputStream jsonOutputStream, final Set<String> vertexPropertyKeys,
                            final Set<String> edgePropertyKeys, final GraphSONMode mode) throws IOException {
        outputGraph(jsonOutputStream, vertexPropertyKeys, edgePropertyKeys, mode, false);
    }


    public void outputGraph(final OutputStream jsonOutputStream, final Set<String> vertexPropertyKeys,
                            final Set<String> edgePropertyKeys, final GraphSONMode mode, final boolean normalize) throws IOException {
        final JsonGenerator jg = jsonFactory.createGenerator(jsonOutputStream);

        // don't let the JsonGenerator close the underlying stream...leave that to the client passing in the stream
        jg.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

        final GraphSONUtility graphson = new GraphSONUtility(mode, null,
                ElementPropertyConfig.includeProperties(vertexPropertyKeys, edgePropertyKeys, normalize));

        jg.writeStartObject();

        jg.writeStringField(GraphSONTokens.MODE, mode.toString());

        jg.writeArrayFieldStart(GraphSONTokens.VERTICES);

        final Iterable<Vertex> vertices = vertices(normalize);
        for (Vertex v : vertices) {
            jg.writeTree(graphson.objectNodeFromElement(v));
        }

        jg.writeEndArray();

        jg.writeArrayFieldStart(GraphSONTokens.EDGES);

        final Iterable<Edge> edges = edges(normalize);
        for (Edge e : edges) {
            jg.writeTree(graphson.objectNodeFromElement(e));
        }
        jg.writeEndArray();

        jg.writeEndObject();

        jg.flush();
        jg.close();
    }

    private Iterable<Vertex> vertices(boolean normalize) {
        Iterable<Vertex> vertices;
        if (normalize) {
            vertices = new ArrayList<Vertex>();
            for (Vertex v : graph.getVertices()) {
                ((Collection<Vertex>) vertices).add(v);
            }
            Collections.sort((List<Vertex>) vertices, new LexicographicalElementComparator());
        } else {
            vertices = graph.getVertices();
        }
        return vertices;
    }

    private Iterable<Edge> edges(boolean normalize) {
        Iterable<Edge> edges;
        if (normalize) {
            edges = new ArrayList<Edge>();
            for (Edge v : graph.getEdges()) {
                ((Collection<Edge>) edges).add(v);
            }
            Collections.sort((List<Edge>) edges, new LexicographicalElementComparator());
        } else {
            edges = graph.getEdges();
        }
        return edges;
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
     * Write the data in a Graph to a JSON OutputStream. All keys are written to JSON. Utilizing
     * GraphSONMode.NORMAL.
     *
     * @param graph    the graph to serialize to JSON
     * @param filename the JSON file to write the Graph data to
     * @throws IOException thrown if there is an error generating the JSON data
     */
    public static void outputGraph(final Graph graph, final String filename) throws IOException {
        final GraphSONWriter writer = new GraphSONWriter(graph);
        writer.outputGraph(filename, null, null, GraphSONMode.NORMAL);
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
     * Write the data in a Graph to a JSON OutputStream. All keys are written to JSON.
     *
     * @param graph    the graph to serialize to JSON
     * @param filename the JSON file to write the Graph data to
     * @param mode     determines the format of the GraphSON
     * @throws IOException thrown if there is an error generating the JSON data
     */
    public static void outputGraph(final Graph graph, final String filename,
                                   final GraphSONMode mode) throws IOException {
        final GraphSONWriter writer = new GraphSONWriter(graph);
        writer.outputGraph(filename, null, null, mode);
    }

    /**
     * Write the data in a Graph to a JSON OutputStream.
     *
     * @param graph              the graph to serialize to JSON
     * @param jsonOutputStream   the JSON OutputStream to write the Graph data to
     * @param vertexPropertyKeys the keys of the vertex elements to write to JSON
     * @param edgePropertyKeys   the keys of the edge elements to write to JSON
     * @param mode               determines the format of the GraphSON
     * @throws IOException thrown if there is an error generating the JSON data
     */
    public static void outputGraph(final Graph graph, final OutputStream jsonOutputStream,
                                   final Set<String> vertexPropertyKeys, final Set<String> edgePropertyKeys,
                                   final GraphSONMode mode) throws IOException {
        final GraphSONWriter writer = new GraphSONWriter(graph);
        writer.outputGraph(jsonOutputStream, vertexPropertyKeys, edgePropertyKeys, mode);
    }

    /**
     * Write the data in a Graph to a JSON OutputStream.
     *
     * @param graph              the graph to serialize to JSON
     * @param filename           the JSON file to write the Graph data to
     * @param vertexPropertyKeys the keys of the vertex elements to write to JSON
     * @param edgePropertyKeys   the keys of the edge elements to write to JSON
     * @param mode               determines the format of the GraphSON
     * @throws IOException thrown if there is an error generating the JSON data
     */
    public static void outputGraph(final Graph graph, final String filename,
                                   final Set<String> vertexPropertyKeys, final Set<String> edgePropertyKeys,
                                   final GraphSONMode mode) throws IOException {
        final GraphSONWriter writer = new GraphSONWriter(graph);
        writer.outputGraph(filename, vertexPropertyKeys, edgePropertyKeys, mode);
    }

}
