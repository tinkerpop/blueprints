package com.tinkerpop.blueprints.pgm.util.io.gml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import junit.framework.TestCase;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;

public class GMLWriterTest extends TestCase {

	public void testNormal() throws Exception {
		TinkerGraph g = new TinkerGraph();
		GMLReader.inputGraph(g, GMLReader.class.getResourceAsStream("example.gml"));

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GMLWriter w = new GMLWriter(g);
		w.setNormalize(true);
		w.outputGraph(bos);

		byte[] expected = streamToByteArray(GMLWriterTest.class.getResourceAsStream("writer.gml"));
		assertEquals(expected, bos.toByteArray());
	}

	public void testRoundTrip() throws Exception {
		TinkerGraph g1 = TinkerGraphFactory.createTinkerGraph();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GMLWriter w = new GMLWriter(g1);
		w.outputGraph(bos);

		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

		Graph g2 = new TinkerGraph();
		GMLReader.inputGraph(g2, bis);

		assertEquals(getIterableCount(g1.getVertices()), getIterableCount(g2.getVertices()));
		assertEquals(getIterableCount(g1.getEdges()), getIterableCount(g2.getEdges()));

		// TODO: Add further tests to check topology of graph is preserved
	}

	private byte[] streamToByteArray(final InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = in.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();
		} finally {
			buffer.close();
		}
		return buffer.toByteArray();
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
