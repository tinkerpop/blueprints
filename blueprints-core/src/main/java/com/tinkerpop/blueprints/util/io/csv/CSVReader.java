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

    private final BatchGraph graph;
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

    private final Map<String, String> vertexMappedIdMap = new HashMap<String, String>();
    private int edgeCount = 0;

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
        this(graph, separator, quoteChar, escapeChar, DEFAULT_BUFFER_SIZE);
    }

    /**
     * @param graph
     * @param separator   the delimiter to use for separating entries
     * @param quoteChar   the delimiter to use for quoting entries
     * @param escapeChar  the character to use for escaping characters
     */
    public CSVReader(final Graph graph, char separator, char quoteChar, char escapeChar, int bufferSize) {
        this.graph = BatchGraph.wrap(graph, bufferSize);
        this.separator = separator;
        this.quoteChar = quoteChar;
        this.escapeChar = escapeChar;
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
        readVertices(inputStream);
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
        readEdges(inputStream);
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

    private void readVertices(InputStream inputStream) throws IOException {
        final Reader r = new BufferedReader(new InputStreamReader(inputStream, charset));
        au.com.bytecode.opencsv.CSVReader csvReader = new au.com.bytecode.opencsv.CSVReader(r, separator, quoteChar,
                escapeChar);

        String[] headers = csvReader.readNext();

        while (true) {
            String[] row = csvReader.readNext();
            if (row == null) { break; }

            Map<String, String> map = readPropertyMap(headers, row);
            addVertex(map);
        }

        csvReader.close();
    }

    private void addVertex(final Map<String, String> map) throws IOException {
        final String id = map.remove("id");
        if (id != null) {
            final Vertex vertex = createVertex(map, id);
            addProperties(vertex, map);
        } else {
            throw new IOException("No id found for node");
        }
    }

    private Vertex createVertex(final Map<String, String> map, final String id) {
        String vertexId = id;
        if (vertexIdKey != null) {
            vertexId = map.remove(vertexIdKey);
            if (vertexId == null) vertexId = id;
            vertexMappedIdMap.put(id, vertexId);
        }

        final Vertex createdVertex = graph.addVertex(vertexId);

        return createdVertex;
    }

    private void readEdges(InputStream inputStream) throws IOException {
        final Reader r = new BufferedReader(new InputStreamReader(inputStream, charset));
        au.com.bytecode.opencsv.CSVReader csvReader = new au.com.bytecode.opencsv.CSVReader(r, separator, quoteChar,
                escapeChar);

        String[] headers = csvReader.readNext();

        while (true) {
            String[] row = csvReader.readNext();
            if (row == null) { break; }

            Map<String, String> map = readPropertyMap(headers, row);

            addEdge(map);
        }

        csvReader.close();
    }

    private void addEdge(final Map<String, String> map) throws IOException {
        String source = map.remove(edgeSourceKey);
        if (source == null) {
            throw new IOException("Edge has no source");
        }

        String target = map.remove(edgeTargetKey);
        if (target == null) {
            throw new IOException("Edge has no target");
        }

        if (vertexIdKey != null) {
            source = vertexMappedIdMap.get(source);
            target = vertexMappedIdMap.get(target);
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
            // try standard label key
            label = map.remove("label");
        } else {
            // remove label in case edge label key is not label
            // label is reserved and cannot be added as a property
            // if so this data will be lost
            map.remove("label");
        }

        if (label == null) {
            label = defaultEdgeLabel;
        }

        String edgeId = String.valueOf(edgeCount++);
        if (edgeIdKey != null) {
            String mappedKey = map.remove(edgeIdKey);
            if (mappedKey != null) {
                edgeId = mappedKey;
            }
            // else use edgecount - could fail if mapped ids overlap with edge count
        }

        // remove id as reserved property - can be left is edgeIdKey in not id
        // This data will be lost
        map.remove("id");

        final Edge edge = graph.addEdge(edgeId, outVertex, inVertex, label);

        addProperties(edge, map);
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
