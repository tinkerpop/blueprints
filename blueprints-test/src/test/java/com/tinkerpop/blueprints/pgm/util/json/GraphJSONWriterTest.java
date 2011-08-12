package com.tinkerpop.blueprints.pgm.util.json;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

public class GraphJSONWriterTest {

    @Test
    public void outputGraphValid() throws JSONException {
        Graph g = TinkerGraphFactory.createTinkerGraph();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        GraphJSONWriter writer = new GraphJSONWriter(g);
        writer.outputGraph(stream, null, null, true);

        System.out.println(new String(stream.toByteArray()));
    }
}
