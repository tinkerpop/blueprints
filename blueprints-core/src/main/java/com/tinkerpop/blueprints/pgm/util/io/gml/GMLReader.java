package com.tinkerpop.blueprints.pgm.util.io.gml;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.batch.BufferGraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A reader for the Graph Modelling Language (GML).
 * <p/>
 * GML definition taken from
 * (http://www.fim.uni-passau.de/fileadmin/files/lehrstuhl/brandenburg/projekte/gml/gml-documentation.tar.gz)
 * <p/>
 * It's not clear that all node have to have id's or that they have to be integers - we assume that this is the case. We
 * also assume that only one graph can be defined in a file.
 *
 * @author Stuart Hendren (http://stuarthendren.net)
 */
public class GMLReader {

    public static final String DEFAULT_LABEL = "undefined";

    private static final int DEFAULT_BUFFER_SIZE = 1000;

    private Map<Object, Object> vertexIds = new HashMap<Object, Object>();

    private Graph graph;

    private final String defaultEdgeLabel;

    private boolean directed = false;

    private int edgeCount = 0;

    private String vertexIdKey;

    private String edgeIdKey;

    private String edgeLabelKey = GMLTokens.LABEL;

    /**
     * Create a new GML reader
     * <p/>
     * (Uses default edge label DEFAULT_LABEL)
     *
     * @param graph the graph to load data into
     */
    public GMLReader(Graph graph) {
        this(graph, DEFAULT_LABEL);
    }

    /**
     * Create a new GML reader
     *
     * @param graph            the graph to load data into
     * @param defaultEdgeLabel the default edge label to be used if the GML edge does not define a label
     */
    public GMLReader(Graph graph, String defaultEdgeLabel) {
        this.graph = graph;
        this.defaultEdgeLabel = defaultEdgeLabel;
    }

    /**
     * @param vertexIdKey gml property to use as id for verticies
     */
    public void setVertexIdKey(String vertexIdKey) {
        this.vertexIdKey = vertexIdKey;
    }

    /**
     * @param edgeIdKey gml property to use as id for edges
     */
    public void setEdgeIdKey(String edgeIdKey) {
        this.edgeIdKey = edgeIdKey;
    }

    /**
     * @param edgeLabelKey gml property to assign edge Labels to
     */
    public void setEdgeLabelKey(String edgeLabelKey) {
        this.edgeLabelKey = edgeLabelKey;
    }

    /**
     * Read the GML from from the stream.
     * <p/>
     * If the file is malformed incomplete data can be loaded.
     *
     * @param inputStream
     * @throws IOException
     */
    public void inputGraph(InputStream inputStream) throws IOException {
        inputGraph(inputStream, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Read the GML from from the stream.
     * <p/>
     * If the file is malformed incomplete data can be loaded.
     *
     * @param inputStream
     * @throws IOException
     */
    public void inputGraph(InputStream inputStream, int bufferSize) throws IOException {


        if (graph instanceof TransactionalGraph) {
            this.graph = new BufferGraph((TransactionalGraph) this.graph, bufferSize);
        }

        Reader r = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("ISO-8859-1")));
        StreamTokenizer st = new StreamTokenizer(r);

        try {
            st.commentChar(GMLTokens.COMMENT_CHAR);
            st.ordinaryChar('[');
            st.ordinaryChar(']');

            String stringCharacters = "/\\(){}<>!£$%^&*-+=,.?:;@_`|~";
            for (int i = 0; i < stringCharacters.length(); i++) {
                st.wordChars(stringCharacters.charAt(i), stringCharacters.charAt(i));
            }

            parse(st);

            if (graph instanceof TransactionalGraph) {
                ((TransactionalGraph) graph).stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
            }
        } catch (IOException e) {
            throw new IOException(error(st), e);
        } finally {
            vertexIds.clear();
        }

    }

    private boolean hasNext(StreamTokenizer st) throws IOException {
        return st.nextToken() != StreamTokenizer.TT_EOF;
    }

    private String error(StreamTokenizer st) {
        return "GML malformed line number " + st.lineno() + ": ";
    }

    private boolean notLineBreak(int type) {
        return type != StreamTokenizer.TT_EOL;
    }

    private void parse(StreamTokenizer st) throws IOException {
        while (hasNext(st)) {
            int type = st.ttype;
            if (notLineBreak(type)) {
                String value = st.sval;
                if (GMLTokens.GRAPH.equals(value)) {
                    parseGraph(st);
                    if (!hasNext(st)) {
                        return;
                    }
                }
            }
        }
        throw new IOException("Graph not complete");
    }

    private void parseGraph(StreamTokenizer st) throws IOException {
        checkValid(st, GMLTokens.GRAPH);
        while (hasNext(st)) {
            // st.nextToken();
            int type = st.ttype;
            if (notLineBreak(type)) {
                if (type == ']') {
                    return;
                } else {
                    String key = st.sval;
                    if (GMLTokens.NODE.equals(key)) {
                        addNode(parseNode(st));
                    } else if (GMLTokens.EDGE.equals(key)) {
                        addEdge(parseEdge(st));
                    } else if (GMLTokens.DIRECTED.equals(key)) {
                        directed = parseBoolean(st);
                    } else {
                        // IGNORE
                        parseValue("ignore", st);
                    }
                }
            }
        }
        throw new IOException("Graph not complete");
    }

    private void addNode(Map<String, Object> map) throws IOException {
        Object id = map.remove(GMLTokens.ID);
        if (id != null) {
            Vertex vertex;
            vertex = createVertex(map, id);
            addProperties(vertex, map);
        } else {
            throw new IOException("No id found for node");
        }
    }

    private Vertex createVertex(Map<String, Object> map, Object id) {
        if (vertexIdKey != null) {
            Object vertexId = map.remove(vertexIdKey);
            if (vertexId != null) {
                vertexIds.put(id, vertexId);
                return graph.addVertex(vertexId);
            }
            // if no id try to use default id this will fail if not unique
            vertexIds.put(id, id);
        }
        // Use default id system
        return graph.addVertex(id);
    }

    private void addEdge(Map<String, Object> map) throws IOException {
        Object source = map.remove(GMLTokens.SOURCE);
        Object target = map.remove(GMLTokens.TARGET);
        if (source == null) {
            throw new IOException("Edge has no source");
        }
        if (target == null) {
            throw new IOException("Edge has no target");
        }
        if (vertexIdKey != null) {
            source = vertexIds.get(source);
            target = vertexIds.get(target);
        }

        Vertex outVertex = graph.getVertex(source);
        Vertex inVertex = graph.getVertex(target);
        if (outVertex == null) {
            throw new IOException("Edge source " + source + " not found");
        }
        if (inVertex == null) {
            throw new IOException("Edge target " + target + " not found");

        }

        Object label = map.remove(edgeLabelKey);
        if (label == null) {
            // try standard label key
            label = map.remove(GMLTokens.LABEL);
        } else {
            // remove label in case edge label key is not label
            // label is reserved and cannot be added as a property
            // if so this data will be lost
            map.remove(GMLTokens.LABEL);
        }

        if (label == null) {
            label = defaultEdgeLabel;
        }
        Object edgeId = edgeCount++;
        if (edgeIdKey != null) {
            Object mappedKey = map.remove(edgeIdKey);
            if (mappedKey != null) {
                edgeId = mappedKey;
            }
            // else use edgecount - could fail if mapped ids overlap with edge count
        }

        // remove id as reserved property - can be left is edgeIdKey in not id
        // This data will be lost
        map.remove(GMLTokens.ID);

        Edge edge = graph.addEdge(edgeId, outVertex, inVertex, label.toString());
        if (directed) {
            edge.setProperty(GMLTokens.DIRECTED, directed);
        }
        addProperties(edge, map);

    }

    private void addProperties(Element element, Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            element.setProperty(entry.getKey(), entry.getValue());
        }
    }

    private Object parseValue(String key, StreamTokenizer st) throws IOException {
        while (hasNext(st)) {
            int type = st.ttype;
            if (notLineBreak(type)) {
                if (type == StreamTokenizer.TT_NUMBER) {
                    Double doubleValue = Double.valueOf(st.nval);
                    if (doubleValue.equals(Double.valueOf(doubleValue.intValue()))) {
                        return doubleValue.intValue();
                    } else {
                        return doubleValue.floatValue();
                    }
                } else {
                    if (type == '[') {
                        return parseMap(key, st);
                    } else if (type == '"') {
                        return st.sval;
                    }
                }
            }
        }
        throw new IOException("value not found");
    }

    private boolean parseBoolean(StreamTokenizer st) throws IOException {
        while (hasNext(st)) {
            int type = st.ttype;
            if (notLineBreak(type)) {
                if (type == StreamTokenizer.TT_NUMBER) {
                    return st.nval == 1.0;
                }
            }
        }
        throw new IOException("boolean not found");
    }

    private Map<String, Object> parseNode(StreamTokenizer st) throws IOException {
        return parseElement(st, GMLTokens.NODE);
    }

    private Map<String, Object> parseEdge(StreamTokenizer st) throws IOException {
        return parseElement(st, GMLTokens.EDGE);
    }

    private Map<String, Object> parseElement(StreamTokenizer st, String node) throws IOException {
        checkValid(st, node);
        return parseMap(node, st);
    }

    private Map<String, Object> parseMap(String node, StreamTokenizer st) throws IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        while (hasNext(st)) {
            int type = st.ttype;
            if (notLineBreak(type)) {
                if (type == ']') {
                    return map;
                } else {
                    String key = st.sval;
                    Object value = parseValue(key, st);
                    map.put(key, value);
                }
            }
        }
        throw new IOException(node + " incomplete");
    }

    private void checkValid(StreamTokenizer st, String token) throws IOException {
        if (st.nextToken() != '[') {
            throw new IOException(token + " not followed by [");
        }
    }

    /**
     * Load the GML file into the Graph.
     *
     * @param graph       to receive the data
     * @param inputStream GML file
     * @throws IOException thrown if the data is not valid
     */
    public static void inputGraph(Graph graph, InputStream inputStream) throws IOException {
        inputGraph(graph, inputStream, DEFAULT_LABEL);
    }

    /**
     * Load the GML file into the Graph.
     *
     * @param graph            to receive the data
     * @param inputStream      GML file
     * @param defaultEdgeLabel default edge label to be used if not defined in the data
     * @throws IOException thrown if the data is not valid
     */
    public static void inputGraph(Graph graph, InputStream inputStream, String defaultEdgeLabel) throws IOException {
        new GMLReader(graph, defaultEdgeLabel).inputGraph(inputStream, DEFAULT_BUFFER_SIZE);
    }
}
