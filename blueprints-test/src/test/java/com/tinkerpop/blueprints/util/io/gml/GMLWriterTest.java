package com.tinkerpop.blueprints.util.io.gml;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;

public class GMLWriterTest extends TestCase {

    public void testNormal() throws Exception {
        TinkerGraph g = new TinkerGraph();
        GMLReader.inputGraph(g, GMLReaderTest.class.getResourceAsStream("example.gml"));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GMLWriter w = new GMLWriter(g);
        w.setNormalize(true);
        w.outputGraph(bos);

        String actual = bos.toString();
        String expected = streamToByteArray(GMLWriterTest.class.getResourceAsStream("writer.gml"));

        // ignore carriage return character...not really relevant to the test
        assertEquals(expected.replace("\r", ""), actual.replace("\r", ""));

    }

    public void testUseIds() throws Exception {
        TinkerGraph g = new TinkerGraph();
        GMLReader.inputGraph(g, GMLReaderTest.class.getResourceAsStream("example.gml"));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GMLWriter w = new GMLWriter(g);
        w.setNormalize(true);
        w.setUseId(true);
        w.outputGraph(bos);

        String actual = bos.toString();
        String expected = streamToByteArray(GMLWriterTest.class.getResourceAsStream("writer2.gml"));

        // ignore carriage return character...not really relevant to the test
        assertEquals(expected.replace("\r", ""), actual.replace("\r", ""));

    }

    public void testRoundTrip() throws Exception {
        TinkerGraph g1 = TinkerGraphFactory.createTinkerGraph();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GMLWriter w = new GMLWriter(g1);
        w.setUseId(true);
        w.outputGraph(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        Graph g2 = new TinkerGraph();
        GMLReader.inputGraph(g2, bis);

        assertEquals(getIterableCount(g1.getVertices()), getIterableCount(g2.getVertices()));
        assertEquals(getIterableCount(g1.getEdges()), getIterableCount(g2.getEdges()));

    }

    public void testRoundTripIgnoreBadProperties() throws Exception {
        TinkerGraph g1 = TinkerGraphFactory.createTinkerGraph();
        Vertex v = g1.getVertex(1);
        v.setProperty("bad_property", "underscore");
        v.setProperty("bad property", "space");
        v.setProperty("bad-property", "dash");
        v.setProperty("bad$property", "other stuff");
        v.setProperty("badproperty_", "but don't get too fancy");
        v.setProperty("_badproperty", "or it won't work");
        v.setProperty("55", "numbers alone are bad");
        v.setProperty("5badproperty", "must start with alpha");
        v.setProperty("badpropertyajflalfjsajfdfjdkfjsdiahfshfksajdhfkjdhfkjhaskdfhaksdhfsalkjdfhkjdhkahsdfkjasdhfkajfdhkajfhkadhfkjsdafhkajfdhasdkfhakfdhakjsdfhkadhfakjfhaksdjhfkajfhakhfaksfdhkahdfkahfkajsdhfkjahdfkahsdfkjahfkhakfsdjhakjksfhakfhaksdhfkadhfakhfdkasfdhiuerfaeafdkjhakfdhfdadfasdfsdafadf", "super long keys won't work");
        v.setProperty("good5property", "numbers are cool");
        v.setProperty("goodproperty5", "numbers are cool");
        v.setProperty("a", "one letter is ok");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GMLWriter w = new GMLWriter(g1);
        w.setStrict(true);
        w.setUseId(true);
        w.outputGraph(bos);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        Graph g2 = new TinkerGraph();
        GMLReader.inputGraph(g2, bis);

        assertEquals(getIterableCount(g1.getVertices()), getIterableCount(g2.getVertices()));
        assertEquals(getIterableCount(g1.getEdges()), getIterableCount(g2.getEdges()));

        Vertex v1 = g2.getVertex(1);
        Assert.assertNull(v1.getProperty("bad_property"));
        Assert.assertNull(v1.getProperty("bad property"));
        Assert.assertNull(v1.getProperty("bad-property"));
        Assert.assertNull(v1.getProperty("bad$property"));
        Assert.assertNull(v1.getProperty("_badproperty"));
        Assert.assertNull(v1.getProperty("badproperty_"));
        Assert.assertNull(v1.getProperty("5badproperty"));
        Assert.assertNull(v1.getProperty("55"));
        Assert.assertNull(v1.getProperty("badpropertyajflalfjsajfdfjdkfjsdiahfshfksajdhfkjdhfkjhaskdfhaksdhfsalkjdfhkjdhkahsdfkjasdhfkajfdhkajfhkadhfkjsdafhkajfdhasdkfhakfdhakjsdfhkadhfakjfhaksdjhfkajfhakhfaksfdhkahdfkahfkajsdhfkjahdfkahsdfkjahfkhakfsdjhakjksfhakfhaksdhfkadhfakhfdkasfdhiuerfaeafdkjhakfdhfdadfasdfsdafadf"));
        Assert.assertEquals("numbers are cool", v1.getProperty("good5property"));
        Assert.assertEquals("numbers are cool", v1.getProperty("goodproperty5"));
        Assert.assertEquals("one letter is ok", v1.getProperty("a"));

    }

    private String streamToByteArray(final InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            int nRead;
            byte[] data = new byte[1024];

            while ((nRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
        } finally {
            buffer.close();
        }
        return buffer.toString("ISO-8859-1");
    }

    // Note: this is only a very lightweight test of writer/reader encoding.
    // It is known that there are characters which, when written by GMLWriter,
    // cause parse errors for GraphMLReader.
    // However, this happens uncommonly enough that is not yet known which characters those are.
    public void testEncoding() throws Exception {

        Graph g = new TinkerGraph();
        Vertex v = g.addVertex(1);
        v.setProperty("text", "\u00E9");

        GMLWriter w = new GMLWriter(g);

        File f = File.createTempFile("test", "txt");
        f.deleteOnExit();
        OutputStream out = new FileOutputStream(f);
        w.outputGraph(out);
        out.close();

        Graph g2 = new TinkerGraph();
        GMLReader r = new GMLReader(g2);

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

        final GMLWriter writer = new GMLWriter(g);
        writer.outputGraph(System.out);
        System.out.println("working");
        System.setOut(oldStream);

        assertTrue(outContent.toString().endsWith("working\n"));
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
