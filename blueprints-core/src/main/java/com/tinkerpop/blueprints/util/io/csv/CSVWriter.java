package com.tinkerpop.blueprints.util.io.csv;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.io.LexicographicalElementComparator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * CSVWriter writes a Graph to a TinkerPop CSV OutputStream.
 */
public class CSVWriter {

    public static final String DEFAULT_LABEL = "undefined";

    private final Graph graph;
    private final char separator;
    private final char quoteChar;
    private final char escapeChar;
    private final String lineEnd;

    private boolean normalize = false;
    private String vertexIdKey;
    private String defaultEdgeLabel;
    private String edgeIdKey;
    private String edgeSourceKey;
    private String edgeTargetKey;
    private String edgeLabelKey;

    /**
     * @param graph the Graph to pull the data from
     */
    public CSVWriter(final Graph graph) {
        this(graph,
                au.com.bytecode.opencsv.CSVWriter.DEFAULT_SEPARATOR,
                au.com.bytecode.opencsv.CSVWriter.DEFAULT_QUOTE_CHARACTER,
                au.com.bytecode.opencsv.CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                au.com.bytecode.opencsv.CSVWriter.DEFAULT_LINE_END);
    }

    /**
     * @param graph
     * @param separator   the delimiter to use for separating entries
     */
    public CSVWriter(final Graph graph, char separator) {
        this(graph, separator,
                au.com.bytecode.opencsv.CSVWriter.DEFAULT_QUOTE_CHARACTER,
                au.com.bytecode.opencsv.CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                au.com.bytecode.opencsv.CSVWriter.DEFAULT_LINE_END);
    }

    /**
     * @param graph
     * @param separator   the delimiter to use for separating entries
     * @param quoteChar   the delimiter to use for quoting entries
     */
    public CSVWriter(final Graph graph, char separator, char quoteChar) {
        this(graph, separator, quoteChar,
                au.com.bytecode.opencsv.CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                au.com.bytecode.opencsv.CSVWriter.DEFAULT_LINE_END);
    }

    /**
     * @param graph
     * @param separator   the delimiter to use for separating entries
     * @param quoteChar   the delimiter to use for quoting entries
     * @param escapeChar  the character to use for escaping characters
     */
    public CSVWriter(final Graph graph, char separator, char quoteChar, char escapeChar) {
        this(graph, separator, quoteChar, escapeChar, au.com.bytecode.opencsv.CSVWriter.DEFAULT_LINE_END);
    }

    /**
     * @param graph
     * @param separator   the delimiter to use for separating entries
     * @param quoteChar   the delimiter to use for quoting entries
     * @param escapeChar  the character to use for escaping characters
     * @param lineEnd     terminate each line with this string
     */
    public CSVWriter(final Graph graph, char separator, char quoteChar, char escapeChar, String lineEnd) {
        this.graph = graph;
        this.separator = separator;
        this.quoteChar = quoteChar;
        this.escapeChar = escapeChar;
        this.lineEnd = lineEnd;
    }

    /**
     * @param normalize whether to normalize the output. Normalized output is deterministic with respect to the order of
     *                  elements and properties in the resulting CSV document, and is compatible with line diff-based tools
     *                  such as Git. Note: normalized output is memory-intensive and is not appropriate for very large graphs.
     */
    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
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
     * Write the vertices in a Graph to a CSV file
     *
     * @param filename
     * @param propertyKeys
     * @throws java.io.IOException
     */
    public void outputVertices(final String filename, final Set<String> propertyKeys) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        outputVertices(fos, propertyKeys);
        fos.close();
    }

    public void outputVertices(final OutputStream csvOutputStream) throws IOException {
        outputVertices(csvOutputStream, null);
    }

    /**
     * Write the vertices in a Graph to a CSV OutputStream
     *
     * @param csvOutputStream
     * @param propertyKeys
     * @throws IOException
     */
    public void outputVertices(final OutputStream csvOutputStream, Set<String> propertyKeys) throws IOException {
        Writer out = new OutputStreamWriter(csvOutputStream);
        au.com.bytecode.opencsv.CSVWriter csvWriter = new au.com.bytecode.opencsv.CSVWriter(out, separator, quoteChar,
            escapeChar, lineEnd);

        List<Vertex> vertices = new ArrayList<Vertex>();

        for (Vertex vertex: graph.getVertices()) {
            vertices.add(vertex);
        }

        if (normalize) {
            Collections.sort(vertices, new LexicographicalElementComparator());
        }

        if (propertyKeys == null) {
            propertyKeys = new TreeSet<String>();

            for (Vertex vertex: vertices) {
                propertyKeys.addAll(vertex.getPropertyKeys());
            }
        }

        List<String> headers = new ArrayList<String>();
        headers.add(vertexIdKey);

        for (String key: propertyKeys) {
            headers.add(key);
        }

        csvWriter.writeNext(headers.toArray(new String[headers.size()]));

        for (Vertex vertex: vertices) {
            List<String> row = new ArrayList<String>();

            row.add(vertex.getId().toString());

            for (String key: propertyKeys) {
                Object property = vertex.getProperty(key);

                if (property == null) {
                    row.add(null);
                } else {
                    row.add(property.toString());
                }
            }

            csvWriter.writeNext(row.toArray(new String[row.size()]));
        }

        csvWriter.close();
    }

    /**
     * Write the data in a Graph to a CSV OutputStream
     *
     * @param graph
     * @param csvOutputStream
     * @throws IOException
     */
    public static void outputVertices(final Graph graph, final OutputStream csvOutputStream,
                                      final String vertexIdKey) throws IOException {
        outputVertices(graph, csvOutputStream, vertexIdKey, null);
    }

    /**
     * Write the data in a Graph to a CSV OutputStream
     *
     * @param graph
     * @param filename
     * @throws IOException
     */
    public static void outputVertices(final Graph graph, final String filename,
                                      final String vertexIdKey) throws IOException {
        outputVertices(graph, filename, vertexIdKey, null);
    }

    /**
     * Write the data in a Graph to a CSV OutputStream
     *
     * @param graph
     * @param csvOutputStream
     * @param propertyKeys
     * @throws IOException
     */
    public static void outputVertices(final Graph graph, final OutputStream csvOutputStream,
                                      final String vertexIdKey, final Set<String> propertyKeys) throws IOException {
        final CSVWriter writer = new CSVWriter(graph);
        writer.setVertexIdKey(vertexIdKey);
        writer.outputVertices(csvOutputStream, propertyKeys);
    }

    /**
     * Write the data in a Graph to a CSV OutputStream
     *
     * @param graph
     * @param filename
     * @param propertyKeys
     * @throws IOException
     */
    public static void outputVertices(final Graph graph, final String filename, final String vertexIdKey,
                                      final Set<String> propertyKeys) throws IOException {
        final CSVWriter writer = new CSVWriter(graph);
        writer.setVertexIdKey(vertexIdKey);
        writer.outputVertices(filename, propertyKeys);
    }

    /**
     * Write the edges in a Graph to a CSV file
     *
     * @param filename
     * @param propertyKeys
     * @throws IOException
     */
    public void outputEdges(final String filename, final Set<String> propertyKeys) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        outputEdges(fos, propertyKeys);
        fos.close();
    }

    public void outputEdges(final OutputStream csvOutputStream) throws IOException {
        outputEdges(csvOutputStream, null);
    }

    /**
     * Write the edges in a Graph to a CSV OutputStream
     *
     * @param csvOutputStream
     * @param propertyKeys
     * @throws IOException
     */
    public void outputEdges(final OutputStream csvOutputStream, Set<String> propertyKeys) throws IOException {
        Writer out = new OutputStreamWriter(csvOutputStream);
        au.com.bytecode.opencsv.CSVWriter csvWriter = new au.com.bytecode.opencsv.CSVWriter(out, separator, quoteChar,
                escapeChar, lineEnd);

        List<Edge> edges = new ArrayList<Edge>();

        for (Edge edge: graph.getEdges()) {
            edges.add(edge);
        }

        if (normalize) {
            Collections.sort(edges, new LexicographicalElementComparator());
        }

        if (propertyKeys == null) {
            propertyKeys = new TreeSet<String>();

            for (Edge edge: edges) {
                propertyKeys.addAll(edge.getPropertyKeys());
            }
        }

        List<String> headers = new ArrayList<String>();
        headers.add(edgeIdKey);
        headers.add(edgeSourceKey);
        headers.add(edgeTargetKey);
        headers.add(edgeLabelKey);

        for (String key: propertyKeys) {
            headers.add(key);
        }

        csvWriter.writeNext(headers.toArray(new String[headers.size()]));

        for (Edge edge: edges) {
            List<String> row = new ArrayList<String>();

            row.add(edge.getId().toString());
            row.add(edge.getVertex(Direction.OUT).getId().toString());
            row.add(edge.getVertex(Direction.IN).getId().toString());
            row.add(edge.getLabel());

            for (String key: propertyKeys) {
                Object property = edge.getProperty(key);

                if (property == null) {
                    row.add(null);
                } else {
                    row.add(property.toString());
                }
            }

            csvWriter.writeNext(row.toArray(new String[row.size()]));
        }

        csvWriter.close();
    }

    /**
     * Write the data in a Graph to a CSV OutputStream
     *
     * @param graph
     * @param csvOutputStream
     * @param edgeIdKey
     * @param edgeSourceKey
     * @param edgeTargetKey
     * @param edgeLabelKey
     * @throws IOException
     */
    public static void outputEdges(final Graph graph, final OutputStream csvOutputStream, String edgeIdKey,
                                   String edgeSourceKey, String edgeTargetKey, String edgeLabelKey) throws IOException {
        outputEdges(graph, csvOutputStream, edgeIdKey, edgeSourceKey, edgeTargetKey, edgeLabelKey, null);
    }

    /**
     * Write the data in a Graph to a CSV OutputStream
     *
     * @param graph
     * @param filename
     * @param edgeIdKey
     * @param edgeSourceKey
     * @param edgeTargetKey
     * @param edgeLabelKey
     * @throws IOException
     */
    public static void outputEdges(final Graph graph, final String filename, final String edgeIdKey,
                                   final String edgeSourceKey, final String edgeTargetKey,
                                   final String edgeLabelKey) throws IOException {
        outputEdges(graph, filename, edgeIdKey, edgeSourceKey, edgeTargetKey, edgeLabelKey, null);
    }

    /**
     * Write the data in a Graph to a CSV OutputStream
     *
     * @param graph
     * @param csvOutputStream
     * @param edgeIdKey
     * @param edgeSourceKey
     * @param edgeTargetKey
     * @param edgeLabelKey
     * @param propertyKeys
     * @throws IOException
     */
    public static void outputEdges(final Graph graph, final OutputStream csvOutputStream, final String edgeIdKey,
                                   final String edgeSourceKey, final String edgeTargetKey, final String edgeLabelKey,
                                   final Set<String> propertyKeys) throws IOException {
        final CSVWriter writer = new CSVWriter(graph);
        writer.setEdgeIdKey(edgeIdKey);
        writer.setEdgeSourceKey(edgeSourceKey);
        writer.setEdgeTargetKey(edgeTargetKey);
        writer.setEdgeLabelKey(edgeLabelKey);
        writer.outputEdges(csvOutputStream, propertyKeys);
    }

    /**
     * Write the data in a Graph to a CSV OutputStream
     *
     * @param graph
     * @param filename
     * @param edgeIdKey
     * @param edgeSourceKey
     * @param edgeTargetKey
     * @param edgeLabelKey
     * @param propertyKeys
     * @throws IOException
     */
    public static void outputEdges(final Graph graph, final String filename, final String edgeIdKey,
                                   final String edgeSourceKey, final String edgeTargetKey,
                                   final String edgeLabelKey, final Set<String> propertyKeys) throws IOException {
        final CSVWriter writer = new CSVWriter(graph);
        writer.setEdgeIdKey(edgeIdKey);
        writer.setEdgeSourceKey(edgeSourceKey);
        writer.setEdgeTargetKey(edgeTargetKey);
        writer.setEdgeLabelKey(edgeLabelKey);
        writer.outputEdges(filename, propertyKeys);
    }
}
