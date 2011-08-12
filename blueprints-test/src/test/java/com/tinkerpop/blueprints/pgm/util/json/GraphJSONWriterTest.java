package com.tinkerpop.blueprints.pgm.util.json;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jettison.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GraphJSONWriterTest {

    @Test
    public void outputGraphValid() throws JSONException, IOException {
        Graph g = TinkerGraphFactory.createTinkerGraph();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        GraphJSONWriter writer = new GraphJSONWriter(g);
        writer.outputGraph(stream, null, null, true);

        stream.flush();
        stream.close();

        String jsonString = new String(stream.toByteArray());

        ObjectMapper m = new ObjectMapper();
        JsonNode rootNode = m.readValue(jsonString, JsonNode.class);

        // ensure that the JSON conforms to basic structure and that the right
        // number of graph elements are present.  other tests already cover element formatting
        Assert.assertNotNull(rootNode);
        Assert.assertTrue(rootNode.has(JSONTokens.VERTICES));

        ArrayNode vertices = (ArrayNode) rootNode.get(JSONTokens.VERTICES);
        Assert.assertEquals(6, vertices.size());

        Assert.assertTrue(rootNode.has(JSONTokens.EDGES));

        ArrayNode edges = (ArrayNode) rootNode.get(JSONTokens.EDGES);
        Assert.assertEquals(6, edges.size());
    }
}
