package com.tinkerpop.blueprints.util.io.graphml;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

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
        assertEquals(expected.replace("\n", "").replace("\r", ""), bos.toString().replace("\n", "").replace("\r", ""));
    }

    public void testWithEdgeLabel() throws Exception {
        TinkerGraph g = new TinkerGraph();
        GraphMLReader.inputGraph(g, GraphMLReader.class.getResourceAsStream("graph-example-1.xml"));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GraphMLWriter w = new GraphMLWriter(g);
        w.setEdgeLabelKey("label");
        w.setNormalize(true);
        w.outputGraph(bos);

        String expected = streamToString(GraphMLWriterTest.class.getResourceAsStream("graph-example-1-schema-valid.xml"));
        //System.out.println(expected);
        assertEquals(expected.replace("\n", "").replace("\r", ""), bos.toString().replace("\n", "").replace("\r", ""));
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

    // Note: this is only a very lightweight test of writer/reader encoding.
    // It is known that there are characters which, when written by GraphMLWriter,
    // cause parse errors for GraphMLReader.
    // However, this happens uncommonly enough that is not yet known which characters those are.
    public void testEncoding() throws Exception {

        Graph g = new TinkerGraph();
        Vertex v = g.addVertex(1);
        v.setProperty("text", "\u00E9");

        GraphMLWriter w = new GraphMLWriter(g);

        File f = File.createTempFile("test", "txt");
        OutputStream out = new FileOutputStream(f);
        w.outputGraph(out);
        out.close();

        Graph g2 = new TinkerGraph();
        GraphMLReader r = new GraphMLReader(g2);

        InputStream in = new FileInputStream(f);
        r.inputGraph(in);
        in.close();

        Vertex v2 = g2.getVertex(1);
        assertEquals("\u00E9", v2.getProperty("text"));
    }

    public void testStreamStaysOpen() throws IOException {
        final Graph g = TinkerGraphFactory.createTinkerGraph();
        final PrintStream oldStream = System.out;
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        final GraphMLWriter writer = new GraphMLWriter(g);
        writer.outputGraph(System.out);
        System.out.println("working");
        System.setOut(oldStream);

        assertTrue(outContent.toString().endsWith("working\n"));
    }
}
