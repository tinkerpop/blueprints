package com.tinkerpop.blueprints.util.io.graphml;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * GraphMLReader writes the data from a GraphML stream to a graph.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class GraphMLReader {
    private final Graph graph;

    private String vertexIdKey = null;
    private String edgeIdKey = null;
    private String edgeLabelKey = null;

    /**
     * @param graph the graph to populate with the GraphML data
     */
    public GraphMLReader(Graph graph) {
        this.graph = graph;
    }

    /**
     * @param vertexIdKey if the id of a vertex is a &lt;data/&gt; property, fetch it from the data property.
     */
    public void setVertexIdKey(String vertexIdKey) {
        this.vertexIdKey = vertexIdKey;
    }

    /**
     * @param edgeIdKey if the id of an edge is a &lt;data/&gt; property, fetch it from the data property.
     */
    public void setEdgeIdKey(String edgeIdKey) {
        this.edgeIdKey = edgeIdKey;
    }

    /**
     * @param edgeLabelKey if the label of an edge is a &lt;data/&gt; property, fetch it from the data property.
     */
    public void setEdgeLabelKey(String edgeLabelKey) {
        this.edgeLabelKey = edgeLabelKey;
    }

    /**
     * Input the GraphML stream data into the graph.
     * In practice, usually the provided graph is empty.
     *
     * @param graphMLInputStream an InputStream of GraphML data
     * @throws IOException thrown when the GraphML data is not correctly formatted
     */
    public void inputGraph(final InputStream graphMLInputStream) throws IOException {
        GraphMLReader.inputGraph(this.graph, graphMLInputStream, 1000, this.vertexIdKey, this.edgeIdKey, this.edgeLabelKey);
    }

    /**
     * Input the GraphML stream data into the graph.
     * In practice, usually the provided graph is empty.
     *
     * @param graphMLInputStream an InputStream of GraphML data
     * @param bufferSize         the amount of elements to hold in memory before committing a transactions (only valid for TransactionalGraphs)
     * @throws IOException thrown when the GraphML data is not correctly formatted
     */
    public void inputGraph(final InputStream graphMLInputStream, int bufferSize) throws IOException {
        GraphMLReader.inputGraph(this.graph, graphMLInputStream, bufferSize, this.vertexIdKey, this.edgeIdKey, this.edgeLabelKey);
    }

    /**
     * Input the GraphML stream data into the graph.
     * In practice, usually the provided graph is empty.
     *
     * @param inputGraph         the graph to populate with the GraphML data
     * @param graphMLInputStream an InputStream of GraphML data
     * @throws IOException thrown when the GraphML data is not correctly formatted
     */
    public static void inputGraph(final Graph inputGraph, final InputStream graphMLInputStream) throws IOException {
        GraphMLReader.inputGraph(inputGraph, graphMLInputStream, 1000, null, null, null);
    }

    /**
     * Input the GraphML stream data into the graph.
     * More control over how data is streamed is provided by this method.
     *
     * @param inputGraph         the graph to populate with the GraphML data
     * @param graphMLInputStream an InputStream of GraphML data
     * @param bufferSize         the amount of elements to hold in memory before committing a transactions (only valid for TransactionalGraphs)
     * @param vertexIdKey        if the id of a vertex is a &lt;data/&gt; property, fetch it from the data property.
     * @param edgeIdKey          if the id of an edge is a &lt;data/&gt; property, fetch it from the data property.
     * @param edgeLabelKey       if the label of an edge is a &lt;data/&gt; property, fetch it from the data property.
     * @throws IOException thrown when the GraphML data is not correctly formatted
     */
    public static void inputGraph(final Graph inputGraph, final InputStream graphMLInputStream, int bufferSize, String vertexIdKey, String edgeIdKey, String edgeLabelKey) throws IOException {

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        try {
            XMLStreamReader reader = inputFactory.createXMLStreamReader(graphMLInputStream);

            final BatchGraph graph = BatchGraph.wrap(inputGraph, bufferSize);

            Map<String, String> keyIdMap = new HashMap<String, String>();
            Map<String, String> keyTypesMaps = new HashMap<String, String>();
            // <Mapped ID String, ID Object>

            // <Default ID String, Mapped ID String>
            Map<String, String> vertexMappedIdMap = new HashMap<String, String>();

            // Buffered Vertex Data
            String vertexId = null;
            Map<String, Object> vertexProps = null;
            boolean inVertex = false;

            // Buffered Edge Data
            String edgeId = null;
            String edgeLabel = null;
            Vertex[] edgeEndVertices = null; //[0] = outVertex , [1] = inVertex
            Map<String, Object> edgeProps = null;
            boolean inEdge = false;

            while (reader.hasNext()) {

                Integer eventType = reader.next();
                if (eventType.equals(XMLEvent.START_ELEMENT)) {
                    String elementName = reader.getName().getLocalPart();

                    if (elementName.equals(GraphMLTokens.KEY)) {
                        String id = reader.getAttributeValue(null, GraphMLTokens.ID);
                        String attributeName = reader.getAttributeValue(null, GraphMLTokens.ATTR_NAME);
                        String attributeType = reader.getAttributeValue(null, GraphMLTokens.ATTR_TYPE);
                        keyIdMap.put(id, attributeName);
                        keyTypesMaps.put(attributeName, attributeType);

                    } else if (elementName.equals(GraphMLTokens.NODE)) {
                        vertexId = reader.getAttributeValue(null, GraphMLTokens.ID);
                        if (vertexIdKey != null)
                            vertexMappedIdMap.put(vertexId, vertexId);
                        inVertex = true;
                        vertexProps = new HashMap<String, Object>();

                    } else if (elementName.equals(GraphMLTokens.EDGE)) {
                        edgeId = reader.getAttributeValue(null, GraphMLTokens.ID);
                        edgeLabel = reader.getAttributeValue(null, GraphMLTokens.LABEL);
                        edgeLabel = edgeLabel == null ? GraphMLTokens._DEFAULT : edgeLabel;

                        String[] vertexIds = new String[2];
                        vertexIds[0] = reader.getAttributeValue(null, GraphMLTokens.SOURCE);
                        vertexIds[1] = reader.getAttributeValue(null, GraphMLTokens.TARGET);
                        edgeEndVertices = new Vertex[2];

                        for (int i = 0; i < 2; i++) { //i=0 => outVertex, i=1 => inVertex
                            if (vertexIdKey == null) {
                                edgeEndVertices[i] = graph.getVertex(vertexIds[i]);
                            } else {
                                edgeEndVertices[i] = graph.getVertex(vertexMappedIdMap.get(vertexIds[i]));
                            }

                            if (null == edgeEndVertices[i]) {
                                edgeEndVertices[i] = graph.addVertex(vertexIds[i]);
                                if (vertexIdKey != null)
                                    // Default to standard ID system (in case no mapped
                                    // ID is found later)
                                    vertexMappedIdMap.put(vertexIds[i], vertexIds[i]);
                            }
                        }

                        inEdge = true;
                        edgeProps = new HashMap<String, Object>();

                    } else if (elementName.equals(GraphMLTokens.DATA)) {
                        String key = reader.getAttributeValue(null, GraphMLTokens.KEY);
                        String attributeName = keyIdMap.get(key);

                        if (attributeName != null) {
                            String value = reader.getElementText();

                            if (inVertex == true) {
                                if ((vertexIdKey != null) && (key.equals(vertexIdKey))) {
                                    // Should occur at most once per Vertex
                                    // Assumes single ID prop per Vertex
                                    vertexMappedIdMap.put(vertexId, value);
                                    vertexId = value;
                                } else
                                    vertexProps.put(attributeName, typeCastValue(key, value, keyTypesMaps));
                            } else if (inEdge == true) {
                                if ((edgeLabelKey != null) && (key.equals(edgeLabelKey)))
                                    edgeLabel = value;
                                else if ((edgeIdKey != null) && (key.equals(edgeIdKey)))
                                    edgeId = value;
                                else
                                    edgeProps.put(attributeName, typeCastValue(key, value, keyTypesMaps));
                            }
                        }

                    }
                } else if (eventType.equals(XMLEvent.END_ELEMENT)) {
                    String elementName = reader.getName().getLocalPart();

                    if (elementName.equals(GraphMLTokens.NODE)) {
                        Vertex currentVertex = graph.getVertex(vertexId);
                        if (currentVertex == null) {
                            currentVertex = graph.addVertex(vertexId);
                        }

                        for (Entry<String, Object> prop : vertexProps.entrySet()) {
                            currentVertex.setProperty(prop.getKey(), prop.getValue());
                        }

                        vertexId = null;
                        vertexProps = null;
                        inVertex = false;
                    } else if (elementName.equals(GraphMLTokens.EDGE)) {
                        Edge currentEdge = graph.addEdge(edgeId, edgeEndVertices[0], edgeEndVertices[1], edgeLabel);

                        for (Entry<String, Object> prop : edgeProps.entrySet()) {
                            currentEdge.setProperty(prop.getKey(), prop.getValue());
                        }

                        edgeId = null;
                        edgeLabel = null;
                        edgeEndVertices = null;
                        edgeProps = null;
                        inEdge = false;
                    }

                }
            }

            reader.close();

            graph.commit();;
        } catch (XMLStreamException xse) {
            throw new IOException(xse);
        }
    }

    private static Object typeCastValue(String key, String value, Map<String, String> keyTypes) {
        String type = keyTypes.get(key);
        if (null == type || type.equals(GraphMLTokens.STRING))
            return value;
        else if (type.equals(GraphMLTokens.FLOAT))
            return Float.valueOf(value);
        else if (type.equals(GraphMLTokens.INT))
            return Integer.valueOf(value);
        else if (type.equals(GraphMLTokens.DOUBLE))
            return Double.valueOf(value);
        else if (type.equals(GraphMLTokens.BOOLEAN))
            return Boolean.valueOf(value);
        else if (type.equals(GraphMLTokens.LONG))
            return Long.valueOf(value);
        else
            return value;
    }
}