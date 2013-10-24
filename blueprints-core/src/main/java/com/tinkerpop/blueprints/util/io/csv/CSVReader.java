package com.tinkerpop.blueprints.util.io.csv;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * CSVReader reads a Graph from a CSV InputStream.
 */
public class CSVReader {

    public static final int DEFAULT_BUFFER_SIZE = 1000;
    public static final String DEFAULT_LABEL = "undefined";

    private final Graph graph;
    private final char separator;
    private final char quoteChar;
    private final char escapeChar;
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private Charset charset = Charset.defaultCharset();
    private String defaultEdgeLabel = DEFAULT_LABEL;
    private String vertexIdKey;
    private String edgeIdKey;
    private String edgeSourceKey;
    private String edgeTargetKey;
    private String edgeLabelKey;

    /**
     * @param graph the Graph to read data into
     */
    public CSVReader(final Graph graph) {
        this(graph,
                au.com.bytecode.opencsv.CSVParser.DEFAULT_SEPARATOR,
                au.com.bytecode.opencsv.CSVParser.DEFAULT_QUOTE_CHARACTER,
                au.com.bytecode.opencsv.CSVParser.DEFAULT_ESCAPE_CHARACTER);
    }


    /**
     * @param graph
     * @param separator   the delimiter to use for separating entries
     */
    public CSVReader(final Graph graph, char separator) {
        this(graph, separator,
                au.com.bytecode.opencsv.CSVParser.DEFAULT_QUOTE_CHARACTER,
                au.com.bytecode.opencsv.CSVParser.DEFAULT_ESCAPE_CHARACTER);
    }

    /**
     * @param graph
     * @param separator   the delimiter to use for separating entries
     * @param quoteChar   the delimiter to use for quoting entries
     */
    public CSVReader(final Graph graph, char separator, char quoteChar) {
        this(graph, separator, quoteChar,
                au.com.bytecode.opencsv.CSVParser.DEFAULT_ESCAPE_CHARACTER);
    }

    /**
     * @param graph
     * @param separator   the delimiter to use for separating entries
     * @param quoteChar   the delimiter to use for quoting entries
     * @param escapeChar  the character to use for escaping characters
     */
    public CSVReader(final Graph graph, char separator, char quoteChar, char escapeChar) {
        this.graph = graph;
        this.separator = separator;
        this.quoteChar = quoteChar;
        this.escapeChar = escapeChar;
    }

    /**
     * @param bufferSize
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * @param charset use this character set for reading the vertex or edge streams.
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * @param defaultEdgeLabel Default edge label to be used if the CSV edge does not define a label
     */
    public void setDefaultLabel(String defaultEdgeLabel) {
        this.defaultEdgeLabel = defaultEdgeLabel;
    }

    /**
     * @param vertexIdKey CSV property to use as id for vertices
     */
    public void setVertexIdKey(String vertexIdKey) {
        this.vertexIdKey = vertexIdKey;
    }

    /**
     * @param edgeIdKey CSV property to use as id for edges
     */
    public void setEdgeIdKey(String edgeIdKey) {
        this.edgeIdKey = edgeIdKey;
    }

    /**
     * @param edgeSourceKey CSV property to use as the edge source
     */
    public void setEdgeSourceKey(String edgeSourceKey) {
        this.edgeSourceKey = edgeSourceKey;
    }

    /**
     * @param edgeTargetKey CSV property to use as the edge target
     */
    public void setEdgeTargetKey(String edgeTargetKey) {
        this.edgeTargetKey = edgeTargetKey;
    }

    /**
     * @param edgeLabelKey CSV property to use as the edge label
     */
    public void setEdgeLabelKey(String edgeLabelKey) {
        this.edgeLabelKey = edgeLabelKey;
    }

    /**
     * Load the CSV vertex stream into the Graph.
     *
     * @param inputStream
     * @throws IOException
     */
    public void inputVertices(InputStream inputStream) throws IOException {
        final BatchGraph graph = BatchGraph.wrap(this.graph, this.bufferSize);
        graph.setLoadingFromScratch(false);
        final Reader r = new BufferedReader(new InputStreamReader(inputStream, charset));
        au.com.bytecode.opencsv.CSVReader csvReader = new au.com.bytecode.opencsv.CSVReader(r, separator, quoteChar,
                escapeChar);

        String[] headers = csvReader.readNext();

        while (true) {
            String[] row = csvReader.readNext();
            if (row == null) { break; }

            Map<String, String> map = readPropertyMap(headers, row);

            final String vertexId = map.remove(vertexIdKey);
            if (vertexId == null) {
                throw new IOException("No id found for vertex");
            } else {
                final Vertex vertex = graph.addVertex(vertexId);
                addProperties(vertex, map);
            }
        }

        csvReader.close();
        graph.commit();
    }

    /**
     * Load the CSV vertex file into the Graph.
     *
     * @param filename
     * @throws IOException
     */
    public void inputVertices(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        inputVertices(fis);
        fis.close();
    }

    /**
     * Load the CSV vertex file into the Graph.
     *
     * @param inputGraph
     * @param filename
     * @param vertexIdKey
     * @throws IOException
     */
    public static void inputVertices(final Graph inputGraph, final String filename,
                                     final String vertexIdKey) throws IOException {
        CSVReader reader = new CSVReader(inputGraph);
        reader.setVertexIdKey(vertexIdKey);
        reader.inputVertices(filename);
    }

    /**
     * Load the CSV vertex stream into the Graph.
     *
     * @param inputGraph
     * @param inputStream
     * @param vertexIdKey
     * @throws IOException
     */
    public static void inputVertices(final Graph inputGraph, final InputStream inputStream,
                                     final String vertexIdKey) throws IOException {
        CSVReader reader = new CSVReader(inputGraph);
        reader.setVertexIdKey(vertexIdKey);
        reader.inputVertices(inputStream);
    }

    /**
     * Load the CSV edge stream into the Graph.
     *
     * @param inputStream
     * @throws IOException
     */
    public void inputEdges(InputStream inputStream) throws IOException {
        final BatchGraph graph = BatchGraph.wrap(this.graph, bufferSize);
        graph.setLoadingFromScratch(false);
        final Reader r = new BufferedReader(new InputStreamReader(inputStream, charset));
        au.com.bytecode.opencsv.CSVReader csvReader = new au.com.bytecode.opencsv.CSVReader(r, separator, quoteChar,
                escapeChar);

        String[] headers = csvReader.readNext();

        while (true) {
            String[] row = csvReader.readNext();
            if (row == null) { break; }

            Map<String, String> map = readPropertyMap(headers, row);

            String edgeId = map.remove(edgeIdKey);
            if (edgeId == null) {
                throw new IOException("No id found for edge");
            }

            String source = map.remove(edgeSourceKey);
            if (source == null) {
                throw new IOException("Edge has no source");
            }

            String target = map.remove(edgeTargetKey);
            if (target == null) {
                throw new IOException("Edge has no target");
            }

            final Vertex outVertex = graph.getVertex(source);
            if (outVertex == null) {
                throw new IOException("Edge source " + source + " not found");
            }

            final Vertex inVertex = graph.getVertex(target);
            if (inVertex == null) {
                throw new IOException("Edge target " + target + " not found");
            }

            String label = map.remove(edgeLabelKey);
            if (label == null) {
                label = defaultEdgeLabel;
            }

            final Edge edge = graph.addEdge(edgeId, outVertex, inVertex, label);

            addProperties(edge, map);
        }

        csvReader.close();
        graph.commit();
    }

    /**
     * Load the CSV edge file into the Graph.
     *
     * @param filename
     * @throws IOException
     */
    public void inputEdges(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        inputEdges(fis);
        fis.close();
    }


    /**
     * Load the CSV edge stream into the Graph.
     *
     * @param inputGraph
     * @param filename
     * @param edgeIdKey
     * @param edgeSourceKey
     * @param edgeTargetKey
     * @param edgeLabelKey
     * @throws IOException
     */
    public static void inputEdges(final Graph inputGraph, final String filename, final String edgeIdKey,
                                  final String edgeSourceKey, final String edgeTargetKey,
                                  final String edgeLabelKey) throws IOException {
        CSVReader reader = new CSVReader(inputGraph);
        reader.setEdgeIdKey(edgeIdKey);
        reader.setEdgeSourceKey(edgeSourceKey);
        reader.setEdgeTargetKey(edgeTargetKey);
        reader.setEdgeLabelKey(edgeLabelKey);
        reader.inputEdges(filename);
    }

    /**
     * Load the CSV edge stream into the Graph.
     *
     * @param inputGraph
     * @param inputStream
     * @param edgeIdKey
     * @param edgeSourceKey
     * @param edgeTargetKey
     * @param edgeLabelKey
     * @throws IOException
     */
    public static void inputEdges(final Graph inputGraph, final InputStream inputStream, final String edgeIdKey,
                                  final String edgeSourceKey, final String edgeTargetKey,
                                  final String edgeLabelKey) throws IOException {
        CSVReader reader = new CSVReader(inputGraph);
        reader.setEdgeIdKey(edgeIdKey);
        reader.setEdgeSourceKey(edgeSourceKey);
        reader.setEdgeTargetKey(edgeTargetKey);
        reader.setEdgeLabelKey(edgeLabelKey);
        reader.inputEdges(inputStream);
    }

    private Map<String, String> readPropertyMap(String[] headers, String[] row) {
        Map<String, String> map = new HashMap<String, String>();

        if (row != null) {
            // Exit early if our row does not have as many items in it as our header.
            for(int i = 0; i < row.length && i < headers.length; ++i) {
                String key = headers[i];
                String property = row[i];

                if (property != null && !property.equals("")) {
                    map.put(key, property);
                }
            }
        }

        return map;
    }

    private void addProperties(final Element element, final Map<String, String> map) {
        for(Map.Entry<String, String> entry: map.entrySet()) {
            element.setProperty(entry.getKey(), entry.getValue());
        }
    }
}
