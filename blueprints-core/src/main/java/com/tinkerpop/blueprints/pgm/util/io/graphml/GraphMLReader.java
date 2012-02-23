package com.tinkerpop.blueprints.pgm.util.io.graphml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.io.graphml.extra.ExtraTypeHandler;
import com.tinkerpop.blueprints.pgm.util.io.graphml.extra.IndexTokens;

/**
 * GraphMLReader writes the data from a GraphML stream to a graph.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 * @author Joshua Shinavier (http://fortytwo.net)
 * @author Salvatore Piccione (TXT e-solutions SpA)
 */
public class GraphMLReader extends GraphMLHandler{
    private final Graph graph;

    private String vertexIdKey = null;
    private String edgeIdKey = null;

    /**
     * @param graph the graph to populate with the GraphML data
     */
    public GraphMLReader(Graph graph) {
        super();
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
     * Input the GraphML stream data into the graph.
     * In practice, usually the provided graph is empty.
     *
     * @param graphMLInputStream an InputStream of GraphML data
     * @throws IOException thrown when the GraphML data is not correctly formatted
     */
    public void inputGraph(final InputStream graphMLInputStream) throws IOException {
        GraphMLReader.inputGraph(this.graph, graphMLInputStream, 1000, this.vertexIdKey, this.edgeIdKey, this.edgeLabelKey, this.extraTypeHandlerMap, 
            this.automaticIndexKey == null ? IndexTokens.DEFAUL_AUTO_INDEX_KEY : this.automaticIndexKey, 
            this.manualIndexKey == null ? IndexTokens.DEFAUL_MANUAL_INDEX_KEY : this.manualIndexKey);
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
        GraphMLReader.inputGraph(this.graph, graphMLInputStream, bufferSize, this.vertexIdKey, this.edgeIdKey, this.edgeLabelKey, this.extraTypeHandlerMap, 
            this.automaticIndexKey == null ? IndexTokens.DEFAUL_AUTO_INDEX_KEY : this.automaticIndexKey, 
            this.manualIndexKey == null ? IndexTokens.DEFAUL_MANUAL_INDEX_KEY : this.manualIndexKey);
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
        //TODO How to pass the ExtraTypeManager map?
        GraphMLReader.inputGraph(graph, graphMLInputStream, 1000, null, null, GraphMLTokens.LABEL, GraphMLReader.buildExtraTypeHadlerMap(), 
                IndexTokens.DEFAUL_AUTO_INDEX_KEY, IndexTokens.DEFAUL_MANUAL_INDEX_KEY);
    }

    /**
     * Input the GraphML stream data into the graph.
     * More control over how data is streamed is provided by this method.
     *
     * @param graph               the graph to populate with the GraphML data
     * @param graphMLInputStream  an InputStream of GraphML data
     * @param bufferSize          the amount of elements to hold in memory before committing a transactions (only valid for TransactionalGraphs)
     * @param vertexIdKey         if the id of a vertex is a &lt;data/&gt; property, fetch it from the data property.
     * @param edgeIdKey           if the id of an edge is a &lt;data/&gt; property, fetch it from the data property.
     * @param edgeLabelKey        if the label of an edge is a &lt;data/&gt; property, fetch it from the data property.
     * @param extraTypeHandlerMap TODO
     * @param automaticIndexAttributeName   TODO
     * @param manualIndexAttributeName      TODO
     * @throws IOException thrown when the GraphML data is not correctly formatted
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void inputGraph(final Graph graph, final InputStream graphMLInputStream, int bufferSize, String vertexIdKey, String edgeIdKey, String edgeLabelKey, 
            Map<String, ExtraTypeHandler> extraTypeHandlerMap, String automaticIndexAttributeName, String manualIndexAttributeName) throws IOException {

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
            
            //Utilities for ExtraTypeHandler support
            Collection<ExtraTypeHandler> extraTypeManagerCollection = extraTypeHandlerMap.values();
            Iterator<ExtraTypeHandler> extraTypeManagerIterator;
            ExtraTypeHandler<?> currentExtraTypeManager;
            String currentExtraTypeValue;
            
            //Utilities for AutomaticIndex support
            HashMap<String,Set<String>> autoIndexMap = null;
            HashMap<String,Class<? extends Element>> autoIndexClassMap = null;
            Set<String> autoIndexAttributeSet;
            String autoIndexAttributeValue;
            Class<? extends Element> indexType;
            Class<? extends Element> existingIndexType;
            if (graph instanceof IndexableGraph && automaticIndexAttributeName != null) {
                autoIndexMap = new HashMap<String, Set<String>>();
                autoIndexClassMap = new HashMap<String, Class<? extends Element>>();
            }
            
            //Utilities for ManualIndex support
            //collects the keys of the indices relating to a specific graph element
            List<String> manualIndexKeys = null;
            //collects the values of the indices relating to a specific graph element
            List<Object> manualIndexValues = null;
            //collects the names of the indices relating to a specific graph element
            List<String> manualIndices = null;
            HashMap<String,TempManualIndex> manualIndexMap = null;
            String manualIndexName = null;
            if (graph instanceof IndexableGraph && manualIndexAttributeName != null) {
                manualIndexKeys = new LinkedList<String>();
                manualIndices = new LinkedList<String>();
                manualIndexValues = new LinkedList<Object>();
                manualIndexMap = new HashMap<String,GraphMLReader.TempManualIndex>();
            }
                        
            while (reader.hasNext()) {

                Integer eventType = reader.next();
                if (eventType.equals(XMLEvent.START_ELEMENT)) {
                    String elementName = reader.getName().getLocalPart();

                    if (elementName.equals(GraphMLTokens.KEY)) {
                        String id = reader.getAttributeValue(null, GraphMLTokens.ID);
                        
                        String attributeName = reader.getAttributeValue(null, GraphMLTokens.ATTR_NAME);
                        String attributeType = reader.getAttributeValue(null, GraphMLTokens.ATTR_TYPE);
                        
                        //ExtraTypeHandler in action...
                        currentExtraTypeValue = null;
                        extraTypeManagerIterator = extraTypeManagerCollection.iterator();
                        while (extraTypeManagerIterator.hasNext() && currentExtraTypeValue == null) {
                            currentExtraTypeManager = extraTypeManagerIterator.next();
                            if ((currentExtraTypeValue = 
                                    reader.getAttributeValue(null, currentExtraTypeManager.getAttributeName())) != null) {
                                attributeType = currentExtraTypeManager.getType(attributeType, currentExtraTypeValue);
                            }
                        }
                        
                        //AutomaticIndex in action...
                        if (autoIndexMap != null) {
                            autoIndexAttributeValue = reader.getAttributeValue(null, automaticIndexAttributeName);
                            if (autoIndexAttributeValue != null) {
                                //split the names
                                for (String autoIndexName : autoIndexAttributeValue.split(IndexTokens.INDEX_SEPARATOR_REGEX)) {
                                    autoIndexAttributeSet = autoIndexMap.get(autoIndexName);
                                    if (autoIndexAttributeSet == null) {
                                        autoIndexAttributeSet = new HashSet<String> ();
                                        autoIndexMap.put(autoIndexName, autoIndexAttributeSet);
                                        existingIndexType = null;
                                    } else
                                        existingIndexType = autoIndexClassMap.get(autoIndexName);
                                    indexType = getIndexClass(reader.getAttributeValue(null, GraphMLTokens.FOR));
                                    if (indexType != null) {
                                        if (existingIndexType == null || indexType.equals(existingIndexType)) {
                                            if (existingIndexType == null)
                                                autoIndexClassMap.put(autoIndexName, indexType);
                                            autoIndexAttributeSet.add(attributeName);
                                        }
                                    }
                                }
                            }
                        }
                        
                        keyIdMap.put(id, attributeName);
                        keyTypesMaps.put(attributeName, attributeType);

                    } else if (elementName.equals(GraphMLTokens.GRAPH)) {
                        //this means that we have can create the automatic indices
                        if (autoIndexMap != null) {
                            IndexableGraph indexGraph = (IndexableGraph) graph;
                            for (String autoIndexKey : autoIndexMap.keySet()) {
                                
                                indexGraph.createAutomaticIndex(autoIndexKey, Vertex.class, autoIndexMap.get(autoIndexKey));
                            }
                        }
                        //free the autoIndexMap
                        autoIndexMap = null;
                        
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
                            if (manualIndexMap != null)
                                manualIndexName = reader.getAttributeValue(null, manualIndexAttributeName);
                                String valueAsString = reader.getElementText();
                                Object value = typeCastValue(keyTypesMaps.get(key), valueAsString, extraTypeHandlerMap, graph);
                            if (inVertex == true) {
                                if ((vertexIdKey != null) && (key.equals(vertexIdKey))) {
                                    // Should occur at most once per Vertex
                                    // Assumes single ID prop per Vertex
                                    vertexMappedIdMap.put(vertexId, valueAsString);
                                    vertexId = valueAsString;
                                } else
                                    vertexProps.put(attributeName, value);
                            } else if (inEdge == true) {
                                if ((edgeLabelKey != null) && (key.equals(edgeLabelKey)))
                                    edgeLabel = valueAsString;
                                else if ((edgeIdKey != null) && (key.equals(edgeIdKey)))
                                    edgeId = valueAsString;
                                else
                                    edgeProps.put(attributeName, value);
                            }
                            //check if we have to look for a manual index AND
                            //check if this attribute is a key of a manual index
                            if (manualIndexName != null) {
                                //split the manualIndexName
                                String[] manualIndexNames = manualIndexName.split(IndexTokens.INDEX_SEPARATOR_REGEX);
                                for (String indexName : manualIndexNames) {
                                    manualIndexKeys.add(attributeName);
                                    manualIndexValues.add(value);
                                    manualIndices.add(indexName);
                                }
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

                        if (manualIndices != null) {
                            if (!manualIndices.isEmpty())
                                handleManualIndex((IndexableGraph) graph, currentVertex, Vertex.class, 
                                    manualIndices, manualIndexKeys, manualIndexValues, manualIndexMap);
                        }
                        vertexId = null;
                        vertexProps = null;
                        inVertex = false;
                    } else if (elementName.equals(GraphMLTokens.EDGE)) {
                        Edge currentEdge = graph.addEdge(edgeId, edgeOutVertex, edgeInVertex, edgeLabel);

                        for (Entry<String, Object> prop : edgeProps.entrySet()) {
                            currentEdge.setProperty(prop.getKey(), prop.getValue());
                        }

                        if (manualIndices != null) {
                            if (!manualIndices.isEmpty())
                                handleManualIndex((IndexableGraph) graph, currentEdge, Vertex.class, 
                                    manualIndices, manualIndexKeys, manualIndexValues, manualIndexMap);
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
            
            if (manualIndexMap != null) {
                if (!manualIndexMap.isEmpty())
                    createManualIndices((IndexableGraph) graph, manualIndexMap);
            }

            if (graph instanceof TransactionalGraph) {
                ((TransactionalGraph) graph).setMaxBufferSize(previousMaxBufferSize);
            }
        } catch (XMLStreamException xse) {
            throw new IOException(xse);
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void createManualIndices (IndexableGraph graph, Map<String, TempManualIndex> indexMap) {
        Index currentIndex;
        TempManualIndex currentIndexData;
        for (Entry<String,TempManualIndex> indexEntry : indexMap.entrySet()) {
            currentIndexData = indexEntry.getValue();
            switch (currentIndexData.getIndexClass()) {
                case TempManualIndex.EDGE_CODE:
                    currentIndex = graph.createManualIndex(indexEntry.getKey(), Edge.class);
                    break;
                case TempManualIndex.VERTEX_CODE:
                    currentIndex = graph.createManualIndex(indexEntry.getKey(), Vertex.class);
                    break;
                default:
                    currentIndex = graph.createManualIndex(indexEntry.getKey(), Element.class);
            }
            for (int index = 0; index < currentIndexData.count(); index++) {
                currentIndex.put(currentIndexData.getKey(index),
                    currentIndexData.getValue(index), 
                    currentIndexData.getIndexedElement(index));
            }
        }
    }
    
    private static void handleManualIndex (IndexableGraph graph, Element indexedElement, 
            Class<? extends Element> elementClass, List<String> indices, List<String> keys, List<Object> values,
            Map<String, TempManualIndex> indexMap) {
        TempManualIndex currentIndexData;
        String indexName;
        for (int i = 0; i < indices.size(); i++) {
            indexName = indices.get(i);
            currentIndexData = indexMap.get(indexName);
            if (currentIndexData == null) {
                currentIndexData = new TempManualIndex();
                indexMap.put(indexName, currentIndexData);
            }
            currentIndexData.addEntry(keys.get(i), values.get(i), indexedElement);
        }
        indices.clear();
        keys.clear();
        values.clear();
    }
    
    private static Class<? extends Element> getIndexClass (String forAttributeValue) {
        if (GraphMLTokens.NODE.equals(forAttributeValue))
            return Vertex.class;
        else if (GraphMLTokens.EDGE.equals(forAttributeValue))
            return Edge.class;
        else if (GraphMLTokens.ALL.equals(forAttributeValue))
            return Element.class;
        else
            return null;
    }

    @SuppressWarnings("rawtypes")
    private static Object typeCastValue(String type, String value, Map<String, ExtraTypeHandler> extraTypeManagers, Graph graph) {
        ExtraTypeHandler<?> extraTypeManager;
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
        else if ((extraTypeManager = extraTypeManagers.get(type)) != null)
            return extraTypeManager.unmarshal(graph, value);
        else
            return value;
    }
    
    private static class TempManualIndex {
        static final int VERTEX_CODE = 0;
        static final int EDGE_CODE = 2;
        static final int ELEMENT_CODE = 4;
        private List<String> keys;
        private List<Object> values;
        private List<Element> indexedElements;
        private int indexClass;
        
        TempManualIndex () {
            keys = new LinkedList<String>();
            values = new LinkedList<Object>();
            indexedElements = new LinkedList<Element>();
            indexClass = -1;
        }
        
        void addEntry (String key, Object value, Element indexedElement) {
            keys.add(key);
            values.add(value);
            indexedElements.add(indexedElement);
            int currentClassCode = this.getClassCode(indexedElement.getClass());
            if (indexClass == -1)
                indexClass = currentClassCode;
            else if (!(indexClass == ELEMENT_CODE || indexClass == currentClassCode))
                indexClass = ELEMENT_CODE;
        }
        
        int count () {
            return keys.size();
        }
        
        String getKey (int index) {
            return keys.get(index);
        }
        
        Object getValue (int index) {
            return values.get(index);
        }
        
        Element getIndexedElement (int index) {
            return indexedElements.get(index);
        }
        
        int getIndexClass () {
            return indexClass;
        }
        
        private int getClassCode (Class elementClass) {
            if (Vertex.class.isAssignableFrom(elementClass))
                return VERTEX_CODE;
            else if (Edge.class.isAssignableFrom(elementClass))
                return EDGE_CODE;
            else
                return ELEMENT_CODE;
        }

        @Override
        public String toString() {
            return "TempManualIndex [keys=" + keys + ", values=" + values
                    + ", indexedElements=" + indexedElements + ", indexClass="
                    + indexClass + "]";
        }
    }
}