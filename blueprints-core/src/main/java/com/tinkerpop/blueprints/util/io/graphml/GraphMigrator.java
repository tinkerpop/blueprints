package com.tinkerpop.blueprints.util.io.graphml;

import com.tinkerpop.blueprints.Graph;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * GraphMigrator takes the data in one graph and pipes it to another graph.
 *
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 */
public class GraphMigrator {

    /**
     * Pipe the data from one graph to another graph.
     *
     * @param fromGraph the graph to take data from
     * @param toGraph   the graph to take data to
     * @throws XMLStreamException thrown if the serialization process causes an exception
     * @throws IOException        thrown if there is an error in steam between the two graphs
     */
    public static void migrateGraph(final Graph fromGraph, final Graph toGraph) throws XMLStreamException, IOException {

        final PipedInputStream inPipe = new PipedInputStream() {
            // Default is 1024
            protected static final int PIPE_SIZE = 1024;
        };

        final PipedOutputStream outPipe = new PipedOutputStream(inPipe) {
            public void close() throws IOException {
                while (inPipe.available() > 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                super.close();
            }
        };

        new Thread(new Runnable() {
            public void run() {
                try {
                    GraphMLWriter.outputGraph(fromGraph, outPipe);
                    outPipe.flush();
                    outPipe.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        GraphMLReader.inputGraph(toGraph, inPipe);
    }
}