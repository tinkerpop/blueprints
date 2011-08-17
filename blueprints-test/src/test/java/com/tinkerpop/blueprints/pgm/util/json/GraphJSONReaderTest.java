package com.tinkerpop.blueprints.pgm.util.json;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jettison.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GraphJSONReaderTest {

    @Test
    public void inputGraphValid() throws IOException, JsonParseException {
        TinkerGraph graph = new TinkerGraph();

        String json = "{ \"vertices\": [ {\"_id\":1, \"test\":\"please work\", \"testlist\":[1, 2, 3], \"testmap\":{\"big\":10000000000, \"small\":0.4954959595959}}, {\"_id\":2, \"testagain\":\"please work again\"}], \"edges\":[{\"_id\":100, \"_outV\":1, \"_inV\":2, \"_label\":\"works\", \"teste\":\"please worke\"}]}";

        byte[] bytes = json.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);

        GraphJSONReader.inputGraph(graph, inputStream);

        Assert.assertEquals(2, getIterableCount(graph.getVertices()));
        Assert.assertEquals(1, getIterableCount(graph.getEdges()));

        Vertex v1 = graph.getVertex(1);
        Assert.assertNotNull(v1);
        Assert.assertEquals("please work", v1.getProperty("test"));

        Map map = (Map) v1.getProperty("testmap");
        Assert.assertNotNull(map);
        Assert.assertEquals(10000000000l, Long.parseLong(map.get("big").toString()));
        Assert.assertEquals(0.4954959595959, Double.parseDouble(map.get("small").toString()), 0);

        List list = (List) v1.getProperty("testlist");
        Assert.assertEquals(3, list.size());

        Vertex v2 = graph.getVertex(2);
        Assert.assertNotNull(v2);
        Assert.assertEquals("please work again", v2.getProperty("testagain"));

        Edge e = graph.getEdge(100);
        Assert.assertNotNull(e);
        Assert.assertEquals("works", e.getLabel());
        Assert.assertEquals(v1, e.getOutVertex());
        Assert.assertEquals(v2, e.getInVertex());
        Assert.assertEquals("please worke", e.getProperty("teste"));

    }

    @Test
    public void inputGraphFullCycle() throws IOException, JsonParseException, JSONException {
        TinkerGraph graph = TinkerGraphFactory.createTinkerGraph();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        GraphJSONWriter writer = new GraphJSONWriter(graph);
        writer.outputGraph(stream, null, null);

        stream.flush();
        stream.close();

        String jsonString = new String(stream.toByteArray());

        byte[] bytes = jsonString.getBytes();
        InputStream inputStream = new ByteArrayInputStream(bytes);

        TinkerGraph emptyGraph = new TinkerGraph();
        GraphJSONReader.inputGraph(emptyGraph, inputStream);

        Assert.assertEquals(6, getIterableCount(emptyGraph.getVertices()));
        Assert.assertEquals(6, getIterableCount(emptyGraph.getEdges()));

        for (Vertex v : graph.getVertices()) {
            Vertex found = emptyGraph.getVertex(v.getId());

            Assert.assertNotNull(v);

            for (String key : found.getPropertyKeys()) {
                Assert.assertEquals(v.getProperty(key), found.getProperty(key));
            }
        }

        for (Edge e : graph.getEdges()) {
            Edge found = emptyGraph.getEdge(e.getId());

            Assert.assertNotNull(e);
        }

    }

    private int getIterableCount(Iterable elements) {
        int counter = 0;

        Iterator iterator = elements.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            counter++;
        }

        return counter;
    }
}
