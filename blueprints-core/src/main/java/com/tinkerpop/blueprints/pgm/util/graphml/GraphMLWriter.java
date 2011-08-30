package com.tinkerpop.blueprints.pgm.util.graphml;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * GraphMLWriter writes a Graph to a GraphML OutputStream.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class GraphMLWriter {
    private final Graph graph;
    private boolean normalize = false;
    private Map<String, String> vertexKeyTypes = null;
    private Map<String, String> edgeKeyTypes = null;

    /**
     * @param graph the Graph to pull the data from
     */
    public GraphMLWriter(final Graph graph) {
        this.graph = graph;
    }

    /**
     * @param normalize whether to normalize the output.
     *                  Normalized output is deterministic with respect to the order of elements and properties in the resulting XML document,
     *                  and is compatible with line diff-based tools such as Git.
     *                  Note: normalized output is memory-intensive and is not appropriate for very large graphs.
     */
    public void setNormalize(final boolean normalize) {
        this.normalize = normalize;
    }

    /**
     * @param vertexKeyTypes a Map of the data types of the vertex keys
     */
    public void setVertexKeyTypes(final Map<String, String> vertexKeyTypes) {
        this.vertexKeyTypes = vertexKeyTypes;
    }

    /**
     * @param edgeKeyTypes a Map of the data types of the edge keys
     */
    public void setEdgeKeyTypes(final Map<String, String> edgeKeyTypes) {
        this.edgeKeyTypes = edgeKeyTypes;
    }

    /**
     * Write the data in a Graph to a GraphML OutputStream.
     *
     * @param graphMLOutputStream the GraphML OutputStream to write the Graph data to
     * @throws IOException thrown if there is an error generating the GraphML data
     */
    public void outputGraph(final OutputStream graphMLOutputStream) throws IOException {

        if (null == this.vertexKeyTypes || null == this.edgeKeyTypes) {
            Map<String, String> vertexKeyTypes = new HashMap<String, String>();
            Map<String, String> edgeKeyTypes = new HashMap<String, String>();

            for (Vertex vertex : graph.getVertices()) {
                for (String key : vertex.getPropertyKeys()) {
                    if (!vertexKeyTypes.containsKey(key)) {
                        vertexKeyTypes.put(key, GraphMLWriter.getStringType(vertex.getProperty(key)));
                    }
                }
                for (Edge edge : vertex.getOutEdges()) {
                    for (String key : edge.getPropertyKeys()) {
                        if (!edgeKeyTypes.containsKey(key)) {
                            edgeKeyTypes.put(key, GraphMLWriter.getStringType(edge.getProperty(key)));
                        }
                    }
                }
            }

            if (null == this.vertexKeyTypes) {
                this.vertexKeyTypes = vertexKeyTypes;
            }

            if (null == this.edgeKeyTypes) {
                this.edgeKeyTypes = edgeKeyTypes;
            }
        }

        XMLOutputFactory inputFactory = XMLOutputFactory.newInstance();
        try {
            XMLStreamWriter writer = inputFactory.createXMLStreamWriter(graphMLOutputStream, "UTF8");
            if (normalize) {
                writer = new GraphMLWriterHelper.IndentingXMLStreamWriter(writer);
                ((GraphMLWriterHelper.IndentingXMLStreamWriter) writer).setIndentStep("    ");
            }

            writer.writeStartDocument();
            writer.writeStartElement(GraphMLTokens.GRAPHML);
            writer.writeAttribute(GraphMLTokens.XMLNS, GraphMLTokens.GRAPHML_XMLNS);

            //<key id="weight" for="edge" attr.name="weight" attr.type="float"/>
            Collection<String> keyset;

            if (normalize) {
                keyset = new LinkedList<String>();
                keyset.addAll(vertexKeyTypes.keySet());
                Collections.sort((List<String>) keyset);
            } else {
                keyset = vertexKeyTypes.keySet();
            }
            for (String key : keyset) {
                writer.writeStartElement(GraphMLTokens.KEY);
                writer.writeAttribute(GraphMLTokens.ID, key);
                writer.writeAttribute(GraphMLTokens.FOR, GraphMLTokens.NODE);
                writer.writeAttribute(GraphMLTokens.ATTR_NAME, key);
                writer.writeAttribute(GraphMLTokens.ATTR_TYPE, vertexKeyTypes.get(key));
                writer.writeEndElement();
            }

            if (normalize) {
                keyset = new LinkedList<String>();
                keyset.addAll(edgeKeyTypes.keySet());
                Collections.sort((List<String>) keyset);
            } else {
                keyset = edgeKeyTypes.keySet();
            }
            for (String key : keyset) {
                writer.writeStartElement(GraphMLTokens.KEY);
                writer.writeAttribute(GraphMLTokens.ID, key);
                writer.writeAttribute(GraphMLTokens.FOR, GraphMLTokens.EDGE);
                writer.writeAttribute(GraphMLTokens.ATTR_NAME, key);
                writer.writeAttribute(GraphMLTokens.ATTR_TYPE, edgeKeyTypes.get(key));
                writer.writeEndElement();
            }

            writer.writeStartElement(GraphMLTokens.GRAPH);
            writer.writeAttribute(GraphMLTokens.ID, GraphMLTokens.G);
            writer.writeAttribute(GraphMLTokens.EDGEDEFAULT, GraphMLTokens.DIRECTED);

            Iterable<Vertex> vertices;
            if (normalize) {
                vertices = new LinkedList<Vertex>();
                for (Vertex v : graph.getVertices()) {
                    ((Collection<Vertex>) vertices).add(v);
                }
                Collections.sort((List<Vertex>) vertices, new ElementComparator());
            } else {
                vertices = graph.getVertices();
            }
            for (Vertex vertex : vertices) {
                writer.writeStartElement(GraphMLTokens.NODE);
                writer.writeAttribute(GraphMLTokens.ID, vertex.getId().toString());
                Collection<String> keys;
                if (normalize) {
                    keys = new LinkedList<String>();
                    keys.addAll(vertex.getPropertyKeys());
                    Collections.sort((List<String>) keys);
                } else {
                    keys = vertex.getPropertyKeys();
                }
                for (String key : keys) {
                    writer.writeStartElement(GraphMLTokens.DATA);
                    writer.writeAttribute(GraphMLTokens.KEY, key);
                    Object value = vertex.getProperty(key);
                    if (null != value)
                        writer.writeCharacters(value.toString());
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }

            if (normalize) {
                List<Edge> edges = new LinkedList<Edge>();
                for (Vertex vertex : graph.getVertices()) {
                    for (Edge edge : vertex.getOutEdges()) {
                        edges.add(edge);
                    }
                }
                Collections.sort(edges, new ElementComparator());

                for (Edge edge : edges) {
                    writer.writeStartElement(GraphMLTokens.EDGE);
                    writer.writeAttribute(GraphMLTokens.ID, edge.getId().toString());
                    writer.writeAttribute(GraphMLTokens.SOURCE, edge.getOutVertex().getId().toString());
                    writer.writeAttribute(GraphMLTokens.TARGET, edge.getInVertex().getId().toString());
                    writer.writeAttribute(GraphMLTokens.LABEL, edge.getLabel());

                    List<String> keys = new LinkedList<String>();
                    keys.addAll(edge.getPropertyKeys());
                    Collections.sort(keys);

                    for (String key : keys) {
                        writer.writeStartElement(GraphMLTokens.DATA);
                        writer.writeAttribute(GraphMLTokens.KEY, key);
                        Object value = edge.getProperty(key);
                        if (null != value)
                            writer.writeCharacters(value.toString());
                        writer.writeEndElement();
                    }
                    writer.writeEndElement();
                }
            } else {
                for (Vertex vertex : graph.getVertices()) {
                    for (Edge edge : vertex.getOutEdges()) {
                        writer.writeStartElement(GraphMLTokens.EDGE);
                        writer.writeAttribute(GraphMLTokens.ID, edge.getId().toString());
                        writer.writeAttribute(GraphMLTokens.SOURCE, edge.getOutVertex().getId().toString());
                        writer.writeAttribute(GraphMLTokens.TARGET, edge.getInVertex().getId().toString());
                        writer.writeAttribute(GraphMLTokens.LABEL, edge.getLabel());

                        for (String key : edge.getPropertyKeys()) {
                            writer.writeStartElement(GraphMLTokens.DATA);
                            writer.writeAttribute(GraphMLTokens.KEY, key);
                            Object value = edge.getProperty(key);
                            if (null != value)
                                writer.writeCharacters(value.toString());
                            writer.writeEndElement();
                        }
                        writer.writeEndElement();
                    }
                }
            }

            writer.writeEndElement(); // graph
            writer.writeEndElement(); // graphml
            writer.writeEndDocument();

            writer.flush();
            writer.close();
        } catch (XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    /**
     * Write the data in a Graph to a GraphML OutputStream.
     *
     * @param graph               the Graph to pull the data from
     * @param graphMLOutputStream the GraphML OutputStream to write the Graph data to
     * @throws IOException thrown if there is an error generating the GraphML data
     */
    public static void outputGraph(final Graph graph, final OutputStream graphMLOutputStream) throws IOException {
        GraphMLWriter writer = new GraphMLWriter(graph);
        writer.outputGraph(graphMLOutputStream);
    }

    /**
     * Write the data in a Graph to a GraphML OutputStream.
     *
     * @param graph               the Graph to pull the data from
     * @param graphMLOutputStream the GraphML OutputStream to write the Graph data to
     * @param vertexKeyTypes      a Map of the data types of the vertex keys
     * @param edgeKeyTypes        a Map of the data types of the edge keys
     * @throws IOException thrown if there is an error generating the GraphML data
     */
    public static void outputGraph(final Graph graph, final OutputStream graphMLOutputStream, final Map<String, String> vertexKeyTypes, final Map<String, String> edgeKeyTypes) throws IOException {
        GraphMLWriter writer = new GraphMLWriter(graph);
        writer.setVertexKeyTypes(vertexKeyTypes);
        writer.setEdgeKeyTypes(edgeKeyTypes);
        writer.outputGraph(graphMLOutputStream);
    }

    private static String getStringType(final Object object) {
        if (object instanceof String)
            return GraphMLTokens.STRING;
        else if (object instanceof Integer)
            return GraphMLTokens.INT;
        else if (object instanceof Long)
            return GraphMLTokens.LONG;
        else if (object instanceof Float)
            return GraphMLTokens.FLOAT;
        else if (object instanceof Double)
            return GraphMLTokens.DOUBLE;
        else if (object instanceof Boolean)
            return GraphMLTokens.BOOLEAN;
        else
            return GraphMLTokens.STRING;
    }

    // Note: elements are sorted in lexicographical, not in numerical, order of IDs.
    private static class ElementComparator implements Comparator<Element> {

        @Override
        public int compare(final Element a,
                           final Element b) {
            return a.getId().toString().compareTo(b.getId().toString());
        }
    }
}
