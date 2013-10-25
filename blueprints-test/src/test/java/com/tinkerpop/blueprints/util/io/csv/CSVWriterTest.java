package com.tinkerpop.blueprints.util.io.csv;

import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CSVWriterTest extends TestCase {

    public void testNormalCommas() throws IOException {
        TinkerGraph g = new TinkerGraph();

        CSVReader r = new CSVReader(g);
        r.setVertexIdKey("id");
        r.setEdgeIdKey("id");
        r.setEdgeSourceKey("source");
        r.setEdgeTargetKey("target");
        r.setEdgeLabelKey("label");
        r.inputGraph(
                CSVWriterTest.class.getResourceAsStream("example-vertices-1.csv"),
                CSVWriterTest.class.getResourceAsStream("example-edges-1.csv"));

        ByteArrayOutputStream bosVertices = new ByteArrayOutputStream();
        ByteArrayOutputStream bosEdges = new ByteArrayOutputStream();

        CSVWriter w = new CSVWriter(g);
        w.setVertexIdKey("id");
        w.setEdgeIdKey("id");
        w.setEdgeSourceKey("source");
        w.setEdgeTargetKey("target");
        w.setEdgeLabelKey("label");
        w.setNormalize(true);
        w.outputVertices(bosVertices);
        w.outputEdges(bosEdges);

        String actualVertices = bosVertices.toString();
        String expectedVertices = streamToByteArray(CSVWriterTest.class.getResourceAsStream("writer-vertices-1.csv"));

        assertEquals(expectedVertices, actualVertices);

        String actualEdges = bosEdges.toString();
        String expectedEdges = streamToByteArray(CSVWriterTest.class.getResourceAsStream("writer-edges-1.csv"));

        assertEquals(expectedEdges, actualEdges);
    }

    public void testNormalTabs() throws IOException {
        TinkerGraph g = new TinkerGraph();

        CSVReader r = new CSVReader(g, '\t');
        r.setVertexIdKey("id");
        r.setEdgeIdKey("id");
        r.setEdgeSourceKey("source");
        r.setEdgeTargetKey("target");
        r.setEdgeLabelKey("label");
        r.inputGraph(
                CSVWriterTest.class.getResourceAsStream("example-vertices-1.tsv"),
                CSVWriterTest.class.getResourceAsStream("example-edges-1.tsv"));

        ByteArrayOutputStream bosVertices = new ByteArrayOutputStream();
        ByteArrayOutputStream bosEdges = new ByteArrayOutputStream();

        CSVWriter w = new CSVWriter(g, '\t');
        w.setVertexIdKey("id");
        w.setEdgeIdKey("id");
        w.setEdgeSourceKey("source");
        w.setEdgeTargetKey("target");
        w.setEdgeLabelKey("label");
        w.setNormalize(true);
        w.outputVertices(bosVertices);
        w.outputEdges(bosEdges);

        String actualVertices = bosVertices.toString();
        String expectedVertices = streamToByteArray(CSVWriterTest.class.getResourceAsStream("writer-vertices-1.tsv"));

        assertEquals(expectedVertices, actualVertices);

        String actualEdges = bosEdges.toString();
        String expectedEdges = streamToByteArray(CSVWriterTest.class.getResourceAsStream("writer-edges-1.tsv"));

        assertEquals(expectedEdges, actualEdges);
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
}
