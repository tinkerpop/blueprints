package com.tinkerpop.blueprints.util.io.gml;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

import org.junit.Assert;
import org.junit.Test;
import sun.misc.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

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

        Iterable<Edge> out1 = v1.getEdges(Direction.OUT);
        Edge e1 = out1.iterator().next();
        Assert.assertEquals(v2, e1.getVertex(Direction.IN));

        Iterable<Edge> out2 = v2.getEdges(Direction.OUT);
        Edge e2 = out2.iterator().next();
        Assert.assertEquals(v3, e2.getVertex(Direction.IN));

        Iterable<Edge> out3 = v3.getEdges(Direction.OUT);
        Edge e3 = out3.iterator().next();
        Assert.assertEquals(v1, e3.getVertex(Direction.IN));

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

        Iterable<Edge> out1 = v1.getEdges(Direction.OUT);
        Edge e1 = out1.iterator().next();
        Assert.assertEquals("Edge from node 1 to node 2", e1.getLabel());

        Iterable<Edge> out2 = v2.getEdges(Direction.OUT);
        Edge e2 = out2.iterator().next();
        Assert.assertEquals("Edge from node 2 to node 3", e2.getLabel());

        Iterable<Edge> out3 = v3.getEdges(Direction.OUT);
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
        Assert.assertEquals(0.1f, map.get("x"));
        // NB comes back as int
        Assert.assertEquals(0, map.get("y"));
        Assert.assertEquals(0.1f, map.get("w"));
        Assert.assertEquals(0.1f, map.get("h"));
        Assert.assertEquals("earth.gif", map.get("bitmap"));

    }

    @Test
    public void testEscapeQuotation() throws Exception {
        TinkerGraph graph = new TinkerGraph();
        GMLReader.inputGraph(graph, GMLReader.class.getResourceAsStream("example.gml"));
        Vertex v3 = graph.getVertex(3);
        Object tempProperty = v3.getProperty("escape_property");
        Assert.assertNotNull(tempProperty);
        Assert.assertEquals("Node 3 \"with quote\"", tempProperty);
    }

    @Test
    public void testIdGenerationInGML() throws IOException {
        TinkerGraph graph1 = new TinkerGraph();
        GMLReader.inputGraph(graph1, GMLReader.class.getResourceAsStream("simple.gml"));

        Vertex toRemove = graph1.getVertex("123");
        graph1.removeVertex(toRemove);

        String file = "/tmp/simple-" + UUID.randomUUID() + ".gml";
        GMLWriter.outputGraph(graph1, file);

        TinkerGraph graph2 = new TinkerGraph();
        GMLReader.inputGraph(graph2, file);

        String gml = new String(Files.readAllBytes(Paths.get(file)));
        String sep = "\r\n";
        String expected = "graph [" + sep +
                          "\tnode [" + sep +
                          "\t\tid 1" + sep +
                          "\t\tblueprintsId \"456\"" + sep +
                          "\t]" + sep +
                          "]" + sep;
        Assert.assertEquals(expected, gml);
        Assert.assertEquals(1, getIterableCount(graph2.getVertices()));
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
