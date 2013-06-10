package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Reads TinkerGraph metadata from an InputStream.
 *
 * @author Victor Su
 */
class TinkerMetadataReader {
    private final TinkerGraph graph;

    /**
     * @param graph the graph to populate with the TinkerGraph metadata
     */
    public TinkerMetadataReader(TinkerGraph graph) {
        this.graph = graph;
    }

    /**
     * Read TinkerGraph metadata from a file.
     *
     * @param filename the name of the file to read the TinkerGraph metadata from
     * @throws IOException thrown if there is an error reading the TinkerGraph metadata
     */
    public void load(final String filename) throws IOException {
        final FileInputStream fos = new FileInputStream(filename);
        load(fos);
        fos.close();
    }

    /**
     * Read TinkerGraph metadata from an InputStream.
     *
     * @param inputStream the InStream to read the TinkerGraph metadata from
     * @throws IOException thrown if there is an error reading the TinkerGraph metadata
     */
    public void load(final InputStream inputStream) throws IOException {
        DataInputStream reader = null;
        try {
            reader = new DataInputStream(inputStream);
            this.graph.currentId = reader.readLong();
            readIndices(reader, this.graph);
            readVertexKeyIndices(reader, this.graph);
            readEdgeKeyIndices(reader, this.graph);
        } catch (IOException e) {
            throw new RuntimeException("Could not read metadata file");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not read metadata file");
            }
        }
    }

    /**
     * Read TinkerGraph metadata from an InputStream.
     *
     * @param graph       the TinkerGraph to push the metadata to
     * @param inputStream the InputStream to read the TinkerGraph metadata from
     * @throws IOException thrown if there is an error reading the TinkerGraph metadata
     */
    public static void load(final TinkerGraph graph, final InputStream inputStream) throws IOException {
        TinkerMetadataReader reader = new TinkerMetadataReader(graph);
        reader.load(inputStream);
    }

    /**
     * Read TinkerGraph metadata from a file.
     *
     * @param graph    the TinkerGraph to push the data to
     * @param filename the name of the file to read the TinkerGraph metadata from
     * @throws IOException thrown if there is an error reading the TinkerGraph metadata
     */
    public static void load(final TinkerGraph graph, final String filename) throws IOException {
        TinkerMetadataReader reader = new TinkerMetadataReader(graph);
        reader.load(filename);
    }

    private void readIndices(final DataInputStream reader, final TinkerGraph graph) throws IOException {
        // Read the number of indices
        int indexCount = reader.readInt();

        for (int i = 0; i < indexCount; i++) {
            // Read the index name
            String indexName = reader.readUTF();

            // Read the index type
            byte indexType = reader.readByte();

            if (indexType != 1 && indexType != 2) {
                throw new RuntimeException("Unknown index class type");
            }

            TinkerIndex tinkerIndex = new TinkerIndex(indexName, indexType == 1 ? Vertex.class : Edge.class);

            // Read the number of items associated with this index name
            int indexItemCount = reader.readInt();
            for (int j = 0; j < indexItemCount; j++) {
                // Read the item key
                String indexItemKey = reader.readUTF();

                // Read the number of sub-items associated with this item
                int indexValueItemSetCount = reader.readInt();
                for (int k = 0; k < indexValueItemSetCount; k++) {
                    // Read the number of vertices or edges in this sub-item
                    int setCount = reader.readInt();
                    for (int l = 0; l < setCount; l++) {
                        // Read the vertex or edge identifier
                        if (indexType == 1) {
                            Vertex v = graph.getVertex(readTypedData(reader));
                            if (v != null) {
                                tinkerIndex.put(indexItemKey, v.getProperty(indexItemKey), v);
                            }
                        } else if (indexType == 2) {
                            Edge e = graph.getEdge(readTypedData(reader));
                            if (e != null) {
                                tinkerIndex.put(indexItemKey, e.getProperty(indexItemKey), e);
                            }
                        }
                    }
                }
            }

            graph.indices.put(indexName, tinkerIndex);
        }
    }

    private void readVertexKeyIndices(final DataInputStream reader, final TinkerGraph graph) throws IOException {
        // Read the number of vertex key indices
        int indexCount = reader.readInt();

        for (int i = 0; i < indexCount; i++) {
            // Read the key index name
            String indexName = reader.readUTF();

            graph.vertexKeyIndex.createKeyIndex(indexName);

            Map<Object, Set<TinkerVertex>> items = new HashMap<Object, Set<TinkerVertex>>();

            // Read the number of items associated with this key index name
            int itemCount = reader.readInt();
            for (int j = 0; j < itemCount; j++) {
                // Read the item key
                Object key = readTypedData(reader);

                Set<TinkerVertex> vertices = new HashSet<TinkerVertex>();

                // Read the number of vertices in this item
                int vertexCount = reader.readInt();
                for (int k = 0; k < vertexCount; k++) {
                    // Read the vertex identifier
                    Vertex v = graph.getVertex(readTypedData(reader));
                    if (v != null) {
                        vertices.add((TinkerVertex) v);
                    }
                }

                items.put(key, vertices);
            }

            graph.vertexKeyIndex.index.put(indexName, items);
        }
    }

    private void readEdgeKeyIndices(final DataInputStream reader, final TinkerGraph graph) throws IOException {
        // Read the number of edge key indices
        int indexCount = reader.readInt();

        for (int i = 0; i < indexCount; i++) {
            // Read the key index name
            String indexName = reader.readUTF();

            graph.edgeKeyIndex.createKeyIndex(indexName);

            Map<Object, Set<TinkerEdge>> items = new HashMap<Object, Set<TinkerEdge>>();

            // Read the number of items associated with this key index name
            int itemCount = reader.readInt();
            for (int j = 0; j < itemCount; j++) {
                // Read the item key
                Object key = readTypedData(reader);

                Set<TinkerEdge> edges = new HashSet<TinkerEdge>();

                // Read the number of edges in this item
                int edgeCount = reader.readInt();
                for (int k = 0; k < edgeCount; k++) {
                    // Read the edge identifier
                    Edge e = graph.getEdge(readTypedData(reader));
                    if (e != null) {
                        edges.add((TinkerEdge) e);
                    }
                }

                items.put(key, edges);
            }

            graph.edgeKeyIndex.index.put(indexName, items);
        }
    }

    private Object readTypedData(final DataInputStream reader) throws IOException {
        byte type = reader.readByte();

        if (type == 1) {
            return reader.readUTF();
        } else if (type == 2) {
            return reader.readInt();
        } else if (type == 3) {
            return reader.readLong();
        } else if (type == 4) {
            return reader.readShort();
        } else if (type == 5) {
            return reader.readFloat();
        } else if (type == 6) {
            return reader.readDouble();
        } else {
            throw new IOException("unknown data type: use java serialization");
        }
    }
}
