package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Victor Su
 */
public class TinkerMetadataWriterTest extends TestCase {

    public void testNormal() throws Exception {
        TinkerGraph g = TinkerGraphFactory.createTinkerGraph();
        createManualIndices(g);
        createKeyIndices(g);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TinkerMetadataWriter.save(g, bos);

        byte[] expected = streamToByteArray(TinkerMetadataWriterTest.class.getResourceAsStream("example-tinkergraph-metadata.dat"));
        byte[] actual = bos.toByteArray();

        assertEquals(expected.length, actual.length);

        for (int ix = 0; ix < actual.length; ix++) {
            assertEquals(expected[ix], actual[ix]);
        }
    }

    private void createKeyIndices(final TinkerGraph g) {
        g.createKeyIndex("name", Vertex.class);
        g.createKeyIndex("weight", Edge.class);
    }

    private void createManualIndices(final TinkerGraph g) {
        final Index<Vertex> idxAge = g.createIndex("age", Vertex.class);
        final Vertex v1 = g.getVertex(1);
        final Vertex v2 = g.getVertex(2);
        idxAge.put("age", v1.getProperty("age"), v1);
        idxAge.put("age", v2.getProperty("age"), v2);

        final Index<Edge> idxWeight = g.createIndex("weight", Edge.class);
        final Edge e7 = g.getEdge(7);
        final Edge e12 = g.getEdge(12);
        idxWeight.put("weight", e7.getProperty("weight"), e7);
        idxWeight.put("weight", e12.getProperty("weight"), e12);
    }

    private byte[] streamToByteArray(final InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }
}
