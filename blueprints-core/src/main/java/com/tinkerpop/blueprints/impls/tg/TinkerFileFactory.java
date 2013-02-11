package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Constructs TinkerFile instances to load and save TinkerGraph instances.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
class TinkerFileFactory {
    private static TinkerFileFactory factory;

    private TinkerFileFactory() {
    }

    public static TinkerFileFactory getInstance(final TinkerGraph.FileType fileType) {
        if (factory == null) {
            factory = new TinkerFileFactory();
        }

        return factory;
    }

    public TinkerFile getTinkerFile(final TinkerGraph.FileType fileType) {
        switch (fileType) {
            case GML:
                return null;
            case GRAPHML:
                return new GraphMLTinkerFile();
            case GRAPHSON:
                return null;
            case JAVA:
                return new JavaTinkerFile();
        }

        throw new RuntimeException(String.format("File Type [%s] is not configurable by the factory", fileType));
    }

    /**
     * Base class for loading and saving a TinkerGraph.
     */
    abstract class AbstractTinkerFile implements TinkerFile {

        /**
         * Clean up the directory that houses the TinkerGraph.
         */
        protected void deleteFile(String path) throws IOException {
            final File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * Base class for loading and saving a TinkerGraph where the implementation separates the data from the
     * meta data stored in the TinkerGraph.
     */
    abstract class AbstractSeparateTinkerFile extends AbstractTinkerFile {
        protected static final String GRAPH_FILE_METADATA = "/tinkergraph-metadata.dat";

        /**
         * Save the data of the graph with the specific file format of the implementation.
         */
        public abstract void saveGraphData(final TinkerGraph graph, final String directory) throws IOException;

        /**
         * Load the data from the graph with the specific file format of the implementation.
         */
        public abstract void loadGraphData(final TinkerGraph graph, final String directory) throws IOException;

        @Override
        public TinkerGraph load(final String directory) throws IOException {
            final TinkerGraph graph = new TinkerGraph();
            loadGraphData(graph, directory);
            TinkerMetadataReader.inputGraph(graph, new FileInputStream(directory + GRAPH_FILE_METADATA));
            return graph;
        }

        @Override
        public void save(final TinkerGraph graph, final String directory) throws IOException {
            saveGraphData(graph, directory);
            deleteFile(directory + GRAPH_FILE_METADATA);
            TinkerMetadataWriter.outputGraph(graph, new FileOutputStream(directory + GRAPH_FILE_METADATA));
        }
    }

    /**
     * Reads and writes a TinkerGraph to with GraphML as the format for the data.
     */
    class GraphMLTinkerFile extends AbstractSeparateTinkerFile {
        private static final String GRAPH_FILE_GRAPHML = "/tinkergraph.xml";

        @Override
        public void loadGraphData(final TinkerGraph graph, final String directory) throws IOException {
            GraphMLReader.inputGraph(graph, new FileInputStream(directory + GRAPH_FILE_GRAPHML));
        }

        @Override
        public void saveGraphData(final TinkerGraph graph, final String directory) throws IOException {
            deleteFile(directory + GRAPH_FILE_GRAPHML);
            GraphMLWriter.outputGraph(graph, new FileOutputStream(directory + GRAPH_FILE_GRAPHML));
        }
    }

    /**
     * Reads and writes a TinkerGraph using java object serialization.
     */
    class JavaTinkerFile extends AbstractTinkerFile {
        private static final String GRAPH_FILE_JAVA = "/tinkergraph.dat";

        @Override
        public TinkerGraph load(final String directory) throws IOException {
            final ObjectInputStream input = new ObjectInputStream(new FileInputStream(directory + GRAPH_FILE_JAVA));

            try {
                return (TinkerGraph) input.readObject();
            } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException(cnfe);
            } finally {
                input.close();
            }
        }

        @Override
        public void save(final TinkerGraph graph, final String directory) throws IOException {
            deleteFile(directory + GRAPH_FILE_JAVA);
            final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(directory + GRAPH_FILE_JAVA));
            out.writeObject(this);
            out.close();
        }
    }
}
