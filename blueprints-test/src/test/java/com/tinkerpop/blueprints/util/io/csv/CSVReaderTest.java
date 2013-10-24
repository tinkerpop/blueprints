package com.tinkerpop.blueprints.util.io.csv;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.IOException;

public class CSVReaderTest extends TestCase {

    public void testGetsCorrectNumberOfElements() throws IOException {
        Graph graph = new TinkerGraph();

        CSVReader r = new CSVReader(graph);
        r.setVertexIdKey("_id");
        r.setEdgeIdKey("_id");
        r.setEdgeSourceKey("_sourceV");
        r.setEdgeTargetKey("_targetV");
        r.setEdgeLabelKey("_label");
        r.inputVertices(CSVReaderTest.class.getResourceAsStream("example-vertices-1.csv"));
        r.inputEdges(CSVReaderTest.class.getResourceAsStream("example-edges-1.csv"));

        Assert.assertEquals(6, getIterableCount(graph.getVertices()));
        Assert.assertEquals(6, getIterableCount(graph.getEdges()));
    }

    public void testGetsCorrectProperties() throws IOException {
        Graph graph = new TinkerGraph();

        CSVReader r = new CSVReader(graph);
        r.setVertexIdKey("_id");
        r.setEdgeIdKey("_id");
        r.setEdgeSourceKey("_sourceV");
        r.setEdgeTargetKey("_targetV");
        r.setEdgeLabelKey("_label");
        r.inputVertices(CSVReaderTest.class.getResourceAsStream("example-vertices-1.csv"));
        r.inputEdges(CSVReaderTest.class.getResourceAsStream("example-edges-1.csv"));


        Vertex v1 = graph.getVertex(1);
        Assert.assertEquals("29", v1.getProperty("age"));
        Assert.assertEquals(null, v1.getProperty("lang"));
        Assert.assertEquals("marko", v1.getProperty("name"));

        Vertex v2 = graph.getVertex(2);
        Assert.assertEquals("27", v2.getProperty("age"));
        Assert.assertEquals(null, v2.getProperty("lang"));
        Assert.assertEquals("vadas", v2.getProperty("name"));

        Vertex v3 = graph.getVertex(3);
        Assert.assertEquals(null, v3.getProperty("age"));
        Assert.assertEquals("java", v3.getProperty("lang"));
        Assert.assertEquals("lop", v3.getProperty("name"));

        Vertex v4 = graph.getVertex(4);
        Assert.assertEquals("32", v4.getProperty("age"));
        Assert.assertEquals(null, v4.getProperty("lang"));
        Assert.assertEquals("josh", v4.getProperty("name"));

        Vertex v5 = graph.getVertex(5);
        Assert.assertEquals(null, v5.getProperty("age"));
        Assert.assertEquals("java", v5.getProperty("lang"));
        Assert.assertEquals("ripple", v5.getProperty("name"));

        Vertex v6 = graph.getVertex(6);
        Assert.assertEquals("35", v6.getProperty("age"));
        Assert.assertEquals(null, v6.getProperty("lang"));
        Assert.assertEquals("peter", v6.getProperty("name"));

        Edge e10 = graph.getEdge(10);
        Assert.assertEquals(v4, e10.getVertex(Direction.OUT));
        Assert.assertEquals(v5, e10.getVertex(Direction.IN));
        Assert.assertEquals("created", e10.getLabel());
        Assert.assertEquals("1.0", e10.getProperty("weight"));

        Edge e11 = graph.getEdge(11);
        Assert.assertEquals(v4, e11.getVertex(Direction.OUT));
        Assert.assertEquals(v3, e11.getVertex(Direction.IN));
        Assert.assertEquals("created", e11.getLabel());
        Assert.assertEquals("0.4", e11.getProperty("weight"));

        Edge e12 = graph.getEdge(12);
        Assert.assertEquals(v6, e12.getVertex(Direction.OUT));
        Assert.assertEquals(v3, e12.getVertex(Direction.IN));
        Assert.assertEquals("created", e12.getLabel());
        Assert.assertEquals("0.2", e12.getProperty("weight"));

        Edge e7 = graph.getEdge(7);
        Assert.assertEquals(v1, e7.getVertex(Direction.OUT));
        Assert.assertEquals(v2, e7.getVertex(Direction.IN));
        Assert.assertEquals("knows", e7.getLabel());
        Assert.assertEquals("0.5", e7.getProperty("weight"));

        Edge e8 = graph.getEdge(8);
        Assert.assertEquals(v1, e8.getVertex(Direction.OUT));
        Assert.assertEquals(v4, e8.getVertex(Direction.IN));
        Assert.assertEquals("knows", e8.getLabel());
        Assert.assertEquals("1.0", e8.getProperty("weight"));

        Edge e9 = graph.getEdge(9);
        Assert.assertEquals(v1, e9.getVertex(Direction.OUT));
        Assert.assertEquals(v3, e9.getVertex(Direction.IN));
        Assert.assertEquals("created", e9.getLabel());
        Assert.assertEquals("0.4", e9.getProperty("weight"));
    }

    private int getIterableCount(Iterable<?> elements) {
        int counter = 0;

        for(Object element: elements) {
            counter++;
        }

        return counter;
    }
}
