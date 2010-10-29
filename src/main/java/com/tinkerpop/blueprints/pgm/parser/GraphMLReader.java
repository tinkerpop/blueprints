package com.tinkerpop.blueprints.pgm.parser;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Mode;
import com.tinkerpop.blueprints.pgm.Vertex;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class GraphMLReader {

	public static void inputGraph(final Graph graph,
			final InputStream graphMLInputStream) throws XMLStreamException {
		int bufferSize = 1000;
		String eId = null;
		String eLabel = null;
		String vId = null;
		GraphMLReader.inputGraph(graph, graphMLInputStream, bufferSize, eId,
				eLabel, vId);
	}

	public static void inputGraph(final Graph graph,
			final InputStream graphMLInputStream, int bufferSize,
			String edgeIdKey, String edgeLabelKey, String vertexIdKey)
			throws XMLStreamException {

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader reader = inputFactory
				.createXMLStreamReader(graphMLInputStream);

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

		Mode transactionMode = null;
		boolean isTransactionalGraph = false;
		Integer transactionBufferSize = 0;
		if (graph instanceof TransactionalGraph) {
			transactionMode = ((TransactionalGraph) graph).getTransactionMode();
			((TransactionalGraph) graph).setTransactionMode(Mode.MANUAL);
			((TransactionalGraph) graph).startTransaction();
			isTransactionalGraph = true;
		}

		while (reader.hasNext()) {

			Integer eventType = reader.next();
			if (eventType.equals(XMLEvent.START_ELEMENT)) {
				String elementName = reader.getName().getLocalPart();

				if (elementName.equals(GraphMLTokens.KEY)) {
					String id = reader
							.getAttributeValue(null, GraphMLTokens.ID);
					String attributeName = reader.getAttributeValue(null,
							GraphMLTokens.ATTR_NAME);
					String attributeType = reader.getAttributeValue(null,
							GraphMLTokens.ATTR_TYPE);
					keyIdMap.put(id, attributeName);
					keyTypesMaps.put(attributeName, attributeType);

				} else if (elementName.equals(GraphMLTokens.NODE)) {
					vertexId = reader.getAttributeValue(null, GraphMLTokens.ID);
					// TODO
					if (vertexIdKey != null)
						vertexMappedIdMap.put(vertexId, vertexId);
					inVertex = true;
					vertexProps = new HashMap<String, Object>();

				} else if (elementName.equals(GraphMLTokens.EDGE)) {
					edgeId = reader.getAttributeValue(null, GraphMLTokens.ID);
					edgeLabel = reader.getAttributeValue(null,
							GraphMLTokens.LABEL);
					edgeLabel = edgeLabel == null ? GraphMLTokens._DEFAULT
							: edgeLabel;

					String outVertexId = reader.getAttributeValue(null,
							GraphMLTokens.SOURCE);
					String inVertexId = reader.getAttributeValue(null,
							GraphMLTokens.TARGET);

					// TODO
					Object outObjectId = null;
					Object inObjectId = null;
					if (vertexIdKey == null) {
						outObjectId = vertexIdMap.get(outVertexId);
						inObjectId = vertexIdMap.get(inVertexId);
					} else {
						outObjectId = vertexIdMap.get(vertexMappedIdMap
								.get(outVertexId));
						inObjectId = vertexIdMap.get(vertexMappedIdMap
								.get(inVertexId));
					}

					edgeOutVertex = null;
					if (null != outObjectId)
						edgeOutVertex = graph.getVertex(outObjectId);
					edgeInVertex = null;
					if (null != inObjectId)
						edgeInVertex = graph.getVertex(inObjectId);

					if (null == edgeOutVertex) {
						edgeOutVertex = graph.addVertex(outVertexId);
						transactionBufferSize++;
						vertexIdMap.put(outVertexId, edgeOutVertex.getId());
						// TODO
						if (vertexIdKey != null)
							vertexMappedIdMap.put(outVertexId, outVertexId);
					}
					if (null == edgeInVertex) {
						edgeInVertex = graph.addVertex(inVertexId);
						transactionBufferSize++;
						vertexIdMap.put(inVertexId, edgeInVertex.getId());
						// TODO
						if (vertexIdKey != null)
							vertexMappedIdMap.put(inVertexId, inVertexId);
					}

					inEdge = true;
					edgeProps = new HashMap<String, Object>();

				} else if (elementName.equals(GraphMLTokens.DATA)) {
					String key = reader.getAttributeValue(null,
							GraphMLTokens.KEY);
					String attributeName = keyIdMap.get(key);

					if (attributeName != null) {
						String value = reader.getElementText();

						if (inVertex == true) {
							if ((vertexIdKey != null)
									&& (key.equals(vertexIdKey))) {
								// Assumes multiple props, same key not possible
								vertexMappedIdMap.put(vertexId, value);
								vertexId = value;
							} else
								vertexProps.put(key, typeCastValue(key, value,
										keyTypesMaps));
						} else if (inEdge == true) {
							if ((edgeLabelKey != null)
									&& (key.equals(edgeLabelKey)))
								edgeLabel = value;
							else if ((edgeIdKey != null)
									&& (key.equals(edgeIdKey)))
								edgeId = value;
							else
								edgeProps.put(key, typeCastValue(key, value,
										keyTypesMaps));
						}
					}

				}
			} else if (eventType.equals(XMLEvent.END_ELEMENT)) {
				String elementName = reader.getName().getLocalPart();

				if (elementName.equals(GraphMLTokens.NODE)) {
					Object vertexObjectId = vertexIdMap.get(vertexId);
					Vertex currentVertex = null;
					if (vertexObjectId != null)
						currentVertex = graph.getVertex(vertexObjectId);
					else {
						currentVertex = graph.addVertex(vertexId);
						transactionBufferSize++;
						vertexIdMap.put(vertexId, currentVertex.getId());
					}
					// FIXME I think this is all that's necessary (Alex)
					// FIXME Reason: IDs should be unique
					// Vertex currentVertex = graph.addVertex(vertexId);
					// transactionBufferSize++;
					// vertexIdMap.put(vertexId, currentVertex.getId());

					for (Entry<String, Object> prop : vertexProps.entrySet()) {
						currentVertex.setProperty(prop.getKey(), prop
								.getValue());
						transactionBufferSize++;
					}

					vertexId = null;
					vertexProps = null;
					inVertex = false;
				} else if (elementName.equals(GraphMLTokens.EDGE)) {
					Edge currentEdge = graph.addEdge(edgeId, edgeOutVertex,
							edgeInVertex, edgeLabel);

					transactionBufferSize++;

					for (Entry<String, Object> prop : edgeProps.entrySet()) {
						currentEdge.setProperty(prop.getKey(), prop.getValue());
						transactionBufferSize++;
					}

					edgeId = null;
					edgeLabel = null;
					edgeInVertex = null;
					edgeOutVertex = null;
					edgeProps = null;
					inEdge = false;
				}

			}

			if (isTransactionalGraph && (transactionBufferSize > bufferSize)) {
				((TransactionalGraph) graph)
						.stopTransaction(Conclusion.SUCCESS);
				((TransactionalGraph) graph).startTransaction();
				transactionBufferSize = 0;
			}

		}
		reader.close();

		if (isTransactionalGraph) {
			((TransactionalGraph) graph).stopTransaction(Conclusion.SUCCESS);
			((TransactionalGraph) graph).setTransactionMode(transactionMode);
		}
	}

	private static Object typeCastValue(String key, String value,
			Map<String, String> keyTypes) {
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
