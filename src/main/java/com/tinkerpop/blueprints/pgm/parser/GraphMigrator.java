package com.tinkerpop.blueprints.pgm.parser;

import com.tinkerpop.blueprints.pgm.Graph;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class GraphMigrator {

    public static void migrateGraph(final Graph fromGraph, final Graph toGraph) throws XMLStreamException, IOException {

        final PipedInputStream inPipe = new PipedInputStream() {
            // Default is 1024
            protected static final int PIPE_SIZE = 1024;
        };

        final PipedOutputStream outPipe = new PipedOutputStream(inPipe) {
            @Override
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
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        GraphMLReader.inputGraph(toGraph, inPipe);
    }
}