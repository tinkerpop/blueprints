package com.tinkerpop.blueprints.pgm.util.io.graphml;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Index.Type;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.io.graphml.extra.ExtraTypeHandler;
import com.tinkerpop.blueprints.pgm.util.io.graphml.extra.IndexTokens;

/**
 * GraphMLWriter writes a Graph to a GraphML OutputStream.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Joshua Shinavier (http://fortytwo.net)
 * @author Salvatore Piccione (TXT e-solutions SpA)
 */
public class GraphMLWriter extends GraphMLHandler {
    private final Graph graph;
    private boolean normalize = false;
    private Map<String, String> vertexKeyTypes = null;
    private Map<String, String> edgeKeyTypes = null;
    private Map<String, String> commonKeyTypes = null;

    @SuppressWarnings("rawtypes")
    private Collection<ExtraTypeHandler> extraTypeHandlers = null;
    private String xmlSchemaLocation = null;
    
    /**
     * @param graph the Graph to pull the data from
     */
    public GraphMLWriter(final Graph graph) {
        super();
        this.graph = graph;
        this.extraTypeHandlers = this.extraTypeHandlerMap.values();
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
     * 
     * @param xmlSchemaLocation the location of the GraphML XML Schema instance
     */
    public void setXmlSchemaLocation(String xmlSchemaLocation) {
        this.xmlSchemaLocation = xmlSchemaLocation;
    }

    /**
     * Write the data in a Graph to a GraphML OutputStream.
     *
     * @param graphMLOutputStream the GraphML OutputStream to write the Graph data to
     * @throws IOException thrown if there is an error generating the GraphML data
     */
    public void outputGraph(final OutputStream graphMLOutputStream) throws IOException {
        
        HashMap<String,Set<String>> automaticIndexMap = null;
        boolean checkManualIndex = false;
        if (graph instanceof IndexableGraph) {
            checkManualIndex = true;
            if (this.automaticIndexKey ==  null)
                this.automaticIndexKey = IndexTokens.DEFAUL_AUTO_INDEX_KEY;
            
            if (this.manualIndexKey == null)
                this.manualIndexKey = IndexTokens.DEFAUL_MANUAL_INDEX_KEY;
            
            automaticIndexMap = new HashMap<String, Set<String>>();
            Iterator<Index<? extends Element>> indices = ((IndexableGraph)graph).getIndices().iterator();
            Index index;
            Set<String> indexSet;
            while (indices.hasNext()) {
                index = indices.next();
                if (Type.AUTOMATIC == index.getIndexType() && !Index.EDGES.equals(index.getIndexName())
                        && !Index.VERTICES.equals(index.getIndexName())) {
                    for (Object indexedField : ((AutomaticIndex) index).getAutoIndexKeys()) {
                        indexSet = automaticIndexMap.get(indexedField);
                        if (indexSet == null) {
                            indexSet = new HashSet<String>();
                            automaticIndexMap.put(indexedField.toString(), indexSet);
                        }
                        indexSet.add(index.getIndexName());
                    }
                }
            }
        }    
        

        if (null == this.vertexKeyTypes || null == this.edgeKeyTypes || null == this.commonKeyTypes) {
            Map<String, String> vertexKeyTypes = new HashMap<String, String>();
            Map<String, String> edgeKeyTypes = new HashMap<String, String>();
            Map<String, String> commonKeyTypes = new HashMap<String, String>();
            Object value;
            for (Vertex vertex : graph.getVertices()) {
                for (String key : vertex.getPropertyKeys()) {
                    if (!vertexKeyTypes.containsKey(key)) {
                        value = vertex.getProperty(key);
                        vertexKeyTypes.put(key, GraphMLWriter.getStringType(value));
                        if (!this.extraTypeHandlers.isEmpty())
                            this.addExtraTypeAttribute(key, value);
                    }
                }
                for (Edge edge : vertex.getOutEdges()) {
                    for (String key : edge.getPropertyKeys()) {
                        //check if the property has been used also for a vertex
                        if (vertexKeyTypes.containsKey(key))
                            //if yes, we put it in the common map
                            commonKeyTypes.put(key, vertexKeyTypes.remove(key));
                        else if (!edgeKeyTypes.containsKey(key)) {
                            value = edge.getProperty(key);
                            edgeKeyTypes.put(key, GraphMLWriter.getStringType(value));
                            if (!this.extraTypeHandlers.isEmpty())
                                this.addExtraTypeAttribute(key, value);
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
            if (this.edgeLabelKey != null) {
                this.edgeKeyTypes.put(this.edgeLabelKey, GraphMLTokens.STRING);
            }
            
            if (null == this.commonKeyTypes) {
                this.commonKeyTypes = commonKeyTypes;
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
            //XML Schema instance namespace definition (xsi)
            writer.writeAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + GraphMLTokens.XML_SCHEMA_NAMESPACE_TAG,
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            //XML Schema location
            writer.writeAttribute(GraphMLTokens.XML_SCHEMA_NAMESPACE_TAG + ":" + GraphMLTokens.XML_SCHEMA_LOCATION_ATTRIBUTE,
                GraphMLTokens.GRAPHML_XMLNS + " " + (this.xmlSchemaLocation == null ? 
                        GraphMLTokens.DEFAULT_GRAPHML_SCHEMA_LOCATION : this.xmlSchemaLocation));
            
            //<key id="weight" for="edge" attr.name="weight" attr.type="float"/>
            this.writeKeys(writer, GraphMLTokens.NODE, vertexKeyTypes, automaticIndexMap);
            this.writeKeys(writer, GraphMLTokens.EDGE, edgeKeyTypes, automaticIndexMap);
            this.writeKeys(writer, GraphMLTokens.ALL, commonKeyTypes, automaticIndexMap);

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
            ExtraTypeHandler<?> extraTypeHandler;
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
                    if (checkManualIndex) {
                        this.checkManualIndex((IndexableGraph) graph, writer, vertex, key, value);
                    }
                    if (null != value) {
                        extraTypeHandler = this.extraTypeHandlerMap.get(key);
                        if (extraTypeHandler == null)
                            writer.writeCharacters(value.toString());
                        else
                            writer.writeCharacters(extraTypeHandler.marshal(graph, vertex, key));
                    }
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

                writeEdges(edges.iterator(), writer, checkManualIndex);
            } else {
                for (Vertex vertex : graph.getVertices()) {
                    this.writeEdges(vertex.getOutEdges().iterator(), writer, checkManualIndex);
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
    
    private void writeEdges (Iterator<Edge> iterator, XMLStreamWriter writer, boolean checkManualIndex) throws XMLStreamException {
        Edge edge;
        ExtraTypeHandler<?> extraTypeHandler;
        while (iterator.hasNext()) {
            edge = iterator.next();
            writer.writeStartElement(GraphMLTokens.EDGE);
            writer.writeAttribute(GraphMLTokens.ID, edge.getId().toString());
            writer.writeAttribute(GraphMLTokens.SOURCE, edge.getOutVertex().getId().toString());
            writer.writeAttribute(GraphMLTokens.TARGET, edge.getInVertex().getId().toString());
            if (this.edgeLabelKey == null)
                writer.writeAttribute(GraphMLTokens.LABEL, edge.getLabel());
            else {
                writer.writeStartElement(GraphMLTokens.DATA);
                writer.writeAttribute(GraphMLTokens.KEY, edgeLabelKey);
                if (checkManualIndex) {
                    this.checkManualIndex((IndexableGraph) graph, writer, edge, edgeLabelKey, edge.getLabel());
                }
                writer.writeCharacters(edge.getLabel());
                writer.writeEndElement();
            }
            Collection<String> keys;
            if (normalize)  {
                keys = new LinkedList<String>();
                keys.addAll(edge.getPropertyKeys());
                Collections.sort((List<String>)keys);
            } else
                keys = edge.getPropertyKeys();
    
            for (String key : keys) {
                writer.writeStartElement(GraphMLTokens.DATA);
                writer.writeAttribute(GraphMLTokens.KEY, key);
                Object value = edge.getProperty(key);
                if (checkManualIndex) {
                    this.checkManualIndex((IndexableGraph) graph, writer, edge, key, value);
                }
                if (null != value) {
                    extraTypeHandler = this.extraTypeHandlerMap.get(key);
                    if (extraTypeHandler == null)
                        writer.writeCharacters(value.toString());
                    else
                        writer.writeCharacters(extraTypeHandler.marshal(graph, edge, key));
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }
    
    private void writeKeys (XMLStreamWriter writer, String forAttributeValue, Map<String, String> keyTypes, 
            Map<String,Set<String>> autoIndexMap) throws XMLStreamException {
        ExtraTypeHandler<?> handler;
        Collection<String> keyset;
        if (normalize) {
            keyset = new LinkedList<String>();
            keyset.addAll(keyTypes.keySet());
            Collections.sort((List<String>) keyset);
        } else {
            keyset = keyTypes.keySet();
        }
        for (String key : keyset) {
            writer.writeStartElement(GraphMLTokens.KEY);
            writer.writeAttribute(GraphMLTokens.ID, key);
            writer.writeAttribute(GraphMLTokens.FOR, forAttributeValue);
            writer.writeAttribute(GraphMLTokens.ATTR_NAME, key);
            writer.writeAttribute(GraphMLTokens.ATTR_TYPE, keyTypes.get(key));
            
            if ((handler = this.extraTypeHandlerMap.get(key)) != null)
                writer.writeAttribute(handler.getAttributeName(), handler.getAttributeValue());
           
            if (autoIndexMap != null) {
                Set<String> indexSet = autoIndexMap.get(key);
                if (indexSet != null) {
                    writer.writeAttribute(automaticIndexKey, getIndexNameList(indexSet));
                }
            }
            
            writer.writeEndElement();
        }
    }
    
    private static String getIndexNameList (Collection<String> indexList) {
        StringBuilder indexBuffer = new StringBuilder ();
        for (String index : indexList)  {
            if (indexBuffer.length() > 0)
                indexBuffer.append(IndexTokens.INDEX_SEPARATOR);
            indexBuffer.append(index);
        }
        return indexBuffer.toString();
    }

    private void addExtraTypeAttribute (String key, Object value) {
        boolean found = false;
        Iterator<ExtraTypeHandler> handlerIterator = this.extraTypeHandlers.iterator();
        ExtraTypeHandler<?> currentHandler = null;
        while (!found && handlerIterator.hasNext()) {
            currentHandler = handlerIterator.next();
            found = currentHandler.canHandle(graph, value);
        }
        if (found) {
            this.extraTypeHandlerMap.put(key, currentHandler);
        }
    }
    
    /*
     * Checks if the given attributeName - attributeValue mapping is manually indexed. If yes, it appends each
     * manual index to the value of the <data> attribute holding the list of manual index.
     */
    private void checkManualIndex (IndexableGraph graph, XMLStreamWriter writer, Element graphElement, 
            String attributeName, Object attributeValue) throws XMLStreamException {
        Iterator<Index<? extends Element>> indices = graph.getIndices().iterator();
        Index index;
        CloseableSequence indexedElements;
        Collection<String> indexSet = new LinkedList<String> ();
        boolean stop;
        while (indices.hasNext()) {
            index = indices.next();
            stop = false;
            if (index.getIndexType() == Type.MANUAL && index.getIndexClass().isInstance(graphElement)) {
                indexedElements = index.get(attributeName, attributeValue);
                while (indexedElements.hasNext() && !stop) {
                    if (graphElement.getId().equals(((Element)indexedElements.next()).getId())) {
                        indexSet.add(index.getIndexName());
                        stop = true;
                    }
                }
                indexedElements.close();
            }
        }
        if (!indexSet.isEmpty())
            writer.writeAttribute(manualIndexKey, getIndexNameList(indexSet));
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
        else if (object instanceof Number)
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
