package com.tinkerpop.blueprints.pgm.util.graphml;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;

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
     * @param graph              the graph to populate with the GraphML data
     * @param graphMLInputStream an InputStream of GraphML data
     * @throws IOException thrown when the GraphML data is not correctly formatted
     */
    public static void inputGraph(final Graph graph, final InputStream graphMLInputStream) throws IOException {
        GraphMLReader.inputGraph(graph, graphMLInputStream, 1000, null, null, null);
    }

    /**
     * Input the GraphML stream data into the graph.
     * More control over how data is streamed is provided by this method.
     *
     * @param graph              the graph to populate with the GraphML data
     * @param graphMLInputStream an InputStream of GraphML data
     * @param bufferSize         the amount of elements to hold in memory before committing a transactions (only valid for TransactionalGraphs)
     * @param vertexIdKey        if the id of a vertex is a &lt;data/&gt; property, fetch it from the data property.
     * @param edgeIdKey          if the id of an edge is a &lt;data/&gt; property, fetch it from the data property.
     * @param edgeLabelKey       if the label of an edge is a &lt;data/&gt; property, fetch it from the data property.
     * @throws IOException thrown when the GraphML data is not correctly formatted
     */
    public static void inputGraph(final Graph graph, final InputStream graphMLInputStream, int bufferSize, String vertexIdKey, String edgeIdKey, String edgeLabelKey) throws IOException {

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        try {
            XMLStreamReader reader = inputFactory.createXMLStreamReader(graphMLInputStream);

            int previousMaxBufferSize = 0;
            if (graph instanceof TransactionalGraph) {
                previousMaxBufferSize = ((TransactionalGraph) graph).getMaxBufferSize();
                ((TransactionalGraph) graph).setMaxBufferSize(bufferSize);
            }

            Map<String, String> keyIdMap = new HashMap<String, String>();
            Map<String, String> keyTypesMaps = new HashMap<String, String>();
            // <Mapped ID String, ID Object>
            Map<String, Object> vertexIdMap = new HashMap<String, Object>();
            // Mapping between Source/Target IDs and "Property IDs"
            // <Default ID String, Mapped ID String>
            Map<String, String> vertexMappedIdMap = new HashMap<String, String>();

            // Buffered Vertex Data
            String vertexId = null;
            Map<String, Object> vertexProps = null;
            boolean inVertex = false;

            // Buffered Edge Data
            String edgeId = null;
            String edgeLabel = null;
            Vertex edgeInVertex = null;
            Vertex edgeOutVertex = null;
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

                        String outVertexId = reader.getAttributeValue(null, GraphMLTokens.SOURCE);
                        String inVertexId = reader.getAttributeValue(null, GraphMLTokens.TARGET);

                        Object outObjectId = null;
                        Object inObjectId = null;
                        if (vertexIdKey == null) {
                            outObjectId = vertexIdMap.get(outVertexId);
                            inObjectId = vertexIdMap.get(inVertexId);
                        } else {
                            outObjectId = vertexIdMap.get(vertexMappedIdMap.get(outVertexId));
                            inObjectId = vertexIdMap.get(vertexMappedIdMap.get(inVertexId));
                        }

                        edgeOutVertex = null;
                        if (null != outObjectId)
                            edgeOutVertex = graph.getVertex(outObjectId);
                        edgeInVertex = null;
                        if (null != inObjectId)
                            edgeInVertex = graph.getVertex(inObjectId);

                        if (null == edgeOutVertex) {
                            edgeOutVertex = graph.addVertex(outVertexId);
                            vertexIdMap.put(outVertexId, edgeOutVertex.getId());
                            if (vertexIdKey != null)
                                // Default to standard ID system (in case no mapped
                                // ID is found later)
                                vertexMappedIdMap.put(outVertexId, outVertexId);
                        }
                        if (null == edgeInVertex) {
                            edgeInVertex = graph.addVertex(inVertexId);
                            vertexIdMap.put(inVertexId, edgeInVertex.getId());
                            if (vertexIdKey != null)
                                // Default to standard ID system (in case no mapped
                                // ID is found later)
                                vertexMappedIdMap.put(inVertexId, inVertexId);
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
                        Object vertexObjectId = vertexIdMap.get(vertexId);
                        Vertex currentVertex;
                        if (vertexObjectId != null)
                            // Duplicate vertices with same ID?
                            // TODO Alex: Shouldn't this throw an Exception?
                            currentVertex = graph.getVertex(vertexObjectId);
                        else {
                            currentVertex = graph.addVertex(vertexId);
                            vertexIdMap.put(vertexId, currentVertex.getId());
                        }

                        for (Entry<String, Object> prop : vertexProps.entrySet()) {
                            currentVertex.setProperty(prop.getKey(), prop.getValue());
                        }

                        vertexId = null;
                        vertexProps = null;
                        inVertex = false;
                    } else if (elementName.equals(GraphMLTokens.EDGE)) {
                        Edge currentEdge = graph.addEdge(edgeId, edgeOutVertex, edgeInVertex, edgeLabel);

                        for (Entry<String, Object> prop : edgeProps.entrySet()) {
                            currentEdge.setProperty(prop.getKey(), prop.getValue());
                        }

                        edgeId = null;
                        edgeLabel = null;
                        edgeInVertex = null;
                        edgeOutVertex = null;
                        edgeProps = null;
                        inEdge = false;
                    }

                }
            }

            reader.close();

            if (graph instanceof TransactionalGraph) {
                ((TransactionalGraph) graph).setMaxBufferSize(previousMaxBufferSize);
            }
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