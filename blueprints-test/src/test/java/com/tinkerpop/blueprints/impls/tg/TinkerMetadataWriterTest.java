package com.tinkerpop.blueprints.impls.tg;

import junit.framework.TestCase;

/**
 * @author Victor Su
 */
public class TinkerMetadataWriterTest extends TestCase {

    public void testTrue() {

    }

    /*public void testNormal() throws Exception {
        TinkerGraph g = TinkerGraphFactory.createTinkerGraph();
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
        g.createIndex("name", Vertex.class);
        g.createIndex("weight", Edge.class);
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
    }*/
}
