package com.tinkerpop.blueprints.util.io.gml;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.io.LexicographicalElementComparator;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * GMLWriter writes a Graph to a GML OutputStream.
 *
 * GML definition taken from
 * (http://www.fim.uni-passau.de/fileadmin/files/lehrstuhl/brandenburg/projekte/gml/gml-documentation.tar.gz)
 *
 * @author Stuart Hendren (http://stuarthendren.net)
 */
public class GMLWriter {

    private static final String DELIMITER = " ";
    private static final String TAB = "\t";
    private static final String NEW_LINE = "\r\n";
    private static final String OPEN_LIST = " [" + NEW_LINE;
    private static final String CLOSE_LIST = "]" + NEW_LINE;
    private final Graph graph;
    private boolean normalize = false;
    private boolean useId = false;
    private boolean strict = false;
    private String vertexIdKey = GMLTokens.BLUEPRINTS_ID;
    private String edgeIdKey = GMLTokens.BLUEPRINTS_ID;

    /**
     * Property keys must be alphanumeric and not exceed 254 characters. They must start with an alpha character.
     */
    private static final String GML_PROPERTY_KEY_REGEX = "[a-zA-Z][a-zA-Z0-9]{0,253}";
    private static final Pattern regex = Pattern.compile(GML_PROPERTY_KEY_REGEX);

    /**
     * @param graph the Graph to pull the data from
     */
    public GMLWriter(final Graph graph) {
        this.graph = graph;
    }

    /**
     * @param strict when set to true, property keys in the graph that do not meet the exact guidelines of the GML
     *               specification are ignored.  By default this value is false.
     */
    public void setStrict(final boolean strict) {
        this.strict = strict;
    }

    /**
     * @param normalize whether to normalize the output. Normalized output is deterministic with respect to the order of
     *                  elements and properties in the resulting XML document, and is compatible with line diff-based tools
     *                  such as Git. Note: normalized output is memory-intensive and is not appropriate for very large graphs.
     */
    public void setNormalize(final boolean normalize) {
        this.normalize = normalize;
    }

    /**
     * @param useId whether to use the blueprints id directly or substitute with a generated integer. To use this option
     *              the blueprints ids must all be Integers of String representations of integers
     */
    public void setUseId(final boolean useId) {
        this.useId = useId;
    }

    /**
     * @param vertexIdKey gml property to use for the blueprints vertex id, defaults to {@link GMLTokens#BLUEPRINTS_ID}
     */
    public void setVertexIdKey(final String vertexIdKey) {
        this.vertexIdKey = vertexIdKey;
    }

    /**
     * @param edgeIdKey gml property to use for the blueprints edges id, defaults to {@link GMLTokens#BLUEPRINTS_ID}
     */
    public void setEdgeIdKey(final String edgeIdKey) {
        this.edgeIdKey = edgeIdKey;
    }

    /**
     * Write the data in a Graph to a GML OutputStream.
     *
     * @param filename the GML file to write the Graph data to
     * @throws IOException thrown if there is an error generating the GML data
     */
    public void outputGraph(final String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        outputGraph(fos);
        fos.close();
    }

    /**
     * Write the data in a Graph to a GML OutputStream.
     *
     * @param gMLOutputStream the GML OutputStream to write the Graph data to
     * @throws IOException thrown if there is an error generating the GML data
     */
    public void outputGraph(final OutputStream gMLOutputStream) throws IOException {

        // ISO 8859-1 as specified in the GML documentation
        final Writer writer = new BufferedWriter(new OutputStreamWriter(gMLOutputStream, Charset.forName("ISO-8859-1")));

        final List<Vertex> vertices = new ArrayList<Vertex>();
        final List<Edge> edges = new ArrayList<Edge>();

        populateLists(vertices, edges);

        if (normalize) {
            final LexicographicalElementComparator comparator = new LexicographicalElementComparator();
            Collections.sort(vertices, comparator);
            Collections.sort(edges, comparator);
        }

        writeGraph(writer, vertices, edges);

        // just flush, don't close...allow the underlying stream to stay open and let the calling function close it
        writer.flush();
    }

    private void writeGraph(final Writer writer, final List<Vertex> vertices, final List<Edge> edges) throws IOException {
        final Map<Vertex, Integer> ids = new HashMap<Vertex, Integer>();

        writer.write(GMLTokens.GRAPH);
        writer.write(OPEN_LIST);
        writeVertices(writer, vertices, ids);
        writeEdges(writer, edges, ids);
        writer.write(CLOSE_LIST);

    }

    private void writeVertices(final Writer writer, final List<Vertex> vertices,
                               final Map<Vertex, Integer> ids) throws IOException {
        int count = 1;
        for (Vertex v : vertices) {
            if (useId) {
                final Integer id = Integer.valueOf(v.getId().toString());
                writeVertex(writer, v, id);
                ids.put(v, id);
            } else {
                writeVertex(writer, v, count);
                ids.put(v, count++);
            }

        }
    }

    private void writeVertex(final Writer writer, final Vertex v, final int id) throws IOException {
        writer.write(TAB);
        writer.write(GMLTokens.NODE);
        writer.write(OPEN_LIST);
        writeKey(writer, GMLTokens.ID);
        writeNumberProperty(writer, id);
        writeVertexProperties(writer, v);
        writer.write(TAB);
        writer.write(CLOSE_LIST);
    }

    private void writeEdges(final Writer writer, final List<Edge> edges,
                            final Map<Vertex, Integer> ids) throws IOException {
        for (Edge e : edges) {
            writeEdgeProperties(writer, e, ids.get(e.getVertex(Direction.OUT)), ids.get(e.getVertex(Direction.IN)));
        }
    }

    private void writeEdgeProperties(final Writer writer, final Edge e,
                                     final Integer source, final Integer target) throws IOException {
        writer.write(TAB);
        writer.write(GMLTokens.EDGE);
        writer.write(OPEN_LIST);
        writeKey(writer, GMLTokens.SOURCE);
        writeNumberProperty(writer, source);
        writeKey(writer, GMLTokens.TARGET);
        writeNumberProperty(writer, target);
        writeKey(writer, GMLTokens.LABEL);
        writeStringProperty(writer, e.getLabel());
        writeEdgeProperties(writer, e);
        writer.write(TAB);
        writer.write(CLOSE_LIST);
    }

    private void writeVertexProperties(final Writer writer, final Vertex e) throws IOException {
        final Object blueprintsId = e.getId();
        if (!useId) {
            writeKey(writer, vertexIdKey);
            if (blueprintsId instanceof Number) {
                writeNumberProperty(writer, (Number) blueprintsId);
            } else {
                writeStringProperty(writer, blueprintsId);
            }
        }
        writeProperties(writer, e);
    }

    private void writeEdgeProperties(final Writer writer, final Edge e) throws IOException {
        final Object blueprintsId = e.getId();
        if (!useId) {
            writeKey(writer, edgeIdKey);
            if (blueprintsId instanceof Number) {
                writeNumberProperty(writer, (Number) blueprintsId);
            } else {
                writeStringProperty(writer, blueprintsId);
            }
        }
        writeProperties(writer, e);
    }

    private void writeProperties(final Writer writer, final Element e) throws IOException {
        for (String key : e.getPropertyKeys()) {
            if (!this.strict || regex.matcher(key).matches()) {
                final Object property = e.getProperty(key);
                writeKey(writer, key);
                writeProperty(writer, property, 0);
            }
        }
    }

    private void writeProperty(final Writer writer, final Object property, int tab) throws IOException {
        if (property instanceof Number) {
            writeNumberProperty(writer, (Number) property);
        } else if (property instanceof Map) {
            writeMapProperty(writer, (Map<?, ?>) property, tab);
        } else {
            writeStringProperty(writer, property.toString());
        }
    }

    private void writeMapProperty(final Writer writer, final Map<?, ?> map, int tabs) throws IOException {
        writer.write(OPEN_LIST);
        tabs++;
        for (Entry<?, ?> entry : map.entrySet()) {
            writeTabs(writer, tabs);
            writeKey(writer, entry.getKey().toString());
            writeProperty(writer, entry.getValue(), tabs);
        }
        writeTabs(writer, tabs - 1);
        writer.write(CLOSE_LIST);

    }

    private void writeTabs(final Writer writer, final int tabs) throws IOException {
        for (int i = 0; i <= tabs; i++) {
            writer.write(TAB);
        }
    }

    private void writeNumberProperty(final Writer writer, final Number integer) throws IOException {
        writer.write(integer.toString());
        writer.write(NEW_LINE);
    }

    private void writeStringProperty(final Writer writer, final Object string) throws IOException {
        writer.write("\"");
        writer.write(string.toString());
        writer.write("\"");
        writer.write(NEW_LINE);
    }

    private void writeKey(final Writer writer, final String command) throws IOException {
        writer.write(TAB);
        writer.write(TAB);
        writer.write(command);
        writer.write(DELIMITER);
    }

    private void populateLists(final List<Vertex> vertices, final List<Edge> edges) {
        for (Vertex v : graph.getVertices()) {
            vertices.add(v);
        }
        for (Edge e : graph.getEdges()) {
            edges.add(e);
        }
    }

    /**
     * Write the data in a Graph to a GML OutputStream.
     *
     * @param graph               the Graph to pull the data from
     * @param graphMLOutputStream the GML OutputStream to write the Graph data to
     * @throws IOException thrown if there is an error generating the GML data
     */
    public static void outputGraph(final Graph graph, final OutputStream graphMLOutputStream) throws IOException {
        final GMLWriter writer = new GMLWriter(graph);
        writer.outputGraph(graphMLOutputStream);
    }

    /**
     * Write the data in a Graph to a GML OutputStream.
     *
     * @param graph    the Graph to pull the data from
     * @param filename the GML file to write the Graph data to
     * @throws IOException thrown if there is an error generating the GML data
     */
    public static void outputGraph(final Graph graph, final String filename) throws IOException {
        final GMLWriter writer = new GMLWriter(graph);
        writer.outputGraph(filename);
    }
}
