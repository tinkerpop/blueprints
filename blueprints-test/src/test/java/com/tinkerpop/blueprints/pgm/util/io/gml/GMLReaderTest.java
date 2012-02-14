package com.tinkerpop.blueprints.pgm.util.io.gml;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

public class GMLReaderTest {

	private static final String LABEL = "label";

	@Test
	public void exampleGMLGetsCorrectNumberOfElements() throws IOException {
		TinkerGraph graph = new TinkerGraph();

		GMLReader.inputGraph(graph, GMLReader.class.getResourceAsStream("example.gml"));

		Assert.assertEquals(3, getIterableCount(graph.getVertices()));
		Assert.assertEquals(3, getIterableCount(graph.getEdges()));
	}

	@Test
	public void exampleGMLGetsCorrectTopology() throws IOException {
		TinkerGraph graph = new TinkerGraph();

		GMLReader.inputGraph(graph, GMLReader.class.getResourceAsStream("example.gml"));

		Vertex v1 = graph.getVertex(1);
		Vertex v2 = graph.getVertex(2);
		Vertex v3 = graph.getVertex(3);

		Iterable<Edge> out1 = v1.getOutEdges();
		Edge e1 = out1.iterator().next();
		Assert.assertEquals(v2, e1.getInVertex());

		Iterable<Edge> out2 = v2.getOutEdges();
		Edge e2 = out2.iterator().next();
		Assert.assertEquals(v3, e2.getInVertex());

		Iterable<Edge> out3 = v3.getOutEdges();
		Edge e3 = out3.iterator().next();
		Assert.assertEquals(v1, e3.getInVertex());

	}

	@Test
	public void exampleGMLGetsCorrectProperties() throws IOException {
		TinkerGraph graph = new TinkerGraph();

		GMLReader.inputGraph(graph, GMLReader.class.getResourceAsStream("example.gml"));

		Vertex v1 = graph.getVertex(1);
		Assert.assertEquals("Node 1", v1.getProperty(LABEL));

		Vertex v2 = graph.getVertex(2);
		Assert.assertEquals("Node 2", v2.getProperty(LABEL));

		Vertex v3 = graph.getVertex(3);
		Assert.assertEquals("Node 3", v3.getProperty(LABEL));

		Iterable<Edge> out1 = v1.getOutEdges();
		Edge e1 = out1.iterator().next();
		Assert.assertEquals("Edge from node 1 to node 2", e1.getLabel());

		Iterable<Edge> out2 = v2.getOutEdges();
		Edge e2 = out2.iterator().next();
		Assert.assertEquals("Edge from node 2 to node 3", e2.getLabel());

		Iterable<Edge> out3 = v3.getOutEdges();
		Edge e3 = out3.iterator().next();
		Assert.assertEquals("Edge from node 3 to node 1", e3.getLabel());

	}

	@Test(expected = IOException.class)
	public void malformedThrowsIOException() throws IOException {
		GMLReader.inputGraph(new TinkerGraph(), GMLReader.class.getResourceAsStream("malformed.gml"));
	}

	@Test
	public void example2GMLTestingMapParsing() throws IOException {
		TinkerGraph graph = new TinkerGraph();

		GMLReader.inputGraph(graph, GMLReader.class.getResourceAsStream("example2.gml"));

		Assert.assertEquals(2, getIterableCount(graph.getVertices()));
		Assert.assertEquals(1, getIterableCount(graph.getEdges()));

		Object property = graph.getVertex(1).getProperty(GMLTokens.GRAPHICS);
		Assert.assertTrue(property instanceof Map<?, ?>);

		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) property;
		Assert.assertEquals(5, map.size());
		Assert.assertEquals(0.1, map.get("x"));
		// NB comes back as int
		Assert.assertEquals(0, map.get("y"));
		Assert.assertEquals(0.1, map.get("w"));
		Assert.assertEquals(0.1, map.get("h"));
		Assert.assertEquals("earth.gif", map.get("bitmap"));

	}

	private int getIterableCount(Iterable<?> elements) {
		int counter = 0;

		Iterator<?> iterator = elements.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			counter++;
		}

		return counter;
	}

}
