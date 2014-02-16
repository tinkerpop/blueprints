package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.util.io.gml.GMLReader;
import com.tinkerpop.blueprints.util.io.gml.GMLWriter;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONMode;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReader;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Constructs TinkerFile instances to load and save TinkerGraph instances.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
class TinkerStorageFactory {
    private static TinkerStorageFactory factory;

    private TinkerStorageFactory() {
    }

    public static TinkerStorageFactory getInstance() {
        if (factory == null) {
            factory = new TinkerStorageFactory();
        }

        return factory;
    }

    public TinkerStorage getTinkerStorage(final TinkerGraph.FileType fileType) {
        switch (fileType) {
            case GML:
                return new GMLTinkerStorage();
            case GRAPHML:
                return new GraphMLTinkerStorage();
            case GRAPHSON:
                return new GraphSONTinkerStorage();
            case JAVA:
                return new JavaTinkerStorage();
        }

        throw new RuntimeException(String.format("File Type [%s] is not configurable by the factory", fileType));
    }

    /**
     * Base class for loading and saving a TinkerGraph.
     */
    abstract class AbstractTinkerStorage implements TinkerStorage {

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
    abstract class AbstractSeparateTinkerStorage extends AbstractTinkerStorage {
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
            final File dir = new File(directory);
            if (!dir.exists()) {
                throw new RuntimeException("Directory " + directory + " does not exist");
            }

            final TinkerGraph graph = new TinkerGraph();
            loadGraphData(graph, directory);

            final File file = new File(directory + GRAPH_FILE_METADATA);
            if (file.exists()) {
                TinkerMetadataReader.load(graph, new FileInputStream(directory + GRAPH_FILE_METADATA));
            }

            return graph;
        }

        @Override
        public void save(final TinkerGraph graph, final String directory) throws IOException {
            final File dir = new File(directory);
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    throw new RuntimeException("Could not create directory " + directory);
                }
            }

            saveGraphData(graph, directory);
            deleteFile(directory + GRAPH_FILE_METADATA);

            final OutputStream os = new FileOutputStream(directory + GRAPH_FILE_METADATA);
            try {
                TinkerMetadataWriter.save(graph, os);
            } catch (IOException ioe) {
                throw ioe;
            } finally {
                os.close();
            }
        }
    }

    /**
     * Reads and writes a TinkerGraph to GML as the format for the data.
     */
    class GMLTinkerStorage extends AbstractSeparateTinkerStorage {
        private static final String GRAPH_FILE_GML = "/tinkergraph.gml";

        @Override
        public void loadGraphData(final TinkerGraph graph, final String directory) throws IOException {
            GMLReader.inputGraph(graph, new FileInputStream(directory + GRAPH_FILE_GML));
        }

        @Override
        public void saveGraphData(final TinkerGraph graph, final String directory) throws IOException {
            deleteFile(directory + GRAPH_FILE_GML);

            final OutputStream os = new FileOutputStream(directory + GRAPH_FILE_GML);
            try {
                GMLWriter.outputGraph(graph, os);
            } catch (IOException ioe) {
                throw ioe;
            } finally {
                os.close();
            }
        }
    }

    /**
     * Reads and writes a TinkerGraph to GraphSON as the format for the data.
     */
    class GraphSONTinkerStorage extends AbstractSeparateTinkerStorage {
        private static final String GRAPH_FILE_GRAPHSON = "/tinkergraph.json";

        @Override
        public void loadGraphData(final TinkerGraph graph, final String directory) throws IOException {
            GraphSONReader.inputGraph(graph, new FileInputStream(directory + GRAPH_FILE_GRAPHSON));
        }

        @Override
        public void saveGraphData(final TinkerGraph graph, final String directory) throws IOException {
            deleteFile(directory + GRAPH_FILE_GRAPHSON);
            final OutputStream os = new FileOutputStream(directory + GRAPH_FILE_GRAPHSON);

            try {
                GraphSONWriter.outputGraph(graph, os, GraphSONMode.EXTENDED);
            } catch (IOException ioe) {
                throw ioe;
            } finally {
                os.close();
            }
        }
    }

    /**
     * Reads and writes a TinkerGraph to GraphML as the format for the data.
     */
    class GraphMLTinkerStorage extends AbstractSeparateTinkerStorage {
        private static final String GRAPH_FILE_GRAPHML = "/tinkergraph.xml";

        @Override
        public void loadGraphData(final TinkerGraph graph, final String directory) throws IOException {
            GraphMLReader.inputGraph(graph, new FileInputStream(directory + GRAPH_FILE_GRAPHML));
        }

        @Override
        public void saveGraphData(final TinkerGraph graph, final String directory) throws IOException {
            deleteFile(directory + GRAPH_FILE_GRAPHML);
            final OutputStream os = new FileOutputStream(directory + GRAPH_FILE_GRAPHML);

            try {
                GraphMLWriter.outputGraph(graph, os);
            } catch (IOException ioe) {
                throw ioe;
            } finally {
                os.close();
            }
        }
    }

    /**
     * Reads and writes a TinkerGraph using java object serialization.
     */
    class JavaTinkerStorage extends AbstractTinkerStorage {
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
            try {
                out.writeObject(graph);
            } catch (IOException ioe) {
                throw ioe;
            } finally {
                out.close();
            }
        }
    }
}
