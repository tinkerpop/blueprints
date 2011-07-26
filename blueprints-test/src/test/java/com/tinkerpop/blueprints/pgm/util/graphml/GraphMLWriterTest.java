package com.tinkerpop.blueprints.pgm.util.graphml;

import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import junit.framework.TestCase;

import java.io.*;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class GraphMLWriterTest extends TestCase {
    public void testNormal() throws Exception {
        TinkerGraph g = new TinkerGraph();
        GraphMLReader.inputGraph(g, GraphMLReader.class.getResourceAsStream("graph-example-1.xml"));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GraphMLWriter w = new GraphMLWriter(g);
        w.setNormalize(true);
        w.outputGraph(bos);

        String expected = streamToString(GraphMLWriterTest.class.getResourceAsStream("graph-example-1-normalized.xml"));
        //System.out.println(expected);
        assertEquals(expected, bos.toString());
    }

    private String streamToString(final InputStream in) throws IOException {
        Writer writer = new StringWriter();

        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(
                    new InputStreamReader(in, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            in.close();
        }
        return writer.toString();
    }
}
