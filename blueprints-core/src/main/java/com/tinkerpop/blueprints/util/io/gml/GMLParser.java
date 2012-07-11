package com.tinkerpop.blueprints.util.io.gml;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Stuart Hendren (http://stuarthendren.net)
 * @author Stephen Mallette
 */
class GMLParser {
    /**
     * <Mapped ID String, ID Object>
     */
    //private final Map<Object, Object> vertexIdMap = new HashMap<Object, Object>();

    private final Map<Object, Object> vertexMappedIdMap = new HashMap<Object, Object>();

    private final String defaultEdgeLabel;

    private final Graph graph;

    private final String vertexIdKey;

    private final String edgeIdKey;

    private final String edgeLabelKey;

    private boolean directed = false;

    private int edgeCount = 0;

    public GMLParser(final Graph graph, final String defaultEdgeLabel, final String vertexIdKey, final String edgeIdKey,
                     final String edgeLabelKey) {
        this.graph = graph;
        this.vertexIdKey = vertexIdKey;
        this.edgeIdKey = edgeIdKey;
        this.edgeLabelKey = edgeLabelKey;
        this.defaultEdgeLabel = defaultEdgeLabel;
    }

    public void parse(final StreamTokenizer st) throws IOException {
        while (hasNext(st)) {
            int type = st.ttype;
            if (notLineBreak(type)) {
                final String value = st.sval;
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

    private void parseGraph(final StreamTokenizer st) throws IOException {
        checkValid(st, GMLTokens.GRAPH);
        while (hasNext(st)) {
            // st.nextToken();
            final int type = st.ttype;
            if (notLineBreak(type)) {
                if (type == ']') {
                    return;
                } else {
                    final String key = st.sval;
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

    private void addNode(final Map<String, Object> map) throws IOException {
        final Object id = map.remove(GMLTokens.ID);
        if (id != null) {
            final Vertex vertex = createVertex(map, id);
            addProperties(vertex, map);
        } else {
            throw new IOException("No id found for node");
        }
    }

    private Vertex createVertex(final Map<String, Object> map, final Object id) {
        //final Object vertexId = vertexIdKey == null ? (graph.getFeatures().ignoresSuppliedIds ? null : id) : map.remove(vertexIdKey);
        Object vertexId = id;
        if (vertexIdKey != null) {
            vertexId = map.remove(vertexIdKey);
            if (vertexId == null) vertexId = id;
            vertexMappedIdMap.put(id, vertexId);
        }
        final Vertex createdVertex = graph.addVertex(vertexId);

        return createdVertex;
    }

    private void addEdge(final Map<String, Object> map) throws IOException {
        Object source = map.remove(GMLTokens.SOURCE);
        Object target = map.remove(GMLTokens.TARGET);

        if (source == null) {
            throw new IOException("Edge has no source");
        }

        if (target == null) {
            throw new IOException("Edge has no target");
        }
        if (vertexIdKey != null) {
            source = vertexMappedIdMap.get(source);
            target = vertexMappedIdMap.get(target);
        }

        final Vertex outVertex = graph.getVertex(source);
        final Vertex inVertex = graph.getVertex(target);
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

    private void addProperties(final Element element, final Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            element.setProperty(entry.getKey(), entry.getValue());
        }
    }

    private Object parseValue(final String key, final StreamTokenizer st) throws IOException {
        while (hasNext(st)) {
            final int type = st.ttype;
            if (notLineBreak(type)) {
                if (type == StreamTokenizer.TT_NUMBER) {
                    final Double doubleValue = Double.valueOf(st.nval);
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

    private boolean parseBoolean(final StreamTokenizer st) throws IOException {
        while (hasNext(st)) {
            final int type = st.ttype;
            if (notLineBreak(type)) {
                if (type == StreamTokenizer.TT_NUMBER) {
                    return st.nval == 1.0;
                }
            }
        }
        throw new IOException("boolean not found");
    }

    private Map<String, Object> parseNode(final StreamTokenizer st) throws IOException {
        return parseElement(st, GMLTokens.NODE);
    }

    private Map<String, Object> parseEdge(final StreamTokenizer st) throws IOException {
        return parseElement(st, GMLTokens.EDGE);
    }

    private Map<String, Object> parseElement(final StreamTokenizer st, final String node) throws IOException {
        checkValid(st, node);
        return parseMap(node, st);
    }

    private Map<String, Object> parseMap(final String node, final StreamTokenizer st) throws IOException {
        final Map<String, Object> map = new HashMap<String, Object>();
        while (hasNext(st)) {
            final int type = st.ttype;
            if (notLineBreak(type)) {
                if (type == ']') {
                    return map;
                } else {
                    final String key = st.sval;
                    final Object value = parseValue(key, st);
                    map.put(key, value);
                }
            }
        }
        throw new IOException(node + " incomplete");
    }

    private void checkValid(final StreamTokenizer st, final String token) throws IOException {
        if (st.nextToken() != '[') {
            throw new IOException(token + " not followed by [");
        }
    }

    private boolean hasNext(final StreamTokenizer st) throws IOException {
        return st.nextToken() != StreamTokenizer.TT_EOF;
    }

    private boolean notLineBreak(final int type) {
        return type != StreamTokenizer.TT_EOL;
    }
}
